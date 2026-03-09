package me.sample.ninja.frog

import io.github.andannn.raylib.base.Colors.RAYWHITE
import io.github.andannn.raylib.core.window

fun main() {
    window(
        "ninja frog game",
        width = 800,
        height = 450,
        initialBackGroundColor = RAYWHITE,
        disableBackfaceCulling = true
    ) {
        ninjaFrogGame()
    }
}