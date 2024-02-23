package com.shoalter.willy.shoaltertools.testtool;

import java.util.List;

public class SystemConstants {
  public static List<String> getRedisNodeKeys() {
    return List.of(
        "nodesku:172.30.255.130:6000",
        "nodesku:172.30.255.130:6001",
        "nodesku:172.30.255.130:6002");
  }

  public static List<String> getAbandonedRedisNodeKeys() {
    return List.of("port:MASTER6001", "port:MASTER6002", "port:MASTER6000");
  }

  public static String createBundleParentKey(String childUuid) {
    return "bundle:parent:" + childUuid;
  }

  public static String createBundleSettingKey(String parentUuid) {
    return "bundle:setting:" + parentUuid;
  }

  public static String getBundleLockParentRedisKey() {
    return "bundle:lock_parent";
  }
}
