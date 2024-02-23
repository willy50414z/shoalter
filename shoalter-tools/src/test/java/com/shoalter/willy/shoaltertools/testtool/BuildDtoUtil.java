package com.shoalter.willy.shoaltertools.testtool;

import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildDtoUtil {
  public static Map<String, String> buildIidsPm20hktvData(String sku, String seqNo) {
    Map<String, String> iidsData = getDefaultIidsPm20Data();
    iidsData.put(seqNo + "_mall", "hktv");
    iidsData.put("hktv_instockstatus", "notSpecified");
    iidsData.put("hktv_share", "0");
    iidsData.put("hktv_sku", sku);
    iidsData.put("hktv_store_code", "H088800118");
    return iidsData;
  }

  private static Map<String, String> getDefaultIidsPm20Data() {
    Map<String, String> iidsData = new HashMap<>();
    for (String wh : getDefaultWarehouses()) {
      iidsData.put(wh + "_qty", "0");
      iidsData.put(wh + "_mall", "");
    }
    iidsData.put("create_time", "20240219145227");
    iidsData.put("update_time", "20240219145227");
    return iidsData;
  }

  private static List<String> getDefaultWarehouses() {
    return List.of("01", "02", "03", "15", "16", "17", "98");
  }

  public static Map<String, String> buildSkuIimsData(String uuid, String seqNo) {
    return Map.of(
        "H088800118" + seqNo + "_available",
        "2400",
        "H088800118" + seqNo + "_instockstatus",
        "notSpecified",
        "share",
        "0",
        "H088800118" + seqNo + "_updatestocktime",
        "20240219145229",
        "uuid",
        uuid);
  }

  public static ProductInfoDto buildUpdateProductInfoDto(String seqNo, String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("UPDATE")
        .products(
            List.of(
                buildProductDtos(
                    uuid,
                    buildMallProductWhDetailDtos("hktv", seqNo),
                    List.of(buildHktvProductMallDetailDto(buildStorefrontStoreCode(sku), sku)))))
        .build();
  }

  private static ProductDto buildProductDtos(
      String uuid,
      List<ProductWarehouseDetailDto> productWhDetailDtos,
      List<ProductMallDetailDto> productMallDetailDtos) {
    return ProductDto.builder()
        .uuid(uuid)
        .warehouseDetail(productWhDetailDtos)
        .mallDetail(productMallDetailDtos)
        .build();
  }

  private static List<ProductWarehouseDetailDto> buildMallProductWhDetailDtos(
      String mall, String newWh) {
    List<ProductWarehouseDetailDto> productWarehouseDetailDtos = new ArrayList<>();
    for (String wh : getDefaultWarehouses()) {
      if (wh.equals(newWh)) {
        productWarehouseDetailDtos.add(buildMallWarehouseDetail(mall, newWh));
      } else {
        productWarehouseDetailDtos.add(buildEmptyWhWarehouseDetail(wh));
      }
    }
    return productWarehouseDetailDtos;
  }

  private static ProductWarehouseDetailDto buildMallWarehouseDetail(String mall, String seqNo) {
    return ProductWarehouseDetailDto.builder()
        .warehouseSeqNo(String.valueOf(seqNo))
        .mall(List.of(mall))
        .build();
  }

  private static ProductWarehouseDetailDto buildEmptyWhWarehouseDetail(String seqNo) {
    return ProductWarehouseDetailDto.builder()
        .warehouseSeqNo(String.valueOf(seqNo))
        .mall(new ArrayList<>())
        .build();
  }

  private static ProductMallDetailDto buildHktvProductMallDetailDto(
      String storefrontStoreCode, String storeSkuId) {
    return ProductMallDetailDto.builder()
        .mall("hktv")
        .storefrontStoreCode(storefrontStoreCode)
        .storeSkuId(storeSkuId)
        .build();
  }

  private static String buildStorefrontStoreCode(String sku) {
    return sku.split("_S_")[0];
  }

  public static Map<String, String> buildIidsParenthktvData(String sku, String seqNo) {
    Map<String, String> parentData = new HashMap<>(buildIidsPm20hktvData(sku, seqNo));
    parentData.put("hktv_selling_qty", "2400");
    parentData.put("hktv_setting_qty", "2400");
    return parentData;
  }
}
