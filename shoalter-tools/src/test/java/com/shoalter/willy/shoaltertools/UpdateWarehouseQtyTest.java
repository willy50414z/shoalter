package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;

import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import com.shoalter.willy.shoaltertools.testtool.RabbitMqUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import com.shoalter.willy.shoaltertools.testtool.updwhqty.UpdateWarehouseQtyTestTool;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
public class UpdateWarehouseQtyTest extends UpdateWarehouseQtyTestTool {

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisLMTemplate")
  ReactiveRedisTemplate<String, String> redisLMTempl;

  @Autowired
  @Qualifier("redisHKTVTemplate")
  ReactiveRedisTemplate<String, String> redisHKTVTempl;

  @Autowired private RedisUtil redisUtil;
  @Autowired private ApiUtil apiUtil;
  @Autowired private RabbitMqUtil rabbitMqUtil;

  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  private static final String INVENTORY_REDIS_HKEY_PREFIX = "inventory:";
  private static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING = "bundle:setting:";
  private static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT = "bundle:parent:";

  // updateWarehouseQuantity case 1
  @Test
  void updateWarehouseQty_testcase0001() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0018";
    String sku = "iims-integration-test-testcase-0018";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));
    AssertUtil.wait_1_sec();

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

    AssertUtil.wait_1_sec();

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
  void updateWarehouseQty_testcase0002() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0019";
    String sku = "iims-integration-test-testcase-0019";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));
    AssertUtil.wait_1_sec();

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
  void updateWarehouseQty_testcase0003() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0020";
    String sku = "iims-integration-test-testcase-0020";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));
    AssertUtil.wait_1_sec();

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
  void updateWarehouseQty_testcase0004() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0021";
    String sku = "iims-integration-test-testcase-0021";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));
    AssertUtil.wait_1_sec();

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

    AssertUtil.wait_1_sec();

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
  void updateWarehouseQty_testcase0005() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0022";
    String sku = "iims-integration-test-testcase-0022";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));
    AssertUtil.wait_1_sec();

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

    AssertUtil.wait_1_sec();

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
  void updateWarehouseQty_testcase0006() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0023";
    String sku = "iims-integration-test-testcase-0023";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));

    AssertUtil.wait_1_sec();

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
  void updateWarehouseQty_testcase0007() {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0024";
    String sku = "iims-integration-test-testcase-0024";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));

    AssertUtil.wait_1_sec();

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

  // 扣庫存因為mall庫存不足所以會扣失敗
  @Test
  void updateWarehouseQty_testcase0008() {
    String uuid = "iids-integration-test-updateWarehouseQty-0001";
    String sku = "iims-integration-test-updateWarehouseQty-0001";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();

    ProductInfoDto productInfoDto =
        ProductInfoDto.builder()
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

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto(uuid, sku));

    AssertUtil.wait_1_sec();

    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"deduct\",\"quantity\":60}]}]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .body(
            Matchers.equalTo(
                "{\"statusCode\":\"IIDS-2001\",\"message\":null,\"data\":{\"success\":[],\"fail\":[{\"uuid\":\"iids-integration-test-updateWarehouseQty-0001\",\"errorCode\":\"IIDS-0029\",\"msg\":\"share and non share quantity not enough to deduct, warehouseSeqNo[01]requestQuantity[60]shareQuantity[0]totalNonShareQuantity[0]\"}]}}"))
        .log()
        .all();

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // 增加非98倉庫數量，有NonShareMall，有bundle，不會影響Mall數量，都加到倉庫
  @Test
  void updWhQty_add_non98Wh_singleNonShareMall_HKTV_hasBundle() {
    // 預期結果: 不會影響nonShareMall數量，都加到共享庫存
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.addWh01Child123Qty100200300();
    // child1 share = 200, mall = 193
    // child2 share = 300, mall = 73
    // child3 share = 400, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "200", "300", "400", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 增加98倉庫數量，有NonShareMall，有bundle，增加的數量加進Mall的數量，不影響bundle數量
  @Test
  void updWhQty_add_98Wh_singleNonShareMall_HKTV_hasBundle() {
    // 預期結果: 增加的數量加進Mall，不影響bundle數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.addWh98Child123Qty100200300();
    // child1 share = 0, mall = 393
    // child2 share = 0, mall = 373
    // child3 share = 0, mall = 553
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "393", "373", "553", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 設置非98倉庫數量，有NonShareMall，有bundle，request set > nonshare 總數，不會影響Mall單賣的跟組合的數量，都加到倉庫
  @Test
  void
      updWhQty_set_non98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_allNonShareQty() {
    // 預期結果: 不會影響nonShareMall數量，都加到共享庫存
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.setWh01Child123Qty433();
    // child1 share = 200, mall = 193
    // child2 share = 200, mall = 73
    // child3 share = 200, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "200", "200", "200", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 設置98倉庫數量，有NonShareMall，有bundle，request set > nonshare 總數，，增加的數量加進Mall單賣的數量，不影響bundle數量
  @Test
  void updWhQty_set_98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_allNonShareQty() {
    // 預期結果: 增加的數量加進Mall，不影響bundle數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.setWh98Child123Qty433();
    // child1 share = 0, mall = 393
    // child2 share = 0, mall = 273
    // child3 share = 0, mall = 353
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "393", "273", "353", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 設置非98倉庫數量，有NonShareMall，沒有bundle，request set > nonshare 總數，不會影響Mall單賣的數量，都加到倉庫
  @Test
  void
      updWhQty_set_non98Wh_singleNonShareMall_HKTV_nonBundle_requestQty_bigger_than_allNonShareQty() {
    // 預期結果: 不會影響nonShareMall數量，都加到共享庫存
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "233", "233", "233", "0", "0", "0", "0", "0", "0");
    this.setWh01Child123Qty433();
    // child1 share = 200, mall = 233
    // child2 share = 200, mall = 233
    // child3 share = 200, mall = 233
    this.assertion_wh_share_mall_parent(
        "01", "200", "200", "200", "233", "233", "233", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 設置LittleMall 倉庫數量，有NonShareMall，沒有bundle，request > nonShare + share
  @Test
  void updWhQty_set_non98Wh_singleNonShareMall_LM_nonBundle_requestQty_bigger_than_whQty() {
    // 預期結果: 不會影響nonShareMall數量，都加到共享庫存
    this.buildLMProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToLM();
    // share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.setWh01Child123Qty433();
    // child1 share = 200, mall = 233
    // child2 share = 200, mall = 233
    // child3 share = 200, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "200", "200", "200", "233", "233", "233");
    this.deleteProductInfoDto();
  }

  // 設置LittleMall 倉庫數量，有NonShareMall，沒有bundle，request > nonShare，request < nonShare + share
  @Test
  void
      updWhQty_set_non98Wh_singleNonShareMall_LM_nonBundle_requestQty_less_than_whQty_but_bigger_than_allNonShareQty() {
    // 預期結果: 不會影響nonShareMall數量，更改共享庫存
    this.buildLMProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToLM();
    // share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.setWh01Child123Qty300();
    // child1 share = 67, mall = 233
    // child2 share = 67, mall = 233
    // child3 share = 67, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "67", "67", "67", "233", "233", "233");
    this.deleteProductInfoDto();
  }

  // 設置LittleMall 倉庫數量，有NonShareMall，沒有bundle，request < nonShare
  @Test
  void updWhQty_set_non98Wh_singleNonShareMall_LM_nonBundle_requestQty_less_than_allNonShareQty() {
    // 預期結果: share歸零，nonShareMall數量等於request
    this.buildLMProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToLM();
    // share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.setWh01Child123Qty170();
    // child1 share = 0, mall = 170
    // child2 share = 0, mall = 170
    // child3 share = 0, mall = 170
    this.assertion_LM_wh_share_mall_parent("01", "0", "0", "0", "170", "170", "170");
    this.deleteProductInfoDto();
  }

  // 減少LittleMall 倉庫數量，有NonShareMall，沒有bundle，request > share + nonshare，拋錯，數量維持現狀
  @Test
  void
      updWhQty_deduct_non98Wh_singleNonShareMall_LM_nonBundle_requestQty_bigger_than_allNonShareQty_plus_shareQty() {
    // 預期結果: IIDS-2001，不影響倉庫或是nonshare數量
    this.buildLMProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToLM();
    // share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.deductWh01Child123Qty433();
    // child1 share = 100, mall = 233
    // child2 share = 100, mall = 233
    // child3 share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.deleteProductInfoDto();
  }

  // 減少LittleMall 倉庫數量，有NonShareMall，沒有bundle，request > share，request < share +
  // nonshare，share歸零nonshare扣差額
  @Test
  void
      updWhQty_deduct_non98Wh_singleNonShareMall_LM_nonBundle_requestQty_bigger_than_shareQty_but_less_than_nonShareQty() {
    this.buildLMProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToLM();
    // share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.deductWh01Child123Qty170();
    // child1 share = 0, mall = 163
    // child2 share = 0, mall = 163
    // child3 share = 0, mall = 163
    this.assertion_LM_wh_share_mall_parent("01", "0", "0", "0", "163", "163", "163");
    this.deleteProductInfoDto();
  }

  // 減少LittleMall 倉庫數量，有NonShareMall，沒有bundle，request < share，只扣share不影響nonshare數量
  @Test
  void updWhQty_deduct_non98Wh_singleNonShareMall_LM_nonBundle_requestQty_less_than_shareQty() {
    this.buildLMProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToLM();
    // share = 100, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "100", "100", "100", "233", "233", "233");
    this.deductWh01Child123Qty70();
    // child1 share = 30, mall = 233
    // child2 share = 30, mall = 233
    // child3 share = 30, mall = 233
    this.assertion_LM_wh_share_mall_parent("01", "30", "30", "30", "233", "233", "233");
    this.deleteProductInfoDto();
  }

  // 設置98倉庫數量，有NonShareMall，沒有bundle，request set > nonshare 總數，增加的數量加進Mall單賣的數量
  @Test
  void updWhQty_set_98Wh_singleNonShareMall_HKTV_nonBundle_requestQty_bigger_than_allNonShareQty() {
    // 預期結果: 增加的數量加進Mall
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "333", "333", "333", "0", "0", "0", "0", "0", "0");
    this.setWh98Child123Qty433();
    // child1 share = 0, mall = 433
    // child2 share = 0, mall = 433
    // child3 share = 0, mall = 433
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "433", "433", "433", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 設置非98倉庫數量，有NonShareMall，有bundle，request < nonshare 總數，request > bundle
  // 總數，倉庫共享數量歸零，單賣的商品數量等於request 減 bundle 總數，不影響bundle數量
  @Test
  void
      updWhQty_set_non98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_less_than_allNonShareQty_but_bigger_than_allBundleQty() {
    // 預期結果: 倉庫共享數量歸零，單賣的商品數量等於request 減 bundle 總數，不影響bundle數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.setWh01Child123Qty170();
    // child1 share = 0, mall = 130
    // child2 share = 0, mall = 10
    // child3 share = 0, mall = 90
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "0", "0", "0", "130", "10", "90", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 設置非98倉庫數量，有NonShareMall，有bundle，request < nonshare 總數，request < bundle
  // 總數，倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
  @Test
  void updWhQty_set_non98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_less_than_allBundleQty() {
    // 預期結果: 倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, nonBundle = 193, inBundle = 40
    // child2 share = 100, nonBundle = 73, inBundle = 160
    // child3 share = 100, nonBundle = 153, inBundle = 80
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.setWh01Child2Qty100();
    // child1 share = 100, mall = 193
    // child2 share = 0, mall = 0
    // child3 share = 100, mall = 153
    // parent1 = 10
    // parent2 = 10
    // parent3 = 10
    // parent4 = 4
    // parent5 = 2
    // parent6 = 2
    this.assertion_wh_share_mall_parent(
        "01", "100", "0", "100", "193", "0", "153", "10", "10", "10", "4", "2", "2");
    this.deleteProductInfoDto();
  }

  // 設置98倉庫數量，有NonShareMall，有bundle，request < nonshare 總數，request > bundle
  // 總數，倉庫共享數量歸零，單賣的商品數量等於request 減 bundle 總數，不影響bundle數量
  @Test
  void
      updWhQty_set_98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_less_than_allNonShareQty_but_bigger_than_allBundleQty() {
    // 預期結果: 倉庫共享數量歸零，單賣的商品數量等於request 減 bundle 總數，不影響bundle數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.setWh98Child123Qty170();
    // child1 share = 0, mall = 130
    // child2 share = 0, mall = 10
    // child3 share = 0, mall = 90
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "130", "10", "90", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 設置98倉庫數量，有NonShareMall，有bundle，request < nonshare 總數，request < bundle
  // 總數，倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
  @Test
  void updWhQty_set_98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_less_than_allBundleQty() {
    // 預期結果: 倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.setWh98Child2Qty100();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 0
    // child3 share = 0, mall = 253
    // parent1 = 10
    // parent2 = 10
    // parent3 = 10
    // parent4 = 4
    // parent5 = 2
    // parent6 = 2
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "0", "253", "10", "10", "10", "4", "2", "2");
    this.deleteProductInfoDto();
  }

  @Test
  void updWhQty_set_non98Wh_qty_to_one_singleNonShareMall_HKTV_hasBundle() {
    // 預期結果: 數量完全歸零，拆掉的bundle要把其他的child還回單賣的數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, nonBundle = 193, inBundle = 40
    // child2 share = 100, nonBundle = 73, inBundle = 160
    // child3 share = 100, nonBundle = 153, inBundle = 80
    // parent123456 = 10
    //     this.setWh01Child1Qty1();
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.setWh01Child2Qty1();
    //     this.setWh01Child3Qty1();
    // child1 share = 100, mall = 233
    // child2 share = 0, mall = 1
    // child3 share = 100, mall = 233
    // parent123456 = 0
    this.assertion_wh_share_mall_parent(
        "01", "100", "0", "100", "233", "1", "233", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  private void setWh01Child2Qty1() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 1\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void updWhQty_set_98Wh_qty_to_one_singleNonShareMall_HKTV_hasBundle() {
    // 預期結果: 數量完全歸零，拆掉的bundle要把其他的child還回單賣的數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.setWh98Child2Qty1();
    // child1 share = 0, mall = 333
    // child2 share = 0, mall = 1
    // child3 share = 0, mall = 333
    // parent123456 = 0
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "333", "1", "333", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 設置98倉庫數量，有NonShareMall，沒有bundle，request < nonshare 總數，倉庫共享數量歸零，單賣的商品數量等於request
  @Test
  void updWhQty_set_98Wh_singleNonShareMall_HKTV_nonBundle_requestQty_less_than_allNonShareQty() {
    // 預期結果: 倉庫共享數量歸零，單賣的商品數量等於request
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "333", "333", "333", "0", "0", "0", "0", "0", "0");
    this.setWh98Child2Qty1();
    // child1 share = 0, mall = 333
    // child2 share = 0, mall = 1
    // child3 share = 0, mall = 333
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "333", "1", "333", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 減少非98倉庫數量，有NonShareMall，沒有bundle，request > (nonshare + share)，IIDS-2001，不影響倉庫或是nonshare數量
  @Test
  void
      updWhQty_deduct_non98Wh_singleNonShareMall_HKTV_nonBundle_requestQty_bigger_than_allNonShareQty_plus_shareQty() {
    // 預期結果: IIDS-2001，不影響倉庫或是nonshare數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "233", "233", "233", "0", "0", "0", "0", "0", "0");
    this.deductWh01Child123Qty433();
    // child1 share = 100, mall = 233
    // child2 share = 100, mall = 233
    // child3 share = 100, mall = 233
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "233", "233", "233", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 減少非98倉庫數量，有NonShareMall，有bundle，request > (nonBundle + bundle +
  // share)，IIDS-2001，不影響倉庫或是nonshare數量
  @Test
  void
      updWhQty_deduct_non98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_allNonShareQty_plus_shareQty() {
    // 預期結果: IIDS-2001，不影響倉庫或是nonshare單賣的或bundle數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.deductWh01Child123Qty433();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 減少非98倉庫數量，有NonShareMall，有bundle，request > share，(nonBundle + share) >
  // request，倉庫共享數量歸零，nonBundle數量調整，不影響bundle數量
  @Test
  void
      updWhQty_deduct_non98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_shareQty_but_less_than_shareQty_plus_nonBundleQty() {
    // 預期結果: 倉庫共享數量歸零，nonBundle數量調整，不影響bundle數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.deductWh01Child123Qty170();
    // child1 share = 0, mall = 123
    // child2 share = 0, mall = 3
    // child3 share = 0, mall = 83
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "01", "0", "0", "0", "123", "3", "83", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 減少非98倉庫數量，有NonShareMall，有bundle，request > (nonBundle + share)，(nonBundle + bundle + share) >
  // request，倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
  @Test
  void
      updWhQty_deduct_non98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_shareQty_plus_nonBundleQty_but_less_than_shareQty_plus_allNonShareQty() {
    // 預期結果: 倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
    this.buildHKTVProductInfoDto("01");
    this.setWh01Child123Qty333();
    this.setChild123Qty233ToMall();
    // share = 100, mall = 233
    this.setAllBundleQtyTo10();
    // child1 share = 100, mall = 193
    // child2 share = 100, mall = 73
    // child3 share = 100, mall = 153
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "01", "100", "100", "100", "193", "73", "153", "10", "10", "10", "10", "10", "10");
    this.deductWh01Child2Qty273();
    // child1 share = 100, mall = 201
    // child2 share = 0, mall = 0
    // child3 share = 100, mall = 169
    // parent1 = 9
    // parent2 = 7
    // parent3 = 7
    // parent4 = 0
    // parent5 = 0
    // parent6 = 0
    this.assertion_wh_share_mall_parent(
        "01", "100", "0", "100", "201", "0", "169", "9", "7", "7", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 減少98倉庫數量，有NonShareMall，沒有bundle，request > nonshare (share數量是0)，IIDS-2001，不影響倉庫或是nonshare數量
  @Test
  void updWhQty_deduct_98Wh_singleNonShareMall_HKTV_nonBundle_requestQty_bigger_than_nonShareQty() {
    // 預期初始: share數量是0
    // 預期結果: IIDS-2001，不影響倉庫或是nonshare數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "333", "333", "333", "0", "0", "0", "0", "0", "0");
    this.deductWh98Child123Qty433();
    // share = 0, mall = 333
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "333", "333", "333", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  // 減少98倉庫數量，有NonShareMall，有bundle，request > nonBundle，(bundle + nonBundle) >
  // request，倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
  @Test
  void
      updWhQty_deduct_98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_nonBundleQty_but_less_than_allNonShareQty() {
    // 預期初始: share數量是0
    // 預期結果: 倉庫共享數量歸零，以維持bundle種類最多的情況拆bundle，剩下無法組成Bundle的數量是單賣的商品數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.deductWh98Child123Qty183();
    // child1 share = 0, mall = 110
    // child2 share = 0, mall = 1
    // child3 share = 0, mall = 70
    // parent1 = 10
    // parent2 = 10
    // parent3 = 10
    // parent4 = 9
    // parent5 = 8
    // parent6 = 9
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "110", "1", "70", "10", "10", "10", "9", "8", "9");
    this.deleteProductInfoDto();
  }

  // 減少98倉庫數量，有NonShareMall，有bundle，request > (bundle + nonBundle)，IIDS-2001，不影響倉庫或是nonshare數量
  @Test
  void
      updWhQty_deduct_98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_bigger_than_allNonShareQty() {
    // 預期初始: share數量是0
    // 預期結果: IIDS-2001，不影響倉庫或是nonshare數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.deductWh98Child123Qty433();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  // 減少98倉庫數量，有NonShareMall，有bundle，request < nonBundle，不影響bundle數量
  @Test
  void updWhQty_deduct_98Wh_singleNonShareMall_HKTV_hasBundle_requestQty_less_than_nonBundleQty() {
    // 預期初始: share數量是0
    // 預期結果: nonBundle數量調整，不影響bundle數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");
    this.deductWh98Child123Qty170();
    // child1 share = 0, mall = 123
    // child2 share = 0, mall = 3
    // child3 share = 0, mall = 83
    // parent123456 = 10
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "123", "3", "83", "10", "10", "10", "10", "10", "10");
    this.deleteProductInfoDto();
  }

  private void buildHKTVProductInfoDto(String warehouseSeqNo) {
    // 清除資料
    this.deleteProductInfoDto();

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0001(warehouseSeqNo));
    AssertUtil.wait_1_sec();

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0002(warehouseSeqNo));
    AssertUtil.wait_1_sec();

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0003(warehouseSeqNo));
    AssertUtil.wait_1_sec();

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0004(warehouseSeqNo));
    AssertUtil.wait_1_sec();

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0005(warehouseSeqNo));
    AssertUtil.wait_1_sec();

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0006(warehouseSeqNo));
    AssertUtil.wait_1_sec();
  }

  private void deleteProductInfoDto() {
    // 清除資料
    for (int i = 1; i < 7; i++) {
      String uuid = "test-BundleParent-000" + i + "-000" + i + "-000" + i;
      String skuId = "H0121001_S_P000" + i;
      redisTempl.delete(skuId).block();
      redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + uuid, uuid).block();
      redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + uuid, uuid).block();
      redisLMTempl.delete(uuid).block();
      redisLMTempl.delete(skuId).block();
    }

    for (int i = 1; i < 4; i++) {
      String uuid = "childUuid-" + i;
      String skuId = "childSku-" + i;
      String storeSkuId = "child_S_" + skuId;
      redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + uuid, uuid).block();
      redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + uuid, uuid).block();
      redisTempl.delete(uuid).block();
      redisTempl.delete(storeSkuId).block();
    }
  }

  private void setWh01Child123Qty333() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 333\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 333\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 333\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh98Child123Qty333() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 333\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 333\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 333\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setChild123Qty233ToMall() {
    // set childUuid1/2/3 qty=20 to mall
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"stockLevels\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 233,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"stockLevels\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 233,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"stockLevels\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 233,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setAllBundleQtyTo10() {
    // 建立每個 parentBundle qty=10
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\"test-BundleParent-0001-0001-0001\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    AssertUtil.wait_1_sec();

    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\"test-BundleParent-0002-0002-0002\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    AssertUtil.wait_1_sec();

    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\"test-BundleParent-0003-0003-0003\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    AssertUtil.wait_1_sec();

    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\"test-BundleParent-0004-0004-0004\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    AssertUtil.wait_1_sec();

    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\"test-BundleParent-0005-0005-0005\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    AssertUtil.wait_1_sec();

    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\"test-BundleParent-0006-0006-0006\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void addWh01Child123Qty100200300() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"quantity\": 100\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"quantity\": 200\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"quantity\": 300\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void addWh98Child123Qty100200300() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"quantity\": 100\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"quantity\": 200\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"quantity\": 300\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh01Child123Qty433() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh01Child123Qty300() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 300\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 300\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 300\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh98Child123Qty433() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh01Child123Qty170() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh98Child123Qty170() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh01Child2Qty100() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 100\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh98Child2Qty100() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 100\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void setWh98Child2Qty1() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 1\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh01Child123Qty433() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh01Child123Qty170() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh98Child123Qty433() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 433\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh98Child123Qty183() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 183\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 183\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 183\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh01Child2Qty273() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 273\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh98Child123Qty170() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 170\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  // 驗證拆bundle後的順序是依照child比例，愈小的愈先補，比例一樣是updatetime愈大的先補
  @Test
  void updWhQty_set_98Wh_qty_to_3_singleNonShareMall_HKTV_hasBundle() {
    // 預期結果: 數量完全歸零，拆掉的bundle要把其他的child還回單賣的數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293
    // child2 share = 0, mall = 173
    // child3 share = 0, mall = 253
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");

    Thread t1 =
        new Thread(
            () -> {
              try {
                URL url = new URL(BASIC_URL + "/warehouse/quantity");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload =
                    "[\n"
                        + "  {\n"
                        + "    \"uuid\": \"childUuid-1\",\n"
                        + "    \"warehouseQty\": [\n"
                        + "      {\n"
                        + "        \"warehouseSeqNo\": \"98\",\n"
                        + "        \"mode\": \"set\",\n"
                        + "        \"quantity\": 3\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "]";
                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = payload.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  System.out.println("Response: " + response.toString());
                }
                connection.disconnect();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });

    Thread t2 =
        new Thread(
            () -> {
              try {
                URL url = new URL(BASIC_URL + "/warehouse/quantity");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload =
                    "[\n"
                        + "  {\n"
                        + "    \"uuid\": \"childUuid-2\",\n"
                        + "    \"warehouseQty\": [\n"
                        + "      {\n"
                        + "        \"warehouseSeqNo\": \"98\",\n"
                        + "        \"mode\": \"set\",\n"
                        + "        \"quantity\": 3\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "]";

                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = payload.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  System.out.println("Response: " + response.toString());
                }
                connection.disconnect();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });

    Thread t3 =
        new Thread(
            () -> {
              try {
                // Specify the URL for the PUT request
                URL url = new URL(BASIC_URL + "/warehouse/quantity");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload =
                    "[\n"
                        + "  {\n"
                        + "    \"uuid\": \"childUuid-3\",\n"
                        + "    \"warehouseQty\": [\n"
                        + "      {\n"
                        + "        \"warehouseSeqNo\": \"98\",\n"
                        + "        \"mode\": \"set\",\n"
                        + "        \"quantity\": 3\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "]";

                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = payload.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  System.out.println("Response: " + response.toString());
                }
                connection.disconnect();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });

    t1.start();
    AssertUtil.wait_1_sec();
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "1", "220", "293", "0", "1", "10", "10", "10", "10");
    t3.start();
    AssertUtil.wait_1_sec();
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "1", "250", "3", "0", "1", "0", "10", "10", "10");
    t2.start();
    AssertUtil.wait_1_sec();
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "1", "0", "3", "0", "1", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  private void assertion_wh_share_mall_parent(
      String sq,
      String share1,
      String share2,
      String share3,
      String mall1,
      String mall2,
      String mall3,
      String parent1,
      String parent2,
      String parent3,
      String parent4,
      String parent5,
      String parent6) {
    Assertions.assertEquals(
        share1,
        redisTempl.<String, String>opsForHash().get("inventory:childUuid-1", sq + "_qty").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("inventory:childUuid-1", "hktv_instockstatus")
            .block());
    Assertions.assertEquals(
        share2,
        redisTempl.<String, String>opsForHash().get("inventory:childUuid-2", sq + "_qty").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("inventory:childUuid-2", "hktv_instockstatus")
            .block());
    Assertions.assertEquals(
        share3,
        redisTempl.<String, String>opsForHash().get("inventory:childUuid-3", sq + "_qty").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("inventory:childUuid-3", "hktv_instockstatus")
            .block());
    Assertions.assertEquals(
        mall1,
        redisTempl
            .<String, String>opsForHash()
            .get("child_S_childSku-1", "child" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("child_S_childSku-1", "child" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        mall2,
        redisTempl
            .<String, String>opsForHash()
            .get("child_S_childSku-2", "child" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("child_S_childSku-2", "child" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        mall3,
        redisTempl
            .<String, String>opsForHash()
            .get("child_S_childSku-3", "child" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("child_S_childSku-3", "child" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        parent1,
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0001", "H0121001" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0001", "H0121001" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        parent2,
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0002", "H0121001" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0002", "H0121001" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        parent3,
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0003", "H0121001" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0003", "H0121001" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        parent4,
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0004", "H0121001" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0004", "H0121001" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        parent5,
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0005", "H0121001" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0005", "H0121001" + sq + "_instockstatus")
            .block());
    Assertions.assertEquals(
        parent6,
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0006", "H0121001" + sq + "_available")
            .block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("H0121001_S_P0006", "H0121001" + sq + "_instockstatus")
            .block());
  }

  private void buildLMProductInfoDto(String warehouseSeqNo) {
    // 清除資料
    this.deleteProductInfoDto();
    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcaseLM(warehouseSeqNo));
    AssertUtil.wait_1_sec();
  }

  private void setChild123Qty233ToLM() {
    // set childUuid1/2/3 qty=20 to mall
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"stockLevels\": [\n"
                + "      {\n"
                + "        \"mall\": \"little_mall\",\n"
                + "        \"qty\": 233,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"stockLevels\": [\n"
                + "      {\n"
                + "        \"mall\": \"little_mall\",\n"
                + "        \"qty\": 233,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"stockLevels\": [\n"
                + "      {\n"
                + "        \"mall\": \"little_mall\",\n"
                + "        \"qty\": 233,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void deductWh01Child123Qty70() {
    // set childUuid1/2/3 qty=233 to share
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-1\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 70\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 70\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 70\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  private void assertion_LM_wh_share_mall_parent(
      String sq,
      String share1,
      String share2,
      String share3,
      String mall1,
      String mall2,
      String mall3) {
    Assertions.assertEquals(
        share1,
        redisTempl.<String, String>opsForHash().get("inventory:childUuid-1", sq + "_qty").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("inventory:childUuid-1", "little_mall_instockstatus")
            .block());
    Assertions.assertEquals(
        share2,
        redisTempl.<String, String>opsForHash().get("inventory:childUuid-2", sq + "_qty").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("inventory:childUuid-2", "little_mall_instockstatus")
            .block());
    Assertions.assertEquals(
        share3,
        redisTempl.<String, String>opsForHash().get("inventory:childUuid-3", sq + "_qty").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl
            .<String, String>opsForHash()
            .get("inventory:childUuid-3", "little_mall_instockstatus")
            .block());
    Assertions.assertEquals(
        mall1, redisLMTempl.<String, String>opsForHash().get("childUuid-1", "quantity").block());
    Assertions.assertEquals(
        "notSpecified",
        redisLMTempl.<String, String>opsForHash().get("childUuid-1", "instockstatus").block());
    Assertions.assertEquals(
        mall2, redisLMTempl.<String, String>opsForHash().get("childUuid-2", "quantity").block());
    Assertions.assertEquals(
        "notSpecified",
        redisLMTempl.<String, String>opsForHash().get("childUuid-2", "instockstatus").block());
    Assertions.assertEquals(
        mall3, redisLMTempl.<String, String>opsForHash().get("childUuid-3", "quantity").block());
    Assertions.assertEquals(
        "notSpecified",
        redisLMTempl.<String, String>opsForHash().get("childUuid-3", "instockstatus").block());
  }

  @Test
  void updWhQty_set_98Wh_qty_to_1_singleNonShareMall_HKTV_hasBundle_With_SAME_TIME_REQUEST() {
    // 預期結果: 數量完全歸零，拆掉的bundle要把其他的child還回單賣的數量
    this.buildHKTVProductInfoDto("98");
    this.setWh98Child123Qty333();
    // share = 0, mall = 333
    this.setAllBundleQtyTo10();
    // child1 share = 0, mall = 293, inBundle = 40
    // child2 share = 0, mall = 173, inBundle = 160
    // child3 share = 0, mall = 253, inBundle = 80
    // parent123456 = 10
    // 40 160 80 在 parent
    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "293", "173", "253", "10", "10", "10", "10", "10", "10");

    Thread t1 =
        new Thread(
            () -> {
              try {
                URL url = new URL(BASIC_URL + "/warehouse/quantity");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload =
                    "[\n"
                        + "  {\n"
                        + "    \"uuid\": \"childUuid-1\",\n"
                        + "    \"warehouseQty\": [\n"
                        + "      {\n"
                        + "        \"warehouseSeqNo\": \"98\",\n"
                        + "        \"mode\": \"set\",\n"
                        + "        \"quantity\": 1\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "]";
                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = payload.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  System.out.println("Response: " + response.toString());
                }
                connection.disconnect();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });

    Thread t2 =
        new Thread(
            () -> {
              try {
                URL url = new URL(BASIC_URL + "/warehouse/quantity");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload =
                    "[\n"
                        + "  {\n"
                        + "    \"uuid\": \"childUuid-2\",\n"
                        + "    \"warehouseQty\": [\n"
                        + "      {\n"
                        + "        \"warehouseSeqNo\": \"98\",\n"
                        + "        \"mode\": \"set\",\n"
                        + "        \"quantity\": 1\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "]";

                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = payload.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  System.out.println("Response: " + response.toString());
                }
                connection.disconnect();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });

    Thread t3 =
        new Thread(
            () -> {
              try {
                // Specify the URL for the PUT request
                URL url = new URL(BASIC_URL + "/warehouse/quantity");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload =
                    "[\n"
                        + "  {\n"
                        + "    \"uuid\": \"childUuid-3\",\n"
                        + "    \"warehouseQty\": [\n"
                        + "      {\n"
                        + "        \"warehouseSeqNo\": \"98\",\n"
                        + "        \"mode\": \"set\",\n"
                        + "        \"quantity\": 1\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "]";

                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = payload.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  System.out.println("Response: " + response.toString());
                }
                connection.disconnect();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });

    t1.start();
    t3.start();
    t2.start();

    AssertUtil.wait_10_sec();

    this.assertion_wh_share_mall_parent(
        "98", "0", "0", "0", "1", "1", "1", "0", "0", "0", "0", "0", "0");
    this.deleteProductInfoDto();
  }

  @Test
  public void updWhQty_lockTimeNotBlockWhenRecordTimeBiggerThanNow() {
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String parentSku = "H088800118_S_parent-SKU-E-1";
    String child1Uuid = "child-UUID-E-1";
    String child2Uuid = "child-UUID-E-2";
    String parentUuid = "parent-E-001";
    String parentSetting =
        "{\"is_reserved\":true,\"is_active\":false,\"priority\":0,\"bundle_mall_info\":[{\"mall\":\"hktv\",\"alert_qty\":100,\"ceiling_qty\":100}],\"bundle_child_info\":[{\"uuid\":\"child-UUID-E-1\",\"sku_id\":\"child-SKU-E-1\",\"storefront_store_code\":\"H088800118\",\"sku_qty\":1,\"ceiling_qty\":0,\"is_loop\":false},{\"uuid\":\"child-UUID-E-2\",\"sku_id\":\"child-SKU-E-2\",\"storefront_store_code\":\"H088800118\",\"sku_qty\":2,\"ceiling_qty\":0,\"is_loop\":false}]}";

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
    redisUtil.deleteBundleLockParentRedisKey();

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);
    redisUtil.insertBundleLockParentData_withSec(parentSku, 7);

    // testing api
    apiUtil.callDeductWh4700QtyApi(child1Uuid);

    // verify
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "7000", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "100", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
    redisUtil.deleteBundleLockParentRedisKey();
  }

  @Test
  public void updWhQty_setChildQtyWillConsiderParentQty() {
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String parentSku = "H088800118_S_parent-SKU-E-1";
    String child1Uuid = "child-UUID-E-1";
    String child2Uuid = "child-UUID-E-2";
    String parentUuid = "parent-E-001";
    String parentSetting =
        "{\"is_reserved\":true,\"is_active\":false,\"priority\":0,\"bundle_mall_info\":[{\"mall\":\"hktv\",\"alert_qty\":100,\"ceiling_qty\":100}],\"bundle_child_info\":[{\"uuid\":\"child-UUID-E-1\",\"sku_id\":\"child-SKU-E-1\",\"storefront_store_code\":\"H088800118\",\"sku_qty\":1,\"ceiling_qty\":0,\"is_loop\":false},{\"uuid\":\"child-UUID-E-2\",\"sku_id\":\"child-SKU-E-2\",\"storefront_store_code\":\"H088800118\",\"sku_qty\":2,\"ceiling_qty\":0,\"is_loop\":false}]}";

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
    redisUtil.deleteBundleLockParentRedisKey();

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callSetWh4900QtyApi(child2Uuid);

    //     verify
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "100", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
    redisUtil.deleteBundleLockParentRedisKey();
  }

  @Test
  public void updWhQty_mallNotInWh98AndAddWh98QtyWillAddInWh() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing api
    apiUtil.addWh98Qty2400(uuid);

    //     verify
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:" + uuid, "98_qty").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  public void updWhQty_mallInWh98AndAddWh98QtyWillAddInMall() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");

    // testing api
    apiUtil.addWh98Qty2400(uuid);

    //     verify
    Assertions.assertEquals(
        "4800", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:" + uuid, "98_qty").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }
}
