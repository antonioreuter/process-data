package com.reuter.data.batch.services;

import com.reuter.data.exceptions.ProcessDataServiceException;
import com.reuter.data.util.ProcessDataArguments;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by aandra1 on 05/11/16.
 */

@RunWith(MockitoJUnitRunner.class)
public class DispatchJobServiceTest {

  @Spy
  @InjectMocks
  DispatchJobService subject;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(subject, "columns", new String[] {"name", "star", "grade"});
  }

  @Test(expected = ProcessDataServiceException.class)
  public void whenTheSortFieldIsInvalid() throws ParseException{
    ProcessDataArguments arguments =
        new ProcessDataArguments("-s=/file.csv","-o=xml", "-sort=xxx");

    subject.process(arguments);
  }
}
