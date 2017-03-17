package vrm;

import com.sun.istack.internal.NotNull;

/**
 * Convenience class to wrap int[] and Word[] as a page table.
 * Each VM has a page table that specifies absolute memory addresses for each memory block.
 */
public class PageTable {

  public final Word[] table;

  /**
   * Creates a page table with absolute memory addresses specified.
   * E.g. 00001, 00005, 00007 would mean that
   * VM's 0th memory block is located at RM's 1st block,
   * VM's 1st memory block is located at RM's 5th block,
   * VM's 2nd memory block is located at RM's 7th block,
   * @param absolutes absolute addresses (indexed from 0).
   */
  public PageTable(@NotNull Word[] absolutes) {
    table = absolutes;
  }

  /**
   * Convenience c-tor to specify absolute addresses in primitive ints.
   * @param absolutes absolute addresses (indexed from 0).
   */
  public PageTable(@NotNull int... absolutes) {
    if (absolutes == null) {
      throw new IllegalArgumentException("Must provide absolute addresses!");
    }

    final Word[] words = new Word[absolutes.length];
    for (int i = 0; i < words.length; i++) {
      words[i] = new Word(String.valueOf(absolutes[i]));
    }
    table = words;
  }

}
