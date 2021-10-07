package org.jetbrains.research.extractMethod

import org.jetbrains.research.extractMethod.core.extractors.PositiveRefactoringsExtractionRunner
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
import java.io.FileWriter
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

fun run(inputDir : Path, fw : FileWriter) {
    val datasetDir = inputDir ?: error("input directory must not be null")
    val runner = PositiveRefactoringsExtractionRunner(fw);
    preprocessor.preprocessDatasetInplace(datasetDir.toFile())
    getSubdirectories(datasetDir).forEach { repositoryRoot ->
        repositoryOpener.openRepository(repositoryRoot.toFile()) { project ->
            println("Project $project opened")
            runner.collectSamples(project)
        }
    }
}

