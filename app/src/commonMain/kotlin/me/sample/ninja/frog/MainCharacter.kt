package me.sample.ninja.frog

import io.github.andannn.easings.awaitDuration
import io.github.andannn.raylib.base.KeyboardKey
import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.base.Vector2Alloc
import io.github.andannn.raylib.base.isCollisionWith
import io.github.andannn.raylib.components.Positional2D
import io.github.andannn.raylib.components.Positional2DEntity
import io.github.andannn.raylib.components.getRect
import io.github.andannn.raylib.components.positional2DAlloc
import io.github.andannn.raylib.components.positional2DEntityComponent
import io.github.andannn.raylib.components.queryNearby
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.MutableState
import io.github.andannn.raylib.core.RememberScope
import io.github.andannn.raylib.core.State
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.getValue
import io.github.andannn.raylib.core.loadTexture
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.nativeStateOf
import io.github.andannn.raylib.core.onUpdate
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.core.rememberSuspendingTask
import io.github.andannn.raylib.core.setValue
import me.sample.ninja.frog.util.checkXAxisCollision
import me.sample.ninja.frog.util.checkYAxisCollision
import me.sample.ninja.frog.util.updatePositionBySpeed
import kotlin.time.Duration.Companion.seconds

class PlayerEntity(
    override val state: Positional2D,
    val spriteAnimationState: MutableState<MainCharacterState> = mutableStateOf(MainCharacterState.IDLE),
) : Positional2DEntity {
    var isHit: Boolean = false

    fun onHit() {
        isHit = true
    }
}


private const val horizontalMoveSpeed = 250f
private const val jumpSpeed = 600f
private const val doubleJumpSpeed = jumpSpeed * 0.8f
private const val G = 1800
private const val characterWidth = 50f
private const val characterHeight = 50f
private const val PLAYER_HITBOX_WIDTH_FACTOR = 0.6f
private const val PLAYER_HITBOX_HEIGHT_FACTOR = 0.8f
private const val WALL_SLIDE_SPEED = 70f
private const val WALL_JUMP_SPEED_X = 450f
private const val WALL_JUMP_SPEED_Y = 450f

fun ComponentRegistry.mainPlayer(mainCharacter: MainCharacter = MainCharacter.VIRTUAL_GUY) =
    component("player") {
        val playerEntity by remember {
            val widthForHitbox = characterWidth * PLAYER_HITBOX_WIDTH_FACTOR
            val heightForHitbox = characterHeight * PLAYER_HITBOX_HEIGHT_FACTOR
            nativeStateOf {
                PlayerEntity(
                    positional2DAlloc(
                        size = Vector2(widthForHitbox, heightForHitbox),
                        position = Vector2(400f, 200f),
                        offset = Vector2(
                            -widthForHitbox / 2f,
                            -heightForHitbox,
                        ),
                    ),
                )
            }
        }

        characterControl(playerEntity)
        positional2DEntityComponent(
            tag = "player",
            positional2DEntity = playerEntity,
        ) {
            mainCharacterSpritAnimation(
                character = mainCharacter,
                width = characterWidth,
                height = characterHeight,
                state = playerEntity.spriteAnimationState,
            )
        }
    }

fun ComponentRegistry.characterControl(playerEntity: PlayerEntity) =
    component("character control") {
        val speedVector by remember {
            nativeStateOf { Vector2Alloc() }
        }
        var isOnGround by remember {
            mutableStateOf(false)
        }

        var jumpCount by remember { mutableStateOf(0) }
        val maxJumps = 2

        var isXAxisInputBlocked by remember {
            mutableStateOf(false)
        }

        // 1 = slide wall at right edge. -1 = slide wall at left edge.
        var wallSlide by remember {
            mutableStateOf(0)
        }

        // block X-Axis input after wall jumping
        val wallJumpingBlockTimer = rememberSuspendingTask(startImmediately = false) {
            isXAxisInputBlocked = true
            awaitDuration(0.1.seconds)
            isXAxisInputBlocked = false
        }

        val transform = playerEntity.state.transform

        onUpdate { dt ->
            wallSlide = 0

            // move
            var inputX = 0f
            if (!isXAxisInputBlocked) {
                inputX = when {
                    KeyboardKey.KEY_RIGHT.isDown() -> 1f
                    KeyboardKey.KEY_LEFT.isDown() -> -1f
                    else -> 0f
                }
                if (inputX != 0f) transform.scale.x = inputX
                speedVector.x = inputX * horizontalMoveSpeed
            }

            playerEntity.checkXAxisCollision(
                dt,
                speedVector,
                onHitLeftEdge = {
                    if (!isOnGround) {
                        // slide wall begin
                        wallJumpingBlockTimer.stop()
                        isXAxisInputBlocked = false
                        speedVector.y = WALL_SLIDE_SPEED
                        wallSlide = -1
                        jumpCount = 0
                    }
                },
                onHitRightEdge = {
                    if (!isOnGround) {
                        // slide wall begin
                        wallJumpingBlockTimer.stop()
                        isXAxisInputBlocked = false
                        speedVector.y = WALL_SLIDE_SPEED
                        wallSlide = 1
                        jumpCount = 0
                    }
                }
            )

            // jump
            if (KeyboardKey.KEY_SPACE.isPressed()) {
                when {
                    wallSlide != 0 -> {
                        // wall jump.
                        wallJumpingBlockTimer.start()
                        speedVector.x = wallSlide * WALL_JUMP_SPEED_X
                        speedVector.y = -1 * WALL_JUMP_SPEED_Y
                    }

                    else -> {
                        if (jumpCount < maxJumps) {
                            if (!isOnGround && jumpCount == 0) {
                                jumpCount++
                            }
                            jumpCount++
                            speedVector.y = if (jumpCount >= 2) -doubleJumpSpeed else -jumpSpeed
                        }
                    }
                }
            }
            speedVector.y += G * dt

            // Check Y-Axis collision
            isOnGround = false
            playerEntity.checkYAxisCollision(dt, speedVector, onHitGround = {
                jumpCount = 0
                isOnGround = true
            })
            playerEntity.updatePositionBySpeed(dt, speedVector)
        }

        onUpdate {
            // Update animation state.
            playerEntity.spriteAnimationState.value = when {
                !isOnGround && wallSlide != 0 -> {
                    MainCharacterState.WALL_JUMP
                }

                !isOnGround -> {
                    if (jumpCount >= 2) {
                        MainCharacterState.DOUBLE_JUMP
                    } else {
                        if (speedVector.y >= 0) {
                            MainCharacterState.FAIL
                        } else {
                            MainCharacterState.JUMP
                        }
                    }
                }

                speedVector.x != 0f -> {
                    MainCharacterState.RUN
                }

                else -> {
                    MainCharacterState.IDLE
                }
            }
        }

        onUpdate {
            playerEntity.state.queryNearby<CollectionItemEntity> { entity ->
                if (entity.getRect().isCollisionWith(playerEntity.getRect())) {
                    entity.collected()
                }
            }
        }

        onUpdate {
            playerEntity.state.queryNearby<TrapEntity> { entity ->
                if (entity.getRect().isCollisionWith(playerEntity.getRect())) {
                    playerEntity.onHit()
                }
            }
        }
    }

