package me.sample.ninja.frog

import kotlinx.cinterop.useContents
import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.onUpdate
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.setValue
import io.github.andannn.raylib.core.ComponentRegistry
import kotlin.math.floor

private const val backgroundDictionary = "resources/TowDSampleRes/Background"

enum class Background(val file: String) {
    BLUE("Blue.png"),
    Brown("Brown.png"),
}

private const val translationAnimationSpeed = 60f
fun ComponentRegistry.background(
    background: Background,
    itemSize: Float = 100f,
) {
    component("background") {
        val texture = remember {
            loadTexture("$backgroundDictionary/${background.file}")
        }
        val sourceRect = remember {
            texture.useContents {
                Rectangle(0f, 0f, width.toFloat(), height.toFloat())
            }
        }
        val rowCount = floor(screenWidth.div(itemSize)).toInt()
        val columnCount = floor(screenHeight.div(itemSize)).toInt() + 1

        var verticalOffset by remember {
            mutableStateOf(0f)
        }
        onUpdate { dt ->
            verticalOffset += translationAnimationSpeed * dt
            if (verticalOffset >= itemSize) {
                verticalOffset = 0f
            }
        }
        onDraw {
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