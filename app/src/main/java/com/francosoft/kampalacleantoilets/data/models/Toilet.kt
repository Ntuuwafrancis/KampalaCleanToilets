package com.francosoft.kampalacleantoilets.data.models

import android.location.Location
import android.os.Parcelable
import com.francosoft.kampalacleantoilets.utilities.extensions.roundTo
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import com.google.maps.android.clustering.ClusterItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class Toilet(
    var stitle: String?= null,
    var address: String? = null,
    var phone: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var type: String? = null,
    var openTime: String? = null,
    var closeTime: String? = null,
    var status: String? = null,
    var charge: String? = null,
    var extraInfo: String? = null,
    var division: String? = null,
    var totalRating: Int = 0,
    var rating: Double = 0.0,
    var ratingTotal:  Double  = 0.0,
    var distance: Double? = null,
    var geofenceRadius: Float? = 1000.0f,
    var approved: String? = null,
    var uid: String? = null,
    var id: String? = null
) : Parcelable, ClusterItem{

    @Exclude
    fun getDistanceInMiles(currentLocation: Location): Float {
        val coordinates = Location("")
        if (latitude != null && longitude != null) {
            coordinates.latitude = latitude as Double
            coordinates.longitude = longitude as Double
        }
        val meters = currentLocation.distanceTo(coordinates)
        return (meters / 1000f).roundTo(1)
//        1609
    }

    @Exclude
    override fun getPosition(): LatLng? {
        return latitude?.let { longitude?.let { it1 -> LatLng(it, it1) } }
    }

    @Exclude
    override fun getTitle(): String? {
        return stitle
    }

    @Exclude
    override fun getSnippet(): String? {
        return address
    }

}
