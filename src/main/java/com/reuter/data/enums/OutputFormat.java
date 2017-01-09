package com.reuter.data.enums;

/**
 * Created by aandra1 on 02/11/16.
 */
public enum OutputFormat {
  XML("xml"), DB("db"), CSV("csv"), JSON("json");

  private String extension;

  OutputFormat(String ext) {
    this.extension = ext;
  }

  public String getExtension() {
    return this.extension;
  }

  public static OutputFormat parse(String value) {
    switch (value.toUpperCase()) {
      case "XML": return XML;
      case "DB": return DB;
      case "CSV": return CSV;
      case "JSON": return JSON;
      default: throw new IllegalStateException("Invalid output format!");
    }
  }
}
