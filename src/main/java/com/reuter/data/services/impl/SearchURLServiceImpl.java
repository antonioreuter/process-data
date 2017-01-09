package com.reuter.data.services.impl;

import com.reuter.data.services.SearchURLService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by aandra1 on 05/11/16.
 */
@Slf4j
@Component("searchURLService")
public class SearchURLServiceImpl implements SearchURLService {

  @Autowired
  private HttpClient httpClient;

  @Override
  public int search(String url) {
    int statusCode = 404;
    try {
      HttpResponse response = httpClient.execute(new HttpHead(url));
      statusCode = response.getStatusLine().getStatusCode();
    } catch (IOException ex) {
      log.debug(ex.getMessage(), ex);
    }

    return statusCode;
  }
}
