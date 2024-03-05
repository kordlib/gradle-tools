package dev.kord.gradle.tools

import dev.kord.gradle.tools.util.libraryVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * This uses [Project.afterEvaluate] to account for changes to [KordExtension.mainBranchName] which is used by
 * [libraryVersion].
 */
internal fun Project.applyVersioning() = afterEvaluate {
    version = libraryVersion
}

/**
 * Lazy accessor of the Project version.
 */
public val Project.lazyVersion: Provider<String>
    get() = provider { version.toString() }
