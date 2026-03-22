package me.sample.ninja.frog

import io.github.andannn.raylib.foundation.Colors.RED
import io.github.andannn.raylib.foundation.Vector2
import io.github.andannn.raylib.foundation.distance
import io.github.andannn.raylib.components.Anchor
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Spatial2D
import io.github.andannn.raylib.components.Spatial2DAlloc
import io.github.andannn.raylib.components.firstOrNull
import io.github.andannn.raylib.components.spatial2DComponent
import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.components.toGlobalRect
import io.github.andannn.raylib.foundation.Vector2Alloc
import io.github.andannn.raylib.foundation.WindowContext
import io.github.andannn.raylib.foundation.draw
import io.github.andannn.raylib.foundation.rememberSuspendingTask
import io.github.andannn.raylib.foundation.update
import io.github.andannn.raylib.runtime.ComponentRegistry
import io.github.andannn.raylib.runtime.RememberScope
import io.github.andannn.raylib.runtime.awaitDuration
import io.github.andannn.raylib.runtime.component
import io.github.andannn.raylib.runtime.find
import io.github.andannn.raylib.runtime.getValue
import io.github.andannn.raylib.runtime.mutableStateOf
import io.github.andannn.raylib.runtime.remember
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import me.sample.ninja.frog.util.centerPoint
import me.sample.ninja.frog.util.updateYAxisWithCollision
import me.sample.ninja.frog.util.updatePositionBySpeed
import me.sample.ninja.frog.util.updateXAxisWithCollision
import kotlin.time.Duration.Companion.seconds

private const val SIZE = 50f

class EnemyEntity(
    rememberScope: RememberScope,
    position: CValue<Vector2>,
) : Entity {
    val hitboxWidth = characterWidth * PLAYER_HITBOX_WIDTH_FACTOR
    val hitboxHeight = characterHeight * PLAYER_HITBOX_HEIGHT_FACTOR

    val rootSpatial: Spatial2D = rememberScope.Spatial2DAlloc(
        size = Vector2(characterWidth, characterHeight),
        position = position,
        anchor = Anchor.BOTTOM_CENTER,
    )
    val hitboxSpatial: Spatial2D = rememberScope.Spatial2DAlloc(
        size = Vector2(hitboxWidth, hitboxHeight),
        position = Vector2(characterWidth / 2f, characterHeight),
        anchor = Anchor.BOTTOM_CENTER
    )
    val spriteAnimationState = mutableStateOf(MainCharacterState.IDLE)

    val enemyState = mutableStateOf(EnemyState.IDLE)
}

enum class EnemyState {
    IDLE, CHASING
}

private const val characterWidth = 50f
private const val characterHeight = 50f
private const val PLAYER_HITBOX_WIDTH_FACTOR = 0.6f
private const val PLAYER_HITBOX_HEIGHT_FACTOR = 0.8f

fun ComponentRegistry.enemy() = component("enemy") {
    val enemyEntity = remember {
        EnemyEntity(
            this,
            position = Vector2(500f, 400f),
        )
    }

    enemyAi(enemyEntity)
    spatial2DComponent(
        "AA", enemyEntity.rootSpatial
    ) {
        spatial2DComponent("hitbox", enemyEntity.hitboxSpatial) {
            registerEntityToWorldGrid2D(enemyEntity, enemyEntity.hitboxSpatial)
        }
//        mainCharacterSpritAnimation(
//            MainCharacter.NINJA_FROG, SIZE, SIZE, enemyEntity.spriteAnimationState
//        )

        draw {
            drawText(enemyEntity.enemyState.value.toString(), Vector2(), 10, RED)
        }
    }
}

private const val IDLE_SPEED = 100f
private const val CHASING_SPEED = 150f
private fun ComponentRegistry.enemyAi(
    enemyEntity: EnemyEntity,
) = component("enemyAI") {
    val speedVector by remember {
        Vector2Alloc()
    }

    val idleTask = rememberSuspendingTask {
        while (true) {
            // Move left
            speedVector.x = -IDLE_SPEED
            enemyEntity.rootSpatial.transform.scale.x = -1f
            enemyEntity.spriteAnimationState.value = MainCharacterState.RUN
            awaitDuration(2.seconds)

            // Wait
            speedVector.x = 0f
            enemyEntity.spriteAnimationState.value = MainCharacterState.IDLE
            awaitDuration(2.seconds)

            // Move Right
            speedVector.x = IDLE_SPEED
            enemyEntity.rootSpatial.transform.scale.x = 1f
            enemyEntity.spriteAnimationState.value = MainCharacterState.RUN
            awaitDuration(2.seconds)


            // Wait
            speedVector.x = 0f
            enemyEntity.spriteAnimationState.value = MainCharacterState.IDLE
            awaitDuration(2.seconds)
        }
    }

    val playerPoint = firstOrNull<PlayerEntity>()?.second?.toGlobalRect()?.centerPoint()
    val enemyPoint = enemyEntity.hitboxSpatial.toGlobalRect().centerPoint()
    if (playerPoint != null) {
        if (enemyEntity.enemyState.value == EnemyState.IDLE) {
            update {
                val distance = playerPoint.distance(enemyPoint)
                if (distance <= 200) {
                    enemyEntity.enemyState.value = EnemyState.CHASING
                    enemyEntity.spriteAnimationState.value = MainCharacterState.RUN
                    idleTask.stop()
                    speedVector.x = 0f
                }
            }
        }

        if (enemyEntity.enemyState.value == EnemyState.CHASING) {
            update {
                val playerX = playerPoint.useContents { x }
                val enemyX = enemyPoint.useContents { x }
                val isLeft = playerX < enemyX
                if (isLeft) {
                    speedVector.x = -CHASING_SPEED
                    enemyEntity.rootSpatial.transform.scale.x = -1f
                } else {
                    speedVector.x = CHASING_SPEED
                    enemyEntity.rootSpatial.transform.scale.x = 1f
                }
            }
        }


        if (find<WindowContext>().isDebug) {
            draw {
                drawLine(
                    start = playerPoint, end = enemyPoint, color = RED
                )
            }
        }
    }

    update { dt ->
        val rootTransform = enemyEntity.rootSpatial.transform
        rootTransform.updateXAxisWithCollision(
            dt, speedVector, enemyEntity.hitboxSpatial
        )

        rootTransform.updateYAxisWithCollision(
            dt, speedVector, enemyEntity.hitboxSpatial
        )

        enemyEntity.rootSpatial.updatePositionBySpeed(dt, speedVector)
    }
}
