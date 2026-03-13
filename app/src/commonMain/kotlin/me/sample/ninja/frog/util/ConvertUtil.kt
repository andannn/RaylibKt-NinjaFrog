package me.sample.ninja.frog.util

import io.github.andannn.raylib.base.Rectangle
import io.github.andannn.raylib.base.Texture
import io.github.andannn.raylib.base.Vector2
import io.github.andannn.raylib.base.distance
import io.github.andannn.raylib.components.Entity
import io.github.andannn.raylib.components.Spatial2D
import io.github.andannn.raylib.components.firstOrNull
import io.github.andannn.raylib.components.toGlobalRect
import io.github.andannn.raylib.core.ContextProvider
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents


fun CValue<Rectangle>.centerPoint() = this.useContents {
    Vector2(x + width / 2f, y + height / 2f)
}

context(provider: ContextProvider)
inline fun <reified T : Entity> Spatial2D.distanceToOrNull(): Float? {
    return provider.firstOrNull<T>()?.second?.toGlobalRect()?.centerPoint()?.distance(this.toGlobalRect().centerPoint())
}

fun CValue<Texture>.toSrcRect() = useContents {
    Rectangle(0f, 0f, width.toFloat(), height.toFloat())
}