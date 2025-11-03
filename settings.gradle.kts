rootProject.name = "reversi"

include("reversi-core", "reversi-cli", "reversi-storage", "reversi-utils")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}