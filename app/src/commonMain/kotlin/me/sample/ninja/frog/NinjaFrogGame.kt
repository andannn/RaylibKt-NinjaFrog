package me.sample.ninja.frog

import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.base.rlMatrix
import io.github.andannn.raylib.components.world2DGridComponent
import io.github.andannn.raylib.core.ComponentRegistry
import io.github.andannn.raylib.core.component
import io.github.andannn.raylib.core.onDraw

private val appleCollectionItems =
    listOf(
        Vector2(50f, 250f),
        Vector2(50f, 300f),
        Vector2(50f, 350f),
        Vector2(100f, 300f),
        Vector2(100f, 350f),
        Vector2(150f, 350f),
    )

fun ComponentRegistry.ninjaFrogGame() =
    world2DGridComponent("2D Game", cellSize = 77) {
        background(Background.Brown)

        blockComponents()

        collectionItem(CollectionItem.APPLE, appleCollectionItems)

        spikeTrapComponent()

        enemy()

        mainPlayer(MainCharacter.VIRTUAL_GUY)

        component("debug") {
            onDraw {
                rlMatrix {
                    translate(0f, 25 * 50f, 0f)
                    rotate(90f, 1f, 0f, 0f)
                    drawGrid(100, 50f)
                }
            }
        }
    }
