package com.shoalter.willy.shoaltertools.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BundleSettingDto {
  @JsonProperty("is_reserved")
  private Boolean isReserved;

  @JsonProperty("is_active")
  private Boolean isActive;

  @JsonProperty("priority")
  private Integer priority;

  @JsonProperty("bundle_mall_info")
  private List<BundleMallInfoDto> bundleMallInfoList;

  @JsonProperty("bundle_child_info")
  private List<BundleChildDto> bundleChildInfoList;
}
