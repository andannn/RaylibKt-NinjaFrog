package me.sample.ninja.frog


import kotlinx.cinterop.CValue
import io.github.andannn.raylib.base.Colors.LIGHTGRAY
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.Context
import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.mutableStateOf
import io.github.andannn.raylib.core.onDraw
import io.github.andannn.raylib.core.provide
import io.github.andannn.raylib.core.remember
import io.github.andannn.raylib.base.rlMatrix
import io.github.andannn.raylib.components.Transform2DAlloc
import io.github.andannn.raylib.components.getHitbox
import io.github.andannn.raylib.components.transform2DComponent

private const val characterWidth = 50f
private const val characterHeight = 50f

private val appleCollectionItems = listOf(
    Vector2(50f, 250f),
    Vector2(50f, 300f),
    Vector2(50f, 350f),
    Vector2(100f, 300f),
    Vector2(100f, 350f),
    Vector2(150f, 350f),
)

fun ComponentRegistry.ninjaFrogGame() {
    component("2D Game") {
        val state = remember {
            mutableStateOf(MainCharacterState.IDLE)
        }
        val transform = remember {
            Transform2DAlloc(
                position = Vector2(400f, 200f),
                offset = Vector2(-characterWidth / 2f, -characterHeight)
            )
        }
        val playerHitboxContext = remember {
            PlayerHitboxContext()
        }

        val collisionBlocks = remember {
            listOf(
                Rectangle(0f, 400f, 800f, 50f)
            )
        }

        background(Background.Brown)
        characterControl(transform, state, collisionBlocks)

        onDraw {
            rlMatrix {
                translate(0f, 25 * 50f, 0f)
                rotate(90f, 1f, 0f, 0f)
                drawGrid(100, 50f)
            }
        }

        onDraw {
            collisionBlocks.forEach { block ->
                drawRectangle(block, LIGHTGRAY)
            }
        }

        playerHitboxContext.internalRectangle = transform.getHitbox(characterWidth.times(0.9f), characterHeight.times(0.9f))

        provide(playerHitboxContext) {
            collectionItem(
                CollectionItem.APPLE,
                appleCollectionItems
            )

            transform2DComponent(transform, tag = "player") {
                mainCharacterSpritAnimation(
                    mainCharacter = MainCharacter.VIRTUAL_GUY,
                    width = characterWidth,
                    height = characterHeight,
                    state = state
                )
            }
        }
    }
}

class PlayerHitboxContext: Context {
    internal var internalRectangle: CValue<Rectangle>? = null

    val hitbox get() = internalRectangle!!
}