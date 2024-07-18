package com.shoalter.willy.shoaltertools.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductShareGroupEntity {

  @EmbeddedId ProductShareGroupKey key;

  @ManyToOne
  @MapsId("productId")
  @JoinColumn(name = "group_id")
  ShareGroupEntity shareGroup;

  @ManyToOne
  @MapsId("groupId")
  @JoinColumn(name = "product_id")
  ProductEntity product;
}
