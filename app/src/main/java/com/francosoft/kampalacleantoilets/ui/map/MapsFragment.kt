package com.francosoft.kampalacleantoilets.ui.map

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.MapsFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.francosoft.kampalacleantoilets.utilities.helpers.TrackingUtility.pleaseEnableLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MapsFragment : Fragment() {

    private lateinit var binding: MapsFragmentBinding
    lateinit var navController: NavController
    private lateinit var googleMap: GoogleMap
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    private val args: MapsFragmentArgs by navArgs()
    private var isNewToilet: Boolean = false
    private var location: Location? = null
    private var toilets: MutableList<Toilet> = mutableListOf()
    private var nearestToilet: Toilet? = null

    private val callback = OnMapReadyCallback { map ->

        googleMap = map
        val kampala = LatLng(0.3476, 32.5825)

//        map.addMarker(MarkerOptions().position(kampala).title("Marker in Kampala"))
        map.moveCamera(CameraUpdateFactory.zoomTo(12f))
        map.moveCamera(CameraUpdateFactory.newLatLng(kampala))
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isTiltGesturesEnabled = false
        map.setOnMapLongClickListener {latlng ->
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Add toilet to location")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", null)
                .show()


            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{view ->
               if (view != null) {
                   val toilet = Toilet()
                   toilet.latitude = latlng.latitude
                   toilet.longitude = latlng.longitude
                   val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("new", false, toilet)
                   val navController = Navigation.findNavController(requireView())
                   navController.navigate(action)
                   dialog.dismiss()
               }
            }
        }

        dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet


                    val point = toilet.latitude?.let { lat -> toilet.longitude?.let { long -> LatLng(lat, long) } }

                    if (view?.context != null){
                        val marker = point?.let {
                            MarkerOptions()
                                .position(it)
                                .title(toilet.title)
                                .icon(
                                    getBitmapFromVector(
                                        R.drawable.kct_icon,
                                        R.color.colorPrimary,
                                        view?.context!!
                                    )
                                )
                                .alpha(0.95f)
                        }?.let {
                            map.addMarker(
                                it
                            )
                        }
                        marker?.tag = toilet
                    }


//                    getToiletRating(marker?.tag as Toilet)
                    map.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireActivity()))
                    toilets.add(toilet)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })

        map.setOnInfoWindowClickListener { marker ->

            val action = MapsFragmentDirections.actionMapFragmentToToiletFragment("edit",false, marker.tag as Toilet)
            val navController = Navigation.findNavController(requireView())
            navController.navigate(action)
        }
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private fun enableMyLocation() {
        if(EasyPermissions.hasPermissions(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        ){
            googleMap.isMyLocationEnabled = true
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.maps_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        binding = MapsFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference

        pleaseEnableLocation(requireContext())
        mapFragment?.getMapAsync(callback)

    }

//    private fun getToiletRating(toilet: Toilet) {
//        var toiletRating: Double = 0.0
//        val reviews = mutableListOf<Review>()
//        dbListener = firebaseDb.getReference("review").addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                for (postSnapshot: DataSnapshot in snapshot.children) {
//                    val review = postSnapshot.getValue(Review::class.java) as Review
//                    review.id = postSnapshot.key
//
//                    if (review.toiletId == toilet.id){
//                        toiletRating =+ review.rating
//                        reviews.add(review)
//                    }
//                }
//
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
//            }
//
//        })
//
//        toilet.rating = toiletRating.div(reviews.size)
//        if (toilet.id != null) {
//            toilet.id?.let { firebaseDb.getReference("toilet").child(it).setValue(toilet) }
//        }
//
//    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    companion object {
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