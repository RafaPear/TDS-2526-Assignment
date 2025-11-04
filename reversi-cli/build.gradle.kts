dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.RafaPear:KtFlag:1.5.4")
    implementation(project(":reversi-core"))
    implementation(project(":reversi-utils"))
    testImplementation(kotlin("test"))
}