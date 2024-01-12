package com.shoalter.willy.shoaltertools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoalter.willy.shoaltertools.dto.BundleChildDto;
import com.shoalter.willy.shoaltertools.dto.BundleMallInfoDto;
import com.shoalter.willy.shoaltertools.dto.BundleSettingDto;
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

import static io.restassured.RestAssured.given;

@SpringBootTest
@Slf4j
public class CreateBundleTest {
  @Autowired private static ObjectMapper objectMapper;

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

  public static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING = "bundle:setting:";
  public static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT = "bundle:parent:";
  public static final String INVENTORY_REDIS_HKEY_PREFIX = "inventory:";

  public static final String childUuid1 = "childUuid-1";
  public static final String childUuid2 = "childUuid-2";
  public static final String childUuid3 = "childUuid-3";

  public static final String childSku1 = "childSku-1";
  public static final String childSku2 = "childSku-2";
  public static final String childSku3 = "childSku-3";

  @Test
  public void bundleCreateWith3Child() throws InterruptedException {
    String parentUuid = "iids-test-BundleParent-9999-9999-9999";
    String sku = "iims-test-BundleParent-0001-child1-0000";
    String storefrontStoreCode = "H0121001";
    String storeSkuId = storefrontStoreCode + "_S_B9999";
    String time = "202401121021";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE,
        ROUTING_KEY,
        buildBundleProductInfoDto_testcase0001(parentUuid, sku, storefrontStoreCode, storeSkuId));

    Thread.sleep(1000L);

