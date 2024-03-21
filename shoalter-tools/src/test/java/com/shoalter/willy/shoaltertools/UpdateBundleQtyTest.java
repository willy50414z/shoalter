package com.shoalter.willy.shoaltertools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import com.shoalter.willy.shoaltertools.testtool.RabbitMqUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import com.shoalter.willy.shoaltertools.testtool.SystemConstants;
import com.shoalter.willy.shoaltertools.testtool.updbundleqty.UpdateBundleQtyTestTool;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
public class UpdateBundleQtyTest extends UpdateBundleQtyTestTool {

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisLMTemplate")
  ReactiveRedisTemplate<String, String> redisLMTempl;

  @Autowired
  @Qualifier("redisHKTVTemplate")
  ReactiveRedisTemplate<String, String> redisHKTVTempl;

  @Autowired private static ObjectMapper objectMapper;
  @Autowired private RabbitTemplate defaultRabbitTemplate;
  @Autowired private RedisUtil redisUtil;
  @Autowired private ApiUtil apiUtil;
  @Autowired private RabbitMqUtil rabbitMqUtil;

  @Test
  public void test1_addBundle_deductEnough() {
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

    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0001());
    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0002());
    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0003());
    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0004());
    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0005());
    rabbitMqUtil.sendMsgToIidsQueue(buildBundleProductInfoDto_testcase0006());
    AssertUtil.wait_10_sec();

    apiUtil.callSetChildUuid123QtyEqual233ToShare();
    apiUtil.callSetChildUuid123Qty233ToMall();

    // 驗證資料正確 所有child qty都是 233
    Map<String, Map<String, String>> resultMap = case1AssertionExpectation();
    Map<String, String> childMap1 = resultMap.get("1");
    // child1
    Assertions.assertEquals(
        childMap1,
        redisTempl
            .<String, String>opsForHash()
            .entries("child_S_childSku-1")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("child01_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    apiUtil.callUpdAllBundleQtyTo10();
    apiUtil.callDeductChild1Qty15();

    // [案例1] child本人夠扣: childUuid1 deduct 15 => childUuid1 share 0 non-share 178
    Map<String, String> case1Result = new HashMap<>();
    case1Result.put("child01_available", "178");
    case1Result.put("uuid", childUuid1);

    Assertions.assertEquals(
        case1Result,
        redisTempl
            .<String, String>opsForHash()
            .entries("child_S_childSku-1")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("child01_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    apiUtil.callDeductChild2Qty92();

    // [案例2] child本人不夠扣，single sku bundle夠扣，order by child ratio, create time desc補貨
    Map<String, String> case2Result_child = new HashMap<>();
    case2Result_child.put("child01_available", "1");
    case2Result_child.put("uuid", childUuid2);

    Map<String, String> case2Result_parent4 = new HashMap<>();
    case2Result_parent4.put("H01210010101_available", "7");
    case2Result_parent4.put("uuid", "test-BundleParent-0004-0004-0004");

    Map<String, String> case2Result_parent5 = new HashMap<>();
    case2Result_parent5.put("H01210010101_available", "6");
    case2Result_parent5.put("uuid", "test-BundleParent-0005-0005-0005");

    Map<String, String> case2Result_parent6 = new HashMap<>();
    case2Result_parent6.put("H01210010101_available", "6");
    case2Result_parent6.put("uuid", "test-BundleParent-0006-0006-0006");

    Assertions.assertEquals(
        case2Result_child,
        redisTempl
            .<String, String>opsForHash()
            .entries("child_S_childSku-2")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("child01_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case2Result_parent4,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0004")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case2Result_parent5,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0005")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case2Result_parent6,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0006")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // [案例3] child本人不夠扣，single sku bundle不夠扣，order by child ratio, create time desc補貨
    apiUtil.callDeductChild2Qty63();

    Map<String, String> case3Result_child = new HashMap<>();
    case3Result_child.put("child01_available", "0");
    case3Result_child.put("uuid", childUuid2);

    Map<String, String> case3Result_parent1 = new HashMap<>();
    case3Result_parent1.put("H01210010101_available", "10");
    case3Result_parent1.put("uuid", "test-BundleParent-0001-0001-0001");

    Map<String, String> case3Result_parent2 = new HashMap<>();
    case3Result_parent2.put("H01210010101_available", "8");
    case3Result_parent2.put("uuid", "test-BundleParent-0002-0002-0002");

    Map<String, String> case3Result_parent3 = new HashMap<>();
    case3Result_parent3.put("H01210010101_available", "8");
    case3Result_parent3.put("uuid", "test-BundleParent-0003-0003-0003");

    Map<String, String> case3Result_parent4 = new HashMap<>();
    case3Result_parent4.put("H01210010101_available", "0");
    case3Result_parent4.put("uuid", "test-BundleParent-0004-0004-0004");

    Map<String, String> case3Result_parent5 = new HashMap<>();
    case3Result_parent5.put("H01210010101_available", "0");
    case3Result_parent5.put("uuid", "test-BundleParent-0005-0005-0005");

    Map<String, String> case3Result_parent6 = new HashMap<>();
    case3Result_parent6.put("H01210010101_available", "0");
    case3Result_parent6.put("uuid", "test-BundleParent-0006-0006-0006");

    Assertions.assertEquals(
        case3Result_child,
        redisTempl
            .<String, String>opsForHash()
            .entries("child_S_childSku-2")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("child01_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case3Result_parent1,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0001")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case3Result_parent2,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0002")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case3Result_parent3,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0003")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case3Result_parent4,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0004")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case3Result_parent5,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0005")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    Assertions.assertEquals(
        case3Result_parent6,
        redisTempl
            .<String, String>opsForHash()
            .entries("H0121001_S_P0006")
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("H01210010101_available")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                    if (entry.getKey().equals("uuid")) {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

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

  @Test
  public void updBundleQty_resetNodeListWhenUpdBundleChildQtyCrossNode() {
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String parentSku = "H088800118_S_parent-SKU-E-1-1";
    String child1Uuid = "child-UUID-E-1";
    String child2Uuid = "child-UUID-E-2";
    String parentUuid = "parent-E-001";
    String parentSetting = UpdateBundleQtyTestTool.getParentSettingWithChild12();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertSkusInSpecifyRedisNode(
        SystemConstants.getRedisNodeKeys().get(0), child1Sku, child2Sku, parentSku);
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callAddBundle2400QtyApi(parentUuid);

    // verify node has reallocated
    redisTempl
        .opsForSet()
        .members(SystemConstants.getRedisNodeKeys().get(0))
        .collectList()
        .doOnNext(
            skus -> Assertions.assertFalse(skus.contains(child1Sku) && skus.contains(child2Sku)))
        .block();
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
  }

  @Test
  public void updBundleQty_replenishChildQtyNotCrash() {
    String child1Sku = "H088800118_S_child-SKU-E-1-1";
    String child2Sku = "H088800118_S_child-SKU-E-2-2";
    String child3Sku = "H088800118_S_child-SKU-E-4-4";
    String parentSku = "SKU-E001-1";
    String child1Uuid = "child-UUID-E-1-1";
    String child2Uuid = "child-UUID-E-2-2";
    String child3Uuid = "child-UUID-E-4-4";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentSettingWithChild124();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callAddBundle2400QtyApi(parentUuid);

    // verify qty
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child3Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // test has redis node situation
    // delete data
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callAddBundle2400QtyApi(parentUuid);

    // verify qty
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child3Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);
  }

  @Test
  public void updBundleQty_parentQtyNotEnoughToDeduct() {
    String childSku = "H088800118_S_child-SKU-E-1-1";
    String parentSku = "SKU-E001-1";
    String childUuid = "child-UUID-E-1-1";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentQtyNotEnoughToDeductParentSetting();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(childUuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(childUuid, parentUuid);
    redisUtil.deleteSku(childSku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(childUuid, childSku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(childUuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callDeductBundle2500QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(childSku, "H08880011898_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(childUuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(childUuid, parentUuid);
    redisUtil.deleteSku(childSku, parentSku);
  }

  @Test
  public void updBundleQty_resetNodeListWhenReplenishQtyCrossNode() {
    // need to change code let replenishChildQty putBundleChildStockLevelByLuaScript skusInSameNode
    // throw cross node error
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String child3Sku = "H088800118_S_child-SKU-E-3";
    String child4Sku = "H088800118_S_child-SKU-E-4";
    String child5Sku = "H088800118_S_child-SKU-E-5";
    String parentSku = "SKU-E001-1";
    String child1Uuid = "child-UUID-E-1";
    String child2Uuid = "child-UUID-E-2";
    String child3Uuid = "child-UUID-E-3";
    String child4Uuid = "child-UUID-E-4";
    String child5Uuid = "child-UUID-E-5";
    String parentUuid = "parent-E-001-1";
    String parentSetting =
        UpdateBundleQtyTestTool.getResetNodeListWhenReplenishQtyCrossNodeParentSetting();

    // delete data
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid, child4Uuid, child5Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(
        child1Uuid, child2Uuid, child3Uuid, child4Uuid, child5Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, child4Sku, child5Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child4Uuid, child4Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child5Uuid, child5Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child4Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child5Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callAddBundle2400QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child3Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child4Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child5Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid, child4Uuid, child5Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(
        child1Uuid, child2Uuid, child3Uuid, child4Uuid, child5Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, child4Sku, child5Sku, parentSku);
  }

  @Test
  public void updBundleQty_replenishChildQty_success() {
    // 減少parent的數量, 應該照比例回補到child裡面
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String child3Sku = "H088800118_S_child-SKU-E-3";
    String parentSku = "H088800118_S_E0001";
    String child1Uuid = "child-UUID-E-1";
    String child2Uuid = "child-UUID-E-2";
    String child3Uuid = "child-UUID-E-3";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getDefaultParentSetting();
    // delete data
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98", "0");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98", "0");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "98", "0");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // set parent default value
    redisTempl.opsForHash().put(parentSku, "H08880011898_available", "10").block();

    // testing api
    apiUtil.callSetBundle0QtyApi(parentUuid);

    String child1IimsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block())
            .toString();
    String child2IimsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block())
            .toString();
    String child3IimsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get(child3Sku, "H08880011898_available").block())
            .toString();

    Assertions.assertEquals("10", child1IimsQty);
    Assertions.assertEquals("20", child2IimsQty);
    Assertions.assertEquals("30", child3IimsQty);
  }

  @Test
  public void updBundleQty_childFrom98ShouldAddTo98() {
    // child在98以外的倉庫時, 因parent減少造成child增加時, child 數量應該被加到iids98倉
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String child3Sku = "H088800118_S_child-SKU-E-3";
    String parentSku = "H088800118_S_E0001";
    String child1Uuid = "child-UUID-E-1";
    String child2Uuid = "child-UUID-E-2";
    String child3Uuid = "child-UUID-E-3";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getDefaultParentSetting();

    // delete data
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98", "0");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "01", "0");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "02", "0");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // set parent default value
    redisTempl.opsForHash().put(parentSku, "H08880011898_available", "10").block();

    apiUtil.callChildFrom98ShouldAddTo98Api(parentUuid);
    String child1IimsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block())
            .toString();
    String child2IimsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get(child2Sku, "H08880011801_available").block())
            .toString();
    String child3IimsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get(child3Sku, "H08880011802_available").block())
            .toString();

    String child2Iids98Qty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get("inventory:" + child2Uuid, "98_qty").block())
            .toString();
    String child3Iids98Qty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get("inventory:" + child3Uuid, "98_qty").block())
            .toString();

    String child2IidsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get("inventory:" + child2Uuid, "01_qty").block())
            .toString();
    String child3IidsQty =
        Objects.requireNonNull(
                redisTempl.opsForHash().get("inventory:" + child3Uuid, "02_qty").block())
            .toString();

    Assertions.assertEquals("10", child1IimsQty);
    Assertions.assertEquals("0", child2IimsQty);
    Assertions.assertEquals("0", child3IimsQty);

    Assertions.assertEquals("0", child2IidsQty);
    Assertions.assertEquals("0", child3IidsQty);

    Assertions.assertEquals("20", child2Iids98Qty);
    Assertions.assertEquals("30", child3Iids98Qty);
  }

  @Test
  public void updBundleQty_parentQtyNotChange() {
    String childSku = "H088800118_S_child-SKU-E-1-1";
    String parentSku = "SKU-E001-1";
    String childUuid = "child-UUID-E-1-1";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentQtyNotChangeParentSetting();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(childUuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(childUuid, parentUuid);
    redisUtil.deleteSku(childSku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(childUuid, childSku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(childUuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callSetBundle2400QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(childSku, "H08880011898_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(childUuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(childUuid, parentUuid);
    redisUtil.deleteSku(childSku, parentSku);
  }

  @Test
  public void updBundleQty_addParent2400Qty() {
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String parentSku = "H088800118_S_parent-SKU-E-1";
    String child1Uuid = "child-UUID-E-1-1";
    String child2Uuid = "child-UUID-E-2-2";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentSettingWithChild12();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callAddBundle2400QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "1200", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
  }

  @Test
  public void updBundleQty_parentOffLine() {
    String child1Sku = "H088800118_S_child-SKU-E-1-1";
    String child2Sku = "H088800118_S_child-SKU-E-2-2";
    String child3Sku = "H088800118_S_child-SKU-E-4-4";
    String parentSku = "H088800118_S_parent-SKU-E-1-1";
    String child1Uuid = "child-UUID-E-1-1";
    String child2Uuid = "child-UUID-E-2-2";
    String child3Uuid = "child-UUID-E-4-4";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentSettingWithChild124();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callSetBundle0QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "4800", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "7200", redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "7200", redisTempl.opsForHash().get(child3Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);
  }

  @Test
  public void updBundleQty_parentOffLine_childWhIsNotMatch() {
    String child1Sku = "H088800118_S_child-SKU-E-1-1";
    String child2Sku = "H088800118_S_child-SKU-E-2-2";
    String child3Sku = "H088800118_S_child-SKU-E-4-4";
    String parentSku = "H088800118_S_parent-SKU-E-1-1";
    String child1Uuid = "child-UUID-E-1-1";
    String child2Uuid = "child-UUID-E-2-2";
    String child3Uuid = "child-UUID-E-4-4";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentSettingWithChild124();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "01");
    redisUtil.insertIidsAndSkuIimsData(child3Uuid, child3Sku, "98");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child3Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callSetBundle0QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "4800", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(child2Sku, "H08880011801_available").block());
    Assertions.assertNull(redisTempl.opsForHash().get(child2Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "4800", redisTempl.opsForHash().get("inventory:" + child2Uuid, "98_qty").block());
    Assertions.assertEquals(
        "7200", redisTempl.opsForHash().get(child3Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid, child3Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, child3Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, child3Sku, parentSku);
  }

  @Test
  public void updBundleQty_addParent2400Qty_childWhIsNotMatch() {
    String child1Sku = "H088800118_S_child-SKU-E-1";
    String child2Sku = "H088800118_S_child-SKU-E-2";
    String parentSku = "H088800118_S_parent-SKU-E-1";
    String child1Uuid = "child-UUID-E-1-1";
    String child2Uuid = "child-UUID-E-2-2";
    String parentUuid = "parent-E-001-1";
    String parentSetting = UpdateBundleQtyTestTool.getParentSettingWithChild12();

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(child1Uuid, child1Sku, "98");
    redisUtil.insertIidsAndSkuIimsData(child2Uuid, child2Sku, "01");
    redisUtil.insertIidsAndSkuIimsParentData(parentUuid, parentSku, "98");
    redisUtil.insertBundleParentKey(child1Uuid, parentUuid);
    redisUtil.insertBundleParentKey(child2Uuid, parentUuid);
    redisUtil.insertBundleSettingKey(parentUuid, parentSetting);

    // testing api
    apiUtil.callAddBundle2400QtyApi(parentUuid);

    // verify qty
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(child1Sku, "H08880011898_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(child2Sku, "H08880011801_available").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get(parentSku, "H08880011898_available").block());

    // delete data
    redisUtil.deleteRedisNodeKey();
    redisUtil.deleteBundleParentKey(child1Uuid, child2Uuid);
    redisUtil.deleteBundleSettingKey(parentUuid);
    redisUtil.deleteInventoryUuid(child1Uuid, child2Uuid, parentUuid);
    redisUtil.deleteSku(child1Sku, child2Sku, parentSku);
  }
}
