group = "org.jetbrains.research.extractMethodExperiments"
version = "1.0-SNAPSHOT"

plugins {
    java
    id("org.jetbrains.intellij") version "0.7.2"
}

intellij {
    type = "IC"
    version = "2021.1"
    setPlugins("java", "git4idea")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.tsantalis:refactoring-miner:2.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.4.3.201909031940-r")
    implementation("org.apache.commons:commons-collections4:4.1")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("commons-cli:commons-cli:1.4")
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

open class IOCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Name of the runner
    @get:Input
    val runner: String? by project

    // Path to the file containing paths to the projects for dataset
    //@get:Input
    val projectsFilePath: String? by project

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
                projectsFilePath?.let { "--projectsFilePath=$it" },
                generatePositiveSamples?.let { "--generatePositiveSamples" },
                generateNegativeSamples?.let { "--generateNegativeSamples" }
        )
    }
}

tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
        .forEach { it.enabled = false }