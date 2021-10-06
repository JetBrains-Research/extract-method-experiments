plugins {
    java
    id("org.jetbrains.intellij") version "0.7.2"
}

group = "org.jetbrains.research.extractMethod"
version = "1.0"

repositories {
    mavenCentral()
}

allprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.intellij")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation("org.apache.logging.log4j:log4j-api:2.14.1")
        implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    }

    intellij {
        type = "IC"
        version = "2021.1"
        setPlugins("java","git4idea")
    }
}