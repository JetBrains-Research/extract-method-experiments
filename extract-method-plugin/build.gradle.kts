group = rootProject.group
version = rootProject.version

plugins {
    java
    kotlin("jvm") version "1.5.21" apply true
    id("org.jetbrains.dokka") version "1.4.30" apply true
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply true
}

dependencies {
    implementation(project(":extract-method-core"))
    implementation(project(":extract-method-metrics"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("org.jetbrains.kotlin:kotlin-reflect:1.5.10"))
    val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
    dependencies {
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = "main"
            }
        }
    }
}

open class IOCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Name of the runner
    @get:Input
    val runner: String? by project

    // Path to the directory containing projects for the dataset
    val projectsDirPath: String? by project

    //Path to the output directory
    val datasetsDirPath: String? by project

    // Runs generation of positive samples
    val generatePositiveSamples: String? by project

    // Runs generation of negative samples
    val generateNegativeSamples: String? by project

    init {
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Djdk.module.illegalAccess.silent=true"
        )
        maxHeapSize = "20g"
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<IOCliTask>("runRefactoringsExperiments") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            runner,
            projectsDirPath?.let { "--projectsDirPath=$it" },
            datasetsDirPath?.let { "--datasetsDirPath=$it" },
            generatePositiveSamples?.let { "--generatePositiveSamples" },
            generateNegativeSamples?.let { "--generateNegativeSamples" }
        )
    }
}

tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
    .forEach { it.enabled = false }