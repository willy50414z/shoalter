package com.shoalter.willy.shoaltertools;

import com.shoalter.willy.shoaltertools.entity.ProductEntity;
import com.shoalter.willy.shoaltertools.entity.ShareGroupEntity;
import com.shoalter.willy.shoaltertools.repository.ProductRepository;
import com.shoalter.willy.shoaltertools.service.ProductShareGroupService;
import com.shoalter.willy.shoaltertools.testtool.ApiUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@SpringBootTest
@Slf4j
public class UpdateShareSkuTest {

  @Autowired private ProductShareGroupService productShareGroupService;
  @Autowired private ProductRepository productRepository;

  @Autowired private RedisUtil redisUtil;
  @Autowired private ApiUtil apiUtil;

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  private String sku01InHktv = "H088800118_S_TEST0001";
  private String uuid01InHktv = "iids-integration-test-testcase-0001";

  private String sku02InHktv = "H088800118_S_TEST0002";
  private String uuid02InHktv = "iids-integration-test-testcase-0002";

  private String uuid03InLm = "iids-integration-test-testcase-LM-0003";

  @Test
  void updateShareSku_updateByAddIimsShouldSuccess() {

    // Set DB Data
    productShareGroupService.deleteProduct(uuid01InHktv);
    productShareGroupService.deleteProduct(uuid02InHktv);
    ProductEntity productEntity1 =
        ProductEntity.builder().uuid(uuid01InHktv).sku(sku01InHktv).build();

    ProductEntity productEntity2 =
        ProductEntity.builder().uuid(uuid02InHktv).sku(sku02InHktv).build();

    ProductEntity productEntity_1 = productShareGroupService.saveProduct(productEntity1);
    ProductEntity productEntity_2 = productShareGroupService.saveProduct(productEntity2);
    ShareGroupEntity shareGroupEntity =
        productShareGroupService.saveShareGroup(
            ShareGroupEntity.builder().createTime(new Date()).build());

    productShareGroupService.updateByProductAndShareGroup(shareGroupEntity, productEntity_1);
    productShareGroupService.updateByProductAndShareGroup(shareGroupEntity, productEntity_2);
    productShareGroupService.deleteProductShareGroupByProduct(productEntity_1);
    productShareGroupService.deleteProductShareGroupByProduct(productEntity_2);

    // Set Redis Data
    redisUtil.deleteSku(sku01InHktv, sku02InHktv);
    redisUtil.deleteInventoryUuid(uuid01InHktv, uuid02InHktv);

    redisUtil.insertIidsAndSkuIimsData(uuid01InHktv, sku01InHktv, "01", "0");
    redisUtil.insertIidsAndSkuIimsData(uuid02InHktv, sku02InHktv, "02", "0");

    // Update sku01InHktv by IIMS, sku02InHktv should have same value

    apiUtil.updateSkuStockLevelIims(sku01InHktv, "add", 10);

    Assertions.assertEquals(
        "10", redisTempl.opsForHash().get(sku01InHktv, "H08880011801_available").block());
    Assertions.assertEquals(
        "10", redisTempl.opsForHash().get(sku02InHktv, "H08880011802_available").block());
  }
}
