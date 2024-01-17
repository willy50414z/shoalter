package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;

import com.shoalter.willy.shoaltertools.dto.BundleChildDto;
import com.shoalter.willy.shoaltertools.dto.BundleMallInfoDto;
import com.shoalter.willy.shoaltertools.dto.BundleSettingDto;
import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
public class CreateBundleTest {
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

  @Test
  public void bundleCreateWith3Child() throws InterruptedException {
    String parentUuid = "iids-test-BundleParent-9999-9999-9999";
    String storefrontStoreCode = "H0121001";
    String storeSkuId = storefrontStoreCode + "_S_B9999";
    String time = "202401121021";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE,
        ROUTING_KEY,
        buildBundleProductInfoDto_testcase0001(parentUuid, storefrontStoreCode, storeSkuId));

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
    Assertions.assertEquals(
        Boolean.TRUE,
        redisTempl.hasKey(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid).block());

    // 清除bundle parent inventory + bundle:setting:
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid, parentUuid).block();
    redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid, parentUuid).block();
    redisLMTempl.delete(parentUuid).block();
    redisLMTempl.delete(storeSkuId).block();

    // 清除bundle children
    redisTempl.delete("child-999-1").block();
    redisTempl.delete("child_S_child-999-sku1").block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-1", "child-999-1").block();
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-1", "child-999-1")
        .block();

    redisTempl.delete("child-999-2").block();
    redisTempl.delete("child_S_child-999-sku2").block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-2", "child-999-2").block();
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-2", "child-999-2")
        .block();

    redisTempl.delete("child-999-3").block();
    redisTempl.delete("child_S_child-999-sku3").block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-3", "child-999-3").block();
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-3", "child-999-3")
        .block();
  }

  @Test // 建立有100個child的bundle 計算扣減該bundle需要的時間
  public void bundleCreateWith100Child() throws JSONException {
    String parentUuid = "parent-E-00";
    String storefrontStoreCode = "H088800118";
    String storeSkuId = storefrontStoreCode + "_S_E00";
    String sku = "SKU-E00";
    String childSku = "child-SKU-E-";
    String childUuid = "child-UUID-E-";

    createBundleWith100Child(parentUuid, sku, storefrontStoreCode);

    // 清除bundle parent inventory + bundle:setting:
    for (int i = 1; i < 11; i++) {
      redisTempl.delete(parentUuid + i).block();
      redisTempl.delete(sku + i).block();
      redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid + i, parentUuid + i).block();
      redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid + i).block();
      redisTempl
          .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + parentUuid + i, parentUuid + i)
          .block();
    }

    for (int i = 1; i < 101; i++) {
      redisTempl.delete(childUuid + i).block();
      redisTempl.delete(childSku + i).block();
      redisTempl.delete(storefrontStoreCode + "_S_" + childSku + i).block();
      redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + childUuid + i, childUuid + i).block();
      redisTempl
          .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + childUuid + i, childUuid + i)
          .block();
    }
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0001(
      String parentUuid, String storefrontStoreCode, String storeSkuId) {

    // 清除bundle parent inventory + bundle:setting:
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

  private void createBundleWith100Child(
      String parentUuid, String parentSku, String storefrontStoreCode) throws JSONException {

    String childSku = "child-SKU-E-";
    String childUuid = "child-UUID-E-";

    // 清除bundle parent inventory + bundle:setting:
    for (int i = 1; i < 11; i++) {
      redisTempl.delete(parentUuid + i).block();
      redisTempl.delete(parentSku + i).block();
      redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid + i, parentUuid + i).block();
      redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid + i).block();
      redisTempl
          .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + parentUuid + i, parentUuid + i)
          .block();
    }

    LinkedList<BundleChildDto> bundleChildList = new LinkedList<BundleChildDto>();

    // build 100 child
    for (int i = 1; i < 101; i++) {

      // 清除bundle children
      redisTempl.delete(childUuid + i).block();
      redisTempl.delete(childSku + i).block();
      redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + childUuid + i, childUuid + i).block();
      redisTempl
          .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + childUuid + i, childUuid + i)
          .block();

      // build
      ProductInfoDto productInfoDto =
          ProductInfoDto.builder()
              .action("CREATE")
              .products(
                  List.of(
                      ProductDto.builder()
                          .uuid(childUuid + i)
                          .warehouseDetail(
                              List.of(
                                  ProductWarehouseDetailDto.builder()
                                      .warehouseSeqNo("98")
                                      .mall(List.of("hktv"))
                                      .build()))
                          .mallDetail(
                              List.of(
                                  ProductMallDetailDto.builder()
                                      .mall("hktv")
                                      .storefrontStoreCode(storefrontStoreCode)
                                      .storeSkuId("H088800118_S_" + childSku + i)
                                      .build()))
                          .build()))
              .build();

      defaultRabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, productInfoDto);

      bundleChildList.add(
          BundleChildDto.builder()
              .uuid(childUuid + i)
              .skuId(childSku + i)
              .skuQty(2)
              .ceilingQty(0)
              .storefrontStoreCode(storefrontStoreCode)
              .isLoop(false)
              .build());
    }

    long start = System.currentTimeMillis();

    // build parent with 100 child
    for (int i = 1; i < 11; i++) {

      // modify parent qty by parent
      if (i == 2) {
        BundleChildDto bundleChildDto = bundleChildList.get(50);
        bundleChildDto.setSkuQty(4);
        bundleChildList.set(50, bundleChildDto);
      } else if (i == 3) {
        BundleChildDto bundleChildDto = bundleChildList.get(50);
        bundleChildDto.setSkuQty(3);
        bundleChildList.set(50, bundleChildDto);
      } else if (i == 5) {
        BundleChildDto bundleChildDto = bundleChildList.get(50);
        bundleChildDto.setSkuQty(5);
        bundleChildList.set(50, bundleChildDto);
      } else {
        BundleChildDto bundleChildDto = bundleChildList.get(50);
        bundleChildDto.setSkuQty(2);
        bundleChildList.set(50, bundleChildDto);
      }

      ProductInfoDto productInfoDto =
          ProductInfoDto.builder()
              .action("CREATE")
              .products(
                  List.of(
                      ProductDto.builder()
                          .uuid(parentUuid + i)
                          .mallDetail(
                              List.of(
                                  ProductMallDetailDto.builder()
                                      .mall("hktv")
                                      .storeSkuId(parentSku + i)
                                      .storefrontStoreCode(storefrontStoreCode)
                                      .build()))
                          .warehouseDetail(
                              List.of(
                                  ProductWarehouseDetailDto.builder()
                                      .warehouseSeqNo("98")
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
                                  .bundleChildInfoList(bundleChildList)
                                  .build())
                          .build()))
              .build();

      defaultRabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, productInfoDto);
    }

    System.out.println(
        "======= 建立bundle 完成, 經過:" + (System.currentTimeMillis() - start) + "ms =======");

    JSONArray requestSetArray = getWarehouseJsonArray(childUuid);

    given()
        .contentType("application/json")
        .body(requestSetArray.toString())
        .when()
        .put(BASIC_URL + "/warehouse/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    JSONArray mallJsonArray = getMallJsonArray(childUuid);
    given()
        .contentType("application/json")
        .body(mallJsonArray.toString())
        .when()
        .put(BASIC_URL + "/mall/stock_levels")
        .then()
        .statusCode(200)
        .log()
        .all();

    long t2 = System.currentTimeMillis();

    JSONArray bundleSetJson = getBundleSetJson(parentUuid);
    given()
        .contentType("application/json")
        .body(bundleSetJson.toString())
        .when()
        .put(BASIC_URL + "/mall/bundle/quantity")
        .then()
        .statusCode(200)
        .log()
        .all();

    System.out.println("======= child分配完成, 經過:" + (System.currentTimeMillis() - t2) + "ms =======");

    long t3 = System.currentTimeMillis();
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"child-UUID-E-51\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 1600\n"
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

    System.out.println(
        "======= 部分 child 扣減完成, 經過:" + (System.currentTimeMillis() - t3) + "ms =======");

    long t4 = System.currentTimeMillis();
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \"child-UUID-E-51\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 1000\n"
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

    System.out.println(
        "======= 全部 child 扣減完成, 經過:" + (System.currentTimeMillis() - t4) + "ms =======");
  }

  private static JSONArray getBundleSetJson(String parentUuid) throws JSONException {
    JSONArray requestSetArray = new JSONArray();
    JSONArray mallQtyArray = new JSONArray();
    JSONObject mallObj = new JSONObject();

    mallObj.put("mall", "hktv");
    mallObj.put("mode", "set");
    mallObj.put("qty", "100");
    mallQtyArray.put(mallObj);

    for (int i = 1; i < 11; i++) {
      JSONObject productJson = new JSONObject();
      productJson.put("uuid", parentUuid + i);
      productJson.put("mallQty", mallQtyArray);
      requestSetArray.put(productJson);
    }
    return requestSetArray;
  }

  private static JSONArray getWarehouseJsonArray(String childUuid) throws JSONException {
    JSONArray requestSetArray = new JSONArray();
    JSONArray warehouseQtyArray = new JSONArray();
    JSONObject wareHouseObj = new JSONObject();

    wareHouseObj.put("warehouseSeqNo", "98");
    wareHouseObj.put("mode", "set");
    wareHouseObj.put("quantity", "2600");
    warehouseQtyArray.put(wareHouseObj);

    for (int i = 1; i < 101; i++) {
      JSONObject productJson = new JSONObject();
      productJson.put("uuid", childUuid + i);
      productJson.put("warehouseQty", warehouseQtyArray);
      requestSetArray.put(productJson);
    }
    return requestSetArray;
  }

  private static JSONArray getMallJsonArray(String childUuid) throws JSONException {
    JSONArray requestSetArray = new JSONArray();
    JSONArray stockLevelArray = new JSONArray();
    JSONObject wareHouseObj = new JSONObject();

    wareHouseObj.put("mall", "hktv");
    wareHouseObj.put("mode", "set");
    wareHouseObj.put("qty", "2600");
    stockLevelArray.put(wareHouseObj);

    for (int i = 1; i < 101; i++) {
      JSONObject productJson = new JSONObject();
      productJson.put("uuid", childUuid + i);
      productJson.put("stockLevels", stockLevelArray);
      requestSetArray.put(productJson);
    }
    return requestSetArray;
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
}
