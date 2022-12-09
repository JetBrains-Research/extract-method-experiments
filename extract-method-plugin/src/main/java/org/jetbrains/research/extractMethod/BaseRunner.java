package org.jetbrains.research.extractMethod;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base abstract class for the runners
 */
public abstract class BaseRunner implements ApplicationStarter {
    protected static final Logger LOG = Logger.getInstance(PositivesRunner.class);
    protected final int featureCount = 78;

    @Override
    public @NonNls
    String getCommandName() {
        return "BaseRunner";
    }

    @Override
    public void main(@NotNull List<String> args) {
        CommandLineParser parser = new DefaultParser();
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
