package me.sample.ninja.frog

import io.github.andannn.easings.Ease
import io.github.andannn.easings.animateTo
import io.github.andannn.raylib.components.rresTextureAsset
import io.github.andannn.raylib.foundation.Colors.WHITE
import io.github.andannn.raylib.foundation.Rectangle
import io.github.andannn.raylib.foundation.Texture
import io.github.andannn.raylib.foundation.Vector2
import io.github.andannn.raylib.foundation.draw
import io.github.andannn.raylib.foundation.randomValue
import io.github.andannn.raylib.foundation.rememberSuspendingTask
import io.github.andannn.raylib.runtime.ComponentRegistry
import io.github.andannn.raylib.runtime.ComponentScope
import io.github.andannn.raylib.runtime.NativeState
import io.github.andannn.raylib.runtime.awaitDuration
import io.github.andannn.raylib.runtime.components
import io.github.andannn.raylib.runtime.getValue
import io.github.andannn.raylib.runtime.mutableStateListOf
import io.github.andannn.raylib.runtime.mutableStateOf
import io.github.andannn.raylib.runtime.remember
import io.github.andannn.raylib.runtime.setValue
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import me.sample.ninja.frog.util.toSrcRect
import raylib.interop.Fade
import rres.resources.rresBundle.RresBundleRes
import kotlin.time.Duration.Companion.seconds

interface DustParticleController {
    fun triggerInPosition(value: CValue<Vector2>, dustCount: Int = 4)
}

fun ComponentRegistry.dustParticle(): DustParticleController {
    val dustContainer = remember {
        mutableStateListOf<DustParticle>()
    }
    var id by remember {
        mutableStateOf(0)
    }

    val controller = remember {
        object : DustParticleController {
            override fun triggerInPosition(value: CValue<Vector2>, dustCount: Int) {
                dustContainer.addState {
                    DustParticle(id++, position = value, dustCount)
                }
            }
        }

    }
    val texture = remember {
        rresTextureAsset(RresBundleRes.rresFile, RresBundleRes.image.image_other_dust_particle_png)
    }

    components(dustContainer, { "dust${it.id}" }) {
        dustEffect(it, texture)
    }

    return controller
}

private class DustParticle(
    val id: Int, val position: CValue<Vector2>, val dustCount: Int = 4
)

private fun ComponentScope.dustEffect(state: NativeState<DustParticle>, dustTexture: CValue<Texture>) {
    var fade by remember {
        mutableStateOf(1f)
    }
    val offsetX = remember {
        Array(state.value.dustCount) { mutableStateOf(0f) }
    }
    val offsetY = remember {
        Array(state.value.dustCount) { mutableStateOf(0f) }
    }
    val randomX: Array<Float> = remember {
        Array(state.value.dustCount) { randomValue(-30, 30).toFloat() }
    }
    val randomY = remember {
        Array(state.value.dustCount) { randomValue(-10, -30).toFloat() }
    }
    rememberSuspendingTask {
        awaitDuration(0.4.seconds) {
            fade = 1f.animateTo(0f, it, Ease.QuadOut)
            offsetX.forEachIndexed { index, state ->
                state.value = 0f.animateTo(randomX[index], it, Ease.QuadOut)
            }
            offsetY.forEachIndexed { index, state ->
                state.value = 0f.animateTo(randomY[index], it, Ease.QuadOut)
            }
        }

        state.dispose()
    }

    val src = remember { dustTexture.toSrcRect() }

    draw {
        for (i in 0 until state.value.dustCount) {
            val (positionX, positionY) = state.value.position.useContents { x to y }
            val dst = dustTexture.useContents {
                Rectangle(
                    positionX - width / 2f + offsetX[i].value,
                    positionY - height / 2f + offsetY[i].value,
                    width.toFloat(),
                    height.toFloat()
                )
            }
            drawTexture(dustTexture, src, dst, tint = Fade(WHITE, fade))
        }
    }
}