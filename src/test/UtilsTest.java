package test;

import org.junit.Test;

import vrm.Utils;
import static org.junit.Assert.assertEquals;

/**
 * Created by Simas on 2017 Mar 15.
 */
public class UtilsTest {

  @Test
  public void checkFlagTest1() {
    final int value = 3;
    assertEquals(Utils.checkFlag(value, 0b1), true);
  }

  @Test
  public void checkFlagTest2() {
    final int value = 3;
    assertEquals(Utils.checkFlag(value, 0b10), true);
  }

  @Test
  public void checkFlagTest3() {
    final int value = 3;
    assertEquals(Utils.checkFlag(value, 0b100), false);
  }

  @Test
  public void checkFlagTest4() {
    final int value = 3;
    assertEquals(Utils.checkFlag(value, 0b111), false);
  }

  @Test
  public void setFlagTest1() {
    final int value = Utils.setFlag(0, 0b1);
    assertEquals(value, 1);
  }

  @Test
  public void setFlagTest2() {
    final int value = Utils.setFlag(0, 0b1000);
    assertEquals(value, 8);
  }

  @Test
  public void setFlagTest3() {
    final int value = Utils.setFlag(0, 0b1010);
    assertEquals(value, 10);
  }

  @Test
  public void clearFlagTest1() {
    final int initial = 8;
    final int value = Utils.clearFlag(initial, 0b1000);
    assertEquals(value, 0);
  }

  @Test
  public void clearFlagTest2() {
    final int initial = 2;
    final int value = Utils.clearFlag(initial, 0b0010);
    assertEquals(value, 0);
  }

}