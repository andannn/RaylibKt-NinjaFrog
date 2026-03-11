package me.sample.ninja.frog.util

import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.components.Positional2DEntity
import io.github.andannn.raylib.components.getRect
import io.github.andannn.raylib.components.queryNearby
import io.github.andannn.raylib.components.queryNearbyUntil
import io.github.andannn.raylib.core.ContextProvider
import io.github.andannn.raylib.core.GameContext
import kotlinx.cinterop.useContents
import me.sample.ninja.frog.BlockEntity

context(_: GameContext, _: ContextProvider)
fun Positional2DEntity.updatePositionBySpeed(dt: Float, speedVector: Vector2) {
    val transform = state.transform
    transform.position.x += speedVector.x * dt
    transform.position.y += speedVector.y * dt
}

context(_: GameContext, _: ContextProvider)
inline fun Positional2DEntity.checkXAxisCollision(
    dt: Float,
    speedVector: Vector2,
    crossinline onHitRightEdge: () -> Unit = {},
    crossinline onHitLeftEdge: () -> Unit = {},
) {
    val transform = state.transform
    val (playerX, playerY) = getRect().useContents { x to y }
    val (playerWidth, playerHeight) = getRect().useContents { width to height }

    if (speedVector.x != 0f) {
        state.queryNearby<BlockEntity> {
            it.getRect().useContents {
                if (playerY + playerHeight >= y && playerY <= y + height) {
                    if (speedVector.x < 0 && playerX >= x + width && playerX + speedVector.x * dt <= x + width) {
                        // hit right of block
                        speedVector.x = 0f
                        transform.position.x = x + width + playerWidth.div(2f)


                        onHitRightEdge()
                    }

                    if (speedVector.x > 0 && playerX + playerWidth <= x && playerX + playerWidth + speedVector.x * dt >= x) {
                        // hit left of block
                        speedVector.x = 0f
                        transform.position.x = x - playerWidth.div(2f)

                        onHitLeftEdge()
                    }
                }
            }
        }
    }
}

context(_: GameContext, _: ContextProvider)
inline fun Positional2DEntity.checkYAxisCollision(
    dt: Float,
    speedVector: Vector2,
    crossinline onHitGround: () -> Unit = {}
) {
    if (speedVector.y >= 0) {
        val transform = state.transform
        val (playerX, playerY) = getRect().useContents { x to y }
        val (playerWidth, playerHeight) = getRect().useContents { width to height }

        var currentHitGround = false
        state.queryNearbyUntil<BlockEntity> { block ->
            block.getRect().useContents {
                val isHorizontalOverlap = playerX < x + width && playerX + playerWidth > x

                if (isHorizontalOverlap && y >= playerY + playerHeight && y <= playerY + playerHeight + speedVector.y * dt) {
                    currentHitGround = true
                    speedVector.y = 0.0f
                    transform.position.y = y

                    onHitGround()
                }
            }
            currentHitGround
        }
    }
}
