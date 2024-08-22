package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.shoalter.willy.shoaltertools.model.IimsModel;
import com.shoalter.willy.shoaltertools.model.MmsProductEntity;
import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.MigrationByHybrisWhUtil;
import com.shoalter.willy.shoaltertools.testtool.MigrationByMMSWhUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import io.restassured.path.json.JsonPath;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@SpringBootTest
public class WarehouseNotMatchTest {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired private RedisUtil redisUtil;
  @Autowired private ApiUtil apiUtil;
  @Autowired private MigrationByHybrisWhUtil migrationByHybrisWhUtil;
  @Autowired private MigrationByMMSWhUtil migrationByMMSWhUtil;

  String UUID = "5d99-8f06-2e5452215b0a";
  String INVENTORY_UUID = "inventory:5d99-8f06-2e5452215b0a";
  String SKU = "H088800118_S_10021451M";
  String MMS_SEQ_NO = "01";
  String HYBRIS_SEQ_NO = "02";

  @BeforeEach
  public void beforeEachSetUp() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisData(SKU, HYBRIS_SEQ_NO);
  }

  @AfterEach
  public void afterEachSetUp() {}

  private void migrationByMMSWh() {
    List<IimsModel> listWorkerResult =
        migrationByMMSWhUtil.iimsWorker(List.of(getMmsProductEntity()));
    migrationByMMSWhUtil.iidsWorker(listWorkerResult);
  }

  private void migrationByHybrisWh() {
    List<IimsModel> listWorkerResult =
        migrationByHybrisWhUtil.iimsWorker(List.of(getMmsProductEntity()));
    migrationByHybrisWhUtil.iidsWorker(listWorkerResult);
  }

  private MmsProductEntity getMmsProductEntity() {
    MmsProductEntity mmsProductEntity = new MmsProductEntity();
    mmsProductEntity.setStorefrontStoreCode("H088800118");
    mmsProductEntity.setWarehouseSeqNumber("01");
    mmsProductEntity.setWarehouseSeqNumbers(List.of("02", "98"));
    mmsProductEntity.setStoreSkuId("H088800118_S_10021451M");
    mmsProductEntity.setUuid("5d99-8f06-2e5452215b0a");
    mmsProductEntity.setMmsId(115);
    return mmsProductEntity;
  }

  @Test
  void migrationByMmsWarehouse_mixUpdateHKTV() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByMMSWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "    \"uuid\":\""
                + UUID
                + "\",\n"
                + "    \"share\": {\n"
                + "        \"quantity\": 200,\n"
                + "        \"stockStatus\": \"notSpecified\"\n"
                + "        },\n"
                + "    \"malls\": [{\n"
                + "        \"name\": \"hktv\",\n"
                + "        \"share\": true\n"
                + "        }]\n"
                + "}]")
        .when()
        .put(apiUtil.getLocalUpdateHktvByMixUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
    redisUtil.simulateHybrisChangeWhTo98();
  }

  @Test
  void migrationByHybrisWarehouse_mixUpdateHKTV() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByHybrisWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "    \"uuid\":\""
                + UUID
                + "\",\n"
                + "    \"share\": {\n"
                + "        \"quantity\": 200,\n"
                + "        \"stockStatus\": \"notSpecified\"\n"
                + "        },\n"
                + "    \"malls\": [{\n"
                + "        \"name\": \"hktv\",\n"
                + "        \"share\": true\n"
                + "        }]\n"
                + "}]")
        .when()
        .put(apiUtil.getLocalUpdateHktvByMixUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
    redisUtil.simulateHybrisChangeWhTo98();
  }

  @Test
  void migrationByMmsWarehouse_findStockLevelByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/findStockLevelByOldFlowResponse.json"));

    migrationByMMSWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .pathParam("products_uuids", UUID)
        .get(apiUtil.getLocalFindStockLevelV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_findStockLevelByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/findStockLevelByOldFlowResponse.json"));

    migrationByHybrisWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .pathParam("products_uuids", UUID)
        .get(apiUtil.getLocalFindStockLevelV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_createHktvProductByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(
            new File("src/test/resources/json/createHktvStockLevelByOldFlowErrorResponse.json"));

    migrationByMMSWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"malls\": [{\n"
                + "    \"name\": \"hktv\",\n"
                + "    \"sku\": \"B12345\",\n"
                + "    \"warehouse\": \"B1001001\",\n"
                + "    \"share\": false,\n"
                + "    \"quantity\": 10,\n"
                + "    \"stockStatus\": \"notSpecified\"\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalCreateHktvStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_createHktvProductByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(
            new File("src/test/resources/json/createHktvStockLevelByOldFlowErrorResponse.json"));

    migrationByHybrisWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"malls\": [{\n"
                + "    \"name\": \"hktv\",\n"
                + "    \"sku\": \"B12345\",\n"
                + "    \"warehouse\": \"B1001001\",\n"
                + "    \"share\": false,\n"
                + "    \"quantity\": 10,\n"
                + "    \"stockStatus\": \"notSpecified\"\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalCreateHktvStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_createLittleMallUnderProductByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByMMSWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"malls\": [{\n"
                + "    \"name\": \"little_mall\",\n"
                + "    \"share\": false,\n"
                + "    \"quantity\": 10,\n"
                + "    \"stockStatus\": \"notSpecified\"\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalCreateLittleMallStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_createLittleMallUnderProductByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByHybrisWh();
    apiUtil.getStockLevelV1(UUID);
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"malls\": [{\n"
                + "    \"name\": \"little_mall\",\n"
                + "    \"share\": false,\n"
                + "    \"quantity\": 10,\n"
                + "    \"stockStatus\": \"notSpecified\"\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalCreateLittleMallStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_updateProductQuantityByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"malls\": [{\n"
                + "    \"name\": \"hktv\",\n"
                + "    \"mode\": \"set\",\n"
                + "    \"quantity\": 10\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalUpdateStockLevelQtyV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        "10", redisTempl.opsForHash().get(SKU, "H08880011802_available").block());
  }

  @Test
  void migrationByHybrisWarehouse_updateProductQuantityByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"malls\": [{\n"
                + "    \"name\": \"hktv\",\n"
                + "    \"mode\": \"set\",\n"
                + "    \"quantity\": 10\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalUpdateStockLevelQtyV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        "10", redisTempl.opsForHash().get(SKU, "H08880011802_available").block());
  }

  @Test
  void migrationByMmsWarehouse_updateProductInStockStatusByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"statuses\": [{\n"
                + "    \"name\": \"hktv\",\n"
                + "    \"stockStatus\": \"forceOutOfStock\"\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalUpdateStockLevelInStockStatusV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        "forceOutOfStock", redisTempl.opsForHash().get(SKU, "H08880011802_instockstatus").block());
  }

  @Test
  void migrationByHybrisWarehouse_updateProductInStockStatusByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"statuses\": [{\n"
                + "    \"name\": \"hktv\",\n"
                + "    \"stockStatus\": \"forceOutOfStock\"\n"
                + "  }]\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalUpdateStockLevelInStockStatusV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        "forceOutOfStock", redisTempl.opsForHash().get(SKU, "H08880011802_instockstatus").block());
  }

  @Test
  void migrationByMmsWarehouse_updateProductToShareModeByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body("[{\n" + "  \"uuid\": \"" + UUID + "\",\n" + "  \"malls\": [\"hktv\"]\n" + "}]")
        .when()
        .post(apiUtil.getLocalUpdateStockLevelToShareModeV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals("1", redisTempl.opsForHash().get(SKU, "share").block());
  }

  @Test
  void migrationByHybrisWarehouse_updateProductToShareModeByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body("[{\n" + "  \"uuid\": \"" + UUID + "\",\n" + "  \"malls\": [\"hktv\"]\n" + "}]")
        .when()
        .post(apiUtil.getLocalUpdateStockLevelToShareModeV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals("1", redisTempl.opsForHash().get(SKU, "share").block());
  }

  @Test
  void migrationByMmsWarehouse_updateProductToNonShareModeByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"mall\": \"hktv\",\n"
                + "  \"transferRatio\": 0.8\n"
                + "}]")
        .when()
        .delete(apiUtil.getLocalUpdateStockLevelToNonShareModeV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals("0", redisTempl.opsForHash().get(SKU, "share").block());
  }

  @Test
  void migrationByHybrisWarehouse_updateProductToNonShareModeByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/oldFlowUpdateSuccessResponse.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"mall\": \"hktv\",\n"
                + "  \"transferRatio\": 0.8\n"
                + "}]")
        .when()
        .delete(apiUtil.getLocalUpdateStockLevelToNonShareModeV1Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals("0", redisTempl.opsForHash().get(SKU, "share").block());
  }

  @Test
  void migrationByMmsWarehouse_updateProductQtyByMixByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/updateProductQtyByMixByOldFlow.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"share\": {\n"
                + "    \"mode\": \"add\",\n"
                + "    \"quantity\": 10\n"
                + "  },\n"
                + "  \"malls\": []\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalUpdateProductQtyByMixUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    //    Assertions.assertEquals("0", redisTempl.opsForHash().get(SKU, "share").block());
  }

  @Test
  void migrationByHybrisWarehouse_updateProductQtyByMixByOldFlow() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/updateProductQtyByMixByOldFlow.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body(
            "[{\n"
                + "  \"uuid\": \""
                + UUID
                + "\",\n"
                + "  \"share\": {\n"
                + "    \"mode\": \"add\",\n"
                + "    \"quantity\": 10\n"
                + "  },\n"
                + "  \"malls\": []\n"
                + "}]")
        .when()
        .post(apiUtil.getLocalUpdateProductQtyByMixUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    //    Assertions.assertEquals("0", redisTempl.opsForHash().get(SKU, "share").block());
  }

  @Test
  void migrationByMmsWarehouse_findStockLevelByNewFlow() {
    // response warehouse is wrong
    JsonPath expectedJson =
        new JsonPath(
            new File("src/test/resources/json/byMmsWhMigrationFindStockLevelByNewFlow.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body("{\n" + "  \"uuidList\": [\n" + "    \"" + UUID + "\"\n" + "  ]\n" + "}")
        .when()
        .post(apiUtil.getLocalStockLevelV3Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_findStockLevelByNewFlow() {
    JsonPath expectedJson =
        new JsonPath(
            new File("src/test/resources/json/byHybrisWhMigrationFindStockLevelByNewFlow.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body("{\n" + "  \"uuidList\": [\n" + "    \"" + UUID + "\"\n" + "  ]\n" + "}")
        .when()
        .post(apiUtil.getLocalStockLevelV3Url())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_updateWarehouseQty() {
    // update 01 warehouse will fail
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/byMmsWhMigrationUpdateWarehouseQty.json"));

    migrationByMMSWh();
    apiUtil.getStockLevelV3(UUID);
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + UUID
                + "\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 233\n"
                + "      },\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"02\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 233\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_updateWarehouseQty() {
    JsonPath expectedJson =
        new JsonPath(
            new File("src/test/resources/json/byHybrisWhMigrationUpdateWarehouseQty.json"));

    migrationByHybrisWh();
    apiUtil.getStockLevelV3(UUID);
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + UUID
                + "\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"01\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 233\n"
                + "      },\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"02\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 233\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(apiUtil.getLocalUpdWhQtyUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_updateMallQty() {
    // update mall will fail
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/byMmsWhMigrationUpdateMallByNewFlow.json"));

    migrationByMMSWh();
    apiUtil.getStockLevelV3(UUID);
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\""
                + UUID
                + "\",\n"
                + "    \"stockLevels\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 40,\n"
                + "        \"mode\": \"set\",\n"
                + "        \"instockstatus\": \"notSpecified\",\n"
                + "        \"share\": 0\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_updateMallQty() {
    JsonPath expectedJson =
        new JsonPath(
            new File("src/test/resources/json/byHybrisWhMigrationUpdateMallByNewFlow.json"));

    migrationByHybrisWh();
    apiUtil.getStockLevelV3(UUID);
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\""
                + UUID
                + "\",\n"
                + "    \"stockLevels\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 40,\n"
                + "        \"mode\": \"set\",\n"
                + "        \"instockstatus\": \"notSpecified\",\n"
                + "        \"share\": 0\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(apiUtil.getLocalUpdMallStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_findStockLevelWithBundleInfoByNewFlow() {
    // response is wrong
    JsonPath expectedJson =
        new JsonPath(
            new File(
                "src/test/resources/json/byMmsWhMigrationFindStockLevelWithBundleInfoByNewFlow.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .body("{\n" + "  \"uuidList\": [\n" + "    \"" + UUID + "\"\n" + "  ]\n" + "}")
        .when()
        .post(apiUtil.getLocalStockLevelWithBundleInfoUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_findStockLevelWithBundleInfoByNewFlow() {
    JsonPath expectedJson =
        new JsonPath(
            new File(
                "src/test/resources/json/byHybrisWhMigrationFindStockLevelWithBundleInfoByNewFlow.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .body("{\n" + "  \"uuidList\": [\n" + "    \"" + UUID + "\"\n" + "  ]\n" + "}")
        .when()
        .post(apiUtil.getLocalStockLevelWithBundleInfoUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_findSkuStockLevel() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/findSkuStockLevel.json"));

    migrationByMMSWh();
    apiUtil.getIimsStockLevel(SKU);
    given()
        .contentType("application/json")
        .pathParam("product_SKU_id", SKU)
        .get(apiUtil.getLocalSkuStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByHybrisWarehouse_findSkuStockLevel() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/findSkuStockLevel.json"));

    migrationByHybrisWh();
    apiUtil.getIimsStockLevel(SKU);
    given()
        .contentType("application/json")
        .pathParam("product_SKU_id", SKU)
        .get(apiUtil.getLocalSkuStockLevelUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();
  }

  @Test
  void migrationByMmsWarehouse_updSkuStockLevelQty() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/updSkuStockLevelQty.json"));

    migrationByMMSWh();
    given()
        .contentType("application/json")
        .pathParam("product_SKU_id", SKU)
        .body("{\n" + "  \"mode\": \"add\",\n" + "  \"quantity\": 1\n" + "}")
        .when()
        .post(apiUtil.getLocalUpdSkuStockLevelQtyUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        "2001", redisTempl.opsForHash().get(SKU, "H08880011802_available").block());
  }

  @Test
  void migrationByHybrisWarehouse_updSkuStockLevelQty() {
    JsonPath expectedJson =
        new JsonPath(new File("src/test/resources/json/updSkuStockLevelQty.json"));

    migrationByHybrisWh();
    given()
        .contentType("application/json")
        .pathParam("product_SKU_id", SKU)
        .body("{\n" + "  \"mode\": \"add\",\n" + "  \"quantity\": 1\n" + "}")
        .when()
        .post(apiUtil.getLocalUpdSkuStockLevelQtyUrl())
        .then()
        .body("", equalTo(expectedJson.getMap("")))
        .statusCode(200)
        .log()
        .all();

    Assertions.assertEquals(
        "2001", redisTempl.opsForHash().get(SKU, "H08880011802_available").block());
  }

  @Test
  void migrationByMmsWarehouse_hasDirtyData() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithDirtyData(SKU, HYBRIS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_hasDirtyData() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithDirtyData(SKU, HYBRIS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistAvailableKey() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistAvailableKey_seqNoMatch(SKU, MMS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistAvailableKey() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistAvailableKey_seqNoMatch(SKU, MMS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistStatusKey() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistStatusKey_seqNoMatch(SKU, MMS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistStatusKey() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistStatusKey_seqNoMatch(SKU, MMS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistTimeKey() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistTimeKey_seqNoMatch(SKU, MMS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistTimeKey() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistTimeKey_seqNoMatch(SKU, MMS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistAvailableKey_seqNoNotMatch() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistAvailableKey_seqNoNotMatch(SKU);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistAvailableKey_seqNoNotMatch() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistAvailableKey_seqNoNotMatch(SKU);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistStatusKey_seqNoNotMatch() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistStatusKey_seqNoNotMatch(SKU);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistStatusKey_seqNoNotMatch() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistStatusKey_seqNoNotMatch(SKU);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistTimeKey_seqNoNotMatch() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistTimeKey_seqNoNotMatch(SKU);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistTimeKey_seqNoNotMatch() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistTimeKey_seqNoNotMatch(SKU);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistAvailableKey_multiSeqNo() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistAvailableKey_multiSeqNo(SKU, MMS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistAvailableKey_multiSeqNo() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistAvailableKey_multiSeqNo(SKU, MMS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistStatusKey_multiSeqNo() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistStatusKey_multiSeqNo(SKU, MMS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistStatusKey_multiSeqNo() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistStatusKey_multiSeqNo(SKU, MMS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_onlyExistTimeKey_multiSeqNo() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistTimeKey_multiSeqNo(SKU, MMS_SEQ_NO);
    // migrationByMMSWh();
  }

  @Test
  void migrationByHybrisWarehouse_onlyExistTimeKey_multiSeqNo() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    redisUtil.insertHybrisDataWithLossData_onlyExistTimeKey_multiSeqNo(SKU, MMS_SEQ_NO);
    migrationByHybrisWh();
  }

  @Test
  void migrationByMmsWarehouse_skuNotExist() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    // migrationByMMSWh();
    Assertions.assertNotEquals(Boolean.TRUE, redisTempl.hasKey(SKU).block());
  }

  @Test
  void migrationByHybrisWarehouse_skuNotExist() {
    redisUtil.deleteUuidAndInventoryUuidAndSku(UUID, SKU);
    migrationByHybrisWh();
    Assertions.assertEquals(Boolean.TRUE, redisTempl.hasKey(SKU).block());
  }
}
