package dev.kord.gradle.tools.util

import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager

internal fun Family.isCurrent() = HostManager.host.family == this
