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
  private static final int LINE_SIZE = Word.MAX_LENGTH * WORD_IO + 9 + 1;
  /**
   * Initially all external memory words will be filled with these characters.
   */
  private static final char FILLER = '0';

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
      initialize();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new IllegalStateException(String.format("External memory file (%s) missing and couldn't be created!", path));
    }
  }

  /**
   * Initializes external memory file with {@link #FILLER}.
   */
  private void initialize() {
    // Create initial word
    String string = "";
    for (int i = 0; i < Word.MAX_LENGTH; i++) {
      string += FILLER;
    }
    final Word word = new Word(string);

    // Combine 10 words (note reference is the same!)
    final Word[] words = new Word[WORD_IO];
    for (int i = 0; i < WORD_IO; i++) {
      words[i] = word;
    }

    // Write 100 times incrementing pointer each time (note reference is the same!)
    for (int i = 0; i < Math.ceil(SIZE / WORD_IO); i++) {
      write(words);
      setPointer(getPointer() + 1);
    }

    // Reset pointer
    setPointer(0);
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
   * @throws RuntimeException if errors occur while reading from external memory file
   */
  public Word[] read() throws RuntimeException {
    try {
      return readInternal();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private Word[] readInternal() throws IOException {
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
      words[i] = new Word(new String(buffer, offset, Word.MAX_LENGTH));

      // Increment offset by word length + a space
      offset += Word.MAX_LENGTH + 1;
    }

    return words;
  }

  /**
   * Writes 10 words starting with the location pointed by {@link #pointer}.
   * The pointer is not shifted afterwards!
   * @throws RuntimeException if errors occur while writing to external memory file
   */
  public void write(@NotNull Word[] words) throws RuntimeException {
    try {
      writeInternal(words);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private void writeInternal(@NotNull Word[] words) throws IOException {
    // Ensure we've the correct amount of words
    if (words.length < WORD_IO) {
      throw new IllegalStateException("Not enough words provided for an output!");
    }

    // Seek to pointed position
    raf.seek(pointer * LINE_SIZE);

    // Convert words to bytes
    final byte[] bytes = new byte[LINE_SIZE];
    int offset = 0;
    for (int i = 0; i < WORD_IO; i++) {
      final byte[] wordBytes = String.format("%5s ", words[i].toString()).getBytes();

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

  @Override
  public int getIndex() {
    return 3;
  }

}
