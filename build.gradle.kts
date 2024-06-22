import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.artifactregistry)
}

group = "dev.kord"
version = "1.6.0"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
        jvmTarget = JVM_1_8
        freeCompilerArgs.addAll("-Xjdk-release=1.8", "-Xcontext-receivers")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 8
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
