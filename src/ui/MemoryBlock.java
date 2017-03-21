package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Memory {@link javafx.scene.control.TableView} item.
 */
public class MemoryBlock {

  /**
   * Memory block index.
   */
  private int index;
  /**
   * Words within this memory block.
   */
  private ObservableList<String> words;

  /**
   * Required c-tor
   */
  public MemoryBlock() {

  }

  public MemoryBlock(int index, ObservableList<String> words) {
    this.index = index;
    this.words = words;
  }

  public MemoryBlock(int index, String... words) {
    this.index = index;
    this.words = FXCollections.observableArrayList(words);
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public ObservableList<String> getWords() {
    return words;
  }

  public void setWords(ObservableList<String> words) {
    this.words = words;
  }

}
