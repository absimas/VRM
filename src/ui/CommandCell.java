package ui;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

/**
 * Command {@link javafx.scene.control.ListView} item model.
 */
public class CommandCell extends ListCell<String> {

  private static final String LAST_CELL_STYLE_CLASS = "last-cell";
  private static final String EMPTY_CELL_STYLE_CLASS = "empty-cell";

  public CommandCell() {

  }

  /**
   * Method is called each time items are updated <b>and</b> when they're re-used.
   * Because of the re-usability, we need to adjust the new styles every time.
   */
  @Override
  public void updateItem(String string, boolean empty) {
    super.updateItem(string, empty);
    // Clear extra styles
    getStyleClass().removeAll(LAST_CELL_STYLE_CLASS);
    getStyleClass().removeAll(EMPTY_CELL_STYLE_CLASS);

    // Add specific extra style
    if (empty) {
      getStyleClass().add(EMPTY_CELL_STYLE_CLASS);
    } else if (getIndex() == getListView().getItems().size() - 1) {
      getStyleClass().add(LAST_CELL_STYLE_CLASS);
    }

    setGraphic(new Label(string));
  }

}
