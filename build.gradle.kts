plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.artifactregistry)
}

group = "dev.kord"
version = "1.4.0"

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    website = "https://github.com/kordlib/gradle-tools"
    vcsUrl = "https://github.com/kordlib/gradle-tools.git"

    plugins {
        create("kordGradleToolsPlugin") {
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
            credentials {
                username = "_json_key_base64"
                password = System.getenv("GOOGLE_KEY")
            }

            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
