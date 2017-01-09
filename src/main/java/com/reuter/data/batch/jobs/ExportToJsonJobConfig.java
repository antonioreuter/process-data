package com.reuter.data.batch.jobs;

import com.reuter.data.batch.writers.JsonFileItemWriter;
import com.reuter.data.batch.aggregators.JsonLineAggregator;
import com.reuter.data.batch.listeners.JobCompletionNotificationListener;
import com.reuter.data.models.Hotel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * Created by aandra1 on 04/11/16.
 */
@Configuration
public class ExportToJsonJobConfig {

  @Value("${csv.hotel.header}")
  private String header;

  @Value("${csv.hotel.header.grade}")
  private String headerGrade;

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Autowired
  @Qualifier("flatFileReader")
  private ItemReader<Hotel> reader;

  @Autowired
  @Qualifier("jdbcReader")
  private ItemReader<Hotel> jdbcReader;

  @Autowired
  @Qualifier("readProcessExportDatabaseStep")
  private Step exporToDatabaseStep;

  @Bean(name = "exportToJsonJob")
  public Job exportToJsonJob(JobCompletionNotificationListener listener, @Qualifier("readProcessExportJsonStep") Step readProcessExportJsonStep) {
    return jobBuilderFactory.get("exportToJsonJob")
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .flow(exporToDatabaseStep)
        .next(readProcessExportJsonStep)
        .end()
        .build();
  }

  @Bean(name = "readProcessExportJsonStep")
  public Step readProcessExportJsonStep(@Qualifier("jsonItemWriter") ItemWriter<Hotel> jsonItemWriter) {
    return stepBuilderFactory.get("readProcessExportJsonStep")
        .<Hotel, Hotel> chunk(10)
        .reader(jdbcReader)
        .writer(jsonItemWriter)
        .build();
  }

  @StepScope
  @Bean(name = "jsonItemWriter")
  public JsonFileItemWriter<Hotel> jsonItemWriter(@Value("#{jobParameters[outputFile]}") String filePath) {
    JsonFileItemWriter<Hotel> writer = new JsonFileItemWriter<>();
    writer.setShouldDeleteIfEmpty(true);
    writer.setShouldDeleteIfExists(true);
    writer.setAppendAllowed(false);
    writer.setResource(new FileSystemResource(filePath));
    writer.setLineAggregator(createHotelJsonLineAggregator());

    return writer;
  }

  private LineAggregator<Hotel> createHotelJsonLineAggregator() {
    return new JsonLineAggregator<>();
  }
}
