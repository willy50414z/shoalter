package com.shoalter.willy.shoaltertools.testtool.updateproductinfo;

import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VerifyUpdateProductInfoTestCase {

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisLMTemplate")
  ReactiveRedisTemplate<String, String> redisLMTempl;

  @Autowired private UpdateProductInfoTestTool updProdInfoTestTool;

  public void updateProduct_EditProductTestCase0010(String uuid, String sku, String updEventKey) {
    // update更新sku且mall移動到別的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0001(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000101", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證LM資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
  }

  public void updateProduct_EditProductTestCase0014(
      String uuid, String sku, String updEventKey, String newUpdEventKey) {
    // update移除share mall
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0004(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000102", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證LM資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedLMStockLevel("30", "notSpecified", "1", time),
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
  }

  public void updateProduct_EditProductTestCase0015(
      String uuid, String sku, String updEventKey, String newUpdEventKey) {
    // update移動mall到有share數量的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0005(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000102", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證LM資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
  }

  public void updateProduct_EditProductTestCase0016(String uuid) {
    // update移除non-share mall
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0006(time),
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
        updProdInfoTestTool.buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
  }

  public void updateProduct_EditProductTestCase0018(
      String uuid, String sku, String updEventKey, String newUpdEventKey) {
    // update移動mall到既有的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0008(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000102", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000102")));
  }

  public void updateProduct_EditProductTestCase0019(String uuid, String sku, String updEventKey) {
    // update刪除warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0009(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000101", "150", "notSpecified", "1", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證LM資料不存在
    Assertions.assertFalse(redisLMTempl.hasKey(uuid).block());
  }

  public void updateProduct_EditProductTestCase0020(String uuid, String sku, String updEventKey) {
    // delete刪除整筆uuid
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0010(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000101", "50", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證LM資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
  }

  public void updateProduct_EditProductTestCase0021(
      String uuid, String sku, String updEventKey, String newUpdEventKey) {
    // delete部份成功，部分失敗
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0011(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000102", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證LM資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedLMStockLevel("0", "notSpecified", "0", time),
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
  }

  public void updateProduct_EditProductTestCase0022(
      String uuid, String sku, String updEventKey, String newUpdEventKey) {
    // delete缺少uuid
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0012(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000101", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000102")));

    // 驗證HKTV updateEvent資料
    Assertions.assertTrue(
        redisTempl
            .opsForList()
            .range(newUpdEventKey, 0, -1)
            .switchIfEmpty(Mono.just(""))
            .collectList()
            .block()
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));
  }

  public void updateProduct_testcase0013_moveOut98(String uuid, String sku, String updEventKey01) {
    // update移動mall到有share數量的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0013_moveOut98(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000101", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000101")));
  }

  public void updateProduct_testcase0013_moveIn98(String uuid, String sku, String updEventKey98) {
    // update移動mall到有share數量的warehouse
    AssertUtil.wait_2_sec();

    String time = "20231207134648";

    // 驗證IIDS資料
    Assertions.assertEquals(
        updProdInfoTestTool.buildExpectedStockLevel_testcase0013_moveIn98(sku, time),
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
        updProdInfoTestTool.buildExpectedHktvStockLevel(
            "H0000198", "0", "notSpecified", "0", uuid, time),
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
            .contains(updProdInfoTestTool.buildExpectedUpdateEventValue(sku, "H0000198")));
  }

  public void moveHktvFromMerchantTo3PLInventory() {
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
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
  }

  public void moveHktvFrom3PLToMerchantInventory() {
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
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void moveHktvFromMerchantToConsignmentInventory() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011815_available")
            .block());
  }

  public void moveHktvFromConsignmentToMerchantInventory() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011815_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void moveHktvFrom3PLToConsignmentInventory() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011815_available")
            .block());
  }

  public void moveHktvFromConsignmentTo3PLInventory() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011815_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011815_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011816_available")
            .block());
  }

  public void moveHktvFromMerchantToMerchantInventory() {
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
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void moveHktvFromMerchantToMerchantInventory_iimsIsOtherMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromMerchantToMerchantInventory_iimsIsConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromMerchantToMerchantInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromMerchantTo3PLInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFromMerchantTo3PLInventory_iimsIsConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFromMerchantToConsignmentInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFromMerchantToConsignmentInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFromMerchantToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFromConsignmentToMerchantInventory_iimsIsOtherMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromConsignmentToMerchantInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromConsignmentToMerchantInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "17_mall").block());
  }

  public void moveHktvFromConsignmentTo3PLInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFromConsignmentTo3PLInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFrom3PLToMerchantInventory_iimsIsOtherMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
  }

  public void moveHktvFrom3PLToMerchantInventory_iimsIsConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
  }

  public void moveHktvFrom3PLToConsignmentInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFrom3PLToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void hktv_wh01_to_wh02_but_iims_already_in_wh02() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "2400",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void hktv_wh01_to_wh15_but_iims_already_in_wh15() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void hktv_wh01_to_wh98_but_iims_already_in_wh98() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void hktv_wh15_to_wh01_but_iims_already_in_wh01() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
  }

  public void hktv_wh15_to_wh16_but_iims_already_in_wh16() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
  }

  public void hktv_wh15_to_wh98_but_iims_already_in_wh98() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void hktv_wh98_to_wh01_but_iims_already_in_wh01() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
  }

  public void hktv_wh98_to_wh15_but_iims_already_in_wh15() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }
}
