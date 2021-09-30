import java.net.URI

rootProject.name = "extract-method-experiments"
include("extract-method-core",
        "extract-method-metrics",
        "extract-method-plugin")

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl{
    gitRepository(URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
    }
}