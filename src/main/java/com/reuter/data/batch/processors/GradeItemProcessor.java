package com.reuter.data.batch.processors;

import com.reuter.data.services.CalculateRecordGradeService;
import com.reuter.data.models.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by aandra1 on 31/10/16.
 */
@Slf4j
@Component("gradeItemProcessor")
public class GradeItemProcessor<T extends Record> implements ItemProcessor<T, T> {

  @Autowired
  private CalculateRecordGradeService<T> calculateGrade;

  @Override
  public T process(T record) throws Exception {
    return calculateGrade.calculate(record);
  }
}
