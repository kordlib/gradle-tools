package dev.kord.gradle.tools

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.konan.target.KonanTarget

interface KordExtension {
    /**
     * The host used to build "common targets" (JVM & JS).
     *
     * Defaults to [KonanTarget.LINUX_X64]
     */
    val commonHost: Property<KonanTarget>
}

internal fun ExtensionAware.createKordExtension() = extensions.create<KordExtension>("kord").apply {
    commonHost.convention(KonanTarget.LINUX_X64)
}

internal val ExtensionAware.kord get() = extensions.getByName<KordExtension>("kord")
