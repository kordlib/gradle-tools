package dev.kord.gradle.tools

import dev.kord.gradle.tools.util.isCurrent
import org.gradle.api.Project
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.getByName
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import kotlin.reflect.KClass

private val darwinFamilies = listOf(
    Family.IOS,
    Family.TVOS,
    Family.OSX,
    Family.WATCHOS
)

fun Project.applyMultiplatformHelpers() {
    val kordExtension = kord
    afterEvaluate {
        with(extensions.getByName<KotlinMultiplatformExtension>("kotlin")) {
            publishTasks("Common", KotlinJvmTarget::class, KotlinJsTarget::class, KotlinMetadataTarget::class)
            publishTasks<KotlinNativeTarget>("Linux") { konanTarget.family == Family.LINUX }
            publishTasks<KotlinNativeTarget>("Windows") { konanTarget.family == Family.MINGW }
            publishTasks<KotlinNativeTarget>("Apple") {
                konanTarget.family in darwinFamilies
            }

            val commonHost = kordExtension.commonHost.get()
            umbrellaTask(commonHost, "Publishes all publications designated to this hosts OS")
            umbrellaTask(
                commonHost, "Publishes all publications designated to this hosts OS to Maven local",
                "ToMavenLocal"
            )

            tasks.register("testOnCurrentOS") {
                group = LifecycleBasePlugin.CHECK_TASK_NAME
                description = "Runs all tests for this OS"
                if (commonHost.isCurrent()) {
                    dependsOn("testCommon")
                    dependsOn("apiCheck")
                }

                if (HostManager.hostIsLinux) {
                    dependsOn("testLinux")
                }
                if (HostManager.hostIsMingw) {
                    dependsOn("testWindows")
                }
                if (HostManager.hostIsMac) {
                    dependsOn("testApple")
                }
            }
        }
    }
}

context(Project)
private fun Project.umbrellaTask(commonHost: KonanTarget, description: String, suffix: String = "") {
    tasks.register("publishForCurrentOs$suffix") {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        this.description = description
        if (commonHost.isCurrent()) {
            dependsOn("publishCommon$suffix")
        }

        if (HostManager.hostIsLinux) {
            dependsOn("publishLinux$suffix")
        }
        if (HostManager.hostIsMingw) {
            dependsOn("publishWindows$suffix")
        }
        if (HostManager.hostIsMac) {
            dependsOn("publishApple$suffix")
        }
    }
}

context(Project)
private inline fun <reified T : KotlinTarget> KotlinMultiplatformExtension.publishTasks(
    name: String,
    crossinline filter: T.() -> Boolean
) = publishTasks(name, T::class) {
    (this as T).filter()
}

context(Project)
private fun KotlinMultiplatformExtension.publishTasks(
    name: String,
    vararg desiredTargets: KClass<out KotlinTarget>,
    filter: KotlinTarget.() -> Boolean = { true }
) {
    val targetNames = targets
        .asSequence()
        .filter { target -> desiredTargets.any { it.isInstance(target) } }
        .filter(filter)
        .map { it.targetName }
        .map { targetName -> targetName.replaceFirstChar { it.uppercaseChar() } }
        .map { if (it == "Metadata") "KotlinMultiplatform" else it }
        .toList()
    if (targetNames.any()) {
        tasks.register("publish$name") {
            description = "Publishes all $name targets"
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            dependsOn(targetNames.map { "publish${it}PublicationToMavenCentralRepository" })
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
            group = LifecycleBasePlugin.CHECK_TASK_NAME
            description = "Runs tests on all $name targets"
            dependsOn(testRuns.toList())
        }
    }
}