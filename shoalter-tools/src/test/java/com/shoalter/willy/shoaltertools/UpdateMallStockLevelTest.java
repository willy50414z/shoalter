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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
public class UpdateMallStockLevelTest {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisLMTemplate")
  ReactiveRedisTemplate<String, String> redisLMTempl;

  @Autowired
  @Qualifier("redisHKTVTemplate")
  ReactiveRedisTemplate<String, String> redisHKTVTempl;

  @Autowired private RabbitTemplate defaultRabbitTemplate;

  private String EXCHANGE = "shoalter-see-product-master_topic";
  private String ROUTING_KEY = "shoalter-see-product-master.product-info-iids";

  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  // UpdateMallStockLevel case 1
  @Test
  void updateMallStockLevel_testcase0001() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0025";
    String sku = "iims-integration-test-testcase-0025";
    String uuid_FIS = "iids-integration-test-testcase-0026";
    String sku_FIS = "iims-integration-test-testcase-0026";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String updEventKey_FIS = buildExpectedUpdateEventKey(sku_FIS, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid, "inventory:" + uuid_FIS, uuid_FIS).block();
    redisHKTVTempl.delete(sku, sku_FIS, updEventKey, updEventKey_FIS).block();
    redisLMTempl.delete(uuid, uuid_FIS).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0001(uuid, sku, uuid_FIS, sku_FIS));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
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

    // UpdateMallStockLevel
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

    // 驗證IIDS資料
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

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "40", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000101_updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "0", "forceInStock", "0", uuid_FIS, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku_FIS)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000101_updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(updEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(updEventKey_FIS, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku_FIS, "H0000101")));

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("10", "forceOutOfStock", "0", time),
        redisLMTempl
            .<String, String>opsForHash()
            .entries(uuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("0", "forceInStock", "1", time),
        redisLMTempl
            .<String, String>opsForHash()
            .entries(uuid_FIS)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    redisTempl.delete("inventory:" + uuid, uuid, "inventory:" + uuid_FIS, uuid_FIS).block();
    redisHKTVTempl.delete(sku, sku_FIS, updEventKey, updEventKey_FIS).block();
    redisLMTempl.delete(uuid, uuid_FIS).block();
  }

  // UpdateMallStockLevel case 3
  @Test
  void updateMallStockLevel_testcase0002() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0027";
    String sku = "iims-integration-test-testcase-0027";
    String uuid_FIS = "iids-integration-test-testcase-0028";
    String sku_FIS = "iims-integration-test-testcase-0028";

    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String updEventKey_FIS = buildExpectedUpdateEventKey(sku_FIS, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid, "inventory:" + uuid_FIS, uuid_FIS).block();
    redisHKTVTempl.delete(sku, sku_FIS, updEventKey, updEventKey_FIS).block();
    redisLMTempl.delete(uuid, uuid_FIS).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0002(uuid, sku, uuid_FIS, sku_FIS));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
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

    // UpdateMallStockLevel
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

    // 驗證IIDS資料
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

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "10", "forceOutOfStock", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000101_updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "0", "forceInStock", "1", uuid_FIS, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku_FIS)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000101_updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(updEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(updEventKey_FIS, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku_FIS, "H0000101")));

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("40", "forceOutOfStock", "1", time),
        redisLMTempl
            .<String, String>opsForHash()
            .entries(uuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("0", "forceInStock", "0", time),
        redisLMTempl
            .<String, String>opsForHash()
            .entries(uuid_FIS)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    redisTempl.delete("inventory:" + uuid, uuid, "inventory:" + uuid_FIS, uuid_FIS).block();
    redisHKTVTempl.delete(sku, sku_FIS, updEventKey, updEventKey_FIS).block();
    redisLMTempl.delete(uuid, uuid_FIS).block();
  }

  // UpdateMallStockLevel case 6
  @Test
  void updateMallStockLevel_testcase0003() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0029";
    String sku = "iims-integration-test-testcase-0029";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0003(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
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

    // UpdateMallStockLevel
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

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "65", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000101_updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(updEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("5", "notSpecified", "0", time),
        redisLMTempl
            .<String, String>opsForHash()
            .entries(uuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("updatestocktime")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();
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

  private static Map<String, String> buildExpectedHktvStockLevel(
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

  private static String buildExpectedUpdateEventKey(String sku, String warehouseId) {
    String updateKey = sku + "|||" + warehouseId;
    return "evtq_part_stockdata_" + Math.abs(updateKey.hashCode() % 10);
  }

  private static String buildExpectedUpdateEventValue(String sku, String warehouseId) {
    return sku + "|||" + warehouseId;
  }

  private static Map<String, String> buildExpectedLMStockLevel(
      String quantity, String instockstatus, String share, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("quantity", quantity);
    stockLevelMap.put("instockstatus", instockstatus);
    stockLevelMap.put("updatestocktime", time);
    stockLevelMap.put("share", share);
    return stockLevelMap;
  }
}
