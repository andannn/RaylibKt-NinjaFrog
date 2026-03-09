package me.sample.ninja.frog

import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import io.github.andannn.raylib.base.Colors
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Texture
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.base.isCollisionWith
import io.github.andannn.raylib.components.SpriteGrid
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.core.find
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.onUpdate
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.setValue

private const val itemsBaseDictionary = "resources/TowDSampleRes/Items/Fruits"

enum class CollectionItem(val fileName: String) {
    APPLE("Apple.png"),
    BANANAS("Bananas.png"),
}

private fun CollectionItem.grid() = when (this) {
    CollectionItem.APPLE -> 17 to 1
    CollectionItem.BANANAS -> 17 to 1
}

private const val itemSize = 50f

fun ComponentRegistry.collectionItem(
    item: CollectionItem,
    positions: List<CValue<Vector2>>,
) = component("collectionItem_$item") {
    val itemTexture = remember {
        loadTexture("$itemsBaseDictionary/${item.fileName}")
    }
    val collectedTexture = remember {
        loadTexture("$itemsBaseDictionary/Collected.png")
    }
    positions.forEachIndexed { index, position ->
        val (positionX, positionY) = position.useContents { x to y }
        val offset = itemSize.div(2f)
        item(
            "$index",
            itemTexture = itemTexture,
            collectedTexture = collectedTexture,
            grid = item.grid(),
            position = position,
            dst = Rectangle(positionX - offset, positionY - offset, itemSize, itemSize),
        )
    }
}

private fun ComponentRegistry.item(
    tag: String,
    itemTexture: CValue<Texture>,
    collectedTexture: CValue<Texture>,
    grid: SpriteGrid,
    position: CValue<Vector2>,
    dst: CValue<Rectangle>,
) = component(tag) {
    var isCollected by remember {
        mutableStateOf(false)
    }
    var isDisappear by remember {
        mutableStateOf(false)
    }

    if (!isDisappear) {
        onUpdate {
            val playerHitbox = find<PlayerHitboxContext>().hitbox
            if (position.isCollisionWith(playerHitbox)) {
                isCollected = true
            }
        }
        onDraw {
            drawRectangle(dst, Colors.BROWN)
        }

        component(tag) {
            if (!isCollected) {
                spriteAnimationComponent(
                    tag ="item",
                    texture = itemTexture,
                    spriteGrid = grid,
                    framesSpeed = mutableStateOf(12),
                    dest = dst,
                )
            } else {
                spriteAnimationComponent(
                    tag = "disappear",
                    texture = collectedTexture,
                    spriteGrid = 6 to 1,
                    framesSpeed = mutableStateOf(12),
                    dest = dst,
                    onRestart = {
                        isDisappear = true
                    }
                )
            }
        }
    }
}
