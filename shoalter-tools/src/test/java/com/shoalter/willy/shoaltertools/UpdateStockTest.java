package com.shoalter.willy.shoaltertools;

import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@SpringBootTest
@Slf4j
public class UpdateStockTest {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisHKTVTemplate")
  ReactiveRedisTemplate<String, String> redisHKTVTempl;

  @Autowired private RedisUtil redisUtil;
  @Autowired private ApiUtil apiUtil;

  @Test
  void addMallOrUuidNonShare_testcase0001() throws JSONException {
    // 正常建立nonShare的資料
    String uuid = "testAddMall-uuid-0000-0001";
    String sku = "H9208001_S_TEST_0001";
    String warehouse = "H920800191";

    HashMap<String, Object> requestMap = new HashMap<>();
    requestMap.put("uuid", uuid);
    requestMap.put("sku", sku);
    requestMap.put("warehouse", warehouse);
    requestMap.put("quantity", 0);
    requestMap.put("stockStatus", "notSpecified");

    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    apiUtil.addMallOrUuidStockLevelsV1(requestMap);

    // verify
    Assertions.assertEquals(warehouse, redisTempl.opsForHash().get(uuid, "hktvwarehouse").block());
    Assertions.assertEquals(sku, redisTempl.opsForHash().get(uuid, "hktvsku").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get(sku, warehouse + "_available").block());
    Assertions.assertEquals(
        "notSpecified", redisTempl.opsForHash().get(sku, warehouse + "_instockstatus").block());
    Assertions.assertEquals(uuid, redisTempl.opsForHash().get(sku, "uuid").block());
  }

  @Test
  void addMallOrUuidNonShare_testcase0002() throws JSONException {
    // 已有IIMS資料的情況下正常建立nonShare的資料
    String uuid = "testAddMall-uuid-0000-0001";
    String sku = "H9208001_S_TEST_0001";
    String warehouse = "H920800191";

    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    HashMap<String, Object> hybrisDataMap = new HashMap<>();
    hybrisDataMap.put(warehouse + "_available", "3");
    hybrisDataMap.put(warehouse + "_instockstatus", "notSpecified");
    hybrisDataMap.put(warehouse + "_updatestocktime", "20240325173611");

    redisHKTVTempl.opsForHash().putAll(sku, hybrisDataMap).block();

    HashMap<String, Object> requestMap = new HashMap<>();
    requestMap.put("uuid", uuid);
    requestMap.put("sku", sku);
    requestMap.put("warehouse", warehouse);
    requestMap.put("stockStatus", "notSpecified");
    requestMap.put("quantity", 0);

    apiUtil.addMallOrUuidStockLevelsV1(requestMap);

    // verify
    Assertions.assertEquals(warehouse, redisTempl.opsForHash().get(uuid, "hktvwarehouse").block());
    Assertions.assertEquals(sku, redisTempl.opsForHash().get(uuid, "hktvsku").block());
    Assertions.assertEquals(
        "3", redisTempl.opsForHash().get(sku, warehouse + "_available").block());
    Assertions.assertEquals(
        "notSpecified", redisTempl.opsForHash().get(sku, warehouse + "_instockstatus").block());
    Assertions.assertEquals(uuid, redisTempl.opsForHash().get(sku, "uuid").block());
  }
}
