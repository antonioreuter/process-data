package com.reuter.data.batch.jobs;

import com.reuter.data.batch.listeners.JobCompletionNotificationListener;
import com.reuter.data.models.Hotel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
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
public class ExportToCSVJobConfig {

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
  @Qualifier("gradeItemProcessor")
  private ItemProcessor<Hotel,Hotel> processor;

  @Autowired
  @Qualifier("readProcessExportDatabaseStep")
  private Step exporToDatabaseStep;

  @Bean(name = "exportToCSVJob")
  public Job exportToCSVJob(JobCompletionNotificationListener listener, @Qualifier("readProcessExportCSVStep") Step readProcessExportCSVStep) {
    return jobBuilderFactory.get("exportToCSVJob")
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .flow(exporToDatabaseStep)
        .next(readProcessExportCSVStep)
        .end()
        .build();
  }

  @Bean(name = "readProcessExportCSVStep")
  public Step readProcessExportCSVStep(@Qualifier("csvItemWriter") ItemWriter<Hotel> csvItemWriter) {
    return stepBuilderFactory.get("readProcessExportCSVStep")
        .<Hotel, Hotel> chunk(10)
        .reader(jdbcReader)
        .writer(csvItemWriter)
        .build();
  }

  @StepScope
  @Bean(name = "csvItemWriter")
  public FlatFileItemWriter<Hotel> csvItemWriter(@Value("#{jobParameters[outputFile]}") String filePath) {
    FlatFileItemWriter<Hotel> writer = new FlatFileItemWriter<>();
    writer.setShouldDeleteIfEmpty(true);
    writer.setShouldDeleteIfExists(true);
    writer.setAppendAllowed(false);
    writer.setResource(new FileSystemResource(filePath));
    writer.setLineAggregator(createHotelLineAggregator());
    writer.setHeaderCallback(fileWriter -> fileWriter.write(headerGrade.toUpperCase()));

    return writer;
  }

  private LineAggregator<Hotel> createHotelLineAggregator() {
    DelimitedLineAggregator<Hotel> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(",");

    FieldExtractor<Hotel> fieldExtractor = createHotelFieldExtractor();
    lineAggregator.setFieldExtractor(fieldExtractor);

    return lineAggregator;
  }

  private FieldExtractor<Hotel> createHotelFieldExtractor() {
    BeanWrapperFieldExtractor<Hotel> extractor = new BeanWrapperFieldExtractor<>();
    extractor.setNames(headerGrade.split(","));
    return extractor;
  }
}
