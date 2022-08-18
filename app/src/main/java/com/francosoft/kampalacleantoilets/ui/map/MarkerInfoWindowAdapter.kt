package com.francosoft.kampalacleantoilets.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
//import com.google.codelabs.buildyourfirstmap.place.Place

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val toilet = marker.tag as? Toilet ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_contents, null
        )
        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = toilet.title
        view.findViewById<TextView>(
            R.id.text_view_address
        ).text = toilet.address
        view.findViewById<TextView>(
            R.id.text_view_rating
        ).text = buildString {
        append(context.getString(R.string.rating_marker_text))
        append(toilet.rating)
    }

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}