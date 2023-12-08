package com.shoalter.willy.shoaltertools;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@SpringBootTest
@Slf4j
public class DeleteProductInfoTest {
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired private RabbitTemplate defaultRabbitTemplate;

  private String EXCHANGE = "shoalter-see-product-master_topic";
  private String ROUTING_KEY = "shoalter-see-product-master.product-info-iids";

  @Test
  void deleteProduct_testcase0001() throws InterruptedException {
    String time = "20231207134648";
    String uuid = "iids-integration-test-testcase-0017";
    String sku = "iims-integration-test-testcase-0017";

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, buildProductInfoDto_testcase0001(uuid, sku));

    Assertions.assertEquals(
        buildExpectedStockLevel_testcase0001(sku, time),
        redisTempl
            .<String, String>opsForHash()
            .entries("inventory:" + uuid)
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

    defaultRabbitTemplate.convertAndSend(
        EXCHANGE, ROUTING_KEY, deleteProductInfoDto_testcase0001(uuid, sku));

    Thread.sleep(1000L);
    Assertions.assertFalse(redisTempl.hasKey("inventory:" + uuid).block());
    Assertions.assertFalse(redisTempl.hasKey(uuid).block());
  }

  private ProductInfoDto buildProductInfoDto_testcase0001(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("CREATE")
        .products(
            List.of(
                new ProductDto(
                    uuid,
                    List.of(
                        ProductMallDetailDto.builder()
                            .mall("hktv")
                            .storefrontStoreCode("H00001")
                            .storeSkuId(sku)
                            .build(),
                        ProductMallDetailDto.builder()
                            .mall("little_mall")
                            .storefrontStoreCode("H00001")
                            .build()),
                    List.of(
                        ProductWarehouseDetailDto.builder()
                            .warehouseSeqNo("01")
                            .mall(List.of("hktv", "little_mall"))
                            .build()))))
        .build();
  }

  private ProductInfoDto deleteProductInfoDto_testcase0001(String uuid, String sku) {
    return ProductInfoDto.builder()
        .action("DELETE")
        .products(List.of(new ProductDto(uuid, List.of(), List.of())))
        .build();
  }

  private static Map<String, String> buildExpectedStockLevel_testcase0001(String sku, String time) {
    Map<String, String> stockLevelMap = new HashMap<>();
    stockLevelMap.put("01_mall", "hktv,little_mall");
    stockLevelMap.put("01_qty", "0");
    stockLevelMap.put("hktv_instockstatus", "notSpecified");
    stockLevelMap.put("hktv_share", "0");
    stockLevelMap.put("hktv_sku", sku);
    stockLevelMap.put("hktv_store_code", "H00001");
    stockLevelMap.put("little_mall_instockstatus", "notSpecified");
    stockLevelMap.put("little_mall_share", "0");
    stockLevelMap.put("little_mall_store_code", "H00001");
    stockLevelMap.put("update_time", time);
    stockLevelMap.put("create_time", time);
    return stockLevelMap;
  }
}
