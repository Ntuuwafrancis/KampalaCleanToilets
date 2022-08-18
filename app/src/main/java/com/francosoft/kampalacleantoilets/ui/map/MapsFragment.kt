package com.francosoft.kampalacleantoilets.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.Navigation
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.ui.toilets.ToiletsFragmentDirections
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsFragment : Fragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener

    private val callback = OnMapReadyCallback { map ->

        googleMap = map
        val kampala = LatLng(0.3476, 32.5825)
//        map.addMarker(MarkerOptions().position(kampala).title("Marker in Kampala"))
        map.moveCamera(CameraUpdateFactory.zoomTo(12f))
        map.moveCamera(CameraUpdateFactory.newLatLng(kampala))
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isTiltGesturesEnabled = false

        dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet

                    val point = toilet.latitude?.let { lat -> toilet.longitude?.let { long -> LatLng(lat, long) } }
                    val marker = point?.let {
                        MarkerOptions()
                            .position(it)
                            .title(toilet.title)
                            .snippet("Address: ${toilet.address} \n" +
                                    "Type: ${toilet.type}" +
                                    " Hours: ${toilet.openTime} - ${toilet.closeTime}" +
                                    " Accessibility: ${toilet.status}")
                            .icon(
                                getBitmapFromVector(
                                    R.drawable.kct_icon,
                                    R.color.colorPrimary
                                )
                            )
                            .alpha(0.95f)
                    }?.let {
                        map.addMarker(
                            it
                        )
                    }
                    marker?.tag = toilet

                    map.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireActivity()))
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })

        map.setOnInfoWindowClickListener { marker ->

            val action = MapsFragmentDirections.actionMapFragmentToToiletFragment(false, marker.tag as Toilet)
            val navController = Navigation.findNavController(requireView())
            navController.navigate(action)
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

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference

        mapFragment?.getMapAsync(callback)
    }

    private fun getBitmapFromVector(
        @DrawableRes vectorResourceId: Int,
        @ColorRes colorResourceId: Int
    ): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, vectorResourceId, requireContext().theme)
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
                colorResourceId, requireContext().theme
            )
        )
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}