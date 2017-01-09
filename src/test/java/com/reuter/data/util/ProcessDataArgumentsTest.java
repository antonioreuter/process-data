package com.reuter.data.util;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by aandra1 on 05/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDataArgumentsTest {

  @Test(expected = MissingOptionException.class)
  public void whenTheSourceFileParamIsNotPresent() throws ParseException{
    ProcessDataArguments subject =
        new ProcessDataArguments("-o=xml", "-sort=grade");
  }

  @Test(expected = IllegalStateException.class)
  public void whenTheOutputFormatIsInvalid() throws ParseException {
    ProcessDataArguments subject =
        new ProcessDataArguments("-s=/file.csv","-o=xxx", "-sort=grade");
    subject.outputFormat();
  }

  @Test
  public void whenPassASourceFileCheckSourceFileAndOutputFilePaths() throws ParseException{
    ProcessDataArguments subject =
        new ProcessDataArguments("-s=/tmp/hotels.csv","-o=xml", "-sort=grade");

    String extension = subject.outputFormat().getExtension();
    String sourceFilePath = subject.sourceFile();
    String outputFilePath = subject.outputFilePath();

    assertEquals(sourceFilePath, "/tmp/hotels.csv");
    assertTrue(outputFilePath.startsWith("/tmp/output_hotels"));
    assertTrue(outputFilePath.endsWith(extension));
  }
}
