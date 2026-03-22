package me.sample.ninja.frog.util

import io.github.andannn.raylib.foundation.Vector2
import io.github.andannn.raylib.components.Spatial2D
import io.github.andannn.raylib.components.Transform2D
import io.github.andannn.raylib.components.queryNearby
import io.github.andannn.raylib.components.queryNearbyUntil
import io.github.andannn.raylib.components.toGlobalRect
import io.github.andannn.raylib.foundation.GameContext
import io.github.andannn.raylib.runtime.ContextProvider
import kotlinx.cinterop.useContents
import me.sample.ninja.frog.BlockEntity

context(_: GameContext, _: ContextProvider)
fun Spatial2D.updatePositionBySpeed(dt: Float, speedVector: Vector2) {
    val transform = transform
    transform.position.x += speedVector.x * dt
    transform.position.y += speedVector.y * dt
}

context(_: GameContext, _: ContextProvider)
inline fun Transform2D.updateXAxisWithCollision(
    dt: Float,
    speedVector: Vector2,
    collisionBox: Spatial2D,
    crossinline onHitRightEdge: () -> Unit = {},
    crossinline onHitLeftEdge: () -> Unit = {},
) {
    val rootTransform = this
    val globalRect = collisionBox.toGlobalRect()
    val (playerX, playerY) = globalRect.useContents { x to y }
    val (playerWidth, playerHeight) = globalRect.useContents { width to height }

    if (speedVector.x != 0f) {
        collisionBox.queryNearby<BlockEntity> { entity, position, _ ->
            position.toGlobalRect().useContents {
                if (playerY + playerHeight >= y && playerY <= y + height) {
                    if (speedVector.x < 0 && playerX >= x + width && playerX + speedVector.x * dt <= x + width) {
                        // hit right of block
                        speedVector.x = 0f
                        rootTransform.position.x = x + width + playerWidth.div(2f)


                        onHitRightEdge()
                    }

                    if (speedVector.x > 0 && playerX + playerWidth <= x && playerX + playerWidth + speedVector.x * dt >= x) {
                        // hit left of block
                        speedVector.x = 0f
                        rootTransform.position.x = x - playerWidth.div(2f)

                        onHitLeftEdge()
                    }
                }
            }
        }
    }
}

context(_: GameContext, _: ContextProvider)
inline fun Transform2D.updateYAxisWithCollision(
    dt: Float, speedVector: Vector2, collisionBox: Spatial2D, crossinline onHitGround: () -> Unit = {}
) {
    if (speedVector.y >= 0) {
        val rootTransform = this
        val globalRect = collisionBox.toGlobalRect()
        val (playerX, playerY) = globalRect.useContents { x to y }
        val (playerWidth, playerHeight) = globalRect.useContents { width to height }

        var currentHitGround = false
        collisionBox.queryNearbyUntil<BlockEntity> { entity, block, _ ->
            block.toGlobalRect().useContents {
                val isHorizontalOverlap = playerX < x + width && playerX + playerWidth > x

                if (isHorizontalOverlap && y >= playerY + playerHeight && y <= playerY + playerHeight + speedVector.y * dt) {
                    currentHitGround = true
                    speedVector.y = 0.0f
                    rootTransform.position.y = y

                    onHitGround()
                }
            }
            currentHitGround
        }
    }
}
