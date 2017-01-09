package com.reuter.data.batch.services;

import com.reuter.data.util.ProcessDataArguments;
import com.reuter.data.enums.OutputFormat;
import com.reuter.data.exceptions.ProcessDataServiceException;
import com.reuter.data.services.ProcessDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aandra1 on 02/11/16.
 */
@Slf4j
@Service("dispatchJobService")
public class DispatchJobService implements ProcessDataService {

  @Value("#{'${csv.hotel.header.grade}'.split(',')}")
  private String[] columns;

  @Autowired
  private JobLauncher jobLauncher;

  @Qualifier("exportToCSVJob")
  @Autowired
  private Job exportToCSVJob;

  @Qualifier("exportToXMLJob")
  @Autowired
  private Job exportToXMLJob;

  @Qualifier("exportToJsonJob")
  @Autowired
  private Job exportToJsonJob;

  @Qualifier("exportToDatabaseJob")
  @Autowired
  private Job exportToDatabaseJob;

  public void process(ProcessDataArguments arguments) {
    log.debug("Starting the batch job");

    Job selectedJob = selectJob(arguments.outputFormat());
    log.debug("Selected JOB: "+ selectedJob);

    try {
      jobLauncher.run(selectedJob, jobParams(arguments));
    } catch (Exception ex) {
      throw new ProcessDataServiceException("Wasn't possible to process the job", ex);
    }
  }

  private void validateOrderByParam(String order) {
    if (!StringUtils.isEmpty(order)) {
      List<String> orderBy = Arrays.asList(order.split(","));
      if (!Arrays.asList(columns).containsAll(orderBy))
        throw new IllegalArgumentException("Sorry, we couldn't sort your data with these fields!");
    }
  }

  private JobParameters jobParams(ProcessDataArguments arguments) {
    validateOrderByParam(arguments.orderBy());

    return new JobParametersBuilder()
        .addString("sourceFile", arguments.sourceFile())
        .addString("outputFile", arguments.outputFilePath())
        .addString("orderBy", arguments.orderBy())
        .toJobParameters();
  }

  private Job selectJob(OutputFormat format) {
    Job selected = null;
    switch (format) {
      case XML: selected = exportToXMLJob;
                break;
      case CSV: selected = exportToCSVJob;
                break;
      case JSON: selected = exportToJsonJob;
                break;
      case DB:  selected = exportToDatabaseJob;
                break;
    }

    return selected;
  }
}
