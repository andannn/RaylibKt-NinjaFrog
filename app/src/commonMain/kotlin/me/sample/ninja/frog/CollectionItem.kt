package me.sample.ninja.frog

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Texture
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Anchor
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Spatial2D
import io.github.andannn.raylib.components.Spatial2DAlloc
import io.github.andannn.raylib.components.SpriteGrid
import io.github.andannn.raylib.components.spatial2DComponent
import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.NativeState
import io.github.andannn.raylib.core.RememberScope
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.components
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.mutableStateListOf
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.setValue
import kotlinx.cinterop.CValue

class CollectionItemEntity(
    rememberScope: RememberScope,
    position: CValue<Vector2>
) : Entity {

    val rootSpatial: Spatial2D = rememberScope.Spatial2DAlloc(
        size = Vector2(itemSize, itemSize),
        position = position,
        anchor = Anchor.CENTER
    )

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
    APPLE("Apple.png"), BANANAS("Bananas.png"),
}

private fun CollectionItem.grid() = when (this) {
    CollectionItem.APPLE -> 17 to 1
    CollectionItem.BANANAS -> 17 to 1
}

private const val itemSize = 50f
private const val HITBOX_RECT_FACTOR = 0.4f

fun ComponentRegistry.collectionItem(
    item: CollectionItem,
    positions: List<CValue<Vector2>>,
) = component("collectionItem_$item") {
    val itemList = remember {
        mutableStateListOf<CollectionItemEntity>().apply {
            positions.forEachIndexed { _, position ->
                addState {
                    CollectionItemEntity(this@remember, position)
                }
            }
        }
    }

    val itemTexture = remember {
        loadTexture("$itemsBaseDictionary/${item.fileName}")
    }
    val collectedTexture = remember {
        loadTexture("$itemsBaseDictionary/Collected.png")
    }

    components(
        itemList,
        key = {it}
    ) { entity ->
        spatial2DComponent(
            key = entity,
            state = entity.value.rootSpatial,
        ) {
            val hitboxSize = itemSize * HITBOX_RECT_FACTOR
            spatial2DComponent(
                "hitbox",
                size = Vector2(hitboxSize, hitboxSize),
                position = Vector2(itemSize / 2f, itemSize / 2f),
                anchor = Anchor.CENTER
            ) {
                registerEntityToWorldGrid2D(entity.value, it)
            }
            item(
                entity = entity,
                itemTexture = itemTexture,
                collectedTexture = collectedTexture,
                grid = item.grid(),
            )
        }
    }
}

private fun ComponentRegistry.item(
    entity: NativeState<CollectionItemEntity>,
    itemTexture: CValue<Texture>,
    collectedTexture: CValue<Texture>,
    grid: SpriteGrid,
) = component(entity) {
    var isDisappear by remember {
        mutableStateOf(false)
    }

    val dst = Rectangle(0f, 0f, itemSize, itemSize)

    if (!isDisappear) {
        if (!entity.value.isCollected) {
            spriteAnimationComponent(
                key = "item",
                texture = itemTexture,
                spriteGrid = grid,
                framesSpeed = mutableStateOf(12),
                dest = dst,
            )
        } else {
            spriteAnimationComponent(
                key = "disappear_animation",
                texture = collectedTexture,
                spriteGrid = 6 to 1,
                framesSpeed = mutableStateOf(12),
                dest = dst,
                onRestart = {
                    entity.dispose()
                },
            )
        }
    }
}
