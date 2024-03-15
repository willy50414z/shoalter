package com.shoalter.willy.shoaltertools.testtool.createproductInfo;

import com.shoalter.willy.shoaltertools.dto.BundleChildDto;
import com.shoalter.willy.shoaltertools.dto.BundleMallInfoDto;
import com.shoalter.willy.shoaltertools.dto.BundleSettingDto;
import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import java.util.List;

public class CreateProductInfoTestTool {
  public static ProductInfoDto getCreateParentRabbitMqMsg() {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("parent-E-001-1")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H088800118_S_parent-SKU-E-1")
                                .storefrontStoreCode("H088800118")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .mall(List.of())
                                .warehouseSeqNo("01")
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .mall(List.of("hktv"))
                                .warehouseSeqNo("98")
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid("child-UUID-E-1-1")
                                        .skuId("H088800118_S_child-SKU-E-1")
                                        .storefrontStoreCode("H088800118")
                                        .skuQty(1)
                                        .ceilingQty(0)
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid("child-UUID-E-2-2")
                                        .skuId("H088800118_S_child-SKU-E-2")
                                        .storefrontStoreCode("H088800118")
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }
}
