package com.francosoft.kampalacleantoilets.ui.map

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.databinding.MapsFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.ACTION_GEOFENCE_EVENT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.SHOWCASE_ID
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.francosoft.kampalacleantoilets.utilities.helpers.ShowcaseUtils.newShowcase
import com.francosoft.kampalacleantoilets.utilities.helpers.ToiletRenderer
import com.francosoft.kampalacleantoilets.utilities.helpers.TrackingUtility.pleaseEnableLocation
import com.francosoft.kampalacleantoilets.utilities.receivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.clustering.ClusterManager
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MapsFragment : Fragment() {

    private lateinit var binding: MapsFragmentBinding
    lateinit var navController: NavController
    private var googleMap: GoogleMap? = null
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    private val args: MapsFragmentArgs by navArgs()
    private var isNewToilet: Boolean = false
    private var location: Location? = null
    private var toilets: MutableList<Toilet> = mutableListOf()
    private var nearestToilet: Toilet? = null
    private var circle: Circle? = null
    private lateinit var clusterManager: ClusterManager<Toilet>
    private var toilet: Toilet? = null
    private lateinit var viewModel: MapsViewModel
    private var auth: FirebaseAuth? = null
    private var user: User = User()
    private var isFencesOn: Boolean = false

    private val gadgetQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geoClient: GeofencingClient
    private var geofenceList: MutableList<Geofence> = mutableListOf()
    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        val kampala = LatLng(0.3476, 32.5825)
        map.moveCamera(CameraUpdateFactory.zoomTo(11.5f))
        map.moveCamera(CameraUpdateFactory.newLatLng(kampala))
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isCompassEnabled = false
        if (view != null) {
            addClusteredMarkers(map, requireView())
            getToiletFromNotification(requireView())
        }

//        view?.let { addClusteredMarkers(map, it) }
        enableMyLocation()
//        view?.let { getUser(it) }
//        view?.let { getToiletFromNotification(it) }

    }

    private fun addClusteredMarkers(googleMap: GoogleMap, view: View) {
        clusterManager = ClusterManager<Toilet>(requireActivity(), googleMap)
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        clusterManager.renderer = ToiletRenderer(
            requireContext(),
            googleMap,
            clusterManager
        )
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnInfoWindowClickListener(clusterManager)
        googleMap.setInfoWindowAdapter(clusterManager.markerManager)


        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
        clusterManager.clusterMarkerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
//        googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))

//        clusterManager.setOnClusterClickListener {  }

//        clusterManager.setOnClusterInfoWindowClickListener {
//            val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("edit","map",false, it)
//            val navController = Navigation.findNavController(requireView())
//            navController.navigate(action)
//        }
        clusterManager.setOnClusterItemClickListener { toilet ->
            addCircle(googleMap, toilet, requireContext())
            return@setOnClusterItemClickListener false
        }
        clusterManager.setOnClusterItemInfoWindowClickListener {
            val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("edit","map",false, it)
            val navController = Navigation.findNavController(requireView())
            navController.navigate(action)
        }

        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("toilet")
        val valueEventListener: ValueEventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                toilets.clear()
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                    val point = toilet.latitude?.let { lat -> toilet.longitude?.let { long -> LatLng(lat, long) } }

                    if (toilet.approved.equals("approved") || toilet.approved.equals("delete")){
                        toilets.add(toilet)
                        if (toilet == args.toilet) {
                            point?.let { CameraUpdateFactory.newLatLngZoom(it, 18.0f) }
                                ?.let { googleMap.animateCamera(it) }
                            addCircle(googleMap, toilet, requireActivity())
                        }
                    }
                }
                clusterManager.addItems(toilets)
                clusterManager.cluster()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(), error.message, Toast.LENGTH_SHORT).show()
            }
        }
        dbref.addListenerForSingleValueEvent(valueEventListener)

        googleMap.setOnInfoWindowClickListener {
                marker ->

            val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("edit","map",false, marker.tag as Toilet)
            val navController = Navigation.findNavController(requireView())
            navController.navigate(action)
        }

        googleMap.setOnCameraIdleListener {
            // When the camera stops moving, change the alpha value back to opaque.
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that reclustering
            // can be performed when the camera stops moving.
            clusterManager.onCameraIdle()
        }

        // When the camera starts moving, change the alpha value of the marker to translucent.
        googleMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.0f }
        }
        googleMap.setOnMapLongClickListener {latlng ->
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("Add toilet to location")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", null)
                .show()

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{view ->
                if (view != null) {
                    val toilet = Toilet()
                    toilet.latitude = latlng.latitude
                    toilet.longitude = latlng.longitude
                    val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("new", "map",false, toilet)
                    val navController = Navigation.findNavController(requireView())
                    navController.navigate(action)
                    dialog.dismiss()
                }
            }
        }
        setUpInfoWindowAdapter()
    }

    private fun addCircle(googleMap: GoogleMap, toilet: Toilet?, context: Context) {
        circle?.remove()
        val point = toilet?.latitude?.let { lat -> toilet.longitude?.let { long -> LatLng(lat, long) } }
        circle = googleMap.addCircle(
            CircleOptions()
                .center(point!!)
                .radius(20.0)
                .fillColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimaryTranslucent))
                .strokeColor(ContextCompat.getColor(requireActivity(), android.R.color.holo_red_dark))
        )
    }

    private fun setUpInfoWindowAdapter() {
        googleMap?.setOnInfoWindowClickListener { marker ->

            val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("edit","map",false, marker.tag as Toilet)
            val navController = Navigation.findNavController(requireView())
            navController.navigate(action)
        }

    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private fun enableMyLocation() {
        if(EasyPermissions.hasPermissions(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        ){
            googleMap?.isMyLocationEnabled = true
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation.addOnSuccessListener {currentLocation ->
                location = currentLocation
                if (location != null && toilets.isNotEmpty()) {
                    val sortedToilets = toilets.sortedBy { toilet ->
                        toilet.getDistanceInMiles(currentLocation).toDouble()
                    } as MutableList<Toilet>

                    nearestToilet = sortedToilets[0]
                }

            }
        }else {
            Snackbar.make(
                requireView(),
                getString(R.string.snackbar_map),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.ok)) {
                    EasyPermissions.requestPermissions(
                        requireActivity(),
                        getString(R.string.map_rationale),
                        REQUEST_CODE_LOCATION_PERMISSION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
                .show()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.maps_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        viewModel = ViewModelProvider(this)[MapsViewModel::class.java]
        binding = MapsFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)
        geoClient = LocationServices.getGeofencingClient(view.context)

        val fbNearestMarker = binding.fbNearest
        toilet = args.toilet

        newShowcase(fbNearestMarker, requireActivity(), "GOT IT", "Click this button to show the nearest toilet to your location", SHOWCASE_ID)
        // Create channel for notifications
//        createChannel(requireContext() )

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        pleaseEnableLocation(requireContext())
        mapFragment?.getMapAsync(callback)

        fbNearestMarker.setOnClickListener {
            getClosetToilet()
        }
    }

    private fun getToiletFromNotification(view: View) {

        arguments.let { bundle ->
            if (bundle != null) {
                val toilet = bundle.get("toilet")
                if (toilet != null) {
                    toilet as Toilet
                    val point = toilet.latitude?.let { lat -> toilet.longitude?.let { long -> LatLng(lat, long) } }
                    point?.let { CameraUpdateFactory.newLatLngZoom(it, 18.0f) }
                        ?.let { googleMap?.animateCamera(it) }
                    googleMap?.let { addCircle(it, toilet, requireActivity()) }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private fun getClosetToilet() {

        if(EasyPermissions.hasPermissions(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)){
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation.addOnSuccessListener {currentLocation ->
                if (currentLocation != null && toilets.isNotEmpty()) {
                    val sortedToilets = toilets.sortedBy { toilet ->
                        toilet.getDistanceInMiles(currentLocation).toDouble()
                    } as MutableList<Toilet>

                    val nearestToilet = sortedToilets[0]
                    val point = nearestToilet.latitude?.let { nearestToilet.longitude?.let { it1 ->
                        LatLng(it,
                            it1
                        )
                    } }

                    if (googleMap != null) {
                        point?.let { CameraUpdateFactory.newLatLngZoom(it, 18.0f) }
                            ?.let { googleMap?.animateCamera(it) }
                        addCircle(googleMap!!, nearestToilet, requireContext())
                    }

                }

            }

        } else {
            Toast.makeText(requireActivity(), "Enable Location Please", Toast.LENGTH_SHORT).show()
        }

    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        const val REQUEST_CODE_LOCATION_PERMISSION = 11
    }

    private fun getBitmapFromVector(
        @DrawableRes vectorResourceId: Int,
        @ColorRes colorResourceId: Int,
        context: Context
    ): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, vectorResourceId,
            context.theme
        )
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(
            vectorDrawable,
            ResourcesCompat.getColor(
                resources,
                colorResourceId, context.theme
            )
        )
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onPause() {
        super.onPause()
//        if (googleMap != null){
//            val kampala = LatLng(0.3476, 32.5825)
//            googleMap?.moveCamera(CameraUpdateFactory.zoomTo(11.5f))
//            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(kampala))
//        }
        FirebaseUtil.detachListener()
    }

    override fun onStart() {
        super.onStart()
        //prevent crashing if the map doesn't exist yet (eg. on starting activity)
        if (googleMap != null && context != null){
            clusterManager.clearItems()
            toilets.clear()
            googleMap?.clear()

            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            // add markers from database to the map
            mapFragment?.getMapAsync(callback)
        }
    }

    override fun onResume() {
        super.onResume()


        isNewToilet = args.pickSpot
        if (isNewToilet) {
            Snackbar.make(
                requireView(),
                getString(R.string.snackbar_add_toilet),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.ok)) {

                }
                .show()
        }

    }

}
