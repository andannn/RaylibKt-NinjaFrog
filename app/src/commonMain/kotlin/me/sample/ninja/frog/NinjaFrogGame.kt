package me.sample.ninja.frog

import io.github.andannn.raylib.components.registerEntityToWorldGrid2D
import io.github.andannn.raylib.components.requireParentSpatial2D
import io.github.andannn.raylib.components.world2DGridComponent
import io.github.andannn.raylib.runtime.ComponentRegistry
import io.github.andannn.raylib.runtime.remember
import io.github.andannn.raylib.tiled.TiledMapProvider.Factory.file
import io.github.andannn.raylib.tiled.TiledMapProvider.Factory.rres
import io.github.andannn.raylib.tiled.model.PointObject
import io.github.andannn.raylib.tiled.model.RectObject
import io.github.andannn.raylib.tiled.tiledComponent

fun ComponentRegistry.ninjaFrogGame() = world2DGridComponent("2D Game", cellSize = 77) {
    background(Background.Brown)

    val provider = remember {
        rres("tilemap/ninjafrog.tmj")
    }
    tiledComponent("tiled", provider) { obj ->
        when {
            obj is RectObject && obj.type == "collision" -> {
                registerEntityToWorldGrid2D(BlockEntity, requireParentSpatial2D())
            }

            obj is PointObject && obj.type == "collection" -> {
                collectionItem(key = obj.id, CollectionItem.APPLE)
            }
        }
    }

//        collectionItem(CollectionItem.APPLE, appleCollectionItems)

//        spikeTrapComponent()

//        enemy()

    mainPlayer(MainCharacter.VIRTUAL_GUY)
}
