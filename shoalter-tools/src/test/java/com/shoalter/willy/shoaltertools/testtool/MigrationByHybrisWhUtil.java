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
public class MigrationByHybrisWhUtil {

  private static final String REDIS_KEY_SHARE = "share";
  private static final String REDIS_KEY_UUID = "uuid";
  private static final String IIMS_REDIS_KEY_UPDATESTOCKTIME = "_updatestocktime";
  private static final String IIMS_REDIS_KEY_INSTOCKSTATUS = "_instockstatus";
  private static final String REDIS_KEY_AVAILABLE = "_available";
  private static final String STATUS_NOTSPECIFIED = "notSpecified";

  private ArrayList<String> effectiveWarehouseIdList = new ArrayList<String>();
  private Map<String, Object> iimsProductValueMap = new HashMap<>();

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  private Map<String, Object> iidsProductValueMap = new HashMap<>();

  private static final String REDIS_KEY_HKTVSKU = "hktvsku";
  private static final String REDIS_KEY_HKTVWAREHOUSE = "hktvwarehouse";
  private static final String IIDS_REDIS_KEY_INSTOCKSTATUS = "instockstatus";
  private static final String REDIS_KEY_NONSHAREMALL = "nonsharemall";
  private static final String REDIS_KEY_SHAREMALL = "sharemall";
  private static final String REDIS_KEY_QUANTITY = "quantity";
  private static final String IIDS_REDIS_KEY_UPDATESTOCKTIME = "updatestocktime";
  private static final String INVENTORY_REDIS_KEY_CREATE_TIME = "create_time";
  private static final String INVENTORY_REDIS_KEY_UPDATE_TIME = "update_time";
  private static final String INVENTORY_REDIS_KEY_STATUS = "hktv_instockstatus";
  private static final String INVENTORY_REDIS_KEY_SHARE = "hktv_share";
  private static final String INVENTORY_REDIS_SKU = "hktv_sku";
  private static final String INVENTORY_REDIS_STORE_CODE = "hktv_store_code";
  private static final String INVENTORY_KEY = "inventory:";
  private static final String INVENTORY_REDIS_KEY_SUFFIX_MALL = "_mall";
  private static final String INVENTORY_REDIS_KEY_SUFFIX_QTY = "_qty";

  public List<IimsModel> iimsWorker(List<MmsProductEntity> products) {
    List<IimsModel> returnData = new ArrayList<>();
    String dateTime = "20240101000000";

    for (MmsProductEntity product : products) {
      log.info(
          "start iims migration. sku[{}] merchantId[{}}]",
          product.getStoreSkuId(),
          product.getMerchantId());
      String warehouseId;
      try {
        warehouseId = generateWarehouseId(product);
      } catch (IllegalArgumentException e) {
        log.error("can't get warehouseId, mmsId[{}]", product.getMmsId(), e);
        continue;
      }

      try {
        IimsModel iimsModel = iidsPutProduct(product, warehouseId, dateTime);
        returnData.add(iimsModel);
      } catch (Exception e) {
        log.error("can't put non-shared product: , product[{}]", product, e);
      }
    }
    return returnData;
  }

  private String generateWarehouseId(MmsProductEntity product) {
    if (StringUtils.isEmpty(product.getStorefrontStoreCode())
        || StringUtils.isEmpty(product.getWarehouseSeqNumber())) {
      if (Boolean.TRUE.equals(hasIimsInfo(product.getStoreSkuId()))) {
        log.error(
            "can't find StorefrontStoreCode or WarehouseSeqNumber, use IIMS info to build data, product[{}]",
            product);
        return "";
      } else {
        log.error("can't find StorefrontStoreCode or WarehouseSeqNumber, product[{}]", product);
        throw new IllegalArgumentException("can't find StorefrontStoreCode or WarehouseSeqNumber");
      }
    }
    return product.getStorefrontStoreCode()
        + (product.getWarehouseSeqNumber().length() == 1 ? "0" : "")
        + product.getWarehouseSeqNumber();
  }

