package com.francosoft.kampalacleantoilets.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ToiletTip(
    val id: Int,
    val title: String,
    val tip: String,
    val imageName: String,
    val imageUrl: String
) : Parcelable
