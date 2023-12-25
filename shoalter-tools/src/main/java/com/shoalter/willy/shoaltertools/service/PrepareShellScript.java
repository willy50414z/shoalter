package com.shoalter.willy.shoaltertools.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.Cleanup;

public class PrepareShellScript {
  public static List<String> readToStr(String filePath) throws IOException {
    String strLine;
    String encoding = "UTF-8";
    List<String> originalList = new ArrayList<>();

    StringBuffer resultStr = new StringBuffer();
    @Cleanup FileInputStream is = new FileInputStream(filePath);
    @Cleanup InputStreamReader isr = new InputStreamReader(is, encoding);
    @Cleanup BufferedReader br = new BufferedReader(isr); // bufferedReader
    while ((strLine = br.readLine()) != null) { // 將CSV檔字串一列一列讀入並存起來直到沒有列為止
      //			resultStr.append(strLine).append("\r\n");
      originalList.add(strLine);
    }
    //		return resultStr.toString();
    return originalList;
  }

  public static void main(String[] args) throws IOException {
    String logFilePath = "/tmp/iids_toNShare_20231219.log";
    String shFilePath = "E:/iidsDeleteMallDev.sh";
    List<String> originalList = readToStr("E:\\3PL_UUIDs.txt");
    int maxSize = 500;

    List<List<String>> dividedLists = divideList(originalList, maxSize);
    StringBuilder result = new StringBuilder();
    dividedLists.forEach(
        uuidList -> {
          String requst = prepareScriptRequest(uuidList);
          String cmd =
              "curl -X 'DELETE' \\\n"
                  + "  'https://iids-restful.shoalter.com/s2s/v1/products/stock-levels/share-mode' \\\n"
                  + "  -H 'accept: application/json' \\\n"
                  + "  -H 'Content-Type: application/json' \\\n"
                  + "  -d '"
                  + requst
                  + "' >> "
                  + logFilePath;
          result.append(cmd);
          result.append("\n");
        });

    try (OutputStream os = new FileOutputStream(shFilePath)) {
      byte[] scriptBytes = result.toString().getBytes();
      os.write(scriptBytes);
      System.out.println("Success create shell script");
      System.out.println(result);
    } catch (IOException e) {
      System.out.println("Fail create shell script");
      e.printStackTrace();
    }
  }

  private static String prepareScriptRequest(List<String> uuidList) {
    StringBuilder request = new StringBuilder("[");
    for (int i = 0; i < uuidList.size(); i++) {
      request.append("{");

      request.append("\"uuid\": \"").append(uuidList.get(i)).append("\"");

      request.append("}");
      if (i != uuidList.size() - 1) {
        request.append(",");
      }
    }
    request.append("]");
    return request.toString();
  }

  public static <T> List<List<T>> divideList(List<T> originalList, int batchSize) {
    List<List<T>> dividedLists = new ArrayList<>();

    for (int i = 0; i < originalList.size(); i += batchSize) {
      int end = Math.min(i + batchSize, originalList.size());
      List<T> batch = originalList.subList(i, end);
      dividedLists.add(new ArrayList<>(batch));
    }

    return dividedLists;
  }
}
