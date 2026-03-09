package me.sample.ninja.frog

import io.github.andannn.raylib.base.Colors.RAYWHITE
import io.github.andannn.raylib.core.window
import raylib.interop.rlDisableBackfaceCulling

fun main() {
    window(
        "ninja frog game",
        width = 800,
        height = 450,
        initialBackGroundColor = RAYWHITE,
        init = {
            rlDisableBackfaceCulling()
        }
    ) {
        ninjaFrogGame()
    }
}