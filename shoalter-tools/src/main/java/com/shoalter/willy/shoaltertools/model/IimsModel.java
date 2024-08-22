package com.shoalter.willy.shoaltertools.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class IimsModel {
  private String skuId;
  private String storefrontStoreCode;
  private String warehouseSeqNumber;
  private String uuid;
  private String share;
  private String status;
  private String available;
  private String warehouseId;
  private List<String> warehouseSeqNumbers;
  private Integer mmsId;
}
