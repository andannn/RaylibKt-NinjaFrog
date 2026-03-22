package me.sample.ninja.frog

import io.github.andannn.raylib.foundation.Colors.LIGHTGRAY
import io.github.andannn.raylib.components.gameAssetsComponent
import io.github.andannn.raylib.foundation.window
import io.github.andannn.raylib.runtime.provideStaticDependency
import io.github.andannn.raylib.rres.ResourceContext
import raylib.interop.rlDisableBackfaceCulling
import rres.resources.rresBundle.RresBundleRes

fun main() {
    window(
        "ninja frog game",
        height = 400,
        width = 640,
        isDebug = true,
        initialBackGroundColor = LIGHTGRAY,
        init = {
            rlDisableBackfaceCulling()
            provideStaticDependency(ResourceContext())
        },
    ) {
        gameAssetsComponent(listOf(RresBundleRes.rresFile)) {
            ninjaFrogGame()
        }
    }
}
