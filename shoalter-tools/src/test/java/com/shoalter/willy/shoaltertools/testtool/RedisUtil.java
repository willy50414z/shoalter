package com.shoalter.willy.shoaltertools.testtool;

import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.createBundleParentKey;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.createBundleSettingKey;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.getAbandonedRedisNodeKeys;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.getBundleLockParentRedisKey;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.getRedisNodeKeys;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  public void deleteRedisNodeKey() {
    redisTempl.delete(getRedisNodeKeys().toArray(new String[0])).block();
    redisTempl.delete(getAbandonedRedisNodeKeys().toArray(new String[0])).block();
  }

  public void deleteBundleParentKey(String... childUuids) {
    for (String uuid : childUuids) {
      redisTempl.delete(createBundleParentKey(uuid)).block();
    }
  }

  public void deleteBundleSettingKey(String... parentUuids) {
    for (String uuid : parentUuids) {
      redisTempl.delete(createBundleSettingKey(uuid)).block();
    }
  }

  public void deleteBundleLockParentRedisKey() {
    redisTempl.delete(getBundleLockParentRedisKey()).block();
  }

  public void insertIidsAndSkuIimsData(String uuid, String sku, String seqNo) {
    Map<String, String> iidsData = BuildDtoUtil.buildIidsPm20hktvData(sku, seqNo);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo);
    redisTempl.opsForHash().putAll("inventory:" + uuid, iidsData).block();
    redisTempl.opsForHash().putAll(sku, iimsData).block();
  }

  public void insertIidsAndSkuIimsParentData(String uuid, String sku, String seqNo) {
    String iidsKey = "inventory:" + uuid;
    Map<String, String> iidsData = BuildDtoUtil.buildIidsParenthktvData(sku, seqNo);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo);
    redisTempl.opsForHash().putAll(iidsKey, iidsData).block();
    redisTempl.opsForHash().putAll(sku, iimsData).block();
  }

  public void insertBundleParentKey(String childUuid, String... parentUuids) {
    redisTempl.opsForSet().add(createBundleParentKey(childUuid), parentUuids).block();
  }

  public void insertBundleSettingKey(String parentUuid, String value) {
    redisTempl.opsForValue().set(createBundleSettingKey(parentUuid), value).block();
  }

  public void insertBundleLockParentData_withSec(String parentSku, int sec) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    redisTempl
        .opsForHash()
        .put(
            getBundleLockParentRedisKey(),
            parentSku,
            LocalDateTime.now().plusSeconds(sec).format(dtf))
        .block();
  }

  public void insertSkusInSpecifyRedisNode(String key, String... skus) {
    redisTempl.opsForSet().add(key, skus).block();
  }
}
