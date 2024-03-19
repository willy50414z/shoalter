package com.shoalter.willy.shoaltertools.testtool;

import static io.restassured.RestAssured.given;

import org.springframework.stereotype.Component;

@Component
public class ApiUtil {
  private String getLocalIidsUrl() {
    return "http://127.0.0.1:8099";
  }

  public String getLocalUpdWhQtyUrl() {
    return getLocalIidsUrl() + "/s2s/v3/warehouse/quantity";
  }

  public String getLocalUpdBundleQtyUrl() {
    return getLocalIidsUrl() + "/s2s/v3/mall/bundle/quantity";
  }

  public String getLocalUpdMallStockLevelUrl() {
    return getLocalIidsUrl() + "/s2s/v3/mall/stock_levels";
  }

  public void callDeductWh4700QtyApi(String childUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + childUuid
                + "\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"quantity\": 4700\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callDeductBundle2500QtyApi(String bundleUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + bundleUuid
                + "\",\n"
                + "    \"mallQty\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"mode\": \"deduct\",\n"
                + "        \"qty\": 2500\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdBundleQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callAddBundle2400QtyApi(String bundleUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + bundleUuid
                + "\",\n"
                + "    \"mallQty\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"mode\": \"add\",\n"
                + "        \"qty\": 2400\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdBundleQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetBundle0QtyApi(String bundleUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + bundleUuid
                + "\",\n"
                + "    \"mallQty\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"qty\": 0\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdBundleQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetBundle2400QtyApi(String bundleUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + bundleUuid
                + "\",\n"
                + "    \"mallQty\": [\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"qty\": 2400\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdBundleQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetWh4900QtyApi(String childUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\": \""
                + childUuid
                + "\",\n"
                + "    \"warehouseQty\": [\n"
                + "      {\n"
                + "        \"warehouseSeqNo\": \"98\",\n"
                + "        \"mode\": \"set\",\n"
                + "        \"quantity\": 4900\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callChildFrom98ShouldAddTo98Api(String bundleUuid) {
    given()
        .contentType("application/json")
        .body(
            "[\n"
                + "  {\n"
                + "    \"uuid\":\""
                + bundleUuid
                + "\",\n"
                + "    \"mallQty\":[\n"
                + "      {\n"
                + "        \"mall\": \"hktv\",\n"
                + "        \"qty\": 0,\n"
                + "        \"mode\": \"set\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdBundleQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetChildUuid123QtyEqual233ToShare() {
    //  set childUuid1/2/3 qty=233 to share
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
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetChildUuid123Qty233ToMall() {
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
        .put(getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callUpdAllBundleQtyTo10() {
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
        .put(getLocalUpdBundleQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callDeductChild1Qty15() {
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
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callDeductChild2Qty92() {
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
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callDeductChild2Qty63() {
    // deduct child2 63
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
                + "        \"quantity\": 63\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]")
        .when()
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetWh01Qty20AndWh02Qty30(String uuid) {
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"warehouseQty\":[{\"warehouseSeqNo\":\"01\",\"mode\":\"set\",\"quantity\":20},{\"warehouseSeqNo\":\"02\",\"mode\":\"set\",\"quantity\":30}]}]")
        .when()
        .put(getLocalUpdWhQtyUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  public void callSetHktvAndLittleMallToShare(String uuid) {
    given()
        .contentType("application/json")
        .body(
            "[{\"uuid\":\""
                + uuid
                + "\",\"stockLevels\":[{\"mall\":\"hktv\",\"share\":1},{\"mall\":\"little_mall\",\"share\":1}]}]")
        .when()
        .put(getLocalUpdMallStockLevelUrl())
        .then()
        .statusCode(200)
        .log()
        .all();
  }
}
