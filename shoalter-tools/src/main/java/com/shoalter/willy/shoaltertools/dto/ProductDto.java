package com.shoalter.willy.shoaltertools.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
  private String uuid;

  @JsonProperty("mall_detail")
  private List<ProductMallDetailDto> mallDetail;

  @JsonProperty("warehouse_detail")
  private List<ProductWarehouseDetailDto> warehouseDetail;
}
