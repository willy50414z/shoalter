package com.shoalter.willy.shoaltertools.testtool.updbundleqty;

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

  public void resetNodeListWhenCrossNode() {
    for (String nodeKey : SystemConstants.getRedisNodeKeys()) {
      Assertions.assertEquals(Boolean.FALSE, redisTempl.hasKey(nodeKey).block());
    }
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
}
