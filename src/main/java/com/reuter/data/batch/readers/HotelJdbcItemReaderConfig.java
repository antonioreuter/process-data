package com.reuter.data.batch.readers;

import com.reuter.data.models.Hotel;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Created by aandra1 on 05/11/16.
 */
@Configuration
public class HotelJdbcItemReaderConfig {

  private static final String QUERY_FORMAT = "Select name,address,stars,contact,phone,uri,grade From Hotels %s";

  @Autowired
  private DataSource dataSource;

  @StepScope
  @Bean(name = "jdbcReader")
  JdbcCursorItemReader<Hotel> jdbcReader(@Value("#{jobParameters[orderBy]}") String orderBy) {
    JdbcCursorItemReader<Hotel> databaseReader = new JdbcCursorItemReader<>();
    databaseReader.setDataSource(dataSource);
    databaseReader.setRowMapper(new BeanPropertyRowMapper<>(Hotel.class));

    if (StringUtils.isEmpty(orderBy))
      databaseReader.setSql(String.format(QUERY_FORMAT, ""));
    else
      databaseReader.setSql(String.format(QUERY_FORMAT, "order by "+orderBy));

    return databaseReader;
  }
}
