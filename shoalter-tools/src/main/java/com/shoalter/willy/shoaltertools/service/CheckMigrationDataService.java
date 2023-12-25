package com.shoalter.willy.shoaltertools.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CheckMigrationDataService {
  public void checkMigration(
      Map<String, String> oldStockLevelMap,
      Map<String, String> newStockLevelMap,
      Map<String, String> iimsStockLevelMap,
      String uuid) {
    if (oldStockLevelMap.isEmpty() || newStockLevelMap.isEmpty() || iimsStockLevelMap.isEmpty()) {
      log.error("data is not exist!!!");
      return;
    }
    Boolean correct = Boolean.TRUE;
    try {
      String nonsharemall = oldStockLevelMap.get("nonsharemall");
      String sharemall = oldStockLevelMap.get("sharemall");
      String hktvwarehouse = oldStockLevelMap.get("hktvwarehouse");
      String seqNo = hktvwarehouse.substring(hktvwarehouse.length() - 2);
      String storeCode = hktvwarehouse.split(seqNo)[0];
      String instockstatus = oldStockLevelMap.get("instockstatus");
      String hktvsku = oldStockLevelMap.get("hktvsku");
      String quantity = oldStockLevelMap.get("quantity");

      // 舊資料格式固定長度是7
      if (oldStockLevelMap.size() != 7
          || !oldStockLevelMap.containsKey("nonsharemall")
          || !oldStockLevelMap.containsKey("hktvwarehouse")
          || !oldStockLevelMap.containsKey("instockstatus")
          || !oldStockLevelMap.containsKey("sharemall")
          || !oldStockLevelMap.containsKey("hktvsku")
          || !oldStockLevelMap.containsKey("quantity")
          || !oldStockLevelMap.containsKey("updatestocktime")) {
        log.error("old data is dirty data, size is not 7");
        correct = Boolean.FALSE;
      }
      // sku-iims資料格式固定長度是5
      if (iimsStockLevelMap.size() != 5) {
        log.error("iims data is dirty data, size is not 5");
        correct = Boolean.FALSE;
      }
      // 新資料結構確認
      correct = isIIDSNewDataInvalidate(newStockLevelMap);
      // 舊資料格式，hktv同時存在於share跟nonshare或是同時不存在
      if ((!nonsharemall.contains("hktv") && !sharemall.contains("hktv"))
          || (nonsharemall.contains("hktv") && sharemall.contains("hktv"))) {
        log.error("old data is dirty data, product share mode can not identify");
        correct = Boolean.FALSE;
      } else {
        // IIMS/IIDS新舊資料的share有沒有一致
        String share = nonsharemall.contains("hktv") ? "0" : "1";
        if (!newStockLevelMap.get("hktv_share").equals(share)
            || !share.equals(iimsStockLevelMap.get("share"))) {
          log.error(
              "IIMS orIIDS new/old share mode not same, IIMS["
                  + iimsStockLevelMap.get("share")
                  + "]IIDS old["
                  + share
                  + "]IIDS new["
                  + newStockLevelMap.get("hktv_share")
                  + "]");
          correct = Boolean.FALSE;
        }
        //      else {
        //        // 新舊資料的share是不是都是nonshare
        //        if (share.equals("1")) {
        //          log.error("iids share mode is not non share");
        //          correct = Boolean.FALSE;
        //        }
        //      }
      }
      // 新舊資料的instockstatus有沒有一致
      if (!newStockLevelMap.get("hktv_instockstatus").equals(instockstatus)) {
        log.error("instockstatus not same");
        correct = Boolean.FALSE;
      }
      // 新舊資料的sku有沒有一致
      if (!newStockLevelMap.get("hktv_sku").equals(hktvsku)) {
        log.error("hktv sku not same");
        correct = Boolean.FALSE;
      }
      // 新舊資料的store_code有沒有一致
      if (!newStockLevelMap.get("hktv_store_code").equals(storeCode)) {
        log.error("hktv storecode not same");
        correct = Boolean.FALSE;
      }
      // 新舊資料的warehouseSeqNo有沒有一致
      if (!newStockLevelMap.get(seqNo.concat("_mall")).contains("hktv")) {
        log.error("seqNo not same");
        correct = Boolean.FALSE;
      }
      // 新舊資料的quantity有沒有一致
      if (!newStockLevelMap.get(seqNo.concat("_qty")).contains(quantity)) {
        log.error("quantity not same");
        correct = Boolean.FALSE;
      }
      // IIMS的uuid跟IIDS有沒有一致
      if (!iimsStockLevelMap.get("uuid").equals(uuid)) {
        log.error("iims uuid is not equal IIDS");
        correct = Boolean.FALSE;
      }
      //    // IIMS的share是不是nonshare => 不一定是non-share
      //    if (iimsStockLevelMap.get("share").equals("1")) {
      //      log.error("iims share mode is not non share");
      //      correct = Boolean.FALSE;
      //    }
      // IIMS的warehouseId跟IIDS有沒有一致
      String warehouseId = getIimsWarehousId(iimsStockLevelMap);
      if (StringUtils.isNotEmpty(warehouseId)
          && !oldStockLevelMap.get("hktvwarehouse").equals(warehouseId)
          && !newStockLevelMap
              .get(warehouseId.substring(warehouseId.length() - 2) + "_mall")
              .contains("hktv")) {
        log.error("iims warehouse is not equal IIDS");
        correct = Boolean.FALSE;
      }
      // IIMS的instockstatus跟IIDS有沒有一致 => share-mode才會一致
      if (!iimsStockLevelMap.get(warehouseId.concat("_instockstatus")).equals(instockstatus)
          && iimsStockLevelMap.get("share").equals("1")) {
        log.error("iims instockstatus is not equal IIDS");
        correct = Boolean.FALSE;
      }

    } catch (Exception e) {
      log.error("something exception", e);
      correct = false;
    }
    if (!correct) {
      log.info(
          "uuid[{}], oldStockLevelMap[{}], newStockLevelMap[{}], iimsStockLevelMap[{}]",
          uuid,
          oldStockLevelMap,
          newStockLevelMap,
          iimsStockLevelMap);
    }
  }

  private String getIimsWarehousId(Map<String, String> iimsData) {
    String warehouseId = "";
    for (String key : iimsData.keySet()) {
      if (key.contains("_available")) {
        warehouseId = key.split("_available")[0];
        if (StringUtils.isNotEmpty(iimsData.get(warehouseId + "_updatestocktime"))
            && StringUtils.isNotEmpty(iimsData.get(warehouseId + "_instockstatus"))) {
          return warehouseId;
        }
      }
    }
    return warehouseId;
  }

  private boolean isIIDSNewDataInvalidate(Map<String, String> newStockLevelMap) {
    boolean correct = true;
    if (!newStockLevelMap.containsKey("update_time")
        || !newStockLevelMap.containsKey("hktv_store_code")
        || !newStockLevelMap.containsKey("hktv_sku")
        || !newStockLevelMap.containsKey("hktv_share")
        || !newStockLevelMap.containsKey("hktv_instockstatus")
        || !newStockLevelMap.containsKey("create_time")) {
      log.error("new iids data is not complete");
      correct = false;
    }
    List<String> keyList = new ArrayList<>(newStockLevelMap.keySet());
    keyList.remove("update_time");
    keyList.remove("hktv_store_code");
    keyList.remove("hktv_sku");
    keyList.remove("hktv_share");
    keyList.remove("hktv_instockstatus");
    keyList.remove("create_time");
    if (keyList.isEmpty()) {
      log.error("new iids data no warehouse data");
      correct = false;
    }
    for (String key : keyList) {
      if (!(key.endsWith("qty") || key.endsWith("mall"))) {
        log.error("new iids data is not clean, it contains key[" + key + "]");
        correct = false;
      }
      // 確認qty / mall是成雙成對的
      if (key.endsWith("qty") && !newStockLevelMap.containsKey(key.split("_")[0] + "_mall")) {
        log.error(
            "new iids data miss mall data, qty key["
                + key
                + "] but no mall key["
                + key.split("_")[0]
                + "_mall"
                + "]");
        correct = false;
      }
      if (key.endsWith("mall") && !newStockLevelMap.containsKey(key.split("_")[0] + "_qty")) {
        log.error(
            "new iids data miss qty data, mall key["
                + key
                + "] but no qty key["
                + key.split("_")[0]
                + "_qty"
                + "]");
        correct = false;
      }
    }
    return correct;
  }
}