  private boolean hasIimsInfo(String sku) {
    return Boolean.TRUE.equals(redisTempl.hasKey(sku).block());
  }

  private IimsModel iidsPutProduct(
      MmsProductEntity product, String mmsWarehouseId, String dateTime) {
    if (Boolean.FALSE.equals(hasIimsInfo(product.getStoreSkuId()))) {
      log.warn(
          "Migration can't find target sku in IIMS, create new data by MMS info, sku[{}]",
          product.getStoreSkuId());

      iimsProductValueMap.put(REDIS_KEY_SHARE, "0");
      iimsProductValueMap.put(REDIS_KEY_UUID, product.getUuid());
      iimsProductValueMap.put(mmsWarehouseId + IIMS_REDIS_KEY_UPDATESTOCKTIME, dateTime);
      iimsProductValueMap.put(mmsWarehouseId + IIMS_REDIS_KEY_INSTOCKSTATUS, STATUS_NOTSPECIFIED);
      iimsProductValueMap.put(mmsWarehouseId + REDIS_KEY_AVAILABLE, "0");
      redisTempl.opsForHash().putAll(product.getStoreSkuId(), iimsProductValueMap).block();

      return new IimsModel(
          product.getStoreSkuId(),
          product.getStorefrontStoreCode(),
          product.getWarehouseSeqNumber(),
          product.getUuid(),
          "0",
          STATUS_NOTSPECIFIED,
          "0",
          mmsWarehouseId,
          product.getWarehouseSeqNumbers(),
          product.getMmsId());

    } else {
      Map<String, String> iimsData =
          redisTempl
              .<String, String>opsForHash()
              .entries(product.getStoreSkuId())
              .collect(
                  Collectors.toMap(
                      entry -> entry.getKey(),
                      entry -> StringUtils.defaultString(entry.getValue())))
              .block();

      effectiveWarehouseIdList.clear();
      List warehouseSeqNumbers = new ArrayList(product.getWarehouseSeqNumbers());
      String squNumber = product.getWarehouseSeqNumber();

      // process warehouseId
      String iimsWarehouseId =
          processIimsWarehousId(iimsData, product.getStoreSkuId(), mmsWarehouseId, dateTime);
      if (!iimsWarehouseId.equals(mmsWarehouseId)) {
        log.warn(
            "WarehouseId not equal, use IIMS warehouseId. sku[{}] IIMS warehouseId[{}] MMS warehouseId[{}]",
            product.getStoreSkuId(),
            iimsWarehouseId,
            mmsWarehouseId);

        String iimsSeqNumber = iimsWarehouseId.substring(iimsWarehouseId.length() - 2);
        warehouseSeqNumbers.add(squNumber);
        squNumber = iimsSeqNumber;
        warehouseSeqNumbers.add(iimsSeqNumber);
      }

      buildIimsData(iimsData, product, iimsWarehouseId, dateTime);

      String share =
          StringUtils.isNotEmpty(iimsData.get(REDIS_KEY_SHARE))
              ? iimsData.get(REDIS_KEY_SHARE)
              : "0";
      String status =
          StringUtils.defaultString(
              iimsData.get(iimsWarehouseId + IIMS_REDIS_KEY_INSTOCKSTATUS), STATUS_NOTSPECIFIED);
      status = status.equals("null") ? STATUS_NOTSPECIFIED : status;

      String available =
          StringUtils.defaultString(iimsData.get(iimsWarehouseId + REDIS_KEY_AVAILABLE), "0");
      String storefrontStoreCode =
          StringUtils.isEmpty(product.getStorefrontStoreCode())
              ? iimsWarehouseId.substring(0, iimsWarehouseId.length() - 2)
              : product.getStorefrontStoreCode();

      return new IimsModel(
          product.getStoreSkuId(),
          storefrontStoreCode,
          squNumber,
          product.getUuid(),
          share,
          status,
          available,
          iimsWarehouseId,
          warehouseSeqNumbers,
          product.getMmsId());
    }
  }

