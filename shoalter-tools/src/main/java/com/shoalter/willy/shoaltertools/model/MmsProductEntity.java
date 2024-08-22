package com.shoalter.willy.shoaltertools.model;

import java.util.List;
import lombok.Data;

@Data
public class MmsProductEntity {
  private Integer mmsId;
  private Integer merchantId;
  private String uuid;
  private String storeSkuId;
  private String storefrontStoreCode;
  private String warehouseSeqNumber;
  private List warehouseSeqNumbers;
}
