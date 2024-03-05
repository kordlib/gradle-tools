package dev.kord.gradle.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused") // see build.gradle.kts
class KordGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        createKordExtension()
        applyVersioning()
        applyCommonSettings()
        if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            applyMultiplatformHelpers()
        }
    }
}
