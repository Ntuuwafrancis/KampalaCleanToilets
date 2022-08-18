package com.francosoft.kampalacleantoilets.data.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Toilet (
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
    var rating: Int = 0,
    var geofenceRadius: Float? = null,
    var id: String? = null
) : Parcelable
