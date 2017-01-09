package com.reuter.data.models;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by aandra1 on 03/11/16.
 */

public abstract class Record {
  private static final Integer MAX_GRADE = 10;

  private int grade = MAX_GRADE;

  public void setGrade(Integer grade) {
    this.grade = grade;
  }

  public void subtractGrade(Integer grade) {
    this.grade -= grade;
  }

  @XmlElement(name="grade")
  public Integer getGrade() {
    return this.grade;
  }
}
