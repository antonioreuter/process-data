package com.reuter.data.exceptions;

/**
 * Created by aandra1 on 05/11/16.
 */
public class ProcessDataServiceException extends RuntimeException {

  public ProcessDataServiceException(String msg, Throwable th) {
    super(msg, th);
  }
}
