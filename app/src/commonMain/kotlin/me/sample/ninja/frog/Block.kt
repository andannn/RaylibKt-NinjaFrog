package me.sample.ninja.frog

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.RectangleAlloc
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Positional2D
import io.github.andannn.raylib.components.Positional2DEntity
import io.github.andannn.raylib.components.positional2DAlloc
import io.github.andannn.raylib.components.positional2DEntityComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.nativeStateOf
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.remember

class BlockEntity(
    override val state: Positional2D,
) : Positional2DEntity

fun ComponentRegistry.blockComponents() {
    val collisionBlocks by remember {
        nativeStateOf {
            listOf(
                RectangleAlloc(0f, 400f, 800f, 50f),
//                RectangleAlloc(300f, 200f, 50f, 250f),
//                RectangleAlloc(600f, 200f, 50f, 250f),
            )
        }
    }

    collisionBlocks.forEachIndexed { index, rectangle ->
        blockComponent(rectangle, tag = "block $index")
    }
}

private fun ComponentRegistry.blockComponent(
    rectangle: Rectangle,
    tag: String,
) = component(tag) {
    val entity by remember {
        nativeStateOf {
            BlockEntity(
                positional2DAlloc(
                    size = Vector2(rectangle.width, rectangle.height),
                    position = Vector2(rectangle.x, rectangle.y),
                ),
            )
        }
    }

    positional2DEntityComponent(entity) {
        component("item") {
            onDraw {
//                drawRectangle(Rectangle(0f, 0f, rectangle.width, rectangle.height), LIGHTGRAY)
            }
        }
    }
}
