package com.francosoft.kampalacleantoilets.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletsListItemBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

//import com.google.codelabs.buildyourfirstmap.place.Place

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    private var binding: ToiletsListItemBinding
    private var view: View

    init {

        val inflater = LayoutInflater.from(context)
        binding = ToiletsListItemBinding.inflate(inflater)
        view = binding.root
    }

    override fun getInfoContents(marker: Marker): View? {

        // 1. Get tag
        val toilet = marker.tag as? Toilet ?: return null

        // 2. Inflate view and set title, address, and rating
        binding.tvTitle.text = toilet.stitle
        (toilet.openTime + " - " + toilet.closeTime).also { binding.tvOpenStatus.text = it }
        binding.tvOpStatus.text = toilet.status
        binding.tvTotalRatings.text = buildString {
            append("(")
            append(toilet.totalRating.toString())
            append(")")
        }
        binding.ratingBar1.rating = toilet.rating.toFloat()
        binding.tvType.text = toilet.type
//        if (toilet.charge?.isNotEmpty() == true)
//            binding.tvCharge.text = toilet.charge?.let { buildString { append("Shs") } + it }

        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        // 1. Get tag
        val toilet = marker.tag as? Toilet ?: return null

        binding.tvTitle.text = toilet.stitle
        (toilet.openTime + " - " + toilet.closeTime).also { binding.tvOpenStatus.text = it }
        binding.tvOpStatus.text = toilet.status
        binding.tvTotalRatings.text = buildString {
            append("(")
            append(toilet.totalRating.toString())
            append(")")
        }
        binding.ratingBar1.rating = toilet.rating.toFloat()
        binding.tvType.text = toilet.type
        binding.tvAddress.text = toilet.address
        binding.tvCharge.text = toilet.charge?.let { buildString { append("Shs") } + it }
//        if (toilet.charge?.isNotEmpty() == true)



        return view
    }

}