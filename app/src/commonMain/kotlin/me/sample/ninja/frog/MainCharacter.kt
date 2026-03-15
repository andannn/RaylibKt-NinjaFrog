package me.sample.ninja.frog

import io.github.andannn.raylib.components.Anchor
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Spatial2D
import io.github.andannn.raylib.components.Spatial2DAlloc
import io.github.andannn.raylib.components.queryAABBCollision
import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.components.rresTextureAsset
import io.github.andannn.raylib.components.spatial2DComponent
import io.github.andannn.raylib.components.spriteAnimationComponent
import io.github.andannn.raylib.foundation.KeyboardKey
import io.github.andannn.raylib.foundation.Rectangle
import io.github.andannn.raylib.foundation.Vector2
import io.github.andannn.raylib.foundation.Vector2Alloc
import io.github.andannn.raylib.foundation.rememberSuspendingTask
import io.github.andannn.raylib.foundation.update
import io.github.andannn.raylib.runtime.ComponentRegistry
import io.github.andannn.raylib.runtime.ContextRegistry
import io.github.andannn.raylib.runtime.MutableState
import io.github.andannn.raylib.runtime.RememberScope
import io.github.andannn.raylib.runtime.State
import io.github.andannn.raylib.runtime.awaitDuration
import io.github.andannn.raylib.runtime.component
import io.github.andannn.raylib.runtime.getValue
import io.github.andannn.raylib.runtime.mutableStateOf
import io.github.andannn.raylib.runtime.remember
import io.github.andannn.raylib.runtime.setValue
import kotlinx.cinterop.CValue
import me.sample.ninja.frog.util.updatePositionBySpeed
import me.sample.ninja.frog.util.updateXAxisWithCollision
import me.sample.ninja.frog.util.updateYAxisWithCollision
import rres.resources.rresBundle.RresBundleRes
import kotlin.time.Duration.Companion.seconds