  private String processIimsWarehousId(
      Map<String, String> iimsData, String sku, String mmsWarehouseId, String dateTime) {
    String warehouseId = "";
    warehouseId = getIimsWarehouseId(iimsData, sku, mmsWarehouseId, warehouseId);

    if (StringUtils.isEmpty(warehouseId)) {
      log.error(
          "can't get warehouseId from IIMS, use MMS warehouseId, sku[{}] iimsData[{}] mmsWarehouseId[{}]",
          sku,
          iimsData,
          mmsWarehouseId);

      iimsData.put(mmsWarehouseId + IIMS_REDIS_KEY_INSTOCKSTATUS, STATUS_NOTSPECIFIED);
      iimsData.put(mmsWarehouseId + REDIS_KEY_AVAILABLE, "0");
      iimsData.put(mmsWarehouseId + IIMS_REDIS_KEY_UPDATESTOCKTIME, dateTime);
      warehouseId = mmsWarehouseId;
    }
    effectiveWarehouseIdList.add(warehouseId);
    return warehouseId;
  }

  private String getIimsWarehouseId(
      Map<String, String> iimsData, String sku, String mmsWarehouseId, String warehouseId) {

    List<String> availableKeyList =
        iimsData.entrySet().stream()
            .filter(entry -> entry.getKey().contains(REDIS_KEY_AVAILABLE))
            .map(x -> x.getKey())
            .collect(Collectors.toList());

    if (availableKeyList.size() > 1) {
      availableKeyList.sort(
          (e1, e2) -> {
            Integer e1Qty = Integer.parseInt(iimsData.get(e1));
            Integer e2Qty = Integer.parseInt(iimsData.get(e2));
            return e2Qty.compareTo(e1Qty);
          });

      availableKeyList.forEach(
          key -> {
            // 有多個Available存在時, 若庫存 > 0就加入要保留的清單 effectiveWarehouseIdList
            if (Integer.parseInt(iimsData.get(key)) > 0) {
              effectiveWarehouseIdList.add(key.split(REDIS_KEY_AVAILABLE)[0]);
            }
          });

      // 有多個Available存在時 以和mmsWarehouseId一樣的優先, 放在第一個, 再來是有庫存的
      if (availableKeyList.contains(mmsWarehouseId + REDIS_KEY_AVAILABLE)) {
        availableKeyList.add(0, mmsWarehouseId + REDIS_KEY_AVAILABLE);
      }
    }

    for (String key : availableKeyList) {
      warehouseId = key.split(REDIS_KEY_AVAILABLE)[0];

      // 檢查是否是完整的資料
      if (StringUtils.isNotEmpty(iimsData.get(warehouseId + IIMS_REDIS_KEY_INSTOCKSTATUS))) {
        if (availableKeyList.size() > 1) {
          log.warn(
              "sku[{}] exist multi warehouseId, use warehouseId[{}] iimsData[{}]",
              sku,
              warehouseId,
              iimsData);
        }
        return warehouseId;
      }
    }
    return warehouseId;
  }

