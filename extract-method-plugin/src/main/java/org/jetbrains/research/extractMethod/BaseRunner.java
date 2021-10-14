package org.jetbrains.research.extractMethod;

import com.intellij.openapi.application.ApplicationStarter;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public abstract class BaseRunner implements ApplicationStarter {
    protected final Logger LOG = LogManager.getLogger(PositivesRunner.class);
    protected final int featureCount = 78;

    @Override
    public @NonNls
    String getCommandName() {
        return "BaseRunner";
    }

    @Override
    public void main(@NotNull List<String> args) {
        CommandLineParser parser =  new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(configureOptionsForCLI(), args.toArray(new String[0]));
        } catch (ParseException e) {
            LOG.error("Failed to parse command-line arguments.");
        }
        if (line == null) return;

        extractRefactorings(line);
    }

    abstract Options configureOptionsForCLI();

    abstract void extractRefactorings(CommandLine cmdLine);
}
