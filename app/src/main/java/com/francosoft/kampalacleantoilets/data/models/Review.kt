package com.francosoft.kampalacleantoilets.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    var toiletId: String? = null,
    var userEmail: String? = null,
    var userName: String? = null,
    var rating: Double = 0.0,
    var review: String? = null,
    var date: String? = null,
    var id: String? = null
) : Parcelable
