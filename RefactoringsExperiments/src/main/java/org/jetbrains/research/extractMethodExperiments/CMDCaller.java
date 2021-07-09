package org.jetbrains.research.extractMethodExperiments;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.FileInputStream;
import java.io.IOException;

public class CMDCaller {
    /**
     * Calls for `TrueRefactoringsExtractorCaller` and
     * `FalseRefactoringsExtractorCaller` from command-line
     */
    public static void main(String[] args) throws Exception {

        // Get a reference for logger
        LoggerContext context = configureLogger();
        Logger logger = context.getLogger("cmd");

        // parse the command line arguments
        CommandLine line = parseCMD(args);


        // choosing what to run
        try {
            if (line.hasOption("p")) {
                logger.log(Level.INFO, "Collecting true refactorings at " + line.getOptionValue("p"));
                TrueRefactoringsExtractorCaller.run(line.getOptionValue("p"), context);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Failed on true refactoring, details: "+e.getMessage());
        }
        try {
            if (line.hasOption("n")) {
                logger.log(Level.INFO, "Collecting false refactorings at " + line.getOptionValue("n"));
                FalseRefactoringsExtractorCaller.run(line.getOptionValue("n"), context);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Failed on false refactoring, details: "+e.getMessage());
        }
    }

    private static CommandLine parseCMD(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        Option positive = new Option("p", "pos", true, "positive input file path");
        positive.setRequired(false);
        options.addOption(positive);

        Option negative = new Option("n", "neg", true, "positive input file path");
        negative.setRequired(false);
        options.addOption(negative);

        // parse the command line arguments
        return parser.parse(options, args);
    }

    private static LoggerContext configureLogger() throws IOException {
        ConfigurationFactory factory = XmlConfigurationFactory.getInstance();

        // Locate the source of this configuration
        ConfigurationSource configurationSource = new ConfigurationSource(new FileInputStream("log4j2.xml"));

        // Get a reference from configuration
        LoggerContext context = new LoggerContext("JournalDevLoggerContext");
        Configuration configuration = factory.getConfiguration(context, configurationSource);

        // Create default console appender
        ConsoleAppender appender = ConsoleAppender.createDefaultAppenderForLayout(PatternLayout.createDefaultLayout());
        context.start();
        // Add console appender into configuration
        configuration.addAppender(appender);

        // Create loggerConfig
        LoggerConfig loggerConfig = new LoggerConfig("com", Level.FATAL, false);

        // Add appender
        loggerConfig.addAppender(appender, null, null);

        // Add loggers and associate them with loggerConfig instance
        configuration.addLogger("cmd", loggerConfig);
        configuration.addLogger("extract-call", loggerConfig);
        configuration.addLogger("false-extractor", loggerConfig);
        configuration.addLogger("true-extractor", loggerConfig);

        // Start logging system
        context.start(configuration);

        return context;
    }
}