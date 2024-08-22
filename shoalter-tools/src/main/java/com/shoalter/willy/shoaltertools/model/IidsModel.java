package com.shoalter.willy.shoaltertools.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IidsModel {
  private String sku;
  private String uuid;
  private Integer mmsId;
  private String warehouseId;
}
