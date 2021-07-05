package ru.hse.kirilenko.refactorings;

import org.apache.commons.cli.*;


public class CMDCaller {
    /**
     Calls for <code>ExtractionRunner</code> from command-line
     */
    public static void main(String[] args) {
        //Doing command-line stuff

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        Option positive = new Option("p", "pos", true, "positive input file path");
        positive.setRequired(false);
        options.addOption(positive);

        Option negative = new Option("n", "neg", true, "positive input file path");
        negative.setRequired(false);
        options.addOption(negative);

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            // choosing what to run
            if( line.hasOption( "p" ) ) {
                // print the value of block-size
                System.out.printf("Collecting true refactorings at %s\n",  line.getOptionValue( "p" ) );
                TrueRefactoringsExtractorCaller.run(line.getOptionValue( "p" ));
            }
            if( line.hasOption( "n" ) ) {
                // print the value of block-size
                System.out.printf("Collecting false refactorings at %s\n",  line.getOptionValue( "n" ) );
                FalseRefactoringsExtractorCaller.run(line.getOptionValue( "n" ));
            }
        }
        catch(Exception exp) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
    }
}