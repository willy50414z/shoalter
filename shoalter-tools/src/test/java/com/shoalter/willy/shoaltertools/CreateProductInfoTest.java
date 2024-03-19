package com.shoalter.willy.shoaltertools;

import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import com.shoalter.willy.shoaltertools.testtool.RabbitMqUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import com.shoalter.willy.shoaltertools.testtool.createproductInfo.CreateProductInfoTestTool;
import java.util.ArrayList;
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
public class CreateProductInfoTest extends CreateProductInfoTestTool {
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

  // 驗證正常create product後redis裡的資料
  @Test
  void createProduct_testcase0001() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0001";
    String sku = "iims-integration-test-testcase-0001";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_testcase0001(uuid, sku));

    Thread.sleep(1000L);

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

  // EditProduct case 2
  @Test
  void createProduct_testcase0002() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0002";
    String sku = "iims-integration-test-testcase-0002";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid, sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_testcase0002(uuid, sku));

    Thread.sleep(1000L);

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

  // EditProduct case 3
  @Test
  void createProduct_testcase0003() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0003";
    String sku = "iims-integration-test-testcase-0003";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000109");

    redisTempl.delete("inventory:" + uuid, uuid, sku, updEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_testcase0003(uuid, sku));

    Thread.sleep(1000L);

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
        buildExpectedHktvStockLevel("H0000109", "0", "notSpecified", "0", uuid, time),
        redisTempl
            .<String, String>opsForHash()
            .entries(sku)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H0000109_updatestocktime")) {
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
            .contains(buildExpectedUpdateEventValue(sku, "H0000109")));

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
  }

  // EditProduct case 4
  @Test
  void createProduct_testcase0004() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0004";
    String sku = "iims-integration-test-testcase-0004";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid, sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(buildProductInfoDto_testcase0004(uuid, sku));

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
  void putProductInfo_createParent_childWhNotMatch() {
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String parentSku = "H088800118_S_parent-SKU-E-1";
    String child1Uuid = "child-UUID-E-1-1";
    String child2Uuid = "child-UUID-E-2-2";
    String parentUuid = "parent-E-001-1";

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "02");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(getCreateParentRabbitMqMsg());

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        new ArrayList<>(), redisTempl.opsForHash().entries(parentSku).collectList().block());
    Assertions.assertEquals(
        new ArrayList<>(), redisTempl.opsForHash().entries(parentUuid).collectList().block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
  }
}
