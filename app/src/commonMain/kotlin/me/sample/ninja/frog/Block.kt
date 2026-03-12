package me.sample.ninja.frog

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.RectangleAlloc
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.positional2DComponent
import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.nativeStateOf
import io.github.andannn.raylib.core.remember

object BlockEntity : Entity

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
        blockComponent(rectangle, key = "block $index")
    }
}

private fun ComponentRegistry.blockComponent(
    rectangle: Rectangle,
    key: String,
) = component(key) {
    positional2DComponent(
        key = "A",
        size = Vector2(rectangle.width, rectangle.height),
        position = Vector2(rectangle.x, rectangle.y),
    ) {
        registerEntityToWorldGrid2D(BlockEntity, it)
    }
}
