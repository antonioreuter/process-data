package com.reuter.data.batch.readers;

import com.reuter.data.models.Hotel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * Created by aandra1 on 31/10/16.
 */
@Slf4j
@Configuration
public class HotelCSVItemReaderConfig {

  private static final String DEFAULT_CHARSET = "UTF-8";

  @Value("${csv.hotel.header}")
  private String header;

  @Autowired
  @Qualifier("hotelFieldSetMapper")
  private FieldSetMapper<Hotel> hotelFieldSetMapper;

  @StepScope
  @Bean(name = "flatFileReader")
  public FlatFileItemReader<Hotel> reader(@Value("#{jobParameters[sourceFile]}") String filePath) {
    log.debug("File Path: " + filePath);
    FileSystemResource resource = new FileSystemResource(filePath);

    FlatFileItemReader<Hotel> reader = new FlatFileItemReader<>();
    reader.setStrict(true);
    reader.setResource(resource);
    reader.setLinesToSkip(1);
    reader.setEncoding(DEFAULT_CHARSET);
    reader.setLineMapper(new DefaultLineMapper<Hotel>(){{
      setLineTokenizer(new DelimitedLineTokenizer() {{
          setNames(header.split(","));
      }});
      setFieldSetMapper(hotelFieldSetMapper);
    }});

    return reader;
  }
}
