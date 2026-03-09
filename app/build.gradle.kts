plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint("me.sample.ninja.frog.main")
            linkerOpts(
                "-framework", "CoreVideo",
                "-framework", "CoreGraphics",
                "-framework", "AppKit",
                "-framework", "IOKit",
                "-framework", "OpenGL",
                "-framework", "Cocoa"
            )
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        nativeMain.dependencies {
            implementation(libs.raylib.kt.core)
            implementation(libs.raylib.kt.components)
        }
    }
}