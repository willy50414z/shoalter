package com.shoalter.willy.shoaltertools.testtool;

public class AssertUtil {
  public static void wait_2_sec() {
    try {
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
      throw new RuntimeException();
    }
  }
}
