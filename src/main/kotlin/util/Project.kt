package dev.kord.gradle.tools.util

import dev.kord.gradle.tools.kord
import org.gradle.api.Project

private val Project.tag
    get() = git("tag", "--no-column", "--points-at", "HEAD")
        .takeIf { it.isNotBlank() }
        ?.lines()
        ?.single()

public val Project.libraryVersion: String
    get() = tag ?: run {
        val prBranch = System.getenv("PR_BRANCH")?.ifBlank { null }
        val envBranch = prBranch ?: System.getenv("GIT_BRANCH")?.substringAfter("refs/heads/")
        val snapshotPrefix = when (val branch = envBranch ?: git("branch", "--show-current")) {
            kord.mainBranchName.get() -> providers.gradleProperty("nextPlannedVersion").orNull
                ?: error("Please set nextPlannedVersion in gradle.properties")

            else -> branch.replace('/', '-')
        }
        "$snapshotPrefix-SNAPSHOT"
    }.also { logger.lifecycle("Version set to: $it") }

public val Project.commitHash: String get() = git("rev-parse", "--verify", "HEAD")
public val Project.shortCommitHash: String get() = git("rev-parse", "--short", "HEAD")

public val Project.isRelease: Boolean get() = tag != null
