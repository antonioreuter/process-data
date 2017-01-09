package com.reuter.data.batch.aggregators;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.item.file.transform.LineAggregator;


/**
 * Created by aandra1 on 03/11/16.
 */
@Slf4j
public class JsonLineAggregator<T> implements LineAggregator<T> {
  private Gson gson = new Gson();

  @Override
  public String aggregate(T item) {
    return gson.toJson(item);
  }
}
