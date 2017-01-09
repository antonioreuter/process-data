package com.reuter.data.services;

import com.reuter.data.models.Record;

/**
 * Created by aandra1 on 03/11/16.
 */
public interface CalculateRecordGradeService<T extends Record> {

  T calculate(T record);
}
