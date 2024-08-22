package com.shoalter.willy.shoaltertools.testtool;

import com.shoalter.willy.shoaltertools.model.IidsModel;
import com.shoalter.willy.shoaltertools.model.IimsModel;
import com.shoalter.willy.shoaltertools.model.MmsProductEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MigrationByMMSWhUtil {

  private Map<String, Object> iimsProductValueMap = new HashMap<>();

  private Map<String, Object> iidsProductValueMap = new HashMap<>();

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  public List<IimsModel> iimsWorker(List<MmsProductEntity> products) {
    List<IimsModel> returnData = new ArrayList<>();
    String dateTime = "20240101000000";

    for (MmsProductEntity product : products) {
      String warehouseId;
      try {
        warehouseId = generateWarehouseId(product);
      } catch (IllegalArgumentException e) {
        log.error("can't get warehouseId, mmsId[" + product.getMmsId() + "]", e);
        continue;
      }

      try {
        IimsModel iimsModel = iimsPutProduct(product, warehouseId, dateTime);
        returnData.add(iimsModel);
      } catch (Exception e) {
        log.error("can't put non-shared product: , product[" + product + "]", e);
      }
    }
    return returnData;
  }

  private String generateWarehouseId(MmsProductEntity product) {
    if (StringUtils.isEmpty(product.getStorefrontStoreCode())
        || StringUtils.isEmpty(product.getWarehouseSeqNumber())) {
      log.error("can't find StorefrontStoreCode, product[{}]", product);
      throw new IllegalArgumentException("can't find StorefrontStoreCode");
    }
    return product.getStorefrontStoreCode()
        + (product.getWarehouseSeqNumber().length() == 1 ? "0" : "")
        + product.getWarehouseSeqNumber();
  }

  private IimsModel iimsPutProduct(
      MmsProductEntity product, String mmsWarehouseId, String dateTime) {
    if (Boolean.FALSE.equals(redisTempl.hasKey(product.getStoreSkuId()).block())) {
      throw new IllegalArgumentException("can't find sku[" + product.getStoreSkuId() + "]");
    }

    Map<String, String> iimsData =
        redisTempl
            .<String, String>opsForHash()
            .entries(product.getStoreSkuId())
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey(), entry -> StringUtils.defaultString(entry.getValue())))
            .block();
    String iimsWarehouseId = getIimsWarehousId(iimsData, product, mmsWarehouseId);
    String share = StringUtils.isNotEmpty(iimsData.get("share")) ? iimsData.get("share") : "0";

    if (iimsData.get("uuid") != null && !iimsData.get("uuid").equals(product.getUuid())) {
      log.warn(
          "sku["
              + product.getStoreSkuId()
              + "] exist uuid is ["
              + iimsData.get("uuid")
              + "], MMS uuid["
              + product.getUuid()
              + "], use MMS uuid");
    }

    if (StringUtils.isEmpty(iimsData.get("share")) || StringUtils.isEmpty(iimsData.get("uuid"))) {
      iimsProductValueMap.clear();
      iimsProductValueMap.put("share", share);
      iimsProductValueMap.put("uuid", product.getUuid());
      iimsProductValueMap.put(iimsWarehouseId + "_updatestocktime", dateTime);
      redisTempl.opsForHash().putAll(product.getStoreSkuId(), iimsProductValueMap).block();
    }
    String status =
        StringUtils.defaultString(iimsData.get(iimsWarehouseId + "_instockstatus"), "notSpecified");
    status = status.equals("null") ? "notSpecified" : status;
    return new IimsModel(
        product.getStoreSkuId(),
        product.getStorefrontStoreCode(),
        product.getWarehouseSeqNumber(),
        product.getUuid(),
        share,
        status,
        iimsData.get(iimsWarehouseId + "_available"),
        iimsWarehouseId,
        product.getWarehouseSeqNumbers(),
        product.getMmsId());
  }

  private String getIimsWarehousId(
      Map<String, String> iimsData, MmsProductEntity product, String mmsWarehouseId) {
    String warehouseId = "";
    for (String key : iimsData.keySet()) {
      if (key.contains("_available")) {
        warehouseId = key.split("_available")[0];
        if (StringUtils.isNotEmpty(iimsData.get(warehouseId + "_updatestocktime"))
            && StringUtils.isNotEmpty(iimsData.get(warehouseId + "_instockstatus"))) {
          if (!warehouseId.equals(mmsWarehouseId)) {
            log.warn(
                "sku["
                    + product.getStoreSkuId()
                    + "] warehouseId is ["
                    + warehouseId
                    + "], MMS warehouseId["
                    + mmsWarehouseId
                    + "], use warehouseId["
                    + warehouseId
                    + "]");
          }
          return warehouseId;
        }
      }
    }

    if (StringUtils.isEmpty(warehouseId)) {
      log.error(
          "can't get warehouseId from IIMS, use mms warehouseId, iimsData[{}]mmsWarehouseId[{}]",
          iimsData,
          mmsWarehouseId);
      return mmsWarehouseId;
    }

    log.warn(
        "sku["
            + product.getStoreSkuId()
            + "] exist multi warehouseId, use warehouseId["
            + warehouseId
            + "]");
    return warehouseId;
  }

  public List<IidsModel> iidsWorker(List<IimsModel> iimsModels) {
    List<IidsModel> returnData = new ArrayList<>();
    String dateTime = "20240101000000";

    for (IimsModel iimsModel : iimsModels) {
      String warehouseId;
      try {
        warehouseId = iimsModel.getWarehouseId();
      } catch (IllegalArgumentException e) {
        // 沒有warehouse的產品是為髒資料，skip
        continue;
      } catch (Exception e) {
        log.error("can't get warehouseId, iimsModel[" + iimsModel + "]", e);
        continue;
      }
      try {
        iidsPutProduct(iimsModel, warehouseId, dateTime);
      } catch (Exception e) {
        log.error(
            "put non-shared product fail, product iimsModel["
                + iimsModel
                + "], product mms id["
                + iimsModel.getMmsId()
                + "]",
            e);
      }
    }

    return returnData;
  }

  public void iidsPutProduct(IimsModel iimsModel, String warehouseId, String dateTime) {
    // 檢查IIMS IIDS sharemode 不一樣 丟 warning msg, IIMS 為準
    String createTime =
        isShareModeSync(
            iimsModel.getUuid(), iimsModel.getSkuId(), iimsModel.getShare().equals("1"));

    // prevent dirty data, delete then insert
    redisTempl.delete(iimsModel.getUuid()).block();
    redisTempl.delete("inventory:" + iimsModel.getUuid()).block();

    String sharemall = iimsModel.getShare().equals("1") ? "hktv" : "";
    String nonsharemall = iimsModel.getShare().equals("0") ? "hktv" : "";
    String quantity = iimsModel.getShare().equals("1") ? iimsModel.getAvailable() : "0";
    String instockstatus =
        iimsModel.getShare().equals("1") ? iimsModel.getStatus() : "notSpecified";

    // original data structure
    iidsProductValueMap.clear();
    iidsProductValueMap.put("hktvsku", iimsModel.getSkuId());
    iidsProductValueMap.put("hktvwarehouse", warehouseId);
    iidsProductValueMap.put("instockstatus", instockstatus);
    iidsProductValueMap.put("nonsharemall", nonsharemall);
    iidsProductValueMap.put("quantity", quantity);
    iidsProductValueMap.put("sharemall", sharemall);
    iidsProductValueMap.put("updatestocktime", dateTime);
    redisTempl.opsForHash().putAll(iimsModel.getUuid(), iidsProductValueMap).block();
    // PM2.0 data structure
    iidsProductValueMap.clear();
    iidsProductValueMap.put(iimsModel.getWarehouseSeqNumber() + "_mall", "hktv");
    iidsProductValueMap.put(iimsModel.getWarehouseSeqNumber() + "_qty", quantity);
    iidsProductValueMap.put("create_time", StringUtils.defaultString(createTime, dateTime));
    iidsProductValueMap.put("update_time", dateTime);
    iidsProductValueMap.put("hktv_instockstatus", instockstatus);
    iidsProductValueMap.put("hktv_share", iimsModel.getShare());
    iidsProductValueMap.put("hktv_sku", iimsModel.getSkuId());
    iidsProductValueMap.put("hktv_store_code", iimsModel.getStorefrontStoreCode());
    for (String seqNo : iimsModel.getWarehouseSeqNumbers()) {
      iidsProductValueMap.put(seqNo + "_mall", "");
      iidsProductValueMap.put(seqNo + "_qty", "0");
    }
    try {
      redisTempl
          .opsForHash()
          .putAll("inventory:" + iimsModel.getUuid(), iidsProductValueMap)
          .block();
    } catch (Exception e) {
      log.error(
          "put redis failed, key["
              + "inventory:"
              + iimsModel.getUuid()
              + "]productValueMap["
              + iidsProductValueMap
              + "]");
    }
  }

  private String isShareModeSync(String uuid, String sku, boolean isIimsShare) {
    if (Boolean.FALSE.equals(redisTempl.hasKey(uuid).block())) {
      return null;
    }

    Map<String, String> iidsData =
        redisTempl
            .<String, String>opsForHash()
            .entries(uuid)
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey(), entry -> StringUtils.defaultString(entry.getValue())))
            .block();

    if (iidsData.get("sharemall") != null && iidsData.get("sharemall").contains("hktv")) {
      if (isIimsShare) {
        return iidsData.get("create_time");
      } else {
        log.warn(
            "uuid["
                + uuid
                + "] is share but sku["
                + sku
                + "] is non share, uuid switch to non share mode]");
      }
    }

    if (iidsData.get("nonsharemall") != null && iidsData.get("nonsharemall").contains("hktv")) {
      if (isIimsShare) {
        log.warn(
            "uuid["
                + uuid
                + "] is non share but sku["
                + sku
                + "] is share, uuid switch to share mode]");
      }
    }
    return iidsData.get("create_time");
  }
}
