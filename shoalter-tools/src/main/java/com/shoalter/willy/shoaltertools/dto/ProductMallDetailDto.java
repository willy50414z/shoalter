package com.shoalter.willy.shoaltertools.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMallDetailDto {
  private String mall;

  @JsonProperty("storefront_store_code")
  private String storefrontStoreCode;

  @JsonProperty("store_sku_id")
  private String storeSkuId;
}
