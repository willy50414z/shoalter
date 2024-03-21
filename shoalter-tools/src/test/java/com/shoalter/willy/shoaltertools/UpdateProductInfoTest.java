package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;

import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import com.shoalter.willy.shoaltertools.testtool.BuildDtoUtil;
import com.shoalter.willy.shoaltertools.testtool.RabbitMqUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import com.shoalter.willy.shoaltertools.testtool.updateproductinfo.UpdateProductInfoTestTool;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
public class UpdateProductInfoTest extends UpdateProductInfoTestTool {
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
  @Autowired private RabbitMqUtil rabbitMqUtil;
  @Autowired private ApiUtil apiUtil;

  @Test
  void updateProduct_EditProductTestCase0010() {
    String uuid = "iids-integration-test-testcase-0005";
    String sku = "iims-integration-test-testcase-0005";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0010(uuid, sku));

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0010(uuid, sku));

    // update更新sku且mall移動到別的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0014() {
    String uuid = "iids-integration-test-testcase-0008";
    String sku = "iims-integration-test-testcase-0008";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0014(uuid, sku));
    AssertUtil.wait_2_sec();

    // setting init value
    apiUtil.callSetWh01Qty20AndWh02Qty30(uuid);
    apiUtil.callSetHktvAndLittleMallToShare(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0014(uuid, sku));

    // update移除share mall
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0015() {
    String uuid = "iids-integration-test-testcase-0009";
    String sku = "iims-integration-test-testcase-0009";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0015(uuid, sku));

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0015(uuid, sku));

    // update移動mall到有share數量的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0016() {
    String uuid = "iids-integration-test-testcase-0010";

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0016(uuid));

    // setting init value
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

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0016(uuid));

    // update移除non-share mall
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0018() {
    String uuid = "iids-integration-test-testcase-0012";
    String sku = "iims-integration-test-testcase-0012";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0018(uuid, sku));

    // setting init value
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

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0018(uuid, sku));

    // update移動mall到既有的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0019() {
    String uuid = "iids-integration-test-testcase-0013";
    String sku = "iims-integration-test-testcase-0013";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0019(uuid, sku));

    // setting init value
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

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0019(uuid, sku));

    // update刪除warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0020() {
    String uuid = "iids-integration-test-testcase-0014";
    String sku = "iims-integration-test-testcase-0014";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0020(uuid, sku));

    // setting init value
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

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0020(uuid, sku));

    // delete刪除整筆uuid
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0021() {
    String uuid = "iids-integration-test-testcase-0015";
    String sku = "iims-integration-test-testcase-0015";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0021(uuid, sku));

    // setting init value
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

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0021(uuid, sku));

    // delete部份成功，部分失敗
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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

  @Test
  void updateProduct_EditProductTestCase0022() {
    String uuid = "iids-integration-test-testcase-0016";
    String sku = "iims-integration-test-testcase-0016";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000102");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_EditProductTestCase0022(uuid, sku));

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_EditProductTestCase0022(uuid, sku));

    // delete缺少uuid
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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
  void updateProduct_testcase0013() {
    String uuid = "iids-integration-test-testcase-0030";
    String sku = "iims-integration-test-testcase-0030";
    String updEventKey01 = buildExpectedUpdateEventKey(sku, "H0000101");
    String updEventKey98 = buildExpectedUpdateEventKey(sku, "H0000198");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey01, updEventKey98).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_testcase0013(uuid, sku));

    // setting init value
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

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_testcase0013_moveOut98(uuid, sku));

    // update移動mall到有share數量的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

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
    rabbitMqUtil.sendMsgToIidsQueue(updateProductInfoDto_testcase0013_moveIn98(uuid, sku));

    // update移動mall到有share數量的warehouse
    AssertUtil.wait_2_sec();

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

  private static String buildExpectedUpdateEventKey(String sku, String warehouseId) {
    String updateKey = sku + "|||" + warehouseId;
    return "evtq_part_stockdata_" + Math.abs(updateKey.hashCode() % 10);
  }

  private ProductInfoDto move_HKTV_to_wh(String seqNo, String uuid, String sku) {
    return BuildDtoUtil.buildUpdateProductInfoDto(seqNo, uuid, sku);
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
    // verify_putProductInfo_HKTV_wh01_to_wh98_qty_keep_in_wh01
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    // verify_putProductInfo_HKTV_wh98_to_wh02_qty_keep_in_wh98_and_wh01_qty_move_to_wh02
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());

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
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());

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
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011816_available").block());

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
    // verify_putProductInfo_hktv_wh01_To_wh02_qty_will_move
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011803_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011803_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011803_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011816_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011803_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011816_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011802_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011816_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011803_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011816_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "17_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "17_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011817_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011816_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011803_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011816_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011803_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011803_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011816_available").block());

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
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011802_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011801_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011816_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011898_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011815_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());

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
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(sku, "H08880011815_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(sku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }
}
