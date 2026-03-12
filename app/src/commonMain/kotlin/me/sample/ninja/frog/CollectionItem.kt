package me.sample.ninja.frog

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Texture
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Positional2D
import io.github.andannn.raylib.components.SpriteGrid
import io.github.andannn.raylib.components.Positional2DAlloc
import io.github.andannn.raylib.components.positional2DComponent
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.NativeState
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.doOnce
import io.github.andannn.raylib.core.downEach
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.mutableStateListOf
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.setValue
import kotlinx.cinterop.CValue

class CollectionItemEntity(
    val state: Positional2D,
) : Entity {
    var isCollected = false

    fun collected() {
        println("item collected.")
        isCollected = true
    }
}

//private const val itemsBaseDictionary = "resources/TowDSampleRes/Items/Fruits"
//
//enum class CollectionItem(
//    val fileName: String,
//) {
//    APPLE("Apple.png"), BANANAS("Bananas.png"),
//}
//
//private fun CollectionItem.grid() = when (this) {
//    CollectionItem.APPLE -> 17 to 1
//    CollectionItem.BANANAS -> 17 to 1
//}
//
//private const val itemSize = 50f
//private const val HITBOX_RECT_FACTOR = 0.4f
//
//fun ComponentRegistry.collectionItem(
//    item: CollectionItem,
//    positions: List<CValue<Vector2>>,
//) = component("collectionItem_$item") {
//    val itemList = remember {
//        mutableStateListOf<CollectionItemEntity>()
//    }
//
//    doOnce {
//        positions.forEachIndexed { _, position ->
//            itemList.addState {
//                val hitboxSize = itemSize * HITBOX_RECT_FACTOR
//                CollectionItemEntity(
//                    Positional2DAlloc(
//                        size = Vector2(hitboxSize, hitboxSize),
//                        position = position,
//                        offset = Vector2(-hitboxSize.div(2), -hitboxSize.div(2)),
//                    ),
//                )
//            }
//        }
//    }
//
//    itemList.downEach { _, entity ->
//        positional2DComponent(
//            key = "item_${entity.hashCode()}",
//            state = entity.value.state,
//        ) {
//            val itemTexture = remember {
//                loadTexture("$itemsBaseDictionary/${item.fileName}")
//            }
//            val collectedTexture = remember {
//                loadTexture("$itemsBaseDictionary/Collected.png")
//            }
//            item(
//                entity = entity,
//                itemTexture = itemTexture,
//                collectedTexture = collectedTexture,
//                grid = item.grid(),
//            )
//        }
//    }
//}
//
//private fun ComponentRegistry.item(
//    entity: NativeState<CollectionItemEntity>,
//    itemTexture: CValue<Texture>,
//    collectedTexture: CValue<Texture>,
//    grid: SpriteGrid,
//) = component(entity) {
//    var isDisappear by remember {
//        mutableStateOf(false)
//    }
//    val offset = (1 - HITBOX_RECT_FACTOR) * itemSize / 2f
//
//    val dst = Rectangle(-offset, -offset, itemSize, itemSize)
//
//    if (!isDisappear) {
//        if (!entity.value.isCollected) {
//            spriteAnimationComponent(
//                key = "item",
//                texture = itemTexture,
//                spriteGrid = grid,
//                framesSpeed = mutableStateOf(12),
//                dest = dst,
//            )
//        } else {
//            spriteAnimationComponent(
//                key = "disappear_animation",
//                texture = collectedTexture,
//                spriteGrid = 6 to 1,
//                framesSpeed = mutableStateOf(12),
//                dest = dst,
//                onRestart = {
//                    entity.dispose()
//                },
//            )
//        }
//    }
//}
