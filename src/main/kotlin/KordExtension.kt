package dev.kord.gradle.tools

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

interface KordExtension {
    /**
     * The host used to build "common targets" (JVM & JS).
     *
     * Defaults to [KonanTarget.LINUX_X64]
     */
    val commonHost: Property<Family>

    /**
     * The name of the publication to publish.
     *
     * Defaults to `maven`
     */
    val publicationName: Property<String>

    /**
     * The main development branch of the projects.
     *
     * Defaults to `main`
     */
    val mainBranchName: Property<String>

    /**
     * Host to use for metadata publication.
     */
    val metadataHost: Property<Family>

    /**
     * Whether this host can run Simulators
     */
    @Suppress("IdentifierGrammar")
    val supportsSimulators: Property<Boolean>

    /**
     * The JVM target used.
     */
    val jvmTarget: Property<JvmTarget>
}

internal fun ExtensionAware.createKordExtension() = extensions.create<KordExtension>("kord").apply {
    commonHost.convention(Family.LINUX)
    metadataHost.convention(commonHost)
    publicationName.convention("maven")
    mainBranchName.convention("main")
    supportsSimulators.convention(System.getenv("TEAMCITY_VERSION") == null)
    jvmTarget.convention(JvmTarget.JVM_1_8)
}

internal val ExtensionAware.kord get() = extensions.getByName<KordExtension>("kord")
