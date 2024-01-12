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
public class BundleChildDto {
  private String uuid;

  @JsonProperty("sku_id")
  private String skuId;

  @JsonProperty("storefront_store_code")
  private String storefrontStoreCode;

  @JsonProperty("sku_qty")
  private Integer skuQty;

  @JsonProperty("ceiling_qty")
  private Integer ceilingQty;

  @JsonProperty("is_loop")
  private Boolean isLoop;
}
