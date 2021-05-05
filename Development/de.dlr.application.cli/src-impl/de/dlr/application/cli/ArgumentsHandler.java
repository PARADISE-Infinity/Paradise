/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.cli;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.dlr.application.WindowTitle;

public class ArgumentsHandler {
    
    private static final String OPTION_CONSTRAINT_CHECK = "check";
    private static final String OPTION_CALCULATION = "calculate";
    private static final String OPTION_VALIDATION = "validate";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_VERBOSE = "verbose";
    private static final String OPTION_PROJECTS = "projects";
    private static final String OPTION_MODELS = "models";
    
    private String fileName = null;    
    private EAction action = EAction.STUB;

    private Options options;
    private boolean help = false;
    private boolean verbose = false;   
    
    /**
     * Constructor
     * @param context
     * @throws ParseException 
     * @throws IOException
     */
    public ArgumentsHandler() {
        options = initOptions();
    }

    /**
     * @return
     * Returns the work flow type ...
     */
    public EAction getAction() {
        return action;
    }

    /**
     * @return
     *  Returns the name of the premise model file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return 
     * If true debug information is printed.
     */
    public boolean isVerbose() {
        return verbose;
    }
    
    public boolean isHelp() {
        return help;
    }
    
    public void printHelp() {
        new HelpFormatter().printHelp(WindowTitle.NAME, options, true);
    }
    
    /** 
     * @throws ParseException 
     */
    public void parseArguments(String[] args) throws ParseException {
        
        CommandLineParser parser = new GnuParser();

        // check for help option
        if (hasOnlyHelpOption(args)) {
            help = true;
            return;
        }
        
        // parse arguments
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(OPTION_HELP)) {
            // Note that only help being given is already handled previously
            throw new ParseException("Can't select help in combination with other options.");
        }
        
        // set 
        if (cmd.hasOption(OPTION_VERBOSE)) {
            verbose = true;
        }   

        // get the action
        if (cmd.hasOption(OPTION_CALCULATION)) {
            action = EAction.CALCULATION;
            fileName = cmd.getOptionValue(OPTION_CALCULATION);
        }

        if (cmd.hasOption(OPTION_VALIDATION)) {
            action = EAction.VALIDATION;
            fileName = cmd.getOptionValue(OPTION_VALIDATION);
        }
        
        if (cmd.hasOption(OPTION_CONSTRAINT_CHECK)) {
            action = EAction.CONSTRAINT_CHECK;
            fileName = cmd.getOptionValue(OPTION_CONSTRAINT_CHECK);
        }

        if (cmd.hasOption(OPTION_PROJECTS)) {
            action = EAction.LIST_PROJECTS;
        }

        if (cmd.hasOption(OPTION_MODELS)) {
            action = EAction.LIST_MODELS;
        }
    }

    /**
     * Initialize options
     * @return
     */
    private Options initOptions() {
        
        Options options = new Options();
        
        Option help = createHelpOption();
        options.addOption(help);

        Option validate = new Option("a", OPTION_VALIDATION, true, "Validates the specified model.");
        validate.setRequired(false);
        options.addOption(validate);

        Option calculate = new Option("c", OPTION_CALCULATION, true, "Calculate the model.");
        calculate.setRequired(false);
        options.addOption(calculate);

        Option check = new Option("e", OPTION_CONSTRAINT_CHECK, true, "Check constraints of the model.");
        check.setRequired(false);
        options.addOption(check);

        Option verbose = new Option("v", OPTION_VERBOSE, false, "Set detailed console output.");
        verbose.setRequired(false);
        options.addOption(verbose);

        Option projects = new Option("p", OPTION_PROJECTS, false, "Show the project list of a workspace.");
        projects.setRequired(false);
        options.addOption(projects);

        Option models = new Option("m", OPTION_MODELS, false, "Show the list of models of a workspace.");
        models.setRequired(false);
        options.addOption(models);
        
        return options;
    }

    private Option createHelpOption() {
        Option help = new Option("h", OPTION_HELP, false, "Print help.");
        help.setRequired(false);
        return help;
    }
    
    /**
     * Checks if the arguments consist of only the help option.
     * 
     * Since the default parser has a required option, it will always create an error when this 
     * option isn't present. Therefore we use a small custom parser to check for help by itself.
     */
    private boolean hasOnlyHelpOption(String[] args) {
        Options options = new Options();
        options.addOption(createHelpOption());
        
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            return cmd.hasOption(OPTION_HELP);
        } catch (ParseException e) {
            return false;
        }
    }
}
