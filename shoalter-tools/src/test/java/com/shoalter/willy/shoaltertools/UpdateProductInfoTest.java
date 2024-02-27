package com.shoalter.willy.shoaltertools;

import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import com.shoalter.willy.shoaltertools.testtool.BuildDtoUtil;
import com.shoalter.willy.shoaltertools.testtool.RabbitMqUtil;
import com.shoalter.willy.shoaltertools.testtool.RedisUtil;
import com.shoalter.willy.shoaltertools.testtool.updateproductinfo.UpdateProductInfoTestTool;
import com.shoalter.willy.shoaltertools.testtool.updateproductinfo.VerifyUpdateProductInfoTestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@SpringBootTest
@Slf4j
public class UpdateProductInfoTest {
  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  @Autowired
  @Qualifier("redisLMTemplate")
  ReactiveRedisTemplate<String, String> redisLMTempl;

  @Autowired
  @Qualifier("redisHKTVTemplate")
  ReactiveRedisTemplate<String, String> redisHKTVTempl;

  @Autowired private RedisUtil redisUtil;
  @Autowired private RabbitMqUtil rabbitMqUtil;
  @Autowired private UpdateProductInfoTestTool updProdInfoTestTool;
  @Autowired private VerifyUpdateProductInfoTestCase verifyUpdProdInfo;

