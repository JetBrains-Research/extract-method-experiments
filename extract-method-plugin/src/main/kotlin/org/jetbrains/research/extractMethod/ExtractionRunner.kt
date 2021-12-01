package org.jetbrains.research.extractMethod

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.apache.logging.log4j.LogManager
import org.jetbrains.research.extractMethod.core.extractors.RefactoringsExtractor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class ExtractionRunner {
    private val logger = LogManager.getLogger(ExtractionRunner::class)

    private fun getSubdirectories(path: Path): List<Path> {
        return Files.walk(path, 1)
            .filter { Files.isDirectory(it) && !it.equals(path) }
            .toList()
    }

    fun runMultipleExtractions(inputDir: Path, extractor: RefactoringsExtractor) {
        val wrapperFunction = {
                project: Project -> extractor.collectSamples(project)
        }

        standardRepositoryOpener(inputDir, wrapperFunction)
    }

    fun runSingleExtraction(projectPath: Path, extractor: RefactoringsExtractor) {
        val wrapperFunction = {
                project: Project -> extractor.collectSamples(project)
        }

        openRunAndClose(0, projectPath, wrapperFunction);
    }
    /**
     * Opens a project at the given path, assigns it an index,
     * runs an action on it, and closes it afterwards.
     */
    private fun openRunAndClose(projectIndex: Int, projectPath: Path, action: (Project) -> Unit) {
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

    /**
     * Executes openRunAndClose on all projects inside the given directory
     */
    private fun standardRepositoryOpener(path: Path, action: (Project) -> Unit) {
        getSubdirectories(path).forEachIndexed { projectIndex, projectPath ->
            openRunAndClose(projectIndex, projectPath, action)
        }
    }

    /**
     * Performs an action on the given project
     */
    private fun runAction(project: Project, projectIndex: Int, action: (Project) -> Unit) {
        logger.info("Started action on project ${project.name} index=$projectIndex")
        action(project)
        logger.info("Finished action on project ${project.name} index=$projectIndex")
    }
}