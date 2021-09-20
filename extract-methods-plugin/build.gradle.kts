group = rootProject.group
version = rootProject.version

plugins {
    java
}
repositories {
    // Necessary for psiMiner
//    maven(url = "https://dl.bintray.com/egor-bogomolov/astminer")
}

open class IOCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Name of the runner
    @get:Input
    val runner: String? by project

    // Path to the directory containing projects for the dataset
    //@get:Input
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

dependencies {
    implementation(project(":extract-methods-core"))
    implementation(project(":extract-methods-metrics"))
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