package com.ramstudio.kaskita.ui.component

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A custom shape that draws a semi-circular cutout at the top center.
 * * @param fabRadius The radius of the FAB.
 * @param padding The extra space (gap) between the FAB and the bottom bar.
 */
class BottomNavCurveShape(
    private val fabRadius: Float = 28f, // Standard M3 FAB radius
    private val padding: Float = 8f     // Gap between FAB and BottomBar
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height

            // Convert dp to pixels based on screen density
            val cutoutRadiusPx = (fabRadius + padding) * density.density
            val centerPx = width / 2f

            // 1. Draw top line from left to the start of the cutout
            lineTo(centerPx - cutoutRadiusPx, 0f)

            // 2. Draw the semi-circular cutout
            arcTo(
                rect = Rect(
                    left = centerPx - cutoutRadiusPx,
                    top = -cutoutRadiusPx,
                    right = centerPx + cutoutRadiusPx,
                    bottom = cutoutRadiusPx
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )

            // 3. Draw top line from end of cutout to the right edge
            lineTo(width, 0f)

            // 4. Draw the rest of the rectangle
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        return Outline.Generic(path)
    }
}