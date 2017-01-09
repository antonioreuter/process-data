package com.reuter.data.util;

import org.apache.commons.cli.*;

/**
 * Created by aandra1 on 02/11/16.
 */
public class CommandLineUtil {

  public static CommandLine convertArgToCommandLine(String... args) throws ParseException {
    CommandLineParser cmdParser = new DefaultParser();
    return cmdParser.parse(buildOptions(), args);
  }

  private static Options buildOptions() {
    Option sourceOption = Option.builder("s")
        .longOpt("source")
        .numberOfArgs(1)
        .required(true)
        .desc("source file to be processed")
        .build();

    Option outputOption = Option.builder("o")
        .longOpt("output")
        .numberOfArgs(1)
        .required(false)
        .desc("define the output format")
        .build();

    Option sortOption = Option.builder("sort")
        .longOpt("sort")
        .numberOfArgs(1)
        .required(false)
        .desc("specify how the target can be sorted")
        .build();

    Options options = new Options();
    options.addOption(sourceOption);
    options.addOption(outputOption);
    options.addOption(sortOption);

    return options;
  }
}
