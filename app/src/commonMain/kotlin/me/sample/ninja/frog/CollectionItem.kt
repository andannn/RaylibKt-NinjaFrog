package me.sample.ninja.frog

import io.github.andannn.raylib.foundation.Rectangle
import io.github.andannn.raylib.foundation.Texture
import io.github.andannn.raylib.foundation.Vector2
import io.github.andannn.raylib.components.Anchor
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.SpriteGrid
import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.components.rresTextureAsset
import io.github.andannn.raylib.components.spatial2DComponent
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.runtime.ComponentRegistry
import io.github.andannn.raylib.runtime.component
import io.github.andannn.raylib.runtime.find
import io.github.andannn.raylib.runtime.getValue
import io.github.andannn.raylib.runtime.mutableStateOf
import io.github.andannn.raylib.runtime.remember
import io.github.andannn.raylib.runtime.setValue
import kotlinx.cinterop.CValue
import rres.resources.rresBundle.RresBundleRes

class CollectionItemEntity(
) : Entity {
    var isCollected = false

    fun collected() {
// TODO: collected method will be called after collected.
        println("item collected.")
        isCollected = true
    }
}

enum class CollectionItem(
    val resourceId: UInt,
) {
    APPLE(RresBundleRes.image.image_items_fruits_apple_png), BANANAS(RresBundleRes.image.image_items_fruits_bananas_png),
}

private fun CollectionItem.grid() = when (this) {
    CollectionItem.APPLE -> 17 to 1
    CollectionItem.BANANAS -> 17 to 1
}

private const val itemSize = 32f
private const val HITBOX_RECT_FACTOR = 0.4f

fun ComponentRegistry.collectionItem(
    key: Any,
    item: CollectionItem,
) = component(key) {
    val itemTexture = remember {
        rresTextureAsset(RresBundleRes.rresFile, item.resourceId)
    }
    val collectedTexture = remember {
        rresTextureAsset(RresBundleRes.rresFile, RresBundleRes.image.image_items_fruits_collected_png)
    }

    val entity = remember {
        CollectionItemEntity()
    }

    val hitboxSize = itemSize * HITBOX_RECT_FACTOR
    spatial2DComponent(
        "hitbox", size = Vector2(hitboxSize, hitboxSize), anchor = Anchor.CENTER
    ) {
        registerEntityToWorldGrid2D(entity, it)
    }
    item(
        entity = entity,
        itemTexture = itemTexture,
        collectedTexture = collectedTexture,
        grid = item.grid(),
    )
}

private fun ComponentRegistry.item(
    entity: CollectionItemEntity,
    itemTexture: CValue<Texture>,
    collectedTexture: CValue<Texture>,
    grid: SpriteGrid,
) = component(entity) {
    var isDisappear by remember {
        mutableStateOf(false)
    }

    val dst = Rectangle(-itemSize / 2f, -itemSize / 2f, itemSize, itemSize)

    if (!isDisappear) {
        if (!entity.isCollected) {
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
                    isDisappear = true
                },
            )
        }
    }
}
