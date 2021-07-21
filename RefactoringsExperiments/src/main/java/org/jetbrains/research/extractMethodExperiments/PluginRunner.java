
package org.jetbrains.research.extractMethodExperiments;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.extractors.FalseRefactoringsExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PluginRunner implements ApplicationStarter {
    private final Logger LOG = Logger.getInstance(PluginRunner.class);

    @Override
    public @NonNls
    String getCommandName() {
        return "RefactoringsExperiments";
    }

    @Override
    public void main(@NotNull List<String> args) {
        try {
            //TODO: make it possible to configure parameters in console
            File reposDir = new File("cloned_repos/");
            String[] dirs = reposDir.list();
            List<String> repositoryPaths = new ArrayList<>();
            FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor(repositoryPaths);
            falseRefactoringsExtractor.run();
        } catch (Exception e) {
            LOG.error("Failed to run extraction of false refactorings samples.", e.getMessage());
        }

    }
}