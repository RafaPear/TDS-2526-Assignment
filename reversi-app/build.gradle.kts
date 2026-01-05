import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

dependencies {
    // COMPOSE MODULES
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.uiTest)
    implementation(compose.materialIconsExtended)

    // REVERSI MODULES
    implementation(project(":reversi-core"))
    implementation(project(":reversi-utils"))
    implementation(libs.ktflag)
    implementation(libs.coroutines)

    // TEST MODULE
    testImplementation(libs.kotlin.test)
}

compose.desktop {
    application {
        mainClass = "pt.isel.reversi.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = rootProject.name
            packageVersion = rootProject.version.toString()
            includeAllModules = true

            macOS {
                dockName = "Reversi"
                iconFile.set(project.file("src/main/composeResources/drawable/reversi.png"))
            }
        }
    }
}

// === Fat Jar executável ===
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles an executable fat jar including all dependencies."

    archiveBaseName.set("reversi-app")
    archiveVersion.set("v1.0.1")
    archiveClassifier.set("")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    from(sourceSets.main.get().resources)

    dependsOn(configurations.runtimeClasspath)
    from({
             configurations.runtimeClasspath.get()
                 .filter { it.name.endsWith(".jar") }
                 .map { zipTree(it) }
         })

    manifest {
        attributes["Main-Class"] = "pt.isel.reversi.app.MainKt"
    }
}

// === Usa o fatJar como o jar padrão ===
tasks {
    build {
        dependsOn("fatJar")
    }
}