package vrm;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import vrm.exceptions.MemoryOutOfBoundsException;

/**
 * Memory - an array of fixed position words.
 */
public class Memory implements Iterable<Word> {

  private final List<Word> words;

  /**
   * Create a memory with given amount of words.
   * @param size  word count
   */
  public Memory(int size) {
    if (size <= 0) {
      throw new IllegalStateException("Negative memory size specified!");
    }

    words = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      words.add(new Word());
    }
  }

  /**
   * Create Memory from a given list of words. Private constructor to prevent modification of underlying words outside.
   */
  private Memory(@NotNull List<Word> words) {
    this.words = words;
  }

  /**
   * @see List#size()
   */
  public int size() {
    return words.size();
  }

  /**
   * Fetches the word at the given index.
   * @throws MemoryOutOfBoundsException when referring to a word outside the given memory block
   */
  public Word get(int index) throws MemoryOutOfBoundsException {
    try {
      return words.get(index);
    } catch (IndexOutOfBoundsException ignored) {
      throw new MemoryOutOfBoundsException(ignored);
    }
  }

  /**
   * Fetches a specified amount of words from the given index (inclusive).
   * @throws MemoryOutOfBoundsException when referring to a word outside the given memory block
   */
  public Word[] get(int start, int count) throws MemoryOutOfBoundsException {
    try {
      return words.subList(start, start+count).toArray(new Word[0]);
    } catch (IndexOutOfBoundsException ignored) {
      throw new MemoryOutOfBoundsException(ignored);
    }
  }

  /**
   * Convenience method to access and replace a specific word.
   * @param index  word index
   * @param word   replacement
   * @throws MemoryOutOfBoundsException when referring to a word outside the given memory block
   */
  public void replace(int index, String word) throws MemoryOutOfBoundsException {
    get(index).replace(word);
  }

  /**
   * Convenience method to access and replace a specific word.
   * @param index  word index
   * @param word   replacement
   * @throws MemoryOutOfBoundsException when referring to a word outside the given memory block
   */
  public void replace(int index, Word word) throws MemoryOutOfBoundsException {
    get(index).replace(word.toString());
  }

  /**
   * Convenience method to access and replace multiple words beginning at a specific index.
   * @param index  starting word index (inclusive)
   * @param words  words used for replacement
   * @throws MemoryOutOfBoundsException when referring to a word outside the given memory block
   */
  public void replace(int index, @NotNull String... words) throws MemoryOutOfBoundsException {
    for (String word : words) {
      get(index++).replace(word);
    }
  }

  /**
   * Convenience method to access and replace multiple words beginning at a specific index.
   * @param index  starting word index (inclusive)
   * @param words  words used for replacement
   * @throws MemoryOutOfBoundsException when referring to a word outside the given memory block
   */
  public void replace(int index, @NotNull Word... words) throws MemoryOutOfBoundsException {
    for (Word word : words) {
      get(index++).replace(word.toString());
    }
  }

  /**
   * Get memory sublist.
   * @param from  starting index (inclusive). Must be less than {@link #size()}.
   * @param to    ending index (exclusive). Must be less than {@link #size()}.
   * @return memory sublist.
   */
  public Memory sublist(int from, int to) {
    return new Memory(words.subList(from, to));
  }

  /* Iterable<Word> */
  @Override
  public Iterator iterator() {
    return new Iterator(words.iterator());
  }

  @Override
  public void forEach(Consumer<? super Word> action) {
    words.forEach(action);
  }

  @Override
  public Spliterator<Word> spliterator() {
    return words.spliterator();
  }

  /**
   * Iterator wrapper that disables removing.
   */
  public static class Iterator implements java.util.Iterator<Word> {

    private final java.util.Iterator<Word> iterator;

    public Iterator(java.util.Iterator<Word> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Word next() {
      return iterator.next();
    }

    @Override
    public void remove() {
      throw new IllegalAccessError("Can't remove items from this iterator!");
    }

    @Override
    public void forEachRemaining(Consumer<? super Word> action) {
      iterator.forEachRemaining(action);
    }

  }

}
