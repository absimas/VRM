package test;

import org.junit.Before;
import org.junit.Test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import vrm.ExternalMemory;
import vrm.Word;
import static org.junit.Assert.*;

/**
 * Created by Simas on 2017 Mar 14.
 */
public class ExternalMemoryTest {

  private static final String PATH = "external_memory_test.txt";

  @Before
  public void clearMemory() throws FileNotFoundException {
    // Truncate memory file
    new PrintWriter(PATH).close();
  }

  @Test
  public void write() throws IOException {
    final ExternalMemory mem = new ExternalMemory(PATH);
    mem.initialize();
    final Word[] words = new Word[10];

    // First line
    for (int i = 0; i < words.length; i++) {
      words[i] = new Word("AD00" + i);
    }
    mem.write(words);

    // Second line
    mem.setPointer(1);
    for (int i = 0; i < words.length; i++) {
      words[i] = new Word("DV00" + i);
    }
    mem.write(words);

    // Close the external memory file
    mem.close();

    // See if content written correctly
    final String expectedStart = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
                            "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n";

    final String actual = new String(Files.readAllBytes(Paths.get(PATH)));

    assertEquals(actual.startsWith(expectedStart), true);
  }

  @Test
  public void writeToMiddle() throws IOException {
    // Open external mem object
    final ExternalMemory mem = new ExternalMemory(PATH);
    mem.initialize();
    final Word[] words = new Word[10];

    // Re-Initialize memory
    final String initial = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
        "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n" +
        "MD000 MD001 MD002 MD003 MD004 MD005 MD006 MD007 MD008 MD009\n" +
        "SB000 SB001 SB002 SB003 SB004 SB005 SB006 SB007 SB008 SB009\n";
    final PrintWriter writer = new PrintWriter(PATH);
    writer.write(initial);
    writer.flush();
    writer.close();

    // Overwrite to the 3rd line (words 21-30)
    mem.setPointer(2);
    for (int i = 0; i < words.length; i++) {
      words[i] = new Word("CM00" + i);
    }
    mem.write(words);

    // Close the external memory file
    mem.close();

    // See if only the 3rd line was changed.
    final String expected = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
                            "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n" +
                            "CM000 CM001 CM002 CM003 CM004 CM005 CM006 CM007 CM008 CM009\n" +
                            "SB000 SB001 SB002 SB003 SB004 SB005 SB006 SB007 SB008 SB009\n";
    final String actual = new String(Files.readAllBytes(Paths.get(PATH)));

    assertEquals(expected, actual);
  }

  @Test
  public void read() throws IOException {
    // Open external mem object
    final ExternalMemory mem = new ExternalMemory(PATH);
    mem.initialize();

    // Re-Initialize memory
    final String initial = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
        "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n" +
        "MD000 MD001 MD002 MD003 MD004 MD005 MD006 MD007 MD008 MD009\n" +
        "SB000 SB001 SB002 SB003 SB004 SB005 SB006 SB007 SB008 SB009\n";
    final PrintWriter writer = new PrintWriter(PATH);
    writer.write(initial);
    writer.flush();
    writer.close();

    String actual = "";

    // Read lines as words and combine into a string
    for (int i = 0; i < 4; i++) {
      // Read a line as an array of words
      final Word[] words = mem.read();

      // Combine words into the final string
      actual += lineToString(words);

      // Increment the pointer
      mem.setPointer(mem.getPointer() + 1);
    }

    assertEquals(initial, actual);
  }

  @Test
  public void readFromMiddle() throws IOException {
    // Open external mem object
    final ExternalMemory mem = new ExternalMemory(PATH);
    mem.initialize();

    // Re-Initialize memory
    final String initial = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
        "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n" +
        "MD000 MD001 MD002 MD003 MD004 MD005 MD006 MD007 MD008 MD009\n" +
        "SB000 SB001 SB002 SB003 SB004 SB005 SB006 SB007 SB008 SB009\n";
    final PrintWriter writer = new PrintWriter(PATH);
    writer.write(initial);
    writer.flush();
    writer.close();

      // Read a line as an array of words
    mem.setPointer(2);
    final Word[] words = mem.read();

    // Combine words into final string
    final String actual = lineToString(words);

    // Compare
    final String expected = "MD000 MD001 MD002 MD003 MD004 MD005 MD006 MD007 MD008 MD009\n";
    assertEquals(expected, actual);
  }

  @Test
  public void initializeTest() {
    // Open external mem object
    final ExternalMemory mem = new ExternalMemory(PATH);
    mem.initialize();

    // Read middle line as an array of words
//    mem.setPointer(1);
    final Word[] words = mem.read();

    // Combine words into final string
    final String actual = lineToString(words);

    // Compare
    final String expected = "00000 00000 00000 00000 00000 00000 00000 00000 00000 00000\n";
    assertEquals(expected, actual);
  }

  private String lineToString(Word[] words) {
    String result = "";
    for (int j = 0; j < words.length; j++) {
      result += words[j];

      if (j == words.length - 1) {
        // Last word ends in a new line
        result += '\n';
      } else {
        // All other words end with a space
        result += ' ';
      }
    }

    return result;
  }

}
