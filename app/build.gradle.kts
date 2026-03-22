plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.raylibkt)
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
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }

        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        nativeMain.dependencies {
            implementation(libs.raylibkt.runtime)
            implementation(libs.raylibkt.components)
            implementation(libs.raylibkt.tiled)
        }
    }
}

gameAssets {
    rresAssets.create("rresBundle") {
        baseDir = project.layout.projectDirectory.dir("resources")
        resources {
            register<TextConfig>("tilemap/ninjafrog.tmj")
            register<TextConfig>("tilemap/terrain")
            register<ImageConfig>("tilemap/img/")
            register<ImageConfig>("image/")
        }
    }
}