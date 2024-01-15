package com.shoalter.willy.shoaltertools;

import com.shoalter.willy.shoaltertools.dto.BundleChildDto;
import com.shoalter.willy.shoaltertools.dto.BundleMallInfoDto;
import com.shoalter.willy.shoaltertools.dto.BundleSettingDto;
import com.shoalter.willy.shoaltertools.dto.ProductDto;
import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.dto.ProductMallDetailDto;
import com.shoalter.willy.shoaltertools.dto.ProductWarehouseDetailDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
public class CreateBundleTest {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisLMTemplate")
  ReactiveRedisTemplate<String, String> redisLMTempl;

  @Autowired
  @Qualifier("redisHKTVTemplate")
  ReactiveRedisTemplate<String, String> redisHKTVTempl;

  @Autowired private RabbitTemplate defaultRabbitTemplate;

  private String EXCHANGE = "shoalter-see-product-master_topic";
  private String ROUTING_KEY = "shoalter-see-product-master.product-info-iids";
  private String BASIC_URL = "http://127.0.0.1:8099/s2s/v3";

  public static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING = "bundle:setting:";
  public static final String INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT = "bundle:parent:";
  public static final String INVENTORY_REDIS_HKEY_PREFIX = "inventory:";

  @Test
  public void bundleCreateWith3Child() throws InterruptedException {
    String parentUuid = "iids-test-BundleParent-9999-9999-9999";
    String sku = "iims-test-BundleParent-0001-child1-0000";
    String storefrontStoreCode = "H0121001";
    String storeSkuId = storefrontStoreCode + "_S_B9999";
    String time = "202401121021";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE,
        ROUTING_KEY,
        buildBundleProductInfoDto_testcase0001(parentUuid, sku, storefrontStoreCode, storeSkuId));

    Thread.sleep(1000L);

    // 驗證IIDS資料: inventory
    Assertions.assertEquals(
        buildExpectedBundle_testcase0001(storeSkuId, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + parentUuid)
            .collectList()
            .flatMap(
                entries -> {
                  Map<String, String> entryMap = new HashMap<>();
                  for (Map.Entry<String, String> entry : entries) {
                    if (entry.getKey().equals("create_time")
                        || entry.getKey().equals("update_time")) {
                      entryMap.put(entry.getKey(), time);
                    } else {
                      entryMap.put(entry.getKey(), entry.getValue());
                    }
                  }
                  return Mono.just(entryMap);
                })
            .block());

    // 驗證IIDS資料: bundle:setting
    Assertions.assertTrue(
        redisTempl.hasKey(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid).block());

    // 清除bundle parent inventory + bundle:setting:
    redisTempl.delete(sku).block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid, parentUuid).block();
    redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid, parentUuid).block();
    redisLMTempl.delete(parentUuid).block();
    redisLMTempl.delete(storeSkuId).block();

    // 清除bundle children
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-1", "child-999-1")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-1", "child-999-1").block();
    redisTempl.delete("child-999-1").block();
    redisTempl.delete("child_S_child-999-sku1").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-2", "child-999-2")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-2", "child-999-2").block();
    redisTempl.delete("child-999-2").block();
    redisTempl.delete("child_S_child-999-sku2").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-3", "child-999-3")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-3", "child-999-3").block();
    redisTempl.delete("child-999-3").block();
    redisTempl.delete("child_S_child-999-sku3").block();
  }

  private ProductInfoDto buildBundleProductInfoDto_testcase0001(
      String parentUuid, String sku, String storefrontStoreCode, String storeSkuId) {

    // 清除bundle parent inventory + bundle:setting:
    redisTempl.delete(sku).block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + parentUuid, parentUuid).block();
    redisTempl.delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_SETTING + parentUuid, parentUuid).block();
    redisLMTempl.delete(parentUuid).block();

    // 清除bundle children
    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-1", "child-999-1")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-1", "child-999-1").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-2", "child-999-2")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-2", "child-999-2").block();

    redisTempl
        .delete(INVENTORY_REDIS_KEY_PREFIX_BUNDLE_PARENT + "child-999-3", "child-999-3")
        .block();
    redisTempl.delete(INVENTORY_REDIS_HKEY_PREFIX + "child-999-3", "child-999-3").block();

    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                ProductDto.builder()
                    .uuid(parentUuid)
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storeSkuId(storeSkuId)
                                .storefrontStoreCode(storefrontStoreCode)
                                .build()))
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .bundleSetting(
                        BundleSettingDto.builder()
                            .isReserved(true)
                            .isActive(false)
                            .priority(0)
                            .bundleMallInfoList(
                                List.of(
                                    BundleMallInfoDto.builder()
                                        .mall("hktv")
                                        .alertQty(100)
                                        .ceilingQty(100)
                                        .build()))
                            .bundleChildInfoList(
                                List.of(
                                    BundleChildDto.builder()
                                        .uuid("child-999-1")
                                        .skuId("child-999-sku1")
                                        .skuQty(3)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid("child-999-2")
                                        .skuId("child-999-sku2")
                                        .skuQty(4)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build(),
                                    BundleChildDto.builder()
                                        .uuid("child-999-3")
                                        .skuId("child-999-sku3")
                                        .skuQty(5)
                                        .ceilingQty(0)
                                        .storefrontStoreCode("child")
                                        .isLoop(false)
                                        .build()))
                            .build())
                    .build(),
                ProductDto.builder()
                    .uuid("child-999-1")
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_child-999-sku1")
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid("child-999-2")
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_child-999-sku2")
                                .build()))
                    .build(),
                ProductDto.builder()
                    .uuid("child-999-3")
                    .warehouseDetail(
                        List.of(
                            ProductWarehouseDetailDto.builder()
                                .warehouseSeqNo("01")
                                .mall(List.of("hktv"))
                                .build()))
                    .mallDetail(
                        List.of(
                            ProductMallDetailDto.builder()
                                .mall("hktv")
                                .storefrontStoreCode("child")
                                .storeSkuId("child_S_child-999-sku3")
                                .build()))
                    .build()))
        .build();
  }

  private static Map<String, String> buildExpectedBundle_testcase0001(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_setting_qty", "0");
    stockLevelMap.put("hktv_selling_qty", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H0121001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }
}
