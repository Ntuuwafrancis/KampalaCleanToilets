package com.francosoft.kampalacleantoilets.utilities.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.GEO_TAG
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object GeofenceUtils {
    fun getAllToilets(firebaseDb: FirebaseDatabase, geofenceList: MutableList<Geofence>, context: Context){
        firebaseDb.getReference("toilet").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                    val point = toilet.latitude?.let { lat -> toilet.longitude?.let { long -> LatLng(lat, long) } }

                    if (toilet.approved.equals("approved") || toilet.approved.equals("delete")){
                        geofenceList.add(createGeofence(toilet, 100f))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createGeofence(toilet: Toilet, distance: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(toilet.id.toString())
            .setCircularRegion(
                toilet.latitude!!,
                toilet.longitude!!,
                distance
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun seekGeofencing(geofenceList: MutableList<Geofence>): GeofencingRequest {
        val geoRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
//            addGeofences(toilets, 100f)
        }.build()
        return geoRequest
    }

    fun addGeofences(toilets: MutableList<Toilet>, distance: Float) : MutableList<Geofence> {
        val fences = mutableListOf<Geofence>()
        toilets.map {
            fences.add(createGeofence(it, distance))
        }
        return fences
    }

    fun askLocationPermission(view: View, activity : Activity) {
        if (authorizedLocation(view))
            return
        var grantingPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val customResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            grantingPermission += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        } else {
            Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(GEO_TAG, "askLocationPermission")

        activity.let {
            ActivityCompat.requestPermissions(
                it, grantingPermission, customResult
            )
        }
    }

    fun authorizedLocation(view: View): Boolean {
        val formalizeForeground = (
                PermissionChecker.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    view.context, Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val formalizeBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionChecker.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                view.context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            return true
        }
        return formalizeForeground && formalizeBackground
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(view: View, geofencingClient: GeofencingClient, geofenceIntent: PendingIntent,
                            geofenceList: MutableList<Geofence>,
                            toilets: MutableList<Toilet>,
                            distance: Float){
        if (ActivityCompat.checkSelfPermission(
                view.context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        geofencingClient.removeGeofences(geofenceIntent).run {
            addOnCompleteListener{
                if (geofenceList.isNotEmpty()) {
                    geofencingClient.addGeofences(seekGeofencing(geofenceList), geofenceIntent).run {
                        addOnSuccessListener {
//                            Toast.makeText(view.context, "Geofence(s) added", Toast.LENGTH_SHORT).show()
                        }
                        addOnFailureListener {
//                            Toast.makeText(view.context, "Failed to add geofence(s)", Toast.LENGTH_SHORT).show()
                            if ((it.message != null)) {
                                Log.w(GEO_TAG, it.message!!)
                            }
                        }
                    }
                }
            }
        }

    }

    fun removeGeofence(view: View, geofencingClient: GeofencingClient, geofenceIntent: PendingIntent){
        geofencingClient.removeGeofences(geofenceIntent).run {
            addOnSuccessListener {
//                Toast.makeText(view.context, "Geofences removed", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
//                Toast.makeText(view.context, "Failed to remove geofences", Toast.LENGTH_SHORT).show()
            }
        }
    }
}