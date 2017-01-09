package com.reuter.data.batch.jobs;

import com.reuter.data.batch.listeners.JobCompletionNotificationListener;
import com.reuter.data.models.Hotel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;

/**
 * Created by aandra1 on 04/11/16.
 */

@Configuration
public class ExportToDatabaseJobConfig {

  @Value("${batch.concurrency}")
  private Integer concurrency;

  @Value("${csv.hotel.header}")
  private String header;

  @Value("${csv.hotel.header.grade}")
  private String headerGrade;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Qualifier("flatFileReader")
  @Autowired
  private ItemReader<Hotel> reader;

  @Qualifier("gradeItemProcessor")
  @Autowired
  private ItemProcessor<Hotel,Hotel> processor;

  @Qualifier("databaseItemWriter")
  @Autowired
  private ItemWriter<Hotel> databaseItemWriter;

  @Bean(name = "exportToDatabaseJob")
  public Job exportToDatabaseJob(JobCompletionNotificationListener listener) {
    return jobBuilderFactory.get("exportToDatabaseJob")
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .flow(readProcessExportDatabaseStep())
        .end()
        .build();
  }

  @Bean(name = "readProcessExportDatabaseStep")
  public Step readProcessExportDatabaseStep() {
    return stepBuilderFactory.get("readProcessExportDatabaseStep")
        .<Hotel, Hotel> chunk(10)
        .reader(reader)
        .processor(processor)
        .writer(databaseItemWriter)
        .taskExecutor(taskExecutor())
        .throttleLimit(concurrency)
        .build();
  }

  private TaskExecutor taskExecutor() {
    SimpleAsyncTaskExecutor task = new SimpleAsyncTaskExecutor();
    task.setConcurrencyLimit(concurrency);
    return task;
  }

  @Bean(name = "databaseItemWriter")
  public JdbcBatchItemWriter<Hotel> databaseItemWriter() {
    JdbcBatchItemWriter<Hotel> writer = new JdbcBatchItemWriter<Hotel>();
    writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Hotel>());
    writer.setSql("INSERT INTO hotels ("+ headerGrade +") VALUES (:name, :address, :stars, :contact, :phone, :uri, :grade)");
    writer.setDataSource(dataSource);
    return writer;
  }
}
