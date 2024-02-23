package com.shoalter.willy.shoaltertools.testtool.updateproductinfo;

import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.testtool.BuildDtoUtil;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductInfoTestTool {

  public ProductInfoDto moveHktvToWh(String seqNo, String uuid, String sku) {
    return BuildDtoUtil.buildUpdateProductInfoDto(seqNo, uuid, sku);
  }
}
