package dev.kord.gradle.tools

import dev.kord.gradle.tools.util.isCurrent
import org.gradle.api.Project
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
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
            publishTasks<KotlinNativeTarget>("Apple") { konanTarget.family in darwinFamilies }

            val commonHost = kordExtension.commonHost.get()
            umbrellaTask(commonHost, "Publishes all publications designated to this hosts OS")
            umbrellaTask(
                commonHost, "Publishes all publications designated to this hosts OS to Maven local",
                "ToMavenLocal"
            )
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
        .filter { target ->
            desiredTargets.any { it.isInstance(target) }
        }
        .filter(filter)
        .map { it.targetName }
        .map { targetName -> targetName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
        .map { if(it == "Metadata") "KotlinMultiplatform" else it }
        .toList()
    if (targetNames.any()) {
        tasks.register("publish$name") {
            description = "Publishes all $name targets"
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            dependsOn(targetNames.map { "publish${it}PublicationToMavenRepository" })
        }
        tasks.register("publish${name}ToMavenLocal") {
            description = "Publishes all $name targets to Maven local"
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            dependsOn(targetNames.map { "publish${it}PublicationToMavenLocal" })
        }
    }
}