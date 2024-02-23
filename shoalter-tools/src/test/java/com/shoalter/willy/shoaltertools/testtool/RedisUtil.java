package com.shoalter.willy.shoaltertools.testtool;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  public void deleteInventoryUuid(String... uuids) {
    for (String uuid : uuids) {
      redisTempl.delete("inventory:" + uuid).block();
    }
  }

  public void deleteSku(String... keys) {
    for (String key : keys) {
      redisTempl.delete(key).block();
    }
  }

  public void insertIidsAndSkuIimsDataInRedis(String uuid, String sku, String seqNo) {
    Map<String, String> iidsData = BuildDtoUtil.buildIidsPm20hktvData(sku, seqNo);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo);
    redisTempl.opsForHash().putAll("inventory:" + uuid, iidsData).block();
    redisTempl.opsForHash().putAll(sku, iimsData).block();
  }

  public void insert_iids_skuiims_parent_data_in_redis(String uuid, String sku, String seqNo) {
    String iidsKey = "inventory:" + uuid;
    Map<String, String> iidsData = BuildDtoUtil.buildIidsParenthktvData(sku, seqNo);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo);
    redisTempl.opsForHash().putAll(iidsKey, iidsData).block();
    redisTempl.opsForHash().putAll(sku, iimsData).block();
  }
}
