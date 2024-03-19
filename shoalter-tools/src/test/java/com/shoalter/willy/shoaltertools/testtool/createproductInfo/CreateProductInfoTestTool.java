package com.shoalter.willy.shoaltertools.testtool.createproductInfo;

import com.shoalter.willy.shoaltertools.dto.BundleChildDto;
import com.shoalter.willy.shoaltertools.dto.BundleMallInfoDto;
import com.shoalter.willy.shoaltertools.dto.BundleSettingDto;
import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                        .skuId("child-SKU-E-1")
                                        .storefrontStoreCode("H088800118")
                                        .skuQty(1)
                                        .ceilingQty(0)
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid("child-UUID-E-2-2")
                                        .skuId("child-SKU-E-2")
                                        .storefrontStoreCode("H088800118")
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  public ProductInfoDto buildProductInfoDto_testcase0001(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv", "little_mall"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build(),
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("H00001")
                                .build()))
                    .build()))
        .build();
  }

  public static Map<String, String> buildExpectedStockLevel_testcase0001(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public static Map<String, String> buildExpectedLMStockLevel(
      String quantity, String instockstatus, String share, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("quantity", quantity);
    stockLevelMap.put("instockstatus", instockstatus);
    stockLevelMap.put("updatestocktime", time);
    stockLevelMap.put("share", share);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_testcase0002(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("little_mall"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build(),
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("H00001")
                                .build()))
                    .build()))
        .build();
  }

  public static Map<String, String> buildExpectedStockLevel_testcase0002(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "little_mall");
    stockLevelMap.put("02_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_testcase0003(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("03")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("04")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("05")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("06")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("07")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("08")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("09")
                                .mall(List.of("hktv"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("10")
                                .mall(List.of())
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .build()))
        .build();
  }

  public static Map<String, String> buildExpectedStockLevel_testcase0003(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "");
    stockLevelMap.put("02_qty", "0");
    stockLevelMap.put("03_mall", "");
    stockLevelMap.put("03_qty", "0");
    stockLevelMap.put("04_mall", "");
    stockLevelMap.put("04_qty", "0");
    stockLevelMap.put("05_mall", "");
    stockLevelMap.put("05_qty", "0");
    stockLevelMap.put("06_mall", "");
    stockLevelMap.put("06_qty", "0");
    stockLevelMap.put("07_mall", "");
    stockLevelMap.put("07_qty", "0");
    stockLevelMap.put("08_mall", "");
    stockLevelMap.put("08_qty", "0");
    stockLevelMap.put("09_mall", "hktv");
    stockLevelMap.put("09_qty", "0");
    stockLevelMap.put("10_mall", "");
    stockLevelMap.put("10_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_testcase0004(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv", "little_mall"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build(),
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("H00002")
                                .build()))
                    .build()))
        .build();
  }

  public static Map<String, String> buildExpectedStockLevel_testcase0004(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00002");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public static Map<String, String> buildExpectedHktvStockLevel(
      String warehouseId,
      String available,
      String instockstatus,
      String share,
      String uuid,
      String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put(warehouseId + "_available", available);
    stockLevelMap.put(warehouseId + "_instockstatus", instockstatus);
    stockLevelMap.put(warehouseId + "_updatestocktime", time);
    stockLevelMap.put("share", share);
    stockLevelMap.put("uuid", uuid);
    return stockLevelMap;
  }

  public static String buildExpectedUpdateEventKey(String sku, String warehouseId) {
    String updateKey = sku + "|||" + warehouseId;
    return "evtq_part_stockdata_" + Math.abs(updateKey.hashCode() % 10);
  }

  public static String buildExpectedUpdateEventValue(String sku, String warehouseId) {
    return sku + "|||" + warehouseId;
  }
}
