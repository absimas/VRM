package test;

import org.junit.Before;
import org.junit.Test;
import java.io.File;
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

  //  ToDo read tests

  private static final String PATH = "external_memory.txt";

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Before
  public void clearMemory() {
    new File(PATH).delete();
  }

  @Test
  public void write() throws IOException {
    final ExternalMemory mem = new ExternalMemory(PATH);
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
    final String expected = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
                            "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n";
    final String actual = new String(Files.readAllBytes(Paths.get(PATH)));

    assertEquals(expected, actual);
  }

  @Test
  public void writeToMiddle() throws IOException {
    // Initialize memory
    final String initial = "AD000 AD001 AD002 AD003 AD004 AD005 AD006 AD007 AD008 AD009\n" +
                            "DV000 DV001 DV002 DV003 DV004 DV005 DV006 DV007 DV008 DV009\n" +
                            "MD000 MD001 MD002 MD003 MD004 MD005 MD006 MD007 MD008 MD009\n" +
                            "SB000 SB001 SB002 SB003 SB004 SB005 SB006 SB007 SB008 SB009\n";
    final PrintWriter writer = new PrintWriter(PATH);
    writer.write(initial);
    writer.flush();
    writer.close();

    // Open external mem object
    final ExternalMemory mem = new ExternalMemory(PATH);
    final Word[] words = new Word[10];

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
  public void read() {

  }

  @Test
  public void readFromMiddle() {

  }

}
