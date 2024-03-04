package com.shoalter.willy.shoaltertools.testtool.updbundleqty;

public class UpdateBundleQtyTestTool {
  public static String getResetNodeListWhenCrossNodeParentSetting() {
    return "{\"is_reserved\":true,\"is_active\":false,\"priority\":0,\"bundle_mall_info\":[{\"mall\":\"hktv\",\"alert_qty\":100,\"ceiling_qty\":100}],\"bundle_child_info\":[{\"uuid\":\"child-UUID-E-1\",\"sku_id\":\"child-SKU-E-1\",\"storefront_store_code\":\"H088800118\",\"sku_qty\":1,\"ceiling_qty\":0,\"is_loop\":false},{\"uuid\":\"child-UUID-E-2\",\"sku_id\":\"child-SKU-E-2\",\"storefront_store_code\":\"H088800118\",\"sku_qty\":2,\"ceiling_qty\":0,\"is_loop\":false}]}";
  }

  public static String getReplenishChildQtyNotCrashParentSetting() {
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
}
