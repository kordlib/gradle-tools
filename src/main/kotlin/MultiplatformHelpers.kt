package dev.kord.gradle.tools

import dev.kord.gradle.tools.util.isCurrent
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.hasPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import kotlin.reflect.KClass

private val darwinFamilies = listOf(
    Family.IOS,
    Family.TVOS,
    Family.OSX,
    Family.WATCHOS
)

internal fun Project.applyJvmHelpers() {
    tasks {
        if (plugins.hasPlugin(MavenPublishPlugin::class)) {
            register("publishForCurrentOs") {
                if (project.kord.commonHost.get().isCurrent()) {
                    dependsOn("publish")
                }
            }

            register("publishForCurrentOsToMavenLocal") {
                dependsOn("publishToMavenLocal")
            }
        }

        register("testOnCurrentOS") {
            if (project.kord.commonHost.get().isCurrent()) {
                dependsOn("test")
            }
        }
    }
}

internal fun Project.applyMultiplatformHelpers() {
    val kordExtension = kord
    afterEvaluate {
        with(extensions.getByName<KotlinMultiplatformExtension>("kotlin")) {
            publishAndTestTasks("Common", KotlinJvmTarget::class, KotlinJsIrTarget::class)
            publishAndTestTasks<KotlinNativeTarget>("Linux") { konanTarget.family == Family.LINUX }
            publishAndTestTasks<KotlinNativeTarget>("Windows") { konanTarget.family == Family.MINGW }
            publishAndTestTasks<KotlinNativeTarget>("Apple") { konanTarget.family in darwinFamilies }

            val commonHost = kordExtension.commonHost.get()
            val metadataHost = kordExtension.metadataHost.get()
            umbrellaTask(commonHost, "Publishes all publications designated to this host's OS") {
                if (metadataHost.isCurrent()) {
                    dependOnSafe("publishKotlinMultiplatformPublicationToMavenRepository")
                }
            }
            umbrellaTask(
                commonHost, "Publishes all publications designated to this hosts OS to Maven local",
                "ToMavenLocal"
            ) {
                if (metadataHost.isCurrent()) {
                    dependOnSafe("publishKotlinMultiplatformPublicationToMavenLocal")
                }
            }

            tasks.register("testOnCurrentOS") {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                description = "Runs all tests for this OS"

                if (commonHost.isCurrent()) {
                    dependOnSafe("testCommon")
                    dependOnSafe("apiCheck")
                }

                if (HostManager.hostIsLinux) {
                    dependOnSafe("testLinux")
                }
                if (HostManager.hostIsMingw) {
                    dependOnSafe("testWindows")
                }
                if (HostManager.hostIsMac) {
                    dependOnSafe("testApple")
                }
            }
        }
    }
}

private fun Task.dependOnSafe(name: String) {
    dependsOn(project.tasks.named { it == name })
}

private fun Project.umbrellaTask(
    commonHost: Family,
    description: String,
    suffix: String = "",
    additional: Task.() -> Unit
) {
    if (plugins.hasPlugin("org.gradle.maven-publish")) {
        tasks.register("publishForCurrentOs$suffix") {
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            this.description = description
            if (commonHost.isCurrent()) {
                dependOnSafe("publishCommon$suffix")
            }

            additional()

            if (HostManager.hostIsLinux) {
                dependOnSafe("publishLinux$suffix")
            }
            if (HostManager.hostIsMingw) {
                dependOnSafe("publishWindows$suffix")
            }
            if (HostManager.hostIsMac) {
                dependOnSafe("publishApple$suffix")
            }
        }
    }
}

private inline fun <reified T : KotlinTarget> KotlinMultiplatformExtension.publishAndTestTasks(
    name: String,
    crossinline filter: T.() -> Boolean
) = publishAndTestTasks(name, T::class) {
    (this as T).filter()
}

private fun KotlinMultiplatformExtension.publishAndTestTasks(
    name: String,
    vararg desiredTargets: KClass<out KotlinTarget>,
    filter: KotlinTarget.() -> Boolean = { true }
) = with(project) {
    val targetNames = targets
        .asSequence()
        .filter { target -> desiredTargets.any { it.isInstance(target) } }
        .filter(filter)
        .map { it.targetName }
        .map { targetName -> targetName.replaceFirstChar { it.uppercaseChar() } }
        .map { if (it == "Metadata") "KotlinMultiplatform" else it }
        .toList()

    val repositoryNames = project.the<PublishingExtension>().repositories.names
        .map { it.replaceFirstChar { char -> char.uppercaseChar() } }

    if (targetNames.isNotEmpty() && plugins.hasPlugin("org.gradle.maven-publish")) {
        tasks.register("publish$name") {
            description = "Publishes all $name targets"
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            dependsOn(targetNames.flatMap {
                repositoryNames.map { repositoryName -> "publish${it}PublicationTo${repositoryName}Repository" }
            })
        }
        tasks.register("publish${name}ToMavenLocal") {
            description = "Publishes all $name targets to Maven local"
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            dependsOn(targetNames.map { "publish${it}PublicationToMavenLocal" })
        }
        val testRuns = targets
            .asSequence()
            .filter { target -> desiredTargets.any { it.isInstance(target) } }
            .filter(filter)
            .filterIsInstance<KotlinTargetWithTests<*, *>>()
            .flatMap { target -> target.testRuns.names.map { "${target.targetName}Test" } }

        tasks.register("test${name}") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Runs tests on all $name targets"
            dependsOn(testRuns.toList())
        }
    }
}
