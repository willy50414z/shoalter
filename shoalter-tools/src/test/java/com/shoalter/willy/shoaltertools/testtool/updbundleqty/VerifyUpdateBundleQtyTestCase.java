package com.shoalter.willy.shoaltertools.testtool.updbundleqty;

import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import com.shoalter.willy.shoaltertools.testtool.SystemConstants;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class VerifyUpdateBundleQtyTestCase {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  public void resetNodeListWhenUpdBundleChildQtyCrossNode() {
    redisTempl
        .opsForSet()
        .members(SystemConstants.getRedisNodeKeys().get(0))
        .collectList()
        .doOnNext(
            skus ->
                Assertions.assertFalse(
                    skus.contains("H088800118_S_child-SKU-E-1")
                        && skus.contains("H088800118_S_child-SKU-E-2")))
        .block();
    Assertions.assertEquals(
        "1200",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-2", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get("SKU-E001", "H08880011898_available").block());
  }

  public void replenishChildQtyNotCrash() {
    Assertions.assertEquals(
        "1200",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-2-2", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-4-4", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get("SKU-E001-1", "H08880011898_available").block());
  }

  public void parentQtyNotEnoughToDeduct() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("SKU-E001-1", "H08880011898_available").block());
  }

  public void resetNodeListWhenReplenishQtyCrossNode() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "1200",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-2", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "1200",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-3", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-4", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "1200",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-5", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "3600", redisTempl.opsForHash().get("SKU-E001-1", "H08880011898_available").block());
  }

  public void parentQtyNotChange() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("SKU-E001-1", "H08880011898_available").block());
  }
}