private const val baseResDictionary = "resources/TowDSampleRes/Main Characters"

enum class MainCharacter(
    val resDictionary: String,
) {
    VIRTUAL_GUY("Virtual Guy"), NINJA_FROG("Ninja Frog"),
}

enum class MainCharacterState(
    val file: String,
) {
    IDLE("Idle (32x32).png"),
    RUN("Run (32x32).png"), JUMP("Jump (32x32).png"), FAIL("Fall (32x32).png"), DOUBLE_JUMP(
        "Double Jump (32x32).png"
    ),
    WALL_JUMP(
        "Wall Jump (32x32).png"
    ),
}

fun ComponentRegistry.mainCharacterSpritAnimation(
    character: MainCharacter,
    width: Float,
    height: Float,
    state: State<MainCharacterState>,
) = component("Main Character") {
    val idleTexture = remember { character.texture(MainCharacterState.IDLE) }
    val walkTexture = remember { character.texture(MainCharacterState.RUN) }
    val jumpTexture = remember { character.texture(MainCharacterState.JUMP) }
    val failTexture = remember { character.texture(MainCharacterState.FAIL) }
    val doubleJumpTexture = remember { character.texture(MainCharacterState.DOUBLE_JUMP) }
    val wallJumpTexture = remember { character.texture(MainCharacterState.WALL_JUMP) }

    val frameSpeed = remember { mutableStateOf(12) }
    val doubleJumpFrameSpeed = remember { mutableStateOf(18) }
    val rect = remember {
        Rectangle(
            -(1 - PLAYER_HITBOX_WIDTH_FACTOR) * characterWidth / 2f,
            -(1 - PLAYER_HITBOX_HEIGHT_FACTOR) * characterHeight,
            width,
            height
        )
    }

    when (state.value) {
        MainCharacterState.IDLE -> {
            spriteAnimationComponent(
                tag = "IDLE",
                texture = idleTexture,
                spriteGrid = 11 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.RUN -> {
            spriteAnimationComponent(
                tag = "run",
                texture = walkTexture,
                spriteGrid = 12 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.JUMP -> {
            spriteAnimationComponent(
                tag = "jump",
                texture = jumpTexture,
                spriteGrid = 1 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.FAIL -> {
            spriteAnimationComponent(
                tag = "fail",
                texture = failTexture,
                spriteGrid = 1 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.DOUBLE_JUMP -> {
            spriteAnimationComponent(
                tag = "double jump",
                texture = doubleJumpTexture,
                spriteGrid = 6 to 1,
                framesSpeed = doubleJumpFrameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.WALL_JUMP -> spriteAnimationComponent(
            tag = "wall jump",
            texture = wallJumpTexture,
            spriteGrid = 5 to 1,
            framesSpeed = frameSpeed,
            dest = rect,
        )
    }
}

context(scope: RememberScope)
private fun MainCharacter.texture(state: MainCharacterState) = when (state) {
    MainCharacterState.IDLE -> scope.loadTexture("$baseResDictionary/${resDictionary}/${state.file}")
    MainCharacterState.RUN -> scope.loadTexture("$baseResDictionary/${resDictionary}/${state.file}")
    MainCharacterState.JUMP -> scope.loadTexture("$baseResDictionary/${resDictionary}/${state.file}")
    MainCharacterState.FAIL -> scope.loadTexture("$baseResDictionary/${resDictionary}/${state.file}")
    MainCharacterState.DOUBLE_JUMP -> scope.loadTexture("$baseResDictionary/${resDictionary}/${state.file}")
    MainCharacterState.WALL_JUMP -> scope.loadTexture("$baseResDictionary/${resDictionary}/${state.file}")
}