  private void buildIimsData(
      Map<String, String> iimsData, MmsProductEntity product, String warehouseId, String dateTime) {

    redisTempl.delete(product.getStoreSkuId()).block();
    iimsProductValueMap.clear();

    // 要保留的有效warehouseId(有庫存的) 有什麼資料就留什麼資料
    List.of(REDIS_KEY_AVAILABLE, IIMS_REDIS_KEY_INSTOCKSTATUS, IIMS_REDIS_KEY_UPDATESTOCKTIME)
        .forEach(
            regex -> {
              effectiveWarehouseIdList.forEach(
                  effectiveWarehouseId -> {
                    if (StringUtils.isNotEmpty(iimsData.get(effectiveWarehouseId + regex))) {
                      iimsProductValueMap.put(
                          effectiveWarehouseId + regex, iimsData.get(effectiveWarehouseId + regex));
                    }
                  });
            });

    // 最終使用的 warehouseId 原本缺什麼資料就補上什麼資料
    if (StringUtils.isEmpty(iimsData.get(warehouseId + IIMS_REDIS_KEY_UPDATESTOCKTIME))) {
      iimsProductValueMap.put(warehouseId + IIMS_REDIS_KEY_UPDATESTOCKTIME, dateTime);
    }

    if (StringUtils.isEmpty(iimsData.get(warehouseId + IIMS_REDIS_KEY_INSTOCKSTATUS))) {
      iimsProductValueMap.put(warehouseId + IIMS_REDIS_KEY_INSTOCKSTATUS, STATUS_NOTSPECIFIED);
    }

    // 缺了SHARE或UUID就補上去
    if (StringUtils.isEmpty(iimsData.get(REDIS_KEY_SHARE))
        || StringUtils.isEmpty(iimsData.get(REDIS_KEY_UUID))) {
      iimsProductValueMap.put(REDIS_KEY_SHARE, "0");
      iimsProductValueMap.put(REDIS_KEY_UUID, product.getUuid());
    } else {
      // 若是沒有缺上面兩個就補上原本的資料, 並在補上前檢查UUID是否與MMS一致, 不一致時IIMS使用MMS UUID
      iimsProductValueMap.put(REDIS_KEY_SHARE, iimsData.get(REDIS_KEY_SHARE));

      if (StringUtils.isNotEmpty(iimsData.get(REDIS_KEY_UUID))
          && !iimsData.get(REDIS_KEY_UUID).equals(product.getUuid())) {
        log.warn(
            "Uuid not equal, use MMS uuid. sku[{}] exist uuid[{}] MMS uuid[{}]",
            product.getStoreSkuId(),
            iimsData.get(REDIS_KEY_UUID),
            product.getUuid());
        iimsProductValueMap.put(REDIS_KEY_UUID, product.getUuid());
      } else {
        iimsProductValueMap.put(REDIS_KEY_UUID, iimsData.get(REDIS_KEY_UUID));
      }
    }

    redisTempl.opsForHash().putAll(product.getStoreSkuId(), iimsProductValueMap).block();
  }

  public List<IidsModel> iidsWorker(List<IimsModel> iimsModels) {
    List<IidsModel> returnData = new ArrayList<>();
    String dateTime = "20240101000000";

    for (IimsModel iimsModel : iimsModels) {
      log.info(
          "start iids migration. sku[{}] uuid[{}}]", iimsModel.getSkuId(), iimsModel.getUuid());
      String warehouseId;
      try {
        warehouseId = iimsModel.getWarehouseId();
      } catch (IllegalArgumentException e) {
        // 沒有warehouse的產品是為髒資料，skip
        continue;
      } catch (Exception e) {
        log.error("can't get warehouseId, iimsModel[{}]", iimsModel, e);
        continue;
      }
      try {
        iidsPutProduct(iimsModel, warehouseId, dateTime);
      } catch (Exception e) {
        log.error(
            "put non-shared product fail, product iimsModel[{}], product mms id[{}}]",
            iimsModel,
            iimsModel.getMmsId(),
            e);
      }
    }
    return returnData;
  }

