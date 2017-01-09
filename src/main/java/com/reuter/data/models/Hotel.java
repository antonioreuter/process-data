package com.reuter.data.models;

import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by aandra1 on 31/10/16.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"name", "stars", "uri"})
@EqualsAndHashCode(of = {"name", "stars"})
@XmlRootElement(name = "hotel")
public class Hotel extends Record {
  private String name;

  private String address;

  private Integer stars;

  private String contact;

  private String phone;

  private String uri;
}
