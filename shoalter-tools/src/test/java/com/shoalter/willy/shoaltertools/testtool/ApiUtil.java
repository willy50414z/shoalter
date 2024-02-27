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

  public void callDeductBundle10QtyApi(String bundleUuid) {
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
                + "        \"qty\": 100\n"
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
}
