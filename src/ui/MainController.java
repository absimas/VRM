package ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import vrm.MemoryBlock;
import vrm.Utils;
import vrm.VRM;

public class MainController implements Initializable {

  @FXML
  private ListView<String> commandList;
  @FXML
  private TableView<MemoryBlock> memoryTable;
  @FXML
  private GridPane realMachine, virtualMachine;
  @FXML
  private TextField input, output;
  @FXML
  private Button outputPush;
  private final VRM vrm;

  private int vmIndex = -1;

  /**
   * Required c-tor
   */
  public MainController() throws InterruptedException {
    vrm = new VRM();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Command ListView
    initializeCommandList();

    // Memory TableView
    initializeMemoryTable();
  }

  private void initializeCommandList() {
    commandList.setCellFactory(param -> new CommandCell());
    commandList.getSelectionModel().select(13);
    commandList.scrollTo(13);

    // Consume mouse and keyboard clicks but retain the ability to scroll.
    commandList.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
    commandList.addEventFilter(KeyEvent.ANY, Event::consume);
  }

  private void initializeMemoryTable() {
    // Consume all mouse events that occur at the first 25 pixels (top row). This will disable column reordering, resizing and sorting.
    memoryTable.addEventFilter(MouseEvent.ANY, event -> {
      if (event.getY() <= 25) {
        event.consume();
      }
    });

    // Enable multi-cell selection
    memoryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    memoryTable.getSelectionModel().setCellSelectionEnabled(true);

    // Create columns
    final ObservableList<TableColumn<MemoryBlock, ?>> columns = FXCollections.observableArrayList();

    // First column displays MemoryBlock#index (Integer)
    final TableColumn<MemoryBlock, Integer> baseColumn = new TableColumn<>("#");
    baseColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getIndex()));
    columns.add(baseColumn);

    // Other columns
    for (int i = 0; i < 10; i++) {
      final int index = i;
      final TableColumn<MemoryBlock, String> column = new TableColumn<>(String.valueOf(i));
      // Value: MemoryBlock#words[i] (String)
      column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getWords().get(index).toString()));

      // Cell style is based on the current VM index
      column.setCellFactory(new Callback<TableColumn<MemoryBlock, String>, TableCell<MemoryBlock, String>>() {
        @Override
        public TableCell<MemoryBlock, String> call(TableColumn<MemoryBlock, String> param) {
          return new TableCell<MemoryBlock, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
              super.updateItem(item, empty);
              setGraphic(new Label(item));

              // Clear specific style
              getStyleClass().removeAll("vm-cell");
              if (getVMIndex() == -1) return;

              // Add vm-cell style class to all cells that are within the VM's memory
              final int startInclusive = 7 + getVMIndex() * 10, endExclusive = startInclusive + 10;
              if (getIndex() >= startInclusive && getIndex() < endExclusive) {
                getStyleClass().add("vm-cell");
              }
            }
          };
        }
      });

      columns.add(column);
    }

    // Assign columns to table
    memoryTable.getColumns().setAll(columns);

    // Wrap RM memory (1000 Words) with MemoryBlocks (10 Words each)
    final ObservableList<MemoryBlock> blocks = FXCollections.observableArrayList();
    for (int i = 0; i < vrm.realMachine.memory.size() / 10; i++) {
      blocks.add(new MemoryBlock(i, vrm.realMachine.memory.get(i * 10, 10)));
    }

    memoryTable.getItems().setAll(blocks);

    // Delay a forced scroll so the scrollbar takes sufficient space in the TableView
    Utils.delay(() -> memoryTable.scrollTo(0), 1);
  }

  public void setVMIndex(int index) {
    vmIndex = index;

    // Refresh TableView cells
    memoryTable.refresh();
  }

  public int getVMIndex() {
    return vmIndex;
  }

}