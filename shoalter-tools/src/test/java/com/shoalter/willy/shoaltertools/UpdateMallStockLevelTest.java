package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;

import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
public class UpdateMallStockLevelTest {
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired private RabbitTemplate defaultRabbitTemplate;

  private String EXCHANGE = "shoalter-see-product-master_topic";
  private String ROUTING_KEY = "shoalter-see-product-master.product-info-iids";

  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  @Test
  void updateMallStockLevel_testcase0001() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0025";
    String sku = "iims-integration-test-testcase-0025";
    String uuid_FIS = "iids-integration-test-testcase-0026";
    String sku_FIS = "iims-integration-test-testcase-0026";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0001(uuid, sku, uuid_FIS, sku_FIS));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":50},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":666}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"share\":1}]},{\"uuid\":\""
                + uuid_FIS
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1,\"instockstatus\":\"forceInStock\"},{\"mall\":\"little_mall\",\"share\":1,\"instockstatus\":\"forceInStock\"}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"qty\":40,\"mode\":\"set\",\"instockstatus\":\"notSpecified\"},{\"mall\":\"little_mall\",\"share\":0,\"qty\":30,\"mode\":\"set\",\"instockstatus\":\"forceOutOfStock\"}]},"
                + "{\"uuid\":\""
                + uuid_FIS
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0},{\"mall\":\"little_mall\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0001(sku, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + uuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("create_time")
                        || entry.getKey().equals("update_time")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0001_2(sku_FIS, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + uuid_FIS)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("create_time")
                        || entry.getKey().equals("update_time")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());
  }

  @Test
  void updateMallStockLevel_testcase0002() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0027";
    String sku = "iims-integration-test-testcase-0027";
    String uuid_FIS = "iids-integration-test-testcase-0028";
    String sku_FIS = "iims-integration-test-testcase-0028";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0002(uuid, sku, uuid_FIS, sku_FIS));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":50},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":666}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"set\",\"qty\":20},{\"mall\":\"little_mall\",\"share\":0,\"mode\":\"set\",\"qty\":15}]},{\"uuid\":\""
                + uuid_FIS
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"instockstatus\":\"forceInStock\"},{\"mall\":\"little_mall\",\"share\":1,\"instockstatus\":\"forceInStock\"}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"qty\":10,\"mode\":\"set\",\"instockstatus\":\"forceOutOfStock\"},{\"mall\":\"little_mall\",\"share\":1,\"instockstatus\":\"forceOutOfStock\"}]},"
                + "{\"uuid\":\""
                + uuid_FIS
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0002(sku, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + uuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("create_time")
                        || entry.getKey().equals("update_time")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0002_2(sku_FIS, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + uuid_FIS)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("create_time")
                        || entry.getKey().equals("update_time")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());
  }

  @Test
  void updateMallStockLevel_testcase0003() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0029";
    String sku = "iims-integration-test-testcase-0029";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0003(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":70},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":666}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"set\",\"qty\":70,\"instockstatus\":\"forceOutOfStock\"},{\"mall\":\"little_mall\",\"share\":1,\"instockstatus\":\"forceOutOfStock\"}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"deduct\",\"qty\":5,\"instockstatus\":\"notSpecified\"},{\"mall\":\"little_mall\",\"share\":0,\"instockstatus\":\"notSpecified\",\"mode\":\"set\",\"qty\":30}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0003(sku, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + uuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("create_time")
                        || entry.getKey().equals("update_time")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());
  }

  private ProductInfoDto buildProductInfoDto_testcase0001(
      String uuid, String sku, String uuid_FIS, String sku_FIS) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build(),
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv", "little_mall"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of())
                            .build())),
                new ProductDto(
                    uuid_FIS,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku_FIS)
                            .build(),
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00003")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv", "little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0001(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "");
    stockLevelMap.put("02_qty", "666");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "forceOutOfStock");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0001_2(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "forceInStock");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "forceInStock");
    stockLevelMap.put("little_mall_share", "1");
    stockLevelMap.put("little_mall_store_code", "H00003");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  private ProductInfoDto buildProductInfoDto_testcase0002(
      String uuid, String sku, String uuid_FIS, String sku_FIS) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build(),
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv", "little_mall"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of())
                            .build())),
                new ProductDto(
                    uuid_FIS,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku_FIS)
                            .build(),
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00003")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv", "little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0002(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "40");
    stockLevelMap.put("02_mall", "");
    stockLevelMap.put("02_qty", "666");
    stockLevelMap.put("hktv_instockstatus", "forceOutOfStock");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "forceOutOfStock");
    stockLevelMap.put("little_mall_share", "1");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  private ProductInfoDto buildProductInfoDto_testcase0003(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build(),
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv", "little_mall"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of())
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0003(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "");
    stockLevelMap.put("02_qty", "666");
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

  private static Map<String, String> buildExpectedStockLevel_testcase0002_2(
      String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "forceInStock");
    stockLevelMap.put("hktv_share", "1");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "forceInStock");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00003");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }
}
