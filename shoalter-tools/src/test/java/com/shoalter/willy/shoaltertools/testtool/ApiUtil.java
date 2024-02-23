package com.shoalter.willy.shoaltertools.testtool;

import static io.restassured.RestAssured.given;

import org.springframework.stereotype.Component;

@Component
public class ApiUtil {
  private String getLocalIidsUrl() {
    return "http://127.0.0.1:8099";
  }

  private String getLocalUpdWhQtyUrl() {
    return getLocalIidsUrl() + "/s2s/v3/warehouse/quantity";
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
}
