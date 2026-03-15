package me.sample.ninja.frog

import io.github.andannn.raylib.components.rresTextureAsset
import kotlinx.cinterop.useContents
import io.github.andannn.raylib.foundation.Rectangle
import io.github.andannn.raylib.foundation.draw
import io.github.andannn.raylib.foundation.screenHeight
import io.github.andannn.raylib.foundation.screenWidth
import io.github.andannn.raylib.foundation.update
import io.github.andannn.raylib.runtime.component
import io.github.andannn.raylib.runtime.getValue
import io.github.andannn.raylib.runtime.mutableStateOf
import io.github.andannn.raylib.runtime.remember
import io.github.andannn.raylib.runtime.setValue
import io.github.andannn.raylib.runtime.ComponentRegistry
import rres.resources.rresBundle.RresBundleRes
import kotlin.math.floor

enum class Background(val resourceId: UInt) {
    BLUE(RresBundleRes.image.image_background_blue_png),
    Brown(RresBundleRes.image.image_background_brown_png),
}

private const val translationAnimationSpeed = 60f
fun ComponentRegistry.background(
    background: Background,
    itemSize: Float = 100f,
) {
    component("background") {
        val texture = remember {
            rresTextureAsset(RresBundleRes.rresFile, background.resourceId)
        }
        val sourceRect = remember {
            texture.useContents {
                Rectangle(0f, 0f, width.toFloat(), height.toFloat())
            }
        }
        val rowCount = floor(screenWidth.div(itemSize)).toInt() + 1
        val columnCount = floor(screenHeight.div(itemSize)).toInt() + 1

        var verticalOffset by remember {
            mutableStateOf(0f)
        }
        update { dt ->
            verticalOffset += translationAnimationSpeed * dt
            if (verticalOffset >= itemSize) {
                verticalOffset = 0f
            }
        }
        draw {
            for (rowIndex in 0 until rowCount) {
                for (columnIndex in -1 until columnCount) {
                    val dst = Rectangle(
                        x = itemSize * rowIndex,
                        y = itemSize * columnIndex + verticalOffset,
                        width = itemSize,
                        height = itemSize
                    )
                    drawTexture(texture, sourceRect, dst)
                }
            }
        }
    }
}