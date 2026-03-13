package me.sample.ninja.frog

import io.github.andannn.easings.awaitDuration
import io.github.andannn.raylib.base.Colors.RED
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.base.distance
import io.github.andannn.raylib.components.Anchor
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Spatial2D
import io.github.andannn.raylib.components.Spatial2DAlloc
import io.github.andannn.raylib.components.firstOrNull
import io.github.andannn.raylib.components.spatial2DComponent
import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.components.toGlobalRect
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.RememberScope
import io.github.andannn.raylib.core.Vector2Alloc
import io.github.andannn.raylib.core.WindowContext
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.find
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.onUpdate
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.rememberSuspendingTask
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import me.sample.ninja.frog.util.centerPoint
import me.sample.ninja.frog.util.distanceToOrNull
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

        onDraw {
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
            onUpdate {
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
            onUpdate {
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
            onDraw {
                drawLine(
                    start = playerPoint, end = enemyPoint, color = RED
                )
            }
        }
    }

    onUpdate { dt ->
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
