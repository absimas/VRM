package vrm;

import com.sun.istack.internal.NotNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * External memory displaying output.
 */
public class ExternalMemory implements Channel {

  /**
   * Size in blocks.
   */
  public static final int SIZE = 1000;
  /**
   * How many words are written and read by a single command.
   */
  private static final int WORD_IO = 10;
  /**
   * Line size is equal to {@link #WORD_IO} words with 9 spaces and a newline.
   */
  private static final int LINE_SIZE = Word.LENGTH * WORD_IO + 9 + 1;

  /**
   * External memory raf.
   */
  private final RandomAccessFile raf;

  /**
   * Pointer to a block. Valid values are [0..SIZE].
   */
  private int pointer;


  public ExternalMemory(String path) {
    try {
      raf = new RandomAccessFile(path, "rwd");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new IllegalStateException(String.format("External memory file (%s) missing and couldn't be created!", path));
    }
  }

  public int getPointer() {
    return pointer;
  }

  public void setPointer(int pointer) {
    if (pointer < 0 || pointer > SIZE) {
      throw new IllegalArgumentException("Invalid pointer: " + pointer);
    }

    this.pointer = pointer;
  }


  /**
   * Reads 10 words starting with the location pointed by {@link #pointer}.
   * The pointer is not shifted afterwards!
   * @throws IOException if errors occur while reading from external memory file
   */
  public Word[] read() throws IOException {
    // Seek to pointed position
    raf.seek(pointer * LINE_SIZE);

    // Fill buffer
    final byte[] buffer = new byte[LINE_SIZE];
    raf.readFully(buffer);

    // Convert bytes to words
    final Word[] words = new Word[WORD_IO];
    int offset = 0;
    // There are 10 words on 1 line
    for (int i = 0; i < WORD_IO; i++) {
      words[i] = new Word(new String(buffer, offset, Word.LENGTH));

      // Increment offset by word length + a space
      offset += Word.LENGTH + 1;
    }

    return words;
  }

  /**
   * Writes 10 words starting with the location pointed by {@link #pointer}.
   * The pointer is not shifted afterwards!
   * @throws IOException if errors occur while writing to external memory file
   */
  public void write(@NotNull Word[] words) throws IOException {
    // Ensure we've the correct amount of words
    if (words.length < WORD_IO) {
      throw new IllegalStateException("Not enough words provided for an output!");
    }

    // Seek to pointed position
    raf.seek(pointer * LINE_SIZE);

    // Convert words to bytes
    final byte[] bytes = new byte[LINE_SIZE];
    int offset = 0;
    for (Word word : words) {
      final byte[] wordBytes = String.format("%s ", word.toString()).getBytes();

      System.arraycopy(wordBytes, 0, bytes, offset, wordBytes.length);

      // Increment offset by word length + a space
      offset += wordBytes.length;
    }
    // New line at the end
    bytes[bytes.length - 1] = '\n';

    // Write bytes
    raf.write(bytes);
  }

  /**
   * Closes the file and invalidates this object.
   */
  public void close() {
    try {
      raf.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
