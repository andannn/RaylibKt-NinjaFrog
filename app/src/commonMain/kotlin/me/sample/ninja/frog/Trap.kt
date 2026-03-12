package me.sample.ninja.frog

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Texture2D
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Positional2D
import io.github.andannn.raylib.components.Positional2DAlloc
import io.github.andannn.raylib.components.positional2DComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.remember
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

class TrapEntity( val state: Positional2D) : Entity {

}

//private const val RES_DICTIONARY = "resources/TowDSampleRes/Traps/Spikes"
//private const val SPIKE_SIZE = 25f
//private const val HITBOX_RECT_FACTOR_X = 1f
//private const val HITBOX_RECT_FACTOR_Y = 0.5f
//
//private val spikePositions = listOf(
//    Vector2(150f, 400f),
//    Vector2(175f, 400f),
//    Vector2(200f, 400f),
//    Vector2(225f, 400f),
//    Vector2(250f, 400f),
//    Vector2(275f, 400f),
//)
//
//fun ComponentRegistry.spikeTrapComponent() = component("spike") {
//    val texture = remember {
//        loadTexture("${RES_DICTIONARY}/Idle.png")
//    }
//
//    val trapEntities = remember {
//        val hitboxWith = SPIKE_SIZE * HITBOX_RECT_FACTOR_X
//        val hitboxHeight = SPIKE_SIZE * HITBOX_RECT_FACTOR_Y
//        spikePositions.map { position ->
//            TrapEntity(
//                Positional2DAlloc(
//                    size = Vector2(hitboxWith, hitboxHeight),
//                    offset = Vector2(-hitboxWith / 2f, -hitboxHeight),
//                    position = position
//                )
//            )
//        }
//    }
//
//    trapEntities.forEach { entity ->
//        spikeTrapItem(texture, entity)
//    }
//}
//
//private fun ComponentRegistry.spikeTrapItem(
//    texture: CValue<Texture2D>, spikeEntity: TrapEntity
//) = positional2DComponent(
//    key = spikeEntity.toString(),
//    positional2DEntity = spikeEntity,
//) {
//    component(spikeEntity) {
//        val sourceRect = remember {
//            texture.useContents {
//                Rectangle(0f, 0f, width.toFloat(), height.toFloat())
//            }
//        }
//
//        onDraw {
//            drawTexture(
//                texture,
//                sourceRect,
//                Rectangle(
//                    -(1 - HITBOX_RECT_FACTOR_X) /2f * SPIKE_SIZE,
//                    -(1 - HITBOX_RECT_FACTOR_Y) * SPIKE_SIZE,
//                    SPIKE_SIZE,
//                    SPIKE_SIZE
//                )
//            )
//        }
//    }
//}
