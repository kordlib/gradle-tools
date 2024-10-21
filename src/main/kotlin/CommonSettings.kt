package dev.kord.gradle.tools

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

internal fun Project.applyCommonSettings() {
    if (plugins.hasPlugin("org.gradle.java")) {
        configure<JavaPluginExtension> {
            afterEvaluate {
                sourceCompatibility = JavaVersion.toVersion(kord.jvmTarget.get().target)
            }
        }

    }
    if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
        configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget = kord.jvmTarget
            }
        }
    }
    if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        configure<KotlinMultiplatformExtension> {
            targets.withType<KotlinJvmTarget>().configureEach {
                compilations.all {
                    compileTaskProvider.configure {
                        compilerOptions {
                            jvmTarget = kord.jvmTarget
                        }
                    }
                }
            }
        }

        tasks.withType<KotlinNativeSimulatorTest>().configureEach {
            enabled = project.kord.supportsSimulators.get()
        }
    }
}
