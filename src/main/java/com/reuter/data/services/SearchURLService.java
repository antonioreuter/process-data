package com.reuter.data.services;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Created by aandra1 on 05/11/16.
 */
public interface SearchURLService {

  public static final int HTTP_STATUS_OK = HttpStatus.SC_OK;
  public static final int HTTP_STATUS_SERVER_ERROR = HttpStatus.SC_INTERNAL_SERVER_ERROR;
  public static final int HTTP_STATUS_NOT_FOUND = HttpStatus.SC_NOT_FOUND;
  public static final int HTTP_STATUS_REDIRECT = HttpStatus.SC_TEMPORARY_REDIRECT;
  public static final int HTTP_STATUS_TIMEOUT = HttpStatus.SC_REQUEST_TIMEOUT;

  int search(String url);
}
