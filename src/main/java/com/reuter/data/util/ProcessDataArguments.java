package com.reuter.data.util;

import com.reuter.data.enums.OutputFormat;
import lombok.Data;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.time.Instant;

/**
 * Created by aandra1 on 02/11/16.
 */

@Data
public class ProcessDataArguments {
  private CommandLine commandLine;

  public ProcessDataArguments(String... params) throws ParseException{
    this.commandLine = CommandLineUtil.convertArgToCommandLine(params);
  }

  public String sourceFile() {
    if (commandLine.hasOption("source")) {
      return commandLine.getOptionValue("source");
    } else {
      throw new IllegalArgumentException("You need to inform the source file to be processed.");
    }
  }

  public String outputFilePath() {
    File file = new File(sourceFile());
    String outputFileName = getOutputFileName();
    return new File(file.getParent(), outputFileName).getPath();
  }

  public OutputFormat outputFormat() {
    return OutputFormat.parse(commandLine.getOptionValue("output"));
  }

  public String orderBy() { return commandLine.getOptionValue("sort"); }

  private String getOutputFileName() {
    String fileName = new File(sourceFile()).getName();
    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    String extension = outputFormat().getExtension();
    Long timestamp = Instant.now().toEpochMilli();
    return String.format("output_%s_%s.%s", fileName, timestamp, extension);
  }
}
