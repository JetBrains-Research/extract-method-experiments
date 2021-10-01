package org.jetbrains.research.extractMethod

import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
import java.nio.file.Path
import java.nio.file.Files
import kotlin.streams.toList

private val preprocessor = getKotlinJavaPreprocessorManager(null)
private val repositoryOpener = getKotlinJavaRepositoryOpener()

fun getSubdirectories(path: Path): List<Path> {
    return Files.walk(path, 1)
        .filter { Files.isDirectory(it) && !it.equals(path) }
        .toList()
}

fun run(inputDir : Path) {
    val datasetDir = inputDir ?: error("input directory must not be null")
    preprocessor.preprocessDatasetInplace(datasetDir.toFile())
    getSubdirectories(datasetDir).forEach { repositoryRoot ->
        val allProjectsOpenedSuccessfully = repositoryOpener.openRepository(repositoryRoot.toFile()) { project ->
            println("Project $project opened")
        }

        println("All projects opened successfully: $allProjectsOpenedSuccessfully")
    }
}

