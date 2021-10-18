group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":extract-method-core"))
    implementation(project(":extract-method-metrics"))
}

open class RunPositivesCLITask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Path to the directory containing projects for the dataset
    val projectsDirPath: String? by project

    //Path to the output directory
    val outputFilePath: String? by project

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

open class RunNegativesCLITask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Path to the directory containing projects for the dataset
    val inputProjectPath: String? by project

    //Path to the output directory
    val outputFilePath: String? by project

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
    register<RunPositivesCLITask>("runPositiveRefactorings") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "PositiveRefactorings",
            projectsDirPath?.let { "--projectsDirPath=$it" },
            outputFilePath?.let { "--outputFilePath=$it" }
        )
    }

    register<RunNegativesCLITask>("runNegativeRefactorings") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "NegativeRefactorings",
            inputProjectPath?.let { "--inputProjectPath=$it" },
            outputFilePath?.let { "--outputFilePath=$it" }
        )
    }
}

tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
    .forEach { it.enabled = false }