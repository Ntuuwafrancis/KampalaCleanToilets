package com.francosoft.kampalacleantoilets.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Favorite(
    var toiletId: String? = null,
    var userEmail: String? = null,
    var uid: String? = null,
    var id: String? = null
) : Parcelable
