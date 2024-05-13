package com.shoalter.willy.shoaltertools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@Slf4j
@SpringBootApplication
public class ShoalterToolsApplication {
  static String redisCommend = "redis-cli -c -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ";
  static String inputFilePath = "C:\\Users\\shelby.cheng\\Desktop\\0513.txt";
  static String outPutFilePath = "C:\\Users\\shelby.cheng\\Desktop\\0513_patchFile.txt";
  static String outPutCheckFilePath = "C:\\Users\\shelby.cheng\\Desktop\\0513_checkFile.txt";
  static String outPutErrorFilePath = "C:\\Users\\shelby.cheng\\Desktop\\0513_errorFile.txt";
  static String splitRegex = "\t";

  public static void main(String[] args) {
    SpringApplication.run(ShoalterToolsApplication.class, args);

    // target file path



    try {
      // read the file and split by line
      ArrayList<Map<String, String>> arrayList = readAndSplitTxtFile(inputFilePath);
      StringBuffer updateStringBf = new StringBuffer();
      StringBuffer dirtyDataBf = new StringBuffer();
      for (Map<String, String> map : arrayList) {
        if (!StringUtils.isEmpty(map.get("updatestocktime"))) {
          updateStringBf.append(redisCommend + "HDEL " + map.get("sku") + " updatestocktime");
          map.remove("updatestocktime");
                    updateStringBf.append("\r\n");
        }
        String dirtyDataMsg = checkIimsData(map, map.get("sku"));
        if (!StringUtils.isEmpty(dirtyDataMsg)) {
          dirtyDataBf.append(dirtyDataMsg);
        }

        String content = StringUtil.EMPTY_STRING;
        if (!StringUtils.isEmpty(getWarehouseId(map))){
          content = inputData(map, map.get("sku"), getWarehouseId(map));
        }

        if (!StringUtils.isEmpty(content)) {
          updateStringBf.append(content);
          updateStringBf.append("\r\n");
        }

      }
      generateShellScript(outPutCheckFilePath, dirtyDataBf.toString());
      generateShellScript(outPutFilePath, updateStringBf.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void generateShellScript(String filePath, String content) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      // write the content to the file
      writer.write(content);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static ArrayList<Map<String, String>> readAndSplitTxtFile(String filePath)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;

      ArrayList<Map<String, String>> arrayList = new ArrayList<>();

      while ((line = reader.readLine()) != null) {
        Map<String, String> map = new HashMap<>();
        // split by tab
        String[] values = line.split(splitRegex);


        for (int i = 0; i < values.length; i++) {

          try{
            // put in map
            values[i] = values[i].trim();
            if (i == 0) {
              map.put("sku", values[i].trim());
              System.out.println(values[i].trim());
            } else {
              map.put(values[i].trim(), values[i + 1].trim());
              i++;
            }
          }catch (ArrayIndexOutOfBoundsException e){

            log.error("ArrayIndexOutOfBoundsException, data[{}]",line);
            generateShellScript(outPutErrorFilePath, line+"\r\n");
          }

        }
        arrayList.add(map);

      }
      return arrayList;
    }
  }


  private static String inputData(
      Map<String, String> iimsData, String sku, String iimsWarehouseId) {
    Map<String, Object> productValueMap = new HashMap<>();
    StringBuilder stringBuilder = new StringBuilder();

    if (StringUtils.isEmpty(iimsData.get(iimsWarehouseId + "_available"))) {
      productValueMap.put(iimsWarehouseId + "_available", "0");
    }

    if (StringUtils.isEmpty(iimsData.get(iimsWarehouseId + "_instockstatus"))) {
      productValueMap.put(iimsWarehouseId + "_instockstatus", "notSpecified");
    }

    if (StringUtils.isEmpty(iimsData.get(iimsWarehouseId + "_updatestocktime"))) {
      productValueMap.put(iimsWarehouseId + "_updatestocktime", "20240422163000");
    }

    if (!productValueMap.isEmpty()) {
      for (Map.Entry<String, Object> entry : productValueMap.entrySet()) {
        stringBuilder.append(redisCommend+"HSET " + sku + " " + entry.getKey() + " " + entry.getValue());
//        System.out.println("HSET " + sku + " " + entry.getKey() + " " + entry.getValue());
      }
    }

    return stringBuilder.toString();
  }

  private static String getWarehouseId(Map<String, String> iimsData) {
    String warehouseId = "";

    List<String> availableKeyList =
        iimsData.entrySet().stream()
            .filter(entry -> entry.getKey().contains("_available"))
            .map(x -> x.getKey())
            .collect(Collectors.toList());

    if (availableKeyList.size() > 1) {
      availableKeyList.sort(
          (e1, e2) -> {
            Integer e1Qty = Integer.parseInt(iimsData.get(e1));
            Integer e2Qty = Integer.parseInt(iimsData.get(e2));
            return e2Qty.compareTo(e1Qty);
          });
    }

    for (String key : availableKeyList) {
      warehouseId = key.split("_available")[0];

      // check weather complete data
      if (!StringUtils.isEmpty(iimsData.get(warehouseId + "_instockstatus"))) {
        return warehouseId;
      }
    }

    return warehouseId;
  }

  private static String checkIimsData(Map<String, String> iimsData, String sku) {
    boolean hasDirtyData = false;

    // WarehouseId _available, _instockstatus, _updatestocktime should be 1
    // check categories
    Map<String, Integer> targetCounts = new HashMap<>();
    targetCounts.put("available", 0);
    targetCounts.put("instockstatus", 0);
    targetCounts.put("updatestocktime", 0);

    for (String key : iimsData.keySet()) {
      if (key.contains("_")) {
        String keyType = key.split("_")[1];
        if (targetCounts.containsKey(keyType)) {
          targetCounts.put(keyType, targetCounts.get(keyType) + 1);
        }
      }
    }

    for (int count : targetCounts.values()) {
      if (count != 1) {
        hasDirtyData = true;
      }
    }

    // check warehouse
    Set<String> availableKeySet =
        iimsData.entrySet().stream()
            .filter(
                entry ->
                    entry.getKey().contains("_available")
                        || entry.getKey().contains("_instockstatus")
                        || entry.getKey().contains("_updatestocktime"))
            .map(x -> x.getKey().split("_")[0])
            .collect(Collectors.toSet());

    if (availableKeySet.size() > 1) {
      hasDirtyData = true;
    }

    if (hasDirtyData && availableKeySet.size() > 1) {
      String iimsDirtyDataMessage =
          "sku["
              + sku
              + "], exist dirtyData,warehouse["
              + availableKeySet
              + "] iimsData["
              + iimsData
              + "]";
            log.warn(iimsDirtyDataMessage);

		 return iimsDirtyDataMessage + "\r\n";
    }

    return "";
  }
}
