package vrm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Block of {@link Word}s.
 */
public class MemoryBlock {

  /**
   * Memory block index.
   */
  private int index;
  /**
   * Words within this memory block.
   */
  private final ObservableList<Word> words;

  /**
   * Required c-tor
   */
  public MemoryBlock() {
    words = FXCollections.observableArrayList();
  }

  public MemoryBlock(int index, Word... words) {
    this.index = index;
    this.words = FXCollections.observableArrayList(words);
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public ObservableList<Word> getWords() {
    return words;
  }

}
