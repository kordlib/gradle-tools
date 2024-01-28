import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish.plugin)
    alias(libs.plugins.artifactregistry)
}

group = "dev.kord"
version = "1.0.8"

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    website = "https://github.com/kordlib/gradle-tools"
    vcsUrl = "https://github.com/kordlib/gradle-tools.git"

    plugins {
        create("greetingsPlugin") {
            id = "dev.kord.gradle-tools"
            displayName = "Kord Gradle Tools"
            description = "Tools for maintaining Kord projects"
            implementationClass = "dev.kord.gradle.tools.KordGradlePlugin"
        }
    }
}

publishing {
    repositories {
        maven("artifactregistry://europe-west3-maven.pkg.dev/mik-music/kord") {
//            credentials {
//                username = "_json_key_base64"
//                password = System.getenv("GOOGLE_KEY")?.toByteArray()?.let {
//                    @OptIn(ExperimentalEncodingApi::class)
//                    Base64.encode(it)
//                }
//            }
//
//            authentication {
//                create<BasicAuthentication>("basic")
//            }
        }
    }
}
