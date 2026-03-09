package me.sample.ninja.frog

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Texture
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Positional2D
import io.github.andannn.raylib.components.Positional2DEntity
import io.github.andannn.raylib.components.SpriteGrid
import io.github.andannn.raylib.components.positional2DAlloc
import io.github.andannn.raylib.components.positional2DEntityComponent
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.nativeStateOf
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.setValue
import kotlinx.cinterop.CValue

class CollectionItemEntity(
    override val state: Positional2D,
) : Positional2DEntity {
    var isCollected = false

    fun collected() {
        println("item collected.")
        isCollected = true
    }
}

private const val itemsBaseDictionary = "resources/TowDSampleRes/Items/Fruits"

enum class CollectionItem(
    val fileName: String,
) {
    APPLE("Apple.png"),
    BANANAS("Bananas.png"),
}

private fun CollectionItem.grid() =
    when (this) {
        CollectionItem.APPLE -> 17 to 1
        CollectionItem.BANANAS -> 17 to 1
    }

private const val itemSize = 50f

fun ComponentRegistry.collectionItem(
    item: CollectionItem,
    positions: List<CValue<Vector2>>,
) = component("collectionItem_$item") {
    positions.forEachIndexed { index, position ->
        component(
            "item_$index",
        ) {
            val Entity by remember {
                nativeStateOf {
                    CollectionItemEntity(
                        positional2DAlloc(
                            size = Vector2(itemSize, itemSize),
                            position = position,
                            offset = Vector2(-itemSize.div(2), -itemSize.div(2)),
                        ),
                    )
                }
            }
            positional2DEntityComponent(
                positional2DEntity = Entity,
            ) {
                val itemTexture =
                    remember {
                        loadTexture("$itemsBaseDictionary/${item.fileName}")
                    }
                val collectedTexture =
                    remember {
                        loadTexture("$itemsBaseDictionary/Collected.png")
                    }
                item(
                    Entity = Entity,
                    itemTexture = itemTexture,
                    collectedTexture = collectedTexture,
                    grid = item.grid(),
                )
            }
        }
    }
}

private fun ComponentRegistry.item(
    Entity: CollectionItemEntity,
    itemTexture: CValue<Texture>,
    collectedTexture: CValue<Texture>,
    grid: SpriteGrid,
) = component(Entity) {
    var isDisappear by remember {
        mutableStateOf(false)
    }
    val dst = Rectangle(0f, 0f, itemSize, itemSize)

    if (!isDisappear) {
        component("") {
            if (!Entity.isCollected) {
                spriteAnimationComponent(
                    tag = "item",
                    texture = itemTexture,
                    spriteGrid = grid,
                    framesSpeed = mutableStateOf(12),
                    dest = dst,
                )
            } else {
                spriteAnimationComponent(
                    tag = "disappear_animation",
                    texture = collectedTexture,
                    spriteGrid = 6 to 1,
                    framesSpeed = mutableStateOf(12),
                    dest = dst,
                    onRestart = {
                        isDisappear = true
                    },
                )
            }
        }
    }
}
