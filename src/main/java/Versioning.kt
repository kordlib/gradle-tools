package dev.kord.gradle.tools

import dev.kord.gradle.tools.util.libraryVersion
import org.gradle.api.Project

fun Project.applyVersioning() = afterEvaluate {
    version = libraryVersion
}
