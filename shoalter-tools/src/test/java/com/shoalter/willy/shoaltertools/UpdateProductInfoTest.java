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
public class UpdateProductInfoTest {
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired private RabbitTemplate defaultRabbitTemplate;

  private String EXCHANGE = "shoalter-see-product-master_topic";
  private String ROUTING_KEY = "shoalter-see-product-master.product-info-iids";

  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  @Test
  void updateProduct_testcase0001() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0005";
    String sku = "iims-integration-test-testcase-0005";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0001(uuid, sku));

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0001(uuid, sku));

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
  }

  @Test
  void updateProduct_testcase0002() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0006";
    String sku = "iims-integration-test-testcase-0006";
    String newSku = "iims-integration-test-testcase-0006-new";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0002(uuid, sku));

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0002(uuid, newSku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0002(newSku, time),
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

  @Test
  void updateProduct_testcase0003() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0007";
    String sku = "iims-integration-test-testcase-0007";
    String newSku = "iims-integration-test-testcase-0007-new";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0003(uuid, sku));

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0003(uuid, newSku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0003(newSku, time),
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

  @Test
  void updateProduct_testcase0004() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0008";
    String sku = "iims-integration-test-testcase-0008";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0004(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":20},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":30}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0004(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0004(sku, time),
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

  @Test
  void updateProduct_testcase0005() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0009";
    String sku = "iims-integration-test-testcase-0009";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0005(uuid, sku));

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0005(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0005(sku, time),
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

  @Test
  void updateProduct_testcase0006() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0010";
    String sku = "iims-integration-test-testcase-0010";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0006(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":50}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"little_mall\",\"share\":0,\"qty\":50,\"mode\":\"set\",\"instockstatus\":\"notSpecified\"}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0006(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0006(sku, time),
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

  @Test
  void updateProduct_testcase0007() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0011";
    String sku = "iims-integration-test-testcase-0011";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0007(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0007(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0007(sku, time),
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

  @Test
  void updateProduct_testcase0008() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0012";
    String sku = "iims-integration-test-testcase-0012";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0008(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":30}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    given()
        .contentType("application/json")
        .body("[{\"uuid\":\"" + uuid + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0008(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0008(sku, time),
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

  @Test
  void updateProduct_testcase0009() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0013";
    String sku = "iims-integration-test-testcase-0013";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0009(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":150}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0009(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0009(sku, time),
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

  @Test
  void updateProduct_testcase0010() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0014";
    String sku = "iims-integration-test-testcase-0014";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0010(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"qty\":50,\"mode\":\"set\",\"share\":0},{\"mall\":\"little_mall\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0010(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0010(sku, time),
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

  @Test
  void updateProduct_testcase0011() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0015";
    String sku = "iims-integration-test-testcase-0015";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0011(uuid, sku));

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":90}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"little_mall\",\"qty\":30,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0011(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0011(sku, time),
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

  @Test
  void updateProduct_testcase0012() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0016";
    String sku = "iims-integration-test-testcase-0016";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0012(uuid, sku));

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0012(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0012(sku, time),
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

  private ProductInfoDto buildProductInfoDto_testcase0001(String uuid, String sku) {
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
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0001(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
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

  private static Map<String, String> buildExpectedStockLevel_testcase0001(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0002(String uuid, String sku) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0002(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0002(String sku, String time) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0003(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of())
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0003(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("02_mall", "hktv");
    stockLevelMap.put("02_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  private ProductInfoDto buildProductInfoDto_testcase0004(String uuid, String sku) {
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
                            .mall(List.of("hktv"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("little_mall"))
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0004(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
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
                            .mall(List.of())
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("hktv", "little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0004(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0005(String uuid, String sku) {
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
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0005(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
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
                            .mall(List.of())
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("hktv", "little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0005(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0006(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("little_mall"))
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0006(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of())
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0006(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0007(String uuid, String sku) {
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
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0007(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0007(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0008(String uuid, String sku) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of())
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0008(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of())
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0008(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0009(String uuid, String sku) {
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
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0009(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0009(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0010(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0010(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
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
                            .mall(List.of("hktv"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("little_mall"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0010(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0011(String uuid, String sku) {
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
                            .mall(List.of("hktv"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("little_mall"))
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0011(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
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
                            .mall(List.of("little_mall"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0011(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0012(String uuid, String sku) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of())
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private ProductInfoDto updateProductInfoDto_testcase0012(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build(),
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("02")
                            .mall(List.of())
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0012(String sku, String time) {
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
}
