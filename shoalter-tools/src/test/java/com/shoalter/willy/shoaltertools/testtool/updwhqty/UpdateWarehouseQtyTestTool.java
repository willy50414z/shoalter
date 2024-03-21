package com.shoalter.willy.shoaltertools.testtool.updwhqty;

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

public class UpdateWarehouseQtyTestTool {
  private static final String childSku1 = "childSku-1";
  private static final String childSku2 = "childSku-2";
  private static final String childSku3 = "childSku-3";
  private static final String childUuid1 = "childUuid-1";
  private static final String childUuid2 = "childUuid-2";
  private static final String childUuid3 = "childUuid-3";

  protected ProductInfoDto buildBundleProductInfoDto_testcase0001(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0001-0001-0001")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0001")
                                .storefrontStoreCode("H0121001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
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
                                        .uuid(childUuid1)
                                        .skuId(childSku1)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid3)
                                        .skuId(childSku3)
                                        .skuQty(4)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid1)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSku1)
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid2)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSku2)
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid3)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSku3)
                                .build()))
                    .build()))
        .build();
  }

  protected ProductInfoDto buildBundleProductInfoDto_testcase0002(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0002-0002-0002")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0002")
                                .storefrontStoreCode("H0121001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
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
                                        .uuid(childUuid1)
                                        .skuId(childSku1)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  protected ProductInfoDto buildBundleProductInfoDto_testcase0003(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0003-0003-0003")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0003")
                                .storefrontStoreCode("H0121001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
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
                                        .uuid(childUuid3)
                                        .skuId(childSku3)
                                        .skuQty(4)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  protected ProductInfoDto buildBundleProductInfoDto_testcase0004(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0004-0004-0004")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0004")
                                .storefrontStoreCode("H0121001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
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
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  protected ProductInfoDto buildBundleProductInfoDto_testcase0005(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0005-0005-0005")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0005")
                                .storefrontStoreCode("H0121001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
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
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  protected ProductInfoDto buildBundleProductInfoDto_testcase0006(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0006-0006-0006")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0006")
                                .storefrontStoreCode("H0121001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("hktv"))
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
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  protected ProductInfoDto buildBundleProductInfoDto_testcaseLM(String warehouseSeqNo) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(childUuid1)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("little_mall"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("child")
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid2)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("little_mall"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("child")
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid3)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo(warehouseSeqNo)
                                .mall(List.of("little_mall"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("child")
                                .build()))
                    .build()))
        .build();
  }

  protected ProductInfoDto buildProductInfoDto(String uuid, String sku) {
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

  protected static Map<String, String> buildExpectedStockLevel_testcase0001(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "30");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "1");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedStockLevel_testcase0002(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "10");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedStockLevel_testcase0003(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedStockLevel_testcase0004(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "80");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "1");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedStockLevel_testcase0005(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "20");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "1");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedStockLevel_testcase0006(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "20");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedStockLevel_testcase0007(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  protected static Map<String, String> buildExpectedHktvStockLevel(
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

  protected static String buildExpectedUpdateEventKey(String sku, String warehouseId) {
    String updateKey = sku + "|||" + warehouseId;
    return "evtq_part_stockdata_" + Math.abs(updateKey.hashCode() % 10);
  }

  protected static String buildExpectedUpdateEventValue(String sku, String warehouseId) {
    return sku + "|||" + warehouseId;
  }
}
