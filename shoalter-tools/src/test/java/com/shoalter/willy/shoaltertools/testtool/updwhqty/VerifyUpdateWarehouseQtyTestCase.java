package com.shoalter.willy.shoaltertools.testtool.updwhqty;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class VerifyUpdateWarehouseQtyTestCase {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  public void lockTimeNotBlockWhenRecordTimeBiggerThanNow() {
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "7000",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-2", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "100", redisTempl.opsForHash().get("SKU-E001", "H08880011898_available").block());
  }
}