    // 驗證IIDS資料: inventory
    Assertions.assertEquals(
        buildExpectedBundle_testcase0001(storeSkuId, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + parentUuid)
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

    // 驗證IIDS資料: bundle:setting
    Assertions.assertTrue(
        redisTempl.hasKey(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid).block());

    // 清除bundle parent inventory + bundle:setting:
    redisTempl.delete(sku).block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid, parentUuid).block();
    redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid, parentUuid).block();
    redisLMTempl.delete(parentUuid).block();
    redisLMTempl.delete(storeSkuId).block();

    // 清除bundle children
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-1", "child-999-1")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-1", "child-999-1").block();
    redisTempl.delete("child-999-1").block();
    redisTempl.delete("child_S_child-999-sku1").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-2", "child-999-2")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-2", "child-999-2").block();
    redisTempl.delete("child-999-2").block();
    redisTempl.delete("child_S_child-999-sku2").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-3", "child-999-3")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-3", "child-999-3").block();
    redisTempl.delete("child-999-3").block();
    redisTempl.delete("child_S_child-999-sku3").block();
  }

  @Test
  public void test1_addBundle_deductEnough() throws InterruptedException {
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

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0002());
    Thread.sleep(1000L);

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0003());
    Thread.sleep(1000L);

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0004());
    Thread.sleep(1000L);

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0005());
    Thread.sleep(1000L);

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0006());
    Thread.sleep(1000L);

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0007());
    Thread.sleep(1000L);

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
                + "        \"quantity\": 233\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-2\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 223\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"uuid\": \"childUuid-3\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 233\n"
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

    // set childUuid1/2/3 qty=233 to mall
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
                + "        \"qty\": 223,\n"
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

    // 驗證資料正確 所有child qty都是 233
    Map<String, Map<String, String>> resultMap = case1AssertionExpectation();
    Map<String, String> childMap1 = resultMap.get("1");
    // child1
    Assertions.assertEquals(
        childMap1,
        redisLMTempl
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
                + "  },\n"
                + "    {\n"
                + "    \"uuid\":\"test-BundleParent-0002-0002-0002\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "    {\n"
                + "    \"uuid\":\"test-BundleParent-0003-0003-0003\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "    {\n"
                + "    \"uuid\":\"test-BundleParent-0004-0004-0004\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "    {\n"
                + "    \"uuid\":\"test-BundleParent-0005-0005-0005\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 10,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "    {\n"
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

    // deduct child1 15
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
                + "        \"quantity\": 15\n"
                + "      }\n"
                + "    ]\n"
                + "  }]")
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    //[案例1] child本人夠扣: childUuid1 deduct 15 => childUuid1 share 0 non-share 178
    Map<String, String> case1Result = new HashMap<>();
    case1Result.put("child01_available", "178");
    case1Result.put("uuid", childUuid1);

    Assertions.assertEquals(
          case1Result,
        redisLMTempl
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

    // deduct child2 92
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
                + "        \"quantity\": 92\n"
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

    //[案例2] child本人不夠扣，single sku bundle夠扣，order by child ratio, create time desc補貨
    Map<String, String> case2Result_child = new HashMap<>();
    case2Result_child.put("child01_available", "71");
    case2Result_child.put("uuid", childUuid2);

    Map<String, String> case2Result_parent4 = new HashMap<>();
    case2Result_parent4.put("H01210010101_available", "10");
    case2Result_parent4.put("uuid", "test-BundleParent-0004-0004-0004");

    Map<String, String> case2Result_parent5 = new HashMap<>();
    case2Result_parent5.put("H01210010101_available", "8");
    case2Result_parent5.put("uuid", "test-BundleParent-0005-0005-0005");

    Map<String, String> case2Result_parent6 = new HashMap<>();
    case2Result_parent6.put("H01210010101_available", "9");
    case2Result_parent6.put("uuid", "test-BundleParent-0006-0006-0006");

    Assertions.assertEquals(
          case2Result_parent4,
          redisLMTempl
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
          redisLMTempl
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
          redisLMTempl
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
  }

  @Test
  public void create_parentUuid1() throws InterruptedException {
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0002());

    Thread.sleep(1000L);
    Assertions.assertEquals("s", "s");
  }

  @Test
  public void create_parentUuid2() throws InterruptedException {
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0003());

    Thread.sleep(1000L);
    Assertions.assertEquals("s", "s");
  }

  @Test
  public void create_parentUuid3() throws InterruptedException {
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0004());

    Thread.sleep(1000L);
    Assertions.assertEquals("s", "s");
  }

  @Test
  public void create_parentUuid4() throws InterruptedException {
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0005());

    Thread.sleep(1000L);
    Assertions.assertEquals("s", "s");
  }

  @Test
  public void create_parentUuid5() throws InterruptedException {
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0006());

    Thread.sleep(1000L);
    Assertions.assertEquals("s", "s");
  }

  @Test
  public void create_parentUuid6() throws InterruptedException {
    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildBundleProductInfoDto_testcase0007());

    Thread.sleep(1000L);
    Assertions.assertEquals("s", "s");
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0001(
      String parentUuid, String sku, String storefrontStoreCode, String storeSkuId) {

    // parent has 3 children

    //    String childUuid1 = "childUuid-1";
    //    String childUuid2 = "childUuid-2";
    //    String childUuid3 = "childUuid-3";
    //
    //    String childSku1 = "childSku-1";
    //    String childSku2 = "childSku-2";
    //    String childSku3 = "childSku-3";

    // 清除bundle parent inventory + bundle:setting:
    redisTempl.delete(sku).block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid, parentUuid).block();
    redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid, parentUuid).block();
    redisLMTempl.delete(parentUuid).block();

    // 清除bundle children
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-1", "child-999-1")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-1", "child-999-1").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-2", "child-999-2")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-2", "child-999-2").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-3", "child-999-3")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-3", "child-999-3").block();

    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(parentUuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId(storeSkuId)
                                .storefrontStoreCode(storefrontStoreCode)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid("child-999-1")
                                        .skuId("child-999-sku1")
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid("child-999-2")
                                        .skuId("child-999-sku2")
                                        .skuQty(4)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid("child-999-3")
                                        .skuId("child-999-sku3")
                                        .skuQty(5)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build(),
                ProductDto.builder()
                    .uuid("child-999-1")
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_child-999-sku1")
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid("child-999-2")
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_child-999-sku2")
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid("child-999-3")
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_child-999-sku3")
                                .build()))
                    .build()))
        .build();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0002() {

    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0001-0001-0001")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0001")
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid(childUuid1)
                                        .skuId(childSku1)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid3)
                                        .skuId(childSku3)
                                        .skuQty(4)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid1)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSku1)
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid2)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSku2)
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuid3)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSku3)
                                .build()))
                    .build()))
        .build();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0003() {

    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0002-0002-0002")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0002")
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid(childUuid1)
                                        .skuId(childSku1)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0004() {

    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0003-0003-0003")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0003")
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid(childUuid3)
                                        .skuId(childSku3)
                                        .skuQty(4)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0005() {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0004-0004-0004")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0004")
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(2)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0006() {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0005-0005-0005")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0005")
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(5)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0007() {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid("test-BundleParent-0006-0006-0006")
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId("H0121001_S_P0006")
                                .storefrontStoreCode("H012100101")
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid(childUuid2)
                                        .skuId(childSku2)
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build()))
        .build();
  }

  private ProductInfoDto buildShareBundleChildInfoDto_testcase0001() {
    String childUuidA = "childUuid-A";
    String childUuidB = "childUuid-B";
    String childUuidC = "childUuid-C";

    String childSkuA = "childSku-A";
    String childSkuB = "childSku-B";
    String childSkuC = "childSku-C";

    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(childUuidA)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSkuA)
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuidB)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSkuB)
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid(childUuidC)
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_" + childSkuC)
                                .build()))
                    .build()))
        .build();
  }

  private static Map<String, String> buildExpectedBundle_testcase0001(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_setting_qty", "0");
    stockLevelMap.put("hktv_selling_qty", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H0121001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }

  private static BundleSettingDto buildExpectedBundleSetting_testcase0001() {
    return BundleSettingDto.builder()
        .bundleMallInfoList(
            List.of(BundleMallInfoDto.builder().mall("hktv").alertQty(100).ceilingQty(100).build()))
        .bundleChildInfoList(
            List.of(
                BundleChildDto.builder().uuid(childUuid1).skuId(childSku1).skuQty(3).build(),
                BundleChildDto.builder().uuid(childUuid2).skuId(childSku2).skuQty(4).build(),
                BundleChildDto.builder().uuid(childUuid3).skuId(childSku3).skuQty(5).build()))
        .build();
  }

  private static Map<String, Map<String, String>> case1AssertionExpectation() {
    Map<String, Map<String, String>> resultMap = new HashMap<>();
    Map<String, String> child1Map = new HashMap<>();
    child1Map.put("child01_available", "233");
    child1Map.put("uuid", childUuid1);
    resultMap.put("1", child1Map);

    Map<String, String> child2Map = new HashMap<>();
    child2Map.put("child01_available", "233");
    child2Map.put("uuid", childUuid2);
    resultMap.put("2", child2Map);

    Map<String, String> child3Map = new HashMap<>();
    child3Map.put("child01_available", "233");
    child3Map.put("uuid", childUuid3);
    resultMap.put("3", child3Map);

    return resultMap;
  }
}
