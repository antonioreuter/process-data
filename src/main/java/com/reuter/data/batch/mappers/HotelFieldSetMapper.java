package com.reuter.data.batch.mappers;

import com.reuter.data.models.Hotel;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

/**
 * Created by aandra1 on 02/11/16.
 */
@Component("hotelFieldSetMapper")
public class HotelFieldSetMapper implements FieldSetMapper<Hotel> {

  @Override
  public Hotel mapFieldSet(final FieldSet fieldSet) throws BindException {
    return Hotel.builder()
        .name(fieldSet.readString(0))
        .address(fieldSet.readString(1))
        .stars(fieldSet.readInt(2))
        .contact(fieldSet.readString(3))
        .phone(fieldSet.readString(4))
        .uri(fieldSet.readString(5))
        .build();
  }
}
