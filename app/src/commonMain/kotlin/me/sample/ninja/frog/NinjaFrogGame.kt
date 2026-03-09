package me.sample.ninja.frog

import io.github.andannn.raylib.base.Colors.LIGHTGRAY
import io.github.andannn.raylib.base.RectangleAlloc
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.base.rlMatrix
import io.github.andannn.raylib.components.collision2DComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.nativeStateOf
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.remember
import kotlinx.cinterop.readValue

private val appleCollectionItems = listOf(
    Vector2(50f, 250f),
    Vector2(50f, 300f),
    Vector2(50f, 350f),
    Vector2(100f, 300f),
    Vector2(100f, 350f),
    Vector2(150f, 350f),
)

fun ComponentRegistry.ninjaFrogGame() = collision2DComponent("2D Game", cellSize = 50) {
    val collisionBlocks by remember {
        nativeStateOf {
            listOf(
                RectangleAlloc(0f, 400f, 800f, 50f),
                RectangleAlloc(300f, 200f, 50f, 250f),
            )
        }
    }

    background(Background.Brown)

    collectionItem(CollectionItem.APPLE, appleCollectionItems)

    mainPlayer(MainCharacter.VIRTUAL_GUY, collisionBlocks)

    component("debug") {
        onDraw {
            rlMatrix {
                translate(0f, 25 * 50f, 0f)
                rotate(90f, 1f, 0f, 0f)
                drawGrid(100, 50f)
            }
        }

        onDraw {
            collisionBlocks.map { it.readValue() }.forEach {
                drawRectangle(it, LIGHTGRAY)
            }
        }
    }
}
