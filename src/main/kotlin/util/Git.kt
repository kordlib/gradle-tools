package dev.kord.gradle.tools.util

import org.gradle.api.Project
import java.io.ByteArrayOutputStream

internal fun Project.git(vararg command: String): String {
    val output = ByteArrayOutputStream()
    providers.exec {
        commandLine("git", *command)
        standardOutput = output
        errorOutput = output
        workingDir = rootDir
    }.result.get().rethrowFailure().assertNormalExitValue()
    return output.toString().trim()
}
