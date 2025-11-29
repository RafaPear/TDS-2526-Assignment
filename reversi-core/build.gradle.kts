dependencies {
    implementation(project(":reversi-storage"))
    implementation(project(":reversi-utils"))
    testImplementation(kotlin("test"))
    implementation(libs.coroutines)
}