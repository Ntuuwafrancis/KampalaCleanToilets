package com.francosoft.kampalacleantoilets.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var username: String? = null,
    var email: String?= null,
    var role: String?= null,
    var uid: String? = null,
    var fencesOn: Boolean = false,
    var fences: Double = 100.0,
    var triggeringGeofenceId: String? = null,

) : Parcelable
