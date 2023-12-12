package com.shoalter.willy.shoaltertools;

import static io.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
class ShoalterToolsApplicationTests {
  String baseURI;
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;

  //  @BeforeAll
  //  public void setup() throws Exception {
  //
  //    String url = System.getProperty("host");
  //    if (url == null || url.isBlank()) {
  //      url = "http://localhost:8888";
  //    }
  //    log.info("BaseUrl: {}", url);
  //    baseURI = url;
  //  }

  @Test
  void test() {
    redisTempl.opsForHash().put("key", "hkey", "vaslue").block();
    log.info("test");
    Assert.isTrue(true);
  }

  @Test
  public void getCustomerSKUList() {
    log.info("getCustomerSKUList");
    expect()
        .statusLine(containsString("200"))
        .and()
        .body(
            equalTo(
                "{\"data\":{\"success\":[{\"uuid\":\"willy230706-ba43-11ec-8422-0242ac120002\",\"shareMall\":[],\"nonShareMall\":[\"hktv\"],\"quantity\":0,\"inStockStatus\":\"notSpecified\",\"updateStockTime\":\"20230808140211\",\"skuMap\":{\"hktv\":\"willy_A12345\"},\"warehouseMap\":{\"hktv\":\"H0101010\"},\"nonShareData\":[{\"name\":\"hktv\",\"quantity\":990,\"inStockStatus\":\"notSpecified\"}]}],\"fail\":[cc]}}"))
        .given()
        .when()
        .get(
            "https://iids-restful-shoalter-dev.hkmpcl.com.hk/s2s/v1/products/willy230706-ba43-11ec-8422-0242ac120002/stock-levels");
  }

  @Test
  public void testLua() {
    final List<String> keys = List.of("6000", "6001", "6002", "6003", "6004", "6005");
    final List<String> params = new ArrayList<>();
    params.add("6000");
    params.add("6001");
    params.add("6002");
    params.add("6003");
    params.add("6004");
    params.add("6005");

    DefaultRedisScript script = new DefaultRedisScript<>();
    script.setScriptSource(
        new ResourceScriptSource(new ClassPathResource("/lua/testGet3Keys.lua")));
    script.setResultType(List.class);
    Mono<List> result = redisTempl.execute(script, keys, params).next();
    log.info("[{}]", result.block());
  }
}
