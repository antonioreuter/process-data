package com.reuter.data.services;

import com.reuter.data.models.Hotel;
import com.reuter.data.services.impl.CalculateHotelGradeServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by aandra1 on 05/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculateHotelGradeServiceImplTest {

  @Mock
  SearchURLService searchURLService;

  @Spy
  @InjectMocks
  CalculateHotelGradeServiceImpl subject;

  @Test
  public void whenTheHotelHasAnInvalidRateGradeShouldBe9() {
    Hotel hotel = Hotel.builder().name("Hotel Hilton").stars(-1).uri("http://www.hilton.com").build();

    when(searchURLService.search(any(String.class))).thenReturn(200);
    hotel = subject.calculate(hotel);

    assertEquals(Integer.valueOf(9), Integer.valueOf(hotel.getGrade()));
  }

  @Test
  public void whenTheHotelHasAnInvalidURLGradeShouldBe7() {
    Hotel hotel = Hotel.builder().name("Hotel Hilton").stars(5).uri("http://www.hilton.com").build();

    when(searchURLService.search(any(String.class))).thenReturn(404);
    hotel = subject.calculate(hotel);

    assertEquals(Integer.valueOf(7), Integer.valueOf(hotel.getGrade()));
  }

  @Test
  public void whenTheHotelHasAnInvalidRateAndURLGradeShouldBe6() {
    Hotel hotel = Hotel.builder().name("Hotel Hilton").stars(-1).uri("http://www.hilton.com").build();

    when(searchURLService.search(any(String.class))).thenReturn(404);
    hotel = subject.calculate(hotel);

    assertEquals(Integer.valueOf(6), Integer.valueOf(hotel.getGrade()));
  }
}
