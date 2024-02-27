package com.shoalter.willy.shoaltertools.testtool.updateproductinfo;

import static io.restassured.RestAssured.given;

import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductInfoTestTool {

  @Autowired ApiUtil apiUtil;

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0010(String uuid, String sku) {
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

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0010(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv", "little_mall"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of())
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

  public Map<String, String> buildExpectedStockLevel_testcase0001(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "");
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

  public Map<String, String> buildExpectedLMStockLevel(
      String quantity, String instockstatus, String share, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("quantity", quantity);
    stockLevelMap.put("instockstatus", instockstatus);
    stockLevelMap.put("updatestocktime", time);
    stockLevelMap.put("share", share);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0014(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .build()))
        .build();
  }

  public void updateProduct_EditProductTestCase0014_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":20},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":30}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"share\":1}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0014(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("hktv", "little_mall"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0004(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "20");
    stockLevelMap.put("02_mall", "hktv,little_mall");
    stockLevelMap.put("02_qty", "30");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "1");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0015(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv", "little_mall"))
                                .build()))
                    .build()))
        .build();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0015(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("hktv", "little_mall"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0005(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "hktv,little_mall");
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

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0016(String uuid) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("H00001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("little_mall"))
                                .build()))
                    .build()))
        .build();
  }

  public void updateProduct_EditProductTestCase0016_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":50}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"little_mall\",\"share\":0,\"qty\":50,\"mode\":\"set\",\"instockstatus\":\"notSpecified\"}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0016(String uuid) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("little_mall")
                                .storefrontStoreCode("H00001")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("little_mall"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0006(String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "50");
    stockLevelMap.put("02_mall", "little_mall");
    stockLevelMap.put("02_qty", "0");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public Map<String, String> buildExpectedStockLevel_testcase0007(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "little_mall");
    stockLevelMap.put("01_qty", "100");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "1");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0018(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of())
                                .build()))
                    .build()))
        .build();
  }

  public void updateProduct_EditProductTestCase0018_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":30}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body("[{\"uuid\":\"" + uuid + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0018(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("hktv"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0008(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "hktv");
    stockLevelMap.put("02_qty", "130");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0019(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv", "little_mall"))
                                .build()))
                    .build()))
        .build();
  }

  public void updateProduct_EditProductTestCase0019_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":150}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0019(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0009(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "150");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "1");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0020(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv", "little_mall"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of())
                                .build()))
                    .build()))
        .build();
  }

  public void updateProduct_EditProductTestCase0020_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"qty\":50,\"mode\":\"set\",\"share\":0},{\"mall\":\"little_mall\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0020(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0010(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "50");
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

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0021(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .build()))
        .build();
  }

  public void updateProduct_EditProductTestCase0021_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":90}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"little_mall\",\"qty\":30,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0021(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
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
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("little_mall"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("hktv"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0011(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "little_mall");
    stockLevelMap.put("01_qty", "100");
    stockLevelMap.put("02_mall", "hktv");
    stockLevelMap.put("02_qty", "90");
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

  public ProductInfoDto buildProductInfoDto_EditProductTestCase0022(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of("hktv"))
                                .build()))
                    .build()))
        .build();
  }

  public ProductInfoDto updateProductInfoDto_EditProductTestCase0022(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("02")
                                .mall(List.of())
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0012(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "");
    stockLevelMap.put("02_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public Map<String, String> buildExpectedHktvStockLevel(
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

  public ProductInfoDto buildProductInfoDto_testcase0013(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("98")
                                .mall(List.of("hktv"))
                                .build()))
                    .build()))
        .build();
  }

  public void updateProduct_testcase0013_setting_init_value(String uuid) {
    AssertUtil.wait_2_sec();
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":20},{\"warehouseSeqNo\":\"98\",\"mode\":\"set\",\"quantity\":10}]}]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"set\",\"qty\":10}]}]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public ProductInfoDto updateProductInfoDto_testcase0013_moveOut98(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("98")
                                .mall(List.of())
                                .build()))
                    .build()))
        .build();
  }

  public ProductInfoDto updateProductInfoDto_testcase0013_moveIn98(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(uuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("H00001")
                                .storeSkuId(sku)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of())
                                .build(),
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("98")
                                .mall(List.of("hktv"))
                                .build()))
                    .build()))
        .build();
  }

  public Map<String, String> buildExpectedStockLevel_testcase0013_moveOut98(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "20");
    stockLevelMap.put("98_mall", "");
    stockLevelMap.put("98_qty", "10");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public Map<String, String> buildExpectedStockLevel_testcase0013_moveIn98(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "20");
    stockLevelMap.put("98_mall", "hktv");
    stockLevelMap.put("98_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  public String buildExpectedUpdateEventValue(String sku, String warehouseId) {
    return sku + "|||" + warehouseId;
  }
}
