package dev.kord.gradle.tools.util

import org.gradle.api.Project

private val Project.tag
    get() = git("tag", "--no-column", "--points-at", "HEAD")
        .takeIf { it.isNotBlank() }
        ?.lines()
        ?.single()

val Project.libraryVersion
    get() = tag ?: run {
        val envBranch = System.getenv("GIT_BRANCH")?.substringAfter("refs/heads/")
        val snapshotPrefix = when (val branch = envBranch ?: git("branch", "--show-current")) {
            "main" -> providers.gradleProperty("nextPlannedVersion").orNull
                ?: error("Please set nextPlannedVersion in gradle.properties")

            else -> branch.replace('/', '-')
        }
        "$snapshotPrefix-SNAPSHOT"
    }

val Project.commitHash get() = git("rev-parse", "--verify", "HEAD")
val Project.shortCommitHash get() = git("rev-parse", "--short", "HEAD")

val Project.isRelease get() = tag != null
