package com.francosoft.kampalacleantoilets.data.models

import android.location.Location
import android.os.Parcelable
import com.francosoft.kampalacleantoilets.utilities.extensions.roundTo
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Toilet(
    var title: String? = null,
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
    var rating: Double = 0.0,
    var distance: Double? = null,
    var geofenceRadius: Float? = null,
    var approved: String? = null,
    var uid: String? = null,
    var id: String? = null
) : Parcelable {
    fun getDistanceInMiles(currentLocation: Location): Float {
        val coordinates = Location("")
        if (latitude != null && longitude != null) {
            coordinates.latitude = latitude as Double
            coordinates.longitude = longitude as Double
        }
        val meters = currentLocation.distanceTo(coordinates)
        return (meters / 1609f).roundTo(1)
    }
}
