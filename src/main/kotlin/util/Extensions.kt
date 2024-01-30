package dev.kord.gradle.tools.util

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

fun KonanTarget.isCurrent() = HostManager.host == this
