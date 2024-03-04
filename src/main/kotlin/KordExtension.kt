package dev.kord.gradle.tools

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.konan.target.Family

public interface KordExtension {
    /**
     * The host used to build "common targets" (JVM & JS).
     *
     * Defaults to [Family.LINUX]
     */
    public val commonHost: Property<Family>

    /**
     * The name of the publication to publish.
     *
     * Defaults to `maven`
     */
    public val publicationName: Property<String>

    /**
     * The main development branch of the projects.
     *
     * Defaults to `main`
     */
    public val mainBranchName: Property<String>

    /**
     * Host to use for metadata publication.
     */
    public val metadataHost: Property<Family>
}

internal fun ExtensionAware.createKordExtension() = extensions.create<KordExtension>("kord").apply {
    commonHost.convention(Family.LINUX)
    metadataHost.convention(commonHost)
    publicationName.convention("maven")
    mainBranchName.convention("main")
}

internal val ExtensionAware.kord get() = extensions.getByName<KordExtension>("kord")
