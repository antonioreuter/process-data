package com.reuter.data.batch.jobs;

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
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.net.MalformedURLException;

/**
 * Created by aandra1 on 04/11/16.
 */
@Configuration
public class ExportToXMLJobConfig {

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

  @Bean(name = "exportToXMLJob")
  public Job exportToXMLJob(JobCompletionNotificationListener listener, @Qualifier("readProcessExportXMLStep") Step readProcessExportXMLStep) throws MalformedURLException {
    return jobBuilderFactory.get("exportToXMLJob")
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .flow(exporToDatabaseStep)
        .next(readProcessExportXMLStep)
        .end()
        .build();
  }

  @Bean(name = "readProcessExportXMLStep")
  public Step readProcessExportXMLStep(@Qualifier("xmlItemWriter") ItemWriter<Hotel> xmlItemWriter) throws MalformedURLException{
    return stepBuilderFactory.get("readProcessExportXMLStep")
        .<Hotel, Hotel> chunk(10)
        .reader(jdbcReader)
        .writer(xmlItemWriter)
        .build();
  }

  @StepScope
  @Bean(name = "xmlItemWriter")
  public StaxEventItemWriter<Hotel> xmlItemWriter(@Value("#{jobParameters[outputFile]}") String filePath) throws MalformedURLException{
    StaxEventItemWriter<Hotel> writer = new StaxEventItemWriter<>();
    writer.setMarshaller(marshaller());
    writer.setRootTagName("hotels");
    writer.setResource(new FileSystemResource(filePath));

    return writer;
  }

  @Bean
  public Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setClassesToBeBound(new Class[] { Hotel.class });
    return marshaller;
  }
}