class PlayerEntity(
    rememberScope: RememberScope,
    initialPosition: CValue<Vector2>,
    val spriteAnimationState: MutableState<MainCharacterState> = mutableStateOf(MainCharacterState.IDLE),
) : Entity {
    val hitboxWidth = characterWidth * PLAYER_HITBOX_WIDTH_FACTOR
    val hitboxHeight = characterHeight * PLAYER_HITBOX_HEIGHT_FACTOR

    val rootSpatial: Spatial2D = rememberScope.Spatial2DAlloc(
        size = Vector2(characterWidth, characterWidth), position = initialPosition, anchor = Anchor.BOTTOM_CENTER
    )

    val hitboxSpatial: Spatial2D = rememberScope.Spatial2DAlloc(
        size = Vector2(hitboxWidth, hitboxHeight),
        position = Vector2(characterWidth / 2f, characterHeight),
        anchor = Anchor.BOTTOM_CENTER
    )

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

fun ComponentRegistry.mainPlayer(mainCharacter: MainCharacter = MainCharacter.VIRTUAL_GUY) = component("player") {
    val playerEntity = remember {
        PlayerEntity(
            this,
            Vector2(50f, 300f),
        )
    }

    characterControl(playerEntity)
    spatial2DComponent(
        key = "player",
        playerEntity.rootSpatial,
    ) {
        // hitbox
        spatial2DComponent(
            "hitbox", playerEntity.hitboxSpatial
        ) {
            registerEntityToWorldGrid2D(playerEntity, playerEntity.hitboxSpatial)
        }

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
            Vector2Alloc()
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

        val rootTransform = playerEntity.rootSpatial.transform

        update { dt ->
            wallSlide = 0

            // move
            var inputX = 0f
            if (!isXAxisInputBlocked) {
                inputX = when {
                    KeyboardKey.KEY_RIGHT.isDown() -> 1f
                    KeyboardKey.KEY_LEFT.isDown() -> -1f
                    else -> 0f
                }
                if (inputX != 0f) rootTransform.scale.x = inputX
                speedVector.x = inputX * horizontalMoveSpeed
            }

            rootTransform.updateXAxisWithCollision(
                dt, speedVector, playerEntity.hitboxSpatial,
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
                },
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
            rootTransform.updateYAxisWithCollision(
                dt,
                speedVector,
                playerEntity.hitboxSpatial,
                onHitGround = {
                    jumpCount = 0
                    isOnGround = true
                },
            )
            playerEntity.rootSpatial.updatePositionBySpeed(dt, speedVector)
        }

        update {
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

        update {
            playerEntity.hitboxSpatial.queryAABBCollision<CollectionItemEntity> { entity, spatial, _ ->
                println("hithithi")
                entity.collected()
            }
        }

        update {
            playerEntity.rootSpatial.queryAABBCollision<TrapEntity> { entity, position, _ ->
                playerEntity.onHit()
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
    val resourceId: UInt,
) {
    IDLE(RresBundleRes.image.image_main_characters_virtual_guy_idle_32x32_png),
    RUN(RresBundleRes.image.image_main_characters_virtual_guy_run_32x32_png),
    JUMP(RresBundleRes.image.image_main_characters_virtual_guy_jump_32x32_png),
    FAIL(RresBundleRes.image.image_main_characters_virtual_guy_fall_32x32_png),
    DOUBLE_JUMP(RresBundleRes.image.image_main_characters_virtual_guy_double_jump_32x32_png),
    WALL_JUMP(RresBundleRes.image.image_main_characters_virtual_guy_wall_jump_32x32_png),
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
        Rectangle(0f, 0f, width, height)
    }

    var controller: DustParticleController? by remember {
        mutableStateOf(null)
    }
    val dustPosition = remember { Vector2(width / 2f, height) }
    when (state.value) {
        MainCharacterState.IDLE -> {
            spriteAnimationComponent(
                key = "IDLE",
                texture = idleTexture,
                spriteGrid = 11 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.RUN -> {
            spriteAnimationComponent(
                key = "run",
                texture = walkTexture,
                spriteGrid = 12 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
                onFrame = { (frame, _) ->
                    if (frame == 5 || frame == 11) {
                        controller?.triggerInPosition(dustPosition, dustCount = 3)
                    }
                }
            )
        }

        MainCharacterState.JUMP -> {
            spriteAnimationComponent(
                key = "jump",
                texture = jumpTexture,
                spriteGrid = 1 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
                onFrame = {
                }
            )
        }

        MainCharacterState.FAIL -> {
            spriteAnimationComponent(
                key = "fail",
                texture = failTexture,
                spriteGrid = 1 to 1,
                framesSpeed = frameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.DOUBLE_JUMP -> {
            spriteAnimationComponent(
                key = "double jump",
                texture = doubleJumpTexture,
                spriteGrid = 6 to 1,
                framesSpeed = doubleJumpFrameSpeed,
                dest = rect,
            )
        }

        MainCharacterState.WALL_JUMP -> spriteAnimationComponent(
            key = "wall jump",
            texture = wallJumpTexture,
            spriteGrid = 5 to 1,
            framesSpeed = frameSpeed,
            dest = rect,
        )
    }
    controller = dustParticle()
}

context(scope: ContextRegistry)
private fun MainCharacter.texture(state: MainCharacterState) = when (state) {
    MainCharacterState.IDLE -> scope.rresTextureAsset(RresBundleRes.rresFile, state.resourceId)
    MainCharacterState.RUN -> scope.rresTextureAsset(RresBundleRes.rresFile, state.resourceId)
    MainCharacterState.JUMP -> scope.rresTextureAsset(RresBundleRes.rresFile, state.resourceId)
    MainCharacterState.FAIL -> scope.rresTextureAsset(RresBundleRes.rresFile, state.resourceId)
    MainCharacterState.DOUBLE_JUMP -> scope.rresTextureAsset(RresBundleRes.rresFile, state.resourceId)
    MainCharacterState.WALL_JUMP -> scope.rresTextureAsset(RresBundleRes.rresFile, state.resourceId)
}