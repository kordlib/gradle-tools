package dev.kord.gradle.tools.util

import org.gradle.api.Project

internal fun Project.git(vararg command: String): String {
    val output = providers.exec {
        commandLine("git", *command)
        workingDir = rootDir
    }

    return output.standardOutput.asText.map { it.trim() }.get()
}
