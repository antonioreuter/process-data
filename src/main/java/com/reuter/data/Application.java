package com.reuter.data;

import com.reuter.data.batch.jobs.ExportToCSVJobConfig;
import com.reuter.data.batch.jobs.ExportToDatabaseJobConfig;
import com.reuter.data.batch.jobs.ExportToJsonJobConfig;
import com.reuter.data.batch.jobs.ExportToXMLJobConfig;
import com.reuter.data.batch.readers.HotelCSVItemReaderConfig;
import com.reuter.data.batch.readers.HotelJdbcItemReaderConfig;
import com.reuter.data.config.ApplicationConfig;
import com.reuter.data.services.ProcessDataService;
import com.reuter.data.util.ProcessDataArguments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by aandra1 on 31/10/16.
 */
@Slf4j
public class Application {

  public static void main(String[] args) throws Exception {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(ApplicationConfig.class);
    context.register(HotelCSVItemReaderConfig.class);
    context.register(HotelJdbcItemReaderConfig.class);
    context.register(ExportToXMLJobConfig.class);
    context.register(ExportToCSVJobConfig.class);
    context.register(ExportToJsonJobConfig.class);
    context.register(ExportToDatabaseJobConfig.class);
    context.refresh();

    try {
      log.info("Start execution...");
      ProcessDataService processDataService = (ProcessDataService) context.getBean("dispatchJobService");
      processDataService.process(new ProcessDataArguments(args));
    } catch (Exception ex) {
      log.error("Something went wrong: ", ex.getMessage(), ex);
    }
  }
}
