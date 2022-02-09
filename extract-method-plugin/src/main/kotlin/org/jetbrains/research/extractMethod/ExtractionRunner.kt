package org.jetbrains.research.extractMethod

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.apache.logging.log4j.LogManager
import org.jetbrains.research.extractMethod.core.extractors.RefactoringsExtractor
import java.nio.file.Path
import java.nio.file.Paths

class ExtractionRunner {
    private val logger = LogManager.getLogger(ExtractionRunner::class)

    fun runMultipleExtractions(mappingPath: Path, extractor: RefactoringsExtractor) {
        val wrapperFunction = { project: Project, s1: String, s2: String ->
            extractor.collectSamples(project, s1, s2)
        }

        val lines = mappingPath.toFile().readLines()
        val index = 0;
        for (line: String in lines) {
            val components = line.split(';')
            val path = Paths.get(components[0])
            val projectName = components[1]
            val sha = components[2]
            openRunAndClose(index, path, wrapperFunction, projectName, sha)
        }
    }

    fun runSingleExtraction(mappingPath: Path, extractor: RefactoringsExtractor, index: Int) {
        val wrapperFunction = { project: Project, s1: String, s2: String ->
            extractor.collectSamples(project, s1, s2)
        }
        val components = mappingPath.toFile().readLines()[index].split(';');
        val path = Paths.get(components[0])
        val projectName = components[1]
        val sha = components[2]
        openRunAndClose(index, path, wrapperFunction, projectName, sha);
    }

    /**
     * Opens a project at the given path, assigns it an index,
     * runs an action on it, and closes it afterwards.
     */
    private fun openRunAndClose(
        projectIndex: Int,
        projectPath: Path,
        action: (Project, String, String) -> Unit,
        projectName: String,
        commitID: String
    ) {
        ApplicationManager.getApplication().invokeAndWait {
            ProjectManagerEx.getInstanceEx().openProject(
                projectPath,
                OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
            )?.let { project ->
                try {
                    runAction(project, projectIndex, action, projectName, commitID)
                } catch (ex: Exception) {
                    logger.error(ex)
                } finally {
                    ApplicationManager.getApplication().invokeAndWait {
                        val closeStatus = ProjectManagerEx.getInstanceEx().forceCloseProject(project)
                        logger.info("Project $projectName is closed = $closeStatus")
                    }
                }
            }
        }
    }

    /**
     * Performs an action on the given project
     */
    private fun runAction(
        project: Project,
        projectIndex: Int,
        action: (Project, String, String) -> Unit,
        projectName: String,
        commitID: String
    ) {
        logger.info("Started action on project $projectName index=$projectIndex")
        action(project, projectName, commitID)
        logger.info("Finished action on project $projectName index=$projectIndex")
    }
}