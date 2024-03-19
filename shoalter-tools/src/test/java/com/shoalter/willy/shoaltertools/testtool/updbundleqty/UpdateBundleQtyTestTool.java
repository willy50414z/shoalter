package com.shoalter.willy.shoaltertools.testtool.updbundleqty;

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

public class UpdateBundleQtyTestTool {
  public static final String childUuid1 = "childUuid-1";
  public static final String childUuid2 = "childUuid-2";
  public static final String childUuid3 = "childUuid-3";
  public static final String childSku1 = "childSku-1";
  public static final String childSku2 = "childSku-2";
  public static final String childSku3 = "childSku-3";
  public static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING = "bundle:setting:";
  public static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT = "bundle:parent:";
  public static final String INVENTORY_REDIS_HKEY_PREFIX = "inventory:";

  public static String getDefaultParentSetting() {
    return "{\n"
        + "    \"is_reserved\": true,\n"
        + "    \"is_active\": false,\n"
        + "    \"priority\": 0,\n"
        + "    \"bundle_mall_info\": [\n"
        + "        {\n"
        + "            \"mall\": \"hktv\",\n"
        + "            \"alert_qty\": 100,\n"
        + "            \"ceiling_qty\": 100\n"
        + "        }\n"
        + "    ],\n"
        + "    \"bundle_child_info\": [\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-1\",\n"
        + "            \"sku_id\": \"child-SKU-E-1\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 1,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-2\",\n"
        + "            \"sku_id\": \"child-SKU-E-2\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-3\",\n"
        + "            \"sku_id\": \"child-SKU-E-3\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 3,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

  public static String getParentSettingWithChild12() {
    return "{\n"
        + "    \"is_reserved\": true,\n"
        + "    \"is_active\": false,\n"
        + "    \"priority\": 0,\n"
        + "    \"bundle_mall_info\": [\n"
        + "        {\n"
        + "            \"mall\": \"hktv\",\n"
        + "            \"alert_qty\": 100,\n"
        + "            \"ceiling_qty\": 100\n"
        + "        }\n"
        + "    ],\n"
        + "    \"bundle_child_info\": [\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-1\",\n"
        + "            \"sku_id\": \"child-SKU-E-1\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 1,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-2\",\n"
        + "            \"sku_id\": \"child-SKU-E-2\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

  public static String getParentSettingWithChild124() {
    return "{\n"
        + "    \"is_reserved\": true,\n"
        + "    \"is_active\": false,\n"
        + "    \"priority\": 0,\n"
        + "    \"bundle_mall_info\": [\n"
        + "        {\n"
        + "            \"mall\": \"hktv\",\n"
        + "            \"alert_qty\": 100,\n"
        + "            \"ceiling_qty\": 100\n"
        + "        }\n"
        + "    ],\n"
        + "    \"bundle_child_info\": [\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-1-1\",\n"
        + "            \"sku_id\": \"child-SKU-E-1-1\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 1,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-2-2\",\n"
        + "            \"sku_id\": \"child-SKU-E-2-2\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-4-4\",\n"
        + "            \"sku_id\": \"child-SKU-E-4-4\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

  public static String getParentQtyNotEnoughToDeductParentSetting() {
    return "{\n"
        + "    \"is_reserved\": true,\n"
        + "    \"is_active\": false,\n"
        + "    \"priority\": 0,\n"
        + "    \"bundle_mall_info\": [\n"
        + "        {\n"
        + "            \"mall\": \"hktv\",\n"
        + "            \"alert_qty\": 100,\n"
        + "            \"ceiling_qty\": 100\n"
        + "        }\n"
        + "    ],\n"
        + "    \"bundle_child_info\": [\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-1-1\",\n"
        + "            \"sku_id\": \"child-SKU-E-1-1\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

  public static String getResetNodeListWhenReplenishQtyCrossNodeParentSetting() {
    return "{\n"
        + "    \"is_reserved\": true,\n"
        + "    \"is_active\": false,\n"
        + "    \"priority\": 0,\n"
        + "    \"bundle_mall_info\": [\n"
        + "        {\n"
        + "            \"mall\": \"hktv\",\n"
        + "            \"alert_qty\": 100,\n"
        + "            \"ceiling_qty\": 100\n"
        + "        }\n"
        + "    ],\n"
        + "    \"bundle_child_info\": [\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-1\",\n"
        + "            \"sku_id\": \"child-SKU-E-1\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 1,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-2\",\n"
        + "            \"sku_id\": \"child-SKU-E-2\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-5\",\n"
        + "            \"sku_id\": \"child-SKU-E-5\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 1,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-3\",\n"
        + "            \"sku_id\": \"child-SKU-E-3\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 1,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        },\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-4\",\n"
        + "            \"sku_id\": \"child-SKU-E-4\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

  public static String getParentQtyNotChangeParentSetting() {
    return "{\n"
        + "    \"is_reserved\": true,\n"
        + "    \"is_active\": false,\n"
        + "    \"priority\": 0,\n"
        + "    \"bundle_mall_info\": [\n"
        + "        {\n"
        + "            \"mall\": \"hktv\",\n"
        + "            \"alert_qty\": 100,\n"
        + "            \"ceiling_qty\": 100\n"
        + "        }\n"
        + "    ],\n"
        + "    \"bundle_child_info\": [\n"
        + "        {\n"
        + "            \"uuid\": \"child-UUID-E-1-1\",\n"
        + "            \"sku_id\": \"child-SKU-E-1-1\",\n"
        + "            \"storefront_store_code\": \"H088800118\",\n"
        + "            \"sku_qty\": 2,\n"
        + "            \"ceiling_qty\": 0,\n"
        + "            \"is_loop\": false\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

  public ProductInfoDto buildBundleProductInfoDto_testcase0001() {

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
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
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
                                .warehouseSeqNo("01")
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
                                .warehouseSeqNo("01")
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
                                .warehouseSeqNo("01")
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

  public ProductInfoDto buildBundleProductInfoDto_testcase0002() {

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
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
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

  public ProductInfoDto buildBundleProductInfoDto_testcase0003() {

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
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
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

  public ProductInfoDto buildBundleProductInfoDto_testcase0004() {
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
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
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

  public ProductInfoDto buildBundleProductInfoDto_testcase0005() {
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
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
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

  public ProductInfoDto buildBundleProductInfoDto_testcase0006() {
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
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
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

  public static Map<String, Map<String, String>> case1AssertionExpectation() {
    Map<String, Map<String, String>> resultMap = new HashMap<>();
    Map<String, String> child1Map = new HashMap<>();
    child1Map.put("child01_available", "233");
    child1Map.put("uuid", childUuid1);
    resultMap.put("1", child1Map);

    Map<String, String> child2Map = new HashMap<>();
    child2Map.put("child01_available", "233");
    child2Map.put("uuid", childUuid2);
    resultMap.put("2", child2Map);

    Map<String, String> child3Map = new HashMap<>();
    child3Map.put("child01_available", "233");
    child3Map.put("uuid", childUuid3);
    resultMap.put("3", child3Map);

    return resultMap;
  }
}
