import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    kotlin("jvm") version "2.1.20" apply false
    id("org.jetbrains.dokka") version "2.0.0"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

group = "pt.isel.reversi"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
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
        customAssets = listOf(file("UML_Structure.drawio.png"))
        separateInheritedMembers = false
        mergeImplicitExpectActualDeclarations = true
    }
}