  private void iidsPutProduct(IimsModel iimsModel, String warehouseId, String dateTime) {
    // 檢查IIMS IIDS sharemode 不一樣 丟 warning msg, IIMS 為準
    String createTime =
        isShareModeSync(
            iimsModel.getUuid(), iimsModel.getSkuId(), iimsModel.getShare().equals("1"));

    String sharemall = iimsModel.getShare().equals("1") ? "hktv" : "";
    String nonsharemall = iimsModel.getShare().equals("0") ? "hktv" : "";
    String quantity = iimsModel.getShare().equals("1") ? iimsModel.getAvailable() : "0";
    String instockstatus =
        iimsModel.getShare().equals("1") ? iimsModel.getStatus() : "notSpecified";

    // original data structure
    iidsProductValueMap.clear();
    iidsProductValueMap.put(REDIS_KEY_HKTVSKU, iimsModel.getSkuId());
    iidsProductValueMap.put(REDIS_KEY_HKTVWAREHOUSE, warehouseId);
    iidsProductValueMap.put(IIDS_REDIS_KEY_INSTOCKSTATUS, instockstatus);
    iidsProductValueMap.put(REDIS_KEY_NONSHAREMALL, nonsharemall);
    iidsProductValueMap.put(REDIS_KEY_QUANTITY, quantity);
    iidsProductValueMap.put(REDIS_KEY_SHAREMALL, sharemall);
    iidsProductValueMap.put(IIDS_REDIS_KEY_UPDATESTOCKTIME, dateTime);
    redisTempl.opsForHash().putAll(iimsModel.getUuid(), iidsProductValueMap).block();
    // PM2.0 data structure
    iidsProductValueMap.clear();
    iidsProductValueMap.put(
        INVENTORY_REDIS_KEY_CREATE_TIME, StringUtils.defaultString(createTime, dateTime));
    iidsProductValueMap.put(INVENTORY_REDIS_KEY_UPDATE_TIME, dateTime);
    iidsProductValueMap.put(INVENTORY_REDIS_KEY_STATUS, instockstatus);
    iidsProductValueMap.put(INVENTORY_REDIS_KEY_SHARE, iimsModel.getShare());
    iidsProductValueMap.put(INVENTORY_REDIS_SKU, iimsModel.getSkuId());
    iidsProductValueMap.put(INVENTORY_REDIS_STORE_CODE, iimsModel.getStorefrontStoreCode());

    // 不異動原本倉庫的庫存
    for (String seqNo : iimsModel.getWarehouseSeqNumbers()) {
      if (Boolean.FALSE.equals(
          redisTempl
              .opsForHash()
              .hasKey(INVENTORY_KEY + iimsModel.getUuid(), seqNo + INVENTORY_REDIS_KEY_SUFFIX_QTY)
              .block())) {
        iidsProductValueMap.put(seqNo + INVENTORY_REDIS_KEY_SUFFIX_QTY, "0");
      }
      iidsProductValueMap.put(seqNo + INVENTORY_REDIS_KEY_SUFFIX_MALL, "");
    }

    iidsProductValueMap.put(
        iimsModel.getWarehouseSeqNumber() + INVENTORY_REDIS_KEY_SUFFIX_QTY, quantity);
    iidsProductValueMap.put(
        iimsModel.getWarehouseSeqNumber() + INVENTORY_REDIS_KEY_SUFFIX_MALL, "hktv");

    try {
      redisTempl
          .opsForHash()
          .putAll(INVENTORY_KEY + iimsModel.getUuid(), iidsProductValueMap)
          .block();
    } catch (Exception e) {
      log.error(
          "put redis failed, key[{}]productValueMap[{}]",
          INVENTORY_KEY + iimsModel.getUuid(),
          iidsProductValueMap);
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

    if (iidsData.get(REDIS_KEY_SHAREMALL) != null
        && iidsData.get(REDIS_KEY_SHAREMALL).contains("hktv")) {
      if (isIimsShare) {
        return iidsData.get(INVENTORY_REDIS_KEY_CREATE_TIME);
      } else {
        log.warn(
            "uuid[{}] is share but sku[{}] is non share, uuid switch to non share mode", uuid, sku);
      }
    }

    if (iidsData.get(REDIS_KEY_NONSHAREMALL) != null
        && iidsData.get(REDIS_KEY_NONSHAREMALL).contains("hktv")) {
      if (isIimsShare) {
        log.warn(
            "uuid[{}] is non share but sku[{}] is share, uuid switch to share mode", uuid, sku);
      }
    }
    return iidsData.get(INVENTORY_REDIS_KEY_CREATE_TIME);
  }
}
