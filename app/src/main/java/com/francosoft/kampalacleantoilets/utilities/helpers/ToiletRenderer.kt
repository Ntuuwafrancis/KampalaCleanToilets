package com.francosoft.kampalacleantoilets.utilities.helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class ToiletRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Toilet>
) : DefaultClusterRenderer<Toilet>(context, map, clusterManager) {

    private fun getBitmapFromVector(
        @DrawableRes vectorResourceId: Int,
        @ColorRes colorResourceId: Int,
        context: Context,
        resources: Resources
    ): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, vectorResourceId,
            context.theme
        )
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(
            vectorDrawable,
            ResourcesCompat.getColor(
                resources,
                colorResourceId, context.theme
            )
        )
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onBeforeClusterItemRendered(
        toilet: Toilet,
        markerOptions: MarkerOptions
    ) {
        val lat = toilet.latitude
        val long = toilet.longitude
        val point = lat?.let { long?.let { it1 -> LatLng(it, it1) } }
        if (point != null) {
            markerOptions.title(toilet.stitle)
                .position(point)
                .icon(
                    getBitmapFromVector(
                        R.drawable.kct_icon,
                        R.color.colorPrimary,
                        context,
                        context.resources
                    )
                )
        }
    }

    override fun onClusterItemRendered(clusterItem: Toilet, marker: Marker) {
        marker.tag = clusterItem
//        toilet = clusterItem
//        this.marker = marker
    }

    override fun shouldRenderAsCluster(cluster: Cluster<Toilet>?): Boolean {
        return cluster?.size!! > 3
    }
}