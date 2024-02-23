package com.shoalter.willy.shoaltertools.testtool.updateproductinfo;

import com.shoalter.willy.shoaltertools.testtool.AssertUtil;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class VerifyUpdateProductInfoTestCase {

  @Autowired
  @Qualifier("redisIIDSTemplate")
  ReactiveRedisTemplate<String, String> redisTempl;

  public void moveHktvFromMerchantTo3PLInventory() {
    // verify_putProductInfo_HKTV_wh01_to_wh98_qty_keep_in_wh01
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
  }

  public void moveHktvFrom3PLToMerchantInventory() {
    // verify_putProductInfo_HKTV_wh98_to_wh02_qty_keep_in_wh98_and_wh01_qty_move_to_wh02
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011898_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void moveHktvFromMerchantToMerchantInventory() {
    // verify_putProductInfo_hktv_wh01_To_wh02_qty_will_move
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "0",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void moveHktvFromMerchantToMerchantInventory_iimsIsOtherMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromMerchantToMerchantInventory_iimsIsConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromMerchantToMerchantInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromMerchantTo3PLInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFromMerchantTo3PLInventory_iimsIsConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFromMerchantToConsignmentInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFromMerchantToConsignmentInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFromMerchantToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFromConsignmentToMerchantInventory_iimsIsOtherMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromConsignmentToMerchantInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromConsignmentToMerchantInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory_iimsIs3PLInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
  }

  public void moveHktvFromConsignmentToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "17_mall").block());
  }

  public void moveHktvFromConsignmentTo3PLInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFromConsignmentTo3PLInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void moveHktvFrom3PLToMerchantInventory_iimsIsOtherMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
  }

  public void moveHktvFrom3PLToMerchantInventory_iimsIsConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "03_mall").block());
  }

  public void moveHktvFrom3PLToConsignmentInventory_iimsIsMerchantInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void moveHktvFrom3PLToConsignmentInventory_iimsIsOtherConsignmentInventory() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void hktv_wh01_to_wh02_but_iims_already_in_wh02() {
    AssertUtil.wait_2_sec();

    Assertions.assertEquals(
        "0", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_qty").block());
    Assertions.assertEquals(
        "2400", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_qty").block());
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "02_mall").block());
    Assertions.assertNull(
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011801_available")
            .block());
    Assertions.assertEquals(
        "2400",
        redisTempl
            .opsForHash()
            .get("H088800118_S_child-SKU-E-1", "H08880011802_available")
            .block());
  }

  public void hktv_wh01_to_wh15_but_iims_already_in_wh15() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }

  public void hktv_wh01_to_wh98_but_iims_already_in_wh98() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void hktv_wh15_to_wh01_but_iims_already_in_wh01() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
  }

  public void hktv_wh15_to_wh16_but_iims_already_in_wh16() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "16_mall").block());
  }

  public void hktv_wh15_to_wh98_but_iims_already_in_wh98() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
  }

  public void hktv_wh98_to_wh01_but_iims_already_in_wh01() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "01_mall").block());
  }

  public void hktv_wh98_to_wh15_but_iims_already_in_wh15() {
    AssertUtil.wait_2_sec();
    Assertions.assertEquals(
        "", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "98_mall").block());
    Assertions.assertEquals(
        "hktv", redisTempl.opsForHash().get("inventory:child-UUID-E-1", "15_mall").block());
  }
}
