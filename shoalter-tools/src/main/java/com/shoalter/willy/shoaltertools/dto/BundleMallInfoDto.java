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
public class BundleMallInfoDto {
  private String mall;

  @JsonProperty("alert_qty")
  private Integer alertQty;

  @JsonProperty("ceiling_qty")
  private Integer ceilingQty;
}
