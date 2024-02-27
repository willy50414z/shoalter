package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;

import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import com.shoalter.willy.shoaltertools.testtool.RabbitMqUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import com.shoalter.willy.shoaltertools.testtool.updateproductinfo.UpdateProductInfoTestTool;
import com.shoalter.willy.shoaltertools.testtool.updateproductinfo.VerifyUpdateProductInfoTestCase;
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
public class UpdateProductInfoTest {
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
  @Autowired private RedisUtil redisUtil;
  @Autowired private RabbitMqUtil rabbitMqUtil;
  @Autowired private UpdateProductInfoTestTool updProdInfoTestTool;
  @Autowired private VerifyUpdateProductInfoTestCase verifyUpdProdInfo;

  private String EXCHANGE = "shoalter-see-product-master_topic";
  private String ROUTING_KEY = "shoalter-see-product-master.product-info-iids";

  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  // EditProduct case 10
  @Test
  void updateProduct_testcase0001() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0005";
    String sku = "iims-integration-test-testcase-0005";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0001(uuid, sku));

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0001(uuid, sku));

    Thread.sleep(2000L);

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
        buildExpectedHktvStockLevel("H0000101", "0", "notSpecified", "0", uuid, time),
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
        buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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

  // EditProduct case 14
  @Test
  void updateProduct_testcase0004() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0008";
    String sku = "iims-integration-test-testcase-0008";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0004(uuid, sku));

    // setting init value
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

    // setting init value
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

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0004(uuid, sku));

    Thread.sleep(2000L);

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
        buildExpectedHktvStockLevel("H0000102", "0", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000102_updatestocktime")) {
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
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("30", "notSpecified", "1", time),
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
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  // EditProduct case 15
  @Test
  void updateProduct_testcase0005() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0009";
    String sku = "iims-integration-test-testcase-0009";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0005(uuid, sku));

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0005(uuid, sku));

    Thread.sleep(2000L);

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
        buildExpectedHktvStockLevel("H0000102", "0", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000102_updatestocktime")) {
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
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  // EditProduct case 16
  @Test
  void updateProduct_testcase0006() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0010";

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0006(uuid));

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

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0006(uuid));

    Thread.sleep(1000L);

    // 驗證IIDS資料
    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0006(time),
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

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
    redisLMTempl.delete(uuid).block();
  }

  // EditProduct case 18
  @Test
  void updateProduct_testcase0008() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0012";
    String sku = "iims-integration-test-testcase-0012";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0008(uuid, sku));

    // setting init value
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

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0008(uuid, sku));

    Thread.sleep(1000L);

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000102", "0", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000102_updatestocktime")) {
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
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000102")));

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
  }

  // EditProduct case 19
  @Test
  void updateProduct_testcase0009() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0013";
    String sku = "iims-integration-test-testcase-0013";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0009(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
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

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0009(uuid, sku));

    Thread.sleep(2000L);

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "150", "notSpecified", "1", uuid, time),
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

    // 驗證LM資料不存在
    Assertions.assertFalse(redisLMTempl.hasKey(uuid).block());

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  // EditProduct case 20
  @Test
  void updateProduct_testcase0010() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0014";
    String sku = "iims-integration-test-testcase-0014";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0010(uuid, sku));

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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"qty\":50,\"mode\":\"set\",\"share\":0},{\"mall\":\"little_mall\",\"qty\":50,\"mode\":\"set\",\"share\":0}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0010(uuid, sku));

    Thread.sleep(2000L);

    // 驗證IIDS資料
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

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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

  // EditProduct case 21
  @Test
  void updateProduct_testcase0011() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0015";
    String sku = "iims-integration-test-testcase-0015";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0011(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
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

    // setting init value
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

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0011(uuid, sku));

    Thread.sleep(2000L);

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000102", "0", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000102_updatestocktime")) {
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
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證LM資料
    Assertions.assertEquals(
        buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  // EditProduct case 22
  @Test
  void updateProduct_testcase0012() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0016";
    String sku = "iims-integration-test-testcase-0016";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000102");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0012(uuid, sku));

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0012(uuid, sku));

    Thread.sleep(1000L);

    // 驗證IIDS資料
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

    // 驗證HKTV資料
    Assertions.assertEquals(
        buildExpectedHktvStockLevel("H0000101", "0", "notSpecified", "0", uuid, time),
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
            .contains(buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000101")));

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
  }

  // EditProduct case test move in/out warehouse 98
  @Test
  void updateProduct_testcase0013() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0030";
    String sku = "iims-integration-test-testcase-0030";
    String updEventKey01 = buildExpectedUpdateEventKey(sku, "H0000101");
    String updEventKey98 = buildExpectedUpdateEventKey(sku, "H0000198");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey01, updEventKey98).block();

    // createProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0013(uuid, sku));

    Thread.sleep(1000L);

    // setting init value
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":20},{\"warehouseSeqNo\":\"98\",\"mode\":\"set\",\"quantity\":10}]}]")
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
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":0,\"mode\":\"set\",\"qty\":10}]}]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0013_moveOut98(uuid, sku));

    Thread.sleep(1000L);

    // 驗證IIDS資料
    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0013_moveOut98(sku, time),
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
        buildExpectedHktvStockLevel("H0000101", "0", "notSpecified", "0", uuid, time),
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
            .range(updEventKey01, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000101")));

    // updateProduct
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, updateProductInfoDto_testcase0013_moveIn98(uuid, sku));

    Thread.sleep(1000L);

    // 驗證IIDS資料
    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0013_moveIn98(sku, time),
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
        buildExpectedHktvStockLevel("H0000198", "0", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000198_updatestocktime")) {
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
            .range(updEventKey98, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(buildExpectedUpdateEventValue(sku, "H0000198")));

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey01, updEventKey98).block();
  }

  private ProductInfoDto buildProductInfoDto_testcase0001(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0001(String uuid, String sku) {
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

  private ProductInfoDto buildProductInfoDto_testcase0004(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0004(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0005(String uuid, String sku) {
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

  private ProductInfoDto buildProductInfoDto_testcase0006(String uuid) {
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

  private ProductInfoDto updateProductInfoDto_testcase0006(String uuid) {
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

  private static Map<String, String> buildExpectedStockLevel_testcase0006(String time) {
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

  private ProductInfoDto updateProductInfoDto_testcase0008(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0009(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0010(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0011(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0012(String uuid, String sku) {
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

  private ProductInfoDto buildProductInfoDto_testcase0013(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0013_moveOut98(String uuid, String sku) {
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

  private ProductInfoDto updateProductInfoDto_testcase0013_moveIn98(String uuid, String sku) {
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

  private static Map<String, String> buildExpectedStockLevel_testcase0013_moveOut98(
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

  private static Map<String, String> buildExpectedStockLevel_testcase0013_moveIn98(
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

  private ProductInfoDto move_HKTV_to_wh(String seqNo, String uuid, String sku) {
    return updProdInfoTestTool.moveHktvToWh(seqNo, uuid, sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantTo3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantTo3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_qty", "2400").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentTo3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentTo3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory_iimsIsOtherMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory_iimsIsOtherMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory_iimsIsConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory_iimsIsConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantTo3PLInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantTo3PLInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantTo3PLInventory_iimsIsConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantTo3PLInventory_iimsIsConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory_iimsIsOtherMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory_iimsIsOtherMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToConsignmentInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToConsignmentInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToConsignmentInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToConsignmentInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void
      putProductInfo_moveHktvFromConsignmentToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("17", uuid, sku));

    // verify
    verifyUpdProdInfo
        .moveHktvFromConsignmentToConsignmentInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentTo3PLInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentTo3PLInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentTo3PLInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentTo3PLInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToMerchantInventory_iimsIsOtherMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("03", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToMerchantInventory_iimsIsOtherMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToMerchantInventory_iimsIsConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("03", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToMerchantInventory_iimsIsConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToConsignmentInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToConsignmentInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToConsignmentInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh01_to_wh02_but_iims_already_in_wh02() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "02");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "02_mall", "").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_qty", "2400").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh01_to_wh02_but_iims_already_in_wh02();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh01_to_wh15_but_iims_already_in_wh15() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh01_to_wh15_but_iims_already_in_wh15();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh01_to_wh98_but_iims_already_in_wh98() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh01_to_wh98_but_iims_already_in_wh98();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh15_to_wh01_but_iims_already_in_wh01() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("01", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh15_to_wh01_but_iims_already_in_wh01();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh15_to_wh16_but_iims_already_in_wh16() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh15_to_wh16_but_iims_already_in_wh16();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh15_to_wh98_but_iims_already_in_wh98() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh15_to_wh98_but_iims_already_in_wh98();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh98_to_wh01_but_iims_already_in_wh01() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("01", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh98_to_wh01_but_iims_already_in_wh01();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh98_to_wh15_but_iims_already_in_wh15() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh98_to_wh15_but_iims_already_in_wh15();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }
}
