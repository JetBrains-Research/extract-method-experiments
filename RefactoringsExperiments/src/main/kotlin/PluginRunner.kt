package org.jetbrains.research.extractMethodExperiments

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import org.jetbrains.research.extractMethodExperiments.extractors.FalseRefactoringsExtractor
import org.jetbrains.research.extractMethodExperiments.extractors.TrueRefactoringsExtractor
import java.io.File


class PluginRunner : ApplicationStarter {

    override fun getCommandName(): String = "RefactoringsExperiments"

    override fun main(args: Array<out String>) {
        CliExtractor().main(args.drop(1))
    }
}

class CliExtractor : CliktCommand() {

    private val runPositives by option("-p", "--positives").flag()
    private val runNegatives by option("-n", "--negatives").flag()
    private val repoPathsFile by argument("<repo-paths-file>",
        help = "path to file with enumerated paths to repositories").
    file(mustExist = true, canBeDir = false)

    override fun run() {
        val repositoryPaths: List<String> = parseRepoPathsFile(repoPathsFile)
        if(runNegatives)
            FalseRefactoringsExtractor(repositoryPaths).run()
        if(runPositives)
            TrueRefactoringsExtractor(repositoryPaths).run()

        //TODO: make it possible to configure parameters in console
    }

    private fun parseRepoPathsFile(file: File): MutableList<String> {
        val paths: MutableList<String> = mutableListOf()
        file.forEachLine { paths.add(it) }
        return paths;
    }
}
