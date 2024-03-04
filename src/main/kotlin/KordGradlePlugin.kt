package dev.kord.gradle.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

public class KordGradlePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        createKordExtension()
        applyVersioning()
        if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            applyMultiplatformHelpers()
        }
    }
}
