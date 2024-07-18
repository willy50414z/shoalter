package com.shoalter.willy.shoaltertools.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class ProductShareGroupKey implements java.io.Serializable {

  @Column(name = "product_id")
  Long productId;

  @Column(name = "group_id")
  Long groupId;
}
