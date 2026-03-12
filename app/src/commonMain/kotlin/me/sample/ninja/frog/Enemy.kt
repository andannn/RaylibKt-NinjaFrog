//package me.sample.ninja.frog
//
//import io.github.andannn.easings.awaitDuration
//import io.github.andannn.raylib.base.Vector2
//import io.github.andannn.raylib.base.Vector2Alloc
//import io.github.andannn.raylib.components.Entity
//import io.github.andannn.raylib.components.Positional2D
//import io.github.andannn.raylib.components.positional2DComponent
//import io.github.andannn.raylib.core.ComponentRegistry
//import io.github.andannn.raylib.core.component
//import io.github.andannn.raylib.core.getValue
//import io.github.andannn.raylib.core.mutableStateOf
//import io.github.andannn.raylib.core.nativeStateOf
//import io.github.andannn.raylib.core.onUpdate
//import io.github.andannn.raylib.core.remember
//import io.github.andannn.raylib.core.rememberSuspendingTask
//import me.sample.ninja.frog.util.checkXAxisCollision
//import me.sample.ninja.frog.util.checkYAxisCollision
//import me.sample.ninja.frog.util.updatePositionBySpeed
//import kotlin.time.Duration.Companion.seconds
//
//private const val baseResDictionary = "resources/TowDSampleRes/Main Characters"
//
//private const val SIZE = 50f
//
//class EnemyEntity(val state: Positional2D) : Entity {
//    val spriteAnimationState = mutableStateOf(MainCharacterState.IDLE)
//}
//
//private const val characterWidth = 50f
//private const val characterHeight = 50f
//private const val PLAYER_HITBOX_WIDTH_FACTOR = 0.6f
//private const val PLAYER_HITBOX_HEIGHT_FACTOR = 0.8f
//
//fun ComponentRegistry.enemy() = component("enemy") {
//    val enemyEntity = remember {
//        val widthForHitbox = characterWidth * PLAYER_HITBOX_WIDTH_FACTOR
//        val heightForHitbox = characterHeight * PLAYER_HITBOX_HEIGHT_FACTOR
//        EnemyEntity(
//            positional2DAlloc(
//                size = Vector2(widthForHitbox, heightForHitbox),
//                position = Vector2(500f, 400f),
//                offset = Vector2(
//                    -widthForHitbox / 2f,
//                    -heightForHitbox,
//                ),
//            ),
//        )
//    }
//
//    enemyAi(enemyEntity)
//    positional2DComponent(
//        "AA", enemyEntity.state
//    ) {
//        mainCharacterSpritAnimation(
//            MainCharacter.NINJA_FROG, SIZE, SIZE, enemyEntity.spriteAnimationState
//        )
//    }
//}
//
//private const val IDLE_SPEED = 100f
//private fun ComponentRegistry.enemyAi(
//    enemyEntity: EnemyEntity,
//) = component("enemyAI") {
//    val speedVector by remember {
//        nativeStateOf { Vector2Alloc() }
//    }
//
//    val idleTask = rememberSuspendingTask {
//        while (true) {
//            // Move left
//            speedVector.x = -IDLE_SPEED
//            enemyEntity.state.transform.scale.x = -1f
//            enemyEntity.spriteAnimationState.value = MainCharacterState.RUN
//            awaitDuration(2.seconds)
//
//            // Wait
//            speedVector.x = 0f
//            enemyEntity.spriteAnimationState.value = MainCharacterState.IDLE
//            awaitDuration(2.seconds)
//
//            // Move Right
//            speedVector.x = IDLE_SPEED
//            enemyEntity.state.transform.scale.x = 1f
//            enemyEntity.spriteAnimationState.value = MainCharacterState.RUN
//            awaitDuration(2.seconds)
//
//
//            // Wait
//            speedVector.x = 0f
//            enemyEntity.spriteAnimationState.value = MainCharacterState.IDLE
//            awaitDuration(2.seconds)
//        }
//    }
//
//    onUpdate { dt ->
//        enemyEntity.state.checkXAxisCollision(
//            dt,
//            speedVector,
//        )
//
//        enemyEntity.state.checkYAxisCollision(
//            dt, speedVector
//        )
//
//        enemyEntity.state.updatePositionBySpeed(dt, speedVector)
//    }
//}
