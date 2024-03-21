package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import io.restassured.path.json.JsonPath;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@SpringBootTest
@Slf4j
public class GetStockLevelTest {
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;
  @Autowired private RedisUtil redisUtil;
  @Autowired private ApiUtil apiUtil;
  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  @Test
  void getStockLevelTest() {
    String sku = "H0121001_S_TEST001";
    String uuid = "test-0001-uuid-00001";
    Map<String, String> fieldMapIims = new HashMap<>();
    fieldMapIims.put("H012100101_available", "6");
    fieldMapIims.put("H012100101_instockstatus", "notSpecified");
    fieldMapIims.put("H012100101_updatestocktime", "20240206151723");
    fieldMapIims.put("share", "0");
    fieldMapIims.put("uuid", uuid);
    redisTempl.opsForHash().putAll(sku, fieldMapIims).block();

    Map<String, String> fieldMapIids = new HashMap<>();
    fieldMapIids.put("hktv_sku", sku);
    fieldMapIids.put("01_qty", "10");
    fieldMapIids.put("02_qty", "0");
    fieldMapIids.put("01_mall", "hktv");
    fieldMapIids.put("02_mall", "");
    fieldMapIids.put("hktv_store_code", "H012100101");
    fieldMapIids.put("hktv_instockstatus", "notSpecified");
    fieldMapIids.put("hktv_share", "0");
    redisTempl.opsForHash().putAll("inventory:" + uuid, fieldMapIids).block();

    JsonPath expectedJson = new JsonPath(new File("src/main/resources/json/getStockLevels.json"));

    given()
        .contentType("application/json")
        .body(
            "{\n"
                + "  \"uuidList\": [\n"
                + "    \"test-0001-uuid-00001\",\"test-0001-uuid-noExist\"\n"
                + "  ]\n"
                + "}")
        .when()
        .post(BASIC_URL + "/get_stock_levels")
        .then()
        .assertThat()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    redisTempl.delete("inventory:" + uuid, sku).block();
  }

  @Test
  void getStockLevelBundleTest() {
    String bundleSku = "H0121001_S_BUNDLE";
    String childSku = "H0121001_S_CHILD01";
    String bundleUuid = "test-bundle-uuid-000";
    String childUuid = "test-bundle-child-0001";
    String settingString =
        "{\"is_reserved\":true,\"is_active\":false,\"priority\":0,\"bundle_mall_info\":[{\"mall\":\"hktv\",\"alert_qty\":100,\"ceiling_qty\":100}],\"bundle_child_info\":[{\"uuid\":\"test-bundle-child-0001\",\"sku_id\":\"CHILD01\",\"storefront_store_code\":\"H0121001\",\"sku_qty\":3,\"ceiling_qty\":0,\"is_loop\":false}]}";

    for (int i = 1; i < 11; i++) {
      Map<String, String> fieldMapIims = new HashMap<>();
      fieldMapIims.put("H012100101_available", "10");
      fieldMapIims.put("H012100101_instockstatus", "notSpecified");
      fieldMapIims.put("H012100101_updatestocktime", "20240206151723");
      fieldMapIims.put("share", "0");
      fieldMapIims.put("uuid", bundleUuid + i);
      redisTempl.opsForHash().putAll(bundleSku + i, fieldMapIims).block();
      Map<String, String> fieldMapIids = new HashMap<>();
      fieldMapIids.put("hktv_sku", bundleSku + i);
      fieldMapIids.put("01_qty", "0");
      fieldMapIids.put("01_mall", "hktv");
      fieldMapIids.put("hktv_selling_qty", "10");
      fieldMapIids.put("hktv_setting_qty", "10");
      fieldMapIids.put("hktv_store_code", "H012100101");
      fieldMapIids.put("hktv_instockstatus", "notSpecified");
      fieldMapIids.put("hktv_share", "0");
      redisTempl.opsForHash().putAll("inventory:" + bundleUuid + i, fieldMapIids).block();
      redisTempl.opsForValue().set("bundle:setting:" + bundleUuid + i, settingString).block();
      redisTempl.opsForSet().add("bundle:parent:" + childUuid, bundleUuid + i).block();
    }

    Map<String, String> fieldMapIims = new HashMap<>();
    fieldMapIims.put("H012100101_available", "0");
    fieldMapIims.put("H012100101_instockstatus", "notSpecified");
    fieldMapIims.put("H012100101_updatestocktime", "20240206151723");
    fieldMapIims.put("share", "0");
    fieldMapIims.put("uuid", childUuid);
    redisTempl.opsForHash().putAll(childSku, fieldMapIims).block();

    Map<String, String> fieldMapIids = new HashMap<>();
    fieldMapIids.put("hktv_sku", childSku);
    fieldMapIids.put("01_qty", "33");
    fieldMapIids.put("02_qty", "0");
    fieldMapIids.put("01_mall", "hktv");
    fieldMapIids.put("02_mall", "");
    fieldMapIids.put("hktv_store_code", "H012100101");
    fieldMapIids.put("hktv_instockstatus", "notSpecified");
    fieldMapIids.put("hktv_share", "0");
    redisTempl.opsForHash().putAll("inventory:" + childUuid, fieldMapIids).block();

    JsonPath expectedJson =
        new JsonPath(new File("src/main/resources/json/getStockLevelsBundle.json"));

    given()
        .contentType("application/json")
        .body(
            "{\n"
                + "  \"uuidList\": [\n"
                + "    \"test-bundle-child-0001\",\"test-0001-uuid-noExist\"\n"
                + "  ]\n"
                + "}")
        .when()
        .post(BASIC_URL + "/get_stock_levels/bundle")
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    redisTempl.delete("inventory:" + childUuid, childSku, "bundle:parent:" + childUuid).block();

    for (int i = 1; i < 11; i++) {
      redisTempl
          .delete(bundleUuid + i, "inventory:" + bundleUuid + i, "bundle:setting:" + bundleUuid + i)
          .block();
    }
  }

  @Test
  void getStockLevel_buildPm20DataIfNotExist() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku, uuid);

    // insert default data
    redisUtil.insertIidsV1DataAndSkuIimsData(uuid, sku, "98");

    // testing api
    apiUtil.getStockLevelV3(uuid);

    //     verify
    Assertions.assertEquals(
        "H088800118", redisTempl.opsForHash().get("inventory:" + uuid, "hktv_store_code").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:" + uuid, "98_qty").block());
    Assertions.assertEquals(
        sku, redisTempl.opsForHash().get("inventory:" + uuid, "hktv_sku").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:" + uuid, "98_mall").block());
    Assertions.assertEquals(
        "notSpecified",
        redisTempl.opsForHash().get("inventory:" + uuid, "hktv_instockstatus").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:" + uuid, "hktv_share").block());

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku, uuid);
  }
}
