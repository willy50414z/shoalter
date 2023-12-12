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
public class UpdateWarehouseQtyTest {

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

  // updateWarehouseQuantity case 1
  @Test
  void updateWarehouseQty_testcase0001() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0018";
    String sku = "iims-integration-test-testcase-0018";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0001(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
    given()
        .contentType("application/json")
        .body("[{\"uuid\":\"" + uuid + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":30}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "30", "notSpecified", "1", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // updateWarehouseQuantity case 2
  @Test
  void updateWarehouseQty_testcase0002() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0019";
    String sku = "iims-integration-test-testcase-0019";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0002(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":60}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "50", "notSpecified", "0", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // updateWarehouseQuantity case 3
  @Test
  void updateWarehouseQty_testcase0003() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0020";
    String sku = "iims-integration-test-testcase-0020";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

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
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":100}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":20}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
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
        buildExpectedHktvStockLevel("H0000101", "20", "notSpecified", "0", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // updateWarehouseQuantity case 4
  @Test
  void updateWarehouseQty_testcase0004() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0021";
    String sku = "iims-integration-test-testcase-0021";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0004(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
    given()
        .contentType("application/json")
        .body("[{\"uuid\":\"" + uuid + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"add\",\"quantity\":30}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    Thread.sleep(1000L);

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "80", "notSpecified", "1", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // updateWarehouseQuantity case 5
  @Test
  void updateWarehouseQty_testcase0005() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0022";
    String sku = "iims-integration-test-testcase-0022";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0005(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
    given()
        .contentType("application/json")
        .body("[{\"uuid\":\"" + uuid + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"deduct\",\"quantity\":30}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    Thread.sleep(1000L);

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "20", "notSpecified", "1", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // updateWarehouseQuantity case 7
  @Test
  void updateWarehouseQty_testcase0006() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0023";
    String sku = "iims-integration-test-testcase-0023";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0006(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"set\",\"qty\":50}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"deduct\",\"quantity\":30}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "50", "notSpecified", "0", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // updateWarehouseQuantity case 8
  @Test
  void updateWarehouseQty_testcase0007() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0024";
    String sku = "iims-integration-test-testcase-0024";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0007(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":60}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"set\",\"qty\":50}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateWarehouseQuantity
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"deduct\",\"quantity\":30}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "30", "notSpecified", "0", uuid, time),
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

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0001(String sku, String time) {
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

  private static Map<String, String> buildExpectedStockLevel_testcase0002(String sku, String time) {
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

  private static Map<String, String> buildExpectedStockLevel_testcase0003(String sku, String time) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0004(String sku, String time) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0005(String sku, String time) {
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

  private ProductInfoDto buildProductInfoDto_testcase0006(String uuid, String sku) {
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

  private static Map<String, String> buildExpectedStockLevel_testcase0006(String sku, String time) {
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
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv"))
                            .build()))))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0007(String sku, String time) {
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
