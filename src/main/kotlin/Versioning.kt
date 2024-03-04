package dev.kord.gradle.tools

import dev.kord.gradle.tools.util.libraryVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal fun Project.applyVersioning() = afterEvaluate {
    version = libraryVersion
}

/**
 * Lazy accessor of the Project version.
 */
val Project.lazyVersion: Provider<String>
    get() = provider { version.toString() }
