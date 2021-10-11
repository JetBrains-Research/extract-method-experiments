package org.jetbrains.research.extractMethod

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
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
        val wrapperFunction = {
                project: Project -> extractor.collectSamples(project)
        }

        standardRepositoryOpener(inputDir, wrapperFunction)
    }

    private fun standardRepositoryOpener(path: Path, action: (Project) -> Unit) {
        getSubdirectories(path).forEachIndexed { projectIndex, projectPath ->
            ApplicationManager.getApplication().invokeAndWait {
                ProjectManagerEx.getInstanceEx().openProject(
                    projectPath,
                    OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                )?.let { project ->
                    try {
                        runAction(project, projectIndex, action)
                    } catch (ex: Exception) {
                        logger.error(ex)
                    } finally {
                        ApplicationManager.getApplication().invokeAndWait {
                            val closeStatus = ProjectManagerEx.getInstanceEx().forceCloseProject(project)
                            logger.info("Project ${project.name} is closed = $closeStatus")
                        }
                    }
                }
            }
        }
    }

    private fun runAction(project: Project, projectIndex: Int, action: (Project) -> Unit) {
        logger.info("Start action on project ${project.name} index=$projectIndex")
        action(project)
        logger.info("Finish action on project ${project.name} index=$projectIndex")
    }
}