  @Test
  void updateProduct_EditProductTestCase0010() {
    String uuid = "iids-integration-test-testcase-0005";
    String sku = "iims-integration-test-testcase-0005";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0010(uuid, sku));

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0010(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0010(uuid, sku, updEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0014() {
    String uuid = "iids-integration-test-testcase-0008";
    String sku = "iims-integration-test-testcase-0008";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0014(uuid, sku));

    // setting init value
    updProdInfoTestTool.updateProduct_EditProductTestCase0014_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0014(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0014(uuid, sku, updEventKey, newUpdEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0015() {
    String uuid = "iids-integration-test-testcase-0009";
    String sku = "iims-integration-test-testcase-0009";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0015(uuid, sku));

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0015(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0015(uuid, sku, updEventKey, newUpdEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0016() {
    String uuid = "iids-integration-test-testcase-0010";

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0016(uuid));

    // setting init value
    updProdInfoTestTool.updateProduct_EditProductTestCase0016_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0016(uuid));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0016(uuid);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0018() {
    String uuid = "iids-integration-test-testcase-0012";
    String sku = "iims-integration-test-testcase-0012";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0018(uuid, sku));

    // setting init value
    updProdInfoTestTool.updateProduct_EditProductTestCase0018_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0018(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0018(uuid, sku, updEventKey, newUpdEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
  }

  @Test
  void updateProduct_EditProductTestCase0019() {
    String uuid = "iids-integration-test-testcase-0013";
    String sku = "iims-integration-test-testcase-0013";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0019(uuid, sku));

    // setting init value
    updProdInfoTestTool.updateProduct_EditProductTestCase0019_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0019(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0019(uuid, sku, updEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0020() {
    String uuid = "iids-integration-test-testcase-0014";
    String sku = "iims-integration-test-testcase-0014";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0020(uuid, sku));

    // setting init value
    updProdInfoTestTool.updateProduct_EditProductTestCase0020_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0020(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0020(uuid, sku, updEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0021() {
    String uuid = "iids-integration-test-testcase-0015";
    String sku = "iims-integration-test-testcase-0015";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000101");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000102");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0021(uuid, sku));

    // setting init value
    updProdInfoTestTool.updateProduct_EditProductTestCase0021_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0021(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0021(uuid, sku, updEventKey, newUpdEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
    redisLMTempl.delete(uuid).block();
  }

  @Test
  void updateProduct_EditProductTestCase0022() {
    String uuid = "iids-integration-test-testcase-0016";
    String sku = "iims-integration-test-testcase-0016";
    String updEventKey = buildExpectedUpdateEventKey(sku, "H0000102");
    String newUpdEventKey = buildExpectedUpdateEventKey(sku, "H0000101");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_EditProductTestCase0022(uuid, sku));

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_EditProductTestCase0022(uuid, sku));

    verifyUpdProdInfo.updateProduct_EditProductTestCase0022(uuid, sku, updEventKey, newUpdEventKey);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey, newUpdEventKey).block();
  }

  // EditProduct case test move in/out warehouse 98
  @Test
  void updateProduct_testcase0013() {
    String uuid = "iids-integration-test-testcase-0030";
    String sku = "iims-integration-test-testcase-0030";
    String updEventKey01 = buildExpectedUpdateEventKey(sku, "H0000101");
    String updEventKey98 = buildExpectedUpdateEventKey(sku, "H0000198");

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey01, updEventKey98).block();

    // createProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.buildProductInfoDto_testcase0013(uuid, sku));

    // setting init value
    updProdInfoTestTool.updateProduct_testcase0013_setting_init_value(uuid);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_testcase0013_moveOut98(uuid, sku));

    verifyUpdProdInfo.updateProduct_testcase0013_moveOut98(uuid, sku, updEventKey01);

    // updateProduct
    rabbitMqUtil.sendMsgToIidsQueue(
        updProdInfoTestTool.updateProductInfoDto_testcase0013_moveIn98(uuid, sku));

    verifyUpdProdInfo.updateProduct_testcase0013_moveIn98(uuid, sku, updEventKey98);

    redisTempl.delete("inventory:" + uuid, uuid).block();
    redisHKTVTempl.delete(sku, updEventKey01, updEventKey98).block();
  }

  private static String buildExpectedUpdateEventKey(String sku, String warehouseId) {
    String updateKey = sku + "|||" + warehouseId;
    return "evtq_part_stockdata_" + Math.abs(updateKey.hashCode() % 10);
  }

  private ProductInfoDto move_HKTV_to_wh(String seqNo, String uuid, String sku) {
    return BuildDtoUtil.buildUpdateProductInfoDto(seqNo, uuid, sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantTo3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantTo3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_qty", "2400").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentTo3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentTo3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory_iimsIsOtherMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory_iimsIsOtherMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory_iimsIsConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory_iimsIsConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToMerchantInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToMerchantInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantTo3PLInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantTo3PLInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantTo3PLInventory_iimsIsConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantTo3PLInventory_iimsIsConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromMerchantToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromMerchantToConsignmentInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory_iimsIsOtherMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory_iimsIsOtherMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToMerchantInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToMerchantInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToConsignmentInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToConsignmentInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentToConsignmentInventory_iimsIs3PLInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentToConsignmentInventory_iimsIs3PLInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void
      putProductInfo_moveHktvFromConsignmentToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("17", uuid, sku));

    // verify
    verifyUpdProdInfo
        .moveHktvFromConsignmentToConsignmentInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentTo3PLInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "03");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "03_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentTo3PLInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFromConsignmentTo3PLInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFromConsignmentTo3PLInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToMerchantInventory_iimsIsOtherMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("03", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToMerchantInventory_iimsIsOtherMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToMerchantInventory_iimsIsConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("03", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToMerchantInventory_iimsIsConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToConsignmentInventory_iimsIsMerchantInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToConsignmentInventory_iimsIsMerchantInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_moveHktvFrom3PLToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.moveHktvFrom3PLToConsignmentInventory_iimsIsOtherConsignmentInventory();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh01_to_wh02_but_iims_already_in_wh02() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "02");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "02_mall", "").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_qty", "2400").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("02", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh01_to_wh02_but_iims_already_in_wh02();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh01_to_wh15_but_iims_already_in_wh15() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh01_to_wh15_but_iims_already_in_wh15();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh01_to_wh98_but_iims_already_in_wh98() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh01_to_wh98_but_iims_already_in_wh98();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh15_to_wh01_but_iims_already_in_wh01() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("01", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh15_to_wh01_but_iims_already_in_wh01();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh15_to_wh16_but_iims_already_in_wh16() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "16");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "16_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("16", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh15_to_wh16_but_iims_already_in_wh16();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh15_to_wh98_but_iims_already_in_wh98() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "98");
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("98", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh15_to_wh98_but_iims_already_in_wh98();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh98_to_wh01_but_iims_already_in_wh01() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "01");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "01_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("01", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh98_to_wh01_but_iims_already_in_wh01();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }

  @Test
  void putProductInfo_hktv_wh98_to_wh15_but_iims_already_in_wh15() {
    String sku = "H088800118_S_child-SKU-E-1";
    String uuid = "child-UUID-E-1";

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);

    // insert default data
    redisUtil.insertIidsAndSkuIimsData(uuid, sku, "15");
    redisTempl.opsForHash().put("inventory:" + uuid, "98_mall", "hktv").block();
    redisTempl.opsForHash().put("inventory:" + uuid, "15_mall", "").block();

    // testing
    rabbitMqUtil.sendMsgToIidsQueue(move_HKTV_to_wh("15", uuid, sku));

    // verify
    verifyUpdProdInfo.hktv_wh98_to_wh15_but_iims_already_in_wh15();

    // delete data
    redisUtil.deleteInventoryUuid(uuid);
    redisUtil.deleteSku(sku);
  }
}
