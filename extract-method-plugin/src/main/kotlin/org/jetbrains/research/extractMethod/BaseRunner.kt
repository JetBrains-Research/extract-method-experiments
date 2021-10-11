package org.jetbrains.research.extractMethod

import org.apache.logging.log4j.LogManager
import org.jetbrains.research.extractMethod.core.extractors.RefactoringsExtractor
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class BaseRunner {
    private val logger = LogManager.getLogger(BaseRunner::class)
    private val preprocessor = getKotlinJavaPreprocessorManager(null)
    private val repositoryOpener = getKotlinJavaRepositoryOpener()

    private fun getSubdirectories(path: Path): List<Path> {
        return Files.walk(path, 1)
            .filter { Files.isDirectory(it) && !it.equals(path) }
            .toList()
    }

    fun runExtractions(inputDir: Path, extractor: RefactoringsExtractor) {
        preprocessor.preprocessDatasetInplace(inputDir.toFile())
        getSubdirectories(inputDir).forEach { repositoryRoot ->
            repositoryOpener.openRepository(repositoryRoot.toFile()) { project ->
                logger.info("Project ${project.name} is opened")
                extractor.collectSamples(project)
                logger.info("Finished processing ${project.name}")
            }
        }
    }
}