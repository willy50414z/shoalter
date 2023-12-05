package com.shoalter.willy.shoaltertools.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CheckMigrationDataService {
  public void checkMigration(
      Map<String, String> oldStockLevelMap,
      Map<String, String> newStockLevelMap,
      Map<String, String> iimsStockLevelMap,
      String uuid) {
    log.info(
        "oldStockLevelMap[{}], newStockLevelMap[{}], iimsStockLevelMap[{}], uuid[{}]",
        oldStockLevelMap,
        newStockLevelMap,
        iimsStockLevelMap,
        uuid);

    String nonsharemall = oldStockLevelMap.get("nonsharemall");
    String sharemall = oldStockLevelMap.get("sharemall");
    String hktvwarehouse = oldStockLevelMap.get("hktvwarehouse");
    String seqNo = hktvwarehouse.substring(hktvwarehouse.length() - 2);
    String storeCode = hktvwarehouse.split(seqNo)[0];
    String instockstatus = oldStockLevelMap.get("instockstatus");
    String hktvsku = oldStockLevelMap.get("hktvsku");
    String quantity = oldStockLevelMap.get("quantity");
    Boolean correct = Boolean.TRUE;

    // 舊資料格式固定長度是7
    if (oldStockLevelMap.size() != 7) {
      log.error("old data is dirty data, size is not 7");
      correct = Boolean.FALSE;
    }
    // sku-iims資料格式固定長度是5
    if (iimsStockLevelMap.size() != 5) {
      log.error("iims data is dirty data, size is not 5");
      correct = Boolean.FALSE;
    }
    // 舊資料格式，hktv同時存在於share跟nonshare或是同時不存在
    if ((!nonsharemall.contains("hktv") && !sharemall.contains("hktv"))
        || (nonsharemall.contains("hktv") && sharemall.contains("hktv"))) {
      log.error("old data is dirty data, product share mode can not identify");
      correct = Boolean.FALSE;
    } else {
      // 新舊資料的share有沒有一致
      String share = nonsharemall.contains("hktv") ? "0" : "1";
      if (!newStockLevelMap.get("hktv_share").equals(share)) {
        log.error("share mode not same");
        correct = Boolean.FALSE;
      } else {
        // 新舊資料的share是不是都是nonshare
        if (share.equals("1")) {
          log.error("iids share mode is not non share");
          correct = Boolean.FALSE;
        }
      }
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
    // IIMS的share是不是nonshare
    if (iimsStockLevelMap.get("share").equals("1")) {
      log.error("iims share mode is not non share");
      correct = Boolean.FALSE;
    }
    // IIMS的warehouseId跟IIDS有沒有一致
    if (iimsStockLevelMap.get(hktvwarehouse.concat("_available")) == null
        || iimsStockLevelMap.get(hktvwarehouse.concat("_instockstatus")) == null
        || iimsStockLevelMap.get(hktvwarehouse.concat("_updatestocktime")) == null) {
      log.error("iims warehouse is not equal IIDS");
      correct = Boolean.FALSE;
    } else {
      // IIMS的instockstatus跟IIDS有沒有一致
      if (!iimsStockLevelMap.get(hktvwarehouse.concat("_instockstatus")).equals(instockstatus)) {
        log.error("iims instockstatus is not equal IIDS");
        correct = Boolean.FALSE;
      }
    }
    log.info("correct[{}]", correct);
  }
}
