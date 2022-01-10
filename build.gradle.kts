import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.extractMethod"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.5.21" apply true
    id("org.jetbrains.intellij") version "1.1.3" apply true
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
    val utilitiesBranch = System.getenv("PLUGIN_UTILITIES_BRANCH") ?: properties("pluginUtilitiesBranch")
    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        // Plugin utilities modules
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = utilitiesBranch
            }
        }
        implementation("org.apache.logging.log4j:log4j-api:2.17.1")
        implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    }

    intellij {
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
        downloadSources.set(properties("platformDownloadSources").toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "11"
            targetCompatibility = "11"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }
}