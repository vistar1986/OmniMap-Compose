package com.melody.map.gd_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.model.Circle
import com.amap.api.maps.model.CircleOptions
import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.melody.map.gd_compose.model.GDMapComposable

internal class CircleNode(
    val circle: Circle
) : MapNode {
    override fun onRemoved() {
        circle.remove()
    }
}

/**
 * A composable for a circle on the map.
 *
 * @param center the [LatLng] to use for the center of this circle
 * @param fillColor the fill color of the circle
 * @param radius the radius of the circle in meters.
 * @param strokeColor the stroke color of the circle
 * @param strokeDottedLineType the stroke dash type of the circle
 * @param strokeWidth the width of the circle's outline in screen pixels
 * @param visible the visibility of the circle
 * @param zIndex the z-index of the circle
 */
@Composable
@GDMapComposable
fun Circle(
    center: LatLng,
    fillColor: Color = Color.Transparent,
    radius: Double = 0.0,
    strokeColor: Color = Color.Black,
    strokeDottedLineType: Int = -1,
    strokeWidth: Float = 10f,
    visible: Boolean = true,
    zIndex: Float = 0f,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<CircleNode, MapApplier>(
        factory = {
            val circle = mapApplier?.map?.addCircle(
                CircleOptions().apply  {
                    center(center)
                    fillColor(fillColor.toArgb())
                    radius(radius)
                    strokeColor(strokeColor.toArgb())
                    setStrokeDottedLineType(strokeDottedLineType)
                    strokeWidth(strokeWidth)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding circle")
            CircleNode(circle)
        },
        update = {
            set(center) { this.circle.center = it }
            set(fillColor) { this.circle.fillColor = it.toArgb() }
            set(radius) { this.circle.radius = it }
            set(strokeColor) { this.circle.strokeColor = it.toArgb() }
            set(strokeDottedLineType) {this.circle.strokeDottedLineType = it }
            set(strokeWidth) { this.circle.strokeWidth = it }
            set(visible) { this.circle.isVisible = it }
            set(zIndex) { this.circle.zIndex = it }
        }
    )
}