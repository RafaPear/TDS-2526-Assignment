import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

group = "pt.isel.reversi"
version = "v1.0.1"

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets {
            configureEach {
                moduleName.set(project.name)
                includes.from("MODULE.md")
                suppressObviousFunctions.set(true)
                pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
                    footerMessage = "(c) 2025 ISEL"
                    separateInheritedMembers = false
                    mergeImplicitExpectActualDeclarations = true
                }
            }
        }
    }
}

tasks.dokkaHtmlMultiModule {
    moduleName.set("Reversi Docs")
    includes.from("README.md")
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "(c) 2025 ISEL"
        // Add all images png from the docs/assets folder to the final documentation
        customAssets += fileTree("images") {
            include("**/*.png")
        }
        separateInheritedMembers = false
        mergeImplicitExpectActualDeclarations = true
    }
}

val reversiCliJar = tasks.register<Copy>("copyReversiCliJar") {
    dependsOn(":reversi-cli:build")
    from(project(":reversi-cli").layout.buildDirectory.dir("libs"))
    into(layout.buildDirectory.dir("libs"))
}

val reversiAppJar = tasks.register<Copy>("copyReversiAppJar") {
    dependsOn(":reversi-app:build")
    from(project(":reversi-app").layout.buildDirectory.dir("libs"))
    into(layout.buildDirectory.dir("libs"))
}

// Garante que os jars dos subprojetos aparecem no build do root
tasks.named("build") {
    dependsOn(reversiCliJar, reversiAppJar)
}