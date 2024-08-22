package com.shoalter.willy.shoaltertools.testtool;

import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.createBundleParentKey;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.createBundleSettingKey;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.getAbandonedRedisNodeKeys;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.getBundleLockParentRedisKey;
import static com.shoalter.willy.shoaltertools.testtool.SystemConstants.getRedisNodeKeys;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  public void deleteInventoryUuid(String... uuids) {
    for (String uuid : uuids) {
      redisTempl.delete("inventory:" + uuid).block();
      redisTempl.delete(uuid).block();
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
    buildIidsData(uuid, sku, seqNo, "2400");
  }

  public void insertIidsAndSkuIimsData(String uuid, String sku, String seqNo, String qty) {
    buildIidsData(uuid, sku, seqNo, qty);
  }

  public void buildIidsData(String uuid, String sku, String seqNo, String qty) {
    Map<String, String> iidsData = BuildDtoUtil.buildIidsPm20hktvData(sku, seqNo);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo, qty);
    redisTempl.opsForHash().putAll("inventory:" + uuid, iidsData).block();
    redisTempl.opsForHash().putAll(sku, iimsData).block();
  }

  public void insertIidsAndSkuIimsParentData(String uuid, String sku, String seqNo) {
    insertIidsAndIimsParentData(uuid, sku, seqNo, "2400");
  }

  public void insertIidsAndSkuIimsParentData(String uuid, String sku, String seqNo, String qty) {
    insertIidsAndIimsParentData(uuid, sku, seqNo, qty);
  }

  public void insertIidsAndIimsParentData(String uuid, String sku, String seqNo, String qty) {
    String iidsKey = "inventory:" + uuid;
    Map<String, String> iidsData = BuildDtoUtil.buildIidsParenthktvData(sku, seqNo);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo, qty);
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

  public void insertIidsV1DataAndSkuIimsData(String uuid, String sku, String seqNo) {
    buildIidsV1AndIimsData(uuid, sku, seqNo, "2400");
  }

  public void buildIidsV1AndIimsData(String uuid, String sku, String seqNo, String qty) {
    Map<String, String> iidsData = BuildDtoUtil.buildIidsV1Data(sku, seqNo, qty);
    Map<String, String> iimsData = BuildDtoUtil.buildSkuIimsData(uuid, seqNo, qty);
    redisTempl.opsForHash().putAll(uuid, iidsData).block();
    redisTempl.opsForHash().putAll(sku, iimsData).block();
  }

  public void simulateHybrisChangeWhTo98() {
    DefaultRedisScript<String> script = new DefaultRedisScript<>();
    script.setScriptSource(
        new ResourceScriptSource(new ClassPathResource("/lua/simulateHybrisChangeWhTo98.lua")));
    script.setResultType(String.class);

    redisTempl.execute(script, List.of("H088800118_S_10021451M")).next().block();
  }

  public void deleteUuidAndInventoryUuidAndSku(String uuid, String sku) {
    redisTempl.delete("inventory:" + uuid, uuid, sku).block();
  }

  public void insertHybrisData(String sku, String seqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + seqNo + "_available", "2000");
    insertMap.put("H088800118" + seqNo + "_instockstatus", "null");
    insertMap.put("H088800118" + seqNo + "_updatestocktime", "20240101000000");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithDirtyData(String sku, String seqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + seqNo + "_available", "2000");
    insertMap.put("H088800118" + seqNo + "_instockstatus", "null");
    insertMap.put("H088800118" + seqNo + "_updatestocktime", "20240101000000");
    insertMap.put("updatestocktime", "20230101000000");
    insertMap.put("dirty", "CUFM");

    insertMap.put("H08880011898_instockstatus", "null");
    insertMap.put("H08880011898_updatestocktime", "20240101000000");

    insertMap.put("H08880011897_available", "0");
    insertMap.put("H08880011897_updatestocktime", "20240101000000");

    insertMap.put("H08880011896_updatestocktime", "20240101000000");
    insertMap.put("H08880011896_available", "2");
    insertMap.put("H08880011896_instockstatus", "null");

    insertMap.put("H08880011895_available", "3");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistAvailableKey_seqNoNotMatch(String sku) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H08880011895_available", "3");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistStatusKey_seqNoNotMatch(String sku) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H08880011895_instockstatus", "null");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistTimeKey_seqNoNotMatch(String sku) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H08880011895_updatestocktime", "20230101000000");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistAvailableKey_seqNoMatch(
      String sku, String mmsSeqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + mmsSeqNo + "_available", "3");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistStatusKey_seqNoMatch(
      String sku, String mmsSeqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + mmsSeqNo + "_instockstatus", "null");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistTimeKey_seqNoMatch(
      String sku, String mmsSeqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + mmsSeqNo + "_updatestocktime", "20230101000000");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistAvailableKey_multiSeqNo(
      String sku, String mmsSeqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + mmsSeqNo + "_available", "3");
    insertMap.put("H08880011895_available", "3");
    insertMap.put("H08880011896_available", "3");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistStatusKey_multiSeqNo(
      String sku, String mmsSeqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + mmsSeqNo + "_instockstatus", "null");
    insertMap.put("H08880011895_instockstatus", "null");
    insertMap.put("H08880011896_instockstatus", "null");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }

  public void insertHybrisDataWithLossData_onlyExistTimeKey_multiSeqNo(
      String sku, String mmsSeqNo) {
    Map<String, String> insertMap = new HashMap<>();
    insertMap.put("H088800118" + mmsSeqNo + "_updatestocktime", "20230101000000");
    insertMap.put("H08880011895_updatestocktime", "20230101000000");
    insertMap.put("H08880011896_updatestocktime", "20230101000000");
    redisTempl.opsForHash().putAll(sku, insertMap).block();
  }
}
