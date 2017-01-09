package com.reuter.data.services.impl;

import com.google.common.base.Charsets;
import com.reuter.data.services.CalculateRecordGradeService;
import com.reuter.data.models.Hotel;
import com.reuter.data.services.SearchURLService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.reuter.data.services.SearchURLService.HTTP_STATUS_OK;

/**
 * Created by aandra1 on 03/11/16.
 */
@Slf4j
@Service
public class CalculateHotelGradeServiceImpl implements CalculateRecordGradeService<Hotel> {

  private UrlValidator urlValidator = new UrlValidator();

  @Autowired
  private SearchURLService searchURLService;

  @Override
  public Hotel calculate(Hotel record) {
    return computeName(computeUri(computeStar(record)));
  }

  private Hotel computeUri(Hotel record) {
    if (StringUtils.isEmpty(record.getUri()) || !validateURL(record.getUri())) {
      record.subtractGrade(3);
    }
    return record;
  }

  private boolean validateURL(String url) {
    boolean isValid = false;

    if (urlValidator.isValid(url)) {
      int statusCode = searchURLService.search(url);
      isValid = (statusCode == HTTP_STATUS_OK);
    }

    return isValid;
  }

  private Hotel computeName(Hotel record) {
    String newName = new String(record.getName().getBytes(Charsets.UTF_8), Charsets.UTF_8);
    record.setName(newName);
    return record;
  }

  private Hotel computeStar(Hotel record) {
    if (record.getStars() < 0)
      record.subtractGrade(1);

    return record;
  }
}
