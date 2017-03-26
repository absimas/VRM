package ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import vrm.MemoryBlock;
import vrm.Utils;
import vrm.VRM;
import vrm.Word;

public class MainController implements Initializable {

  @FXML
  private ListView<String> commandLog;
  @FXML
  private TableView<MemoryBlock> memoryTable;
  @FXML
  private MachineRegister rmTMP, rmPTR, rmIC, rmC, rmTI, rmPI, rmSI, rmIOI, rmMODE, rmBUSY;
  @FXML
  private MachineRegister vmTMP, vmIC, vmC;
  @FXML
  private TextField input, output;

  private final VRM vrm;
  private int vmIndex = -1;

  /**
   * Required c-tor
   */
  public MainController() throws InterruptedException {
    vrm = new VRM(this);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Command ListView
    initializeCommandList();

    // Memory TableView
    initializeMemoryTable();

    // Machine registers (just update with VRM data)
    updateRegisters();

    // Input field listener
    initializeIO();

    // Begin execution
    vrm.commandLog.addListener((ListChangeListener<String>) c -> draw());

    // Execute VRM on a different thread so we can interrupt the waits
    new Thread(() -> {
      try {
        vrm.begin();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();

//    // Imitate looping
//    Runnable r = new Runnable() {
//      @Override
//      public void run() {
//        vrm.forward();
//        Utils.delay(this, 100);
//      }
//    };
//
//    Utils.delay(r, 100);
  }

  private void initializeIO() {
    // Input
    input.textProperty().addListener((observable, oldValue, newValue) -> {
      String value;
      try {
        // Try preceding number with zeroes
        final int number = Integer.parseInt(newValue);
        value = Utils.precedeZeroes(number, Word.LENGTH);
      } catch (NumberFormatException ignored) {
        // Otherwise precede with spaces
        value = String.format("%5s", newValue);
      }

      vrm.realMachine.keyboard.word = new Word(value);
    });

    // Output
    // Disable clicking and in/out focusing
    output.setMouseTransparent(false);
    output.setFocusTraversable(false);
    output.addEventFilter(MouseEvent.ANY, Event::consume);
  }

  private void initializeCommandList() {
    // Consume mouse and keyboard clicks but retain the ability to scroll.
    commandLog.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
    commandLog.addEventFilter(KeyEvent.ANY, Event::consume);

    // Initially, the list contains 1 empty item to prevent drawing an empty (white) list
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
              if (vrm.realMachine.getVirtualMachineId() == -1) return;

              // Add vm-cell style class to all cells that are within the VM's memory
              final int startInclusive = 7 + vrm.realMachine.getVirtualMachineId() * 10, endExclusive = startInclusive + 10;
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

  public void draw() {
    final List<String> commands = new ArrayList<>(vrm.commandLog);
    // Execute drawing on the UI thread
    Platform.runLater(() -> {
      // Re-draw log
      updateLog(commands);

      // Re-draw grid
      updateMemory();

      // Re-draw registers
      updateRegisters();

      // Re-draw I/O fields
      updateIO();
    });
  }

  private void updateLog(List<String> commands) {
    commandLog.getItems().setAll(commands);
    commandLog.scrollTo(commands.size());
  }

  private void updateMemory() {
    memoryTable.refresh();
  }

  private void updateRegisters() {
    // RM registers
    rmTMP.setField(vrm.realMachine.TMP.toString());
    rmPTR.setField(String.valueOf(vrm.realMachine.PTR));
    rmIC.setField(String.valueOf(vrm.realMachine.IC));
    rmC.setField(String.valueOf(vrm.realMachine.C));
    rmTI.setField(String.valueOf(vrm.realMachine.TI));
    rmPI.setField(String.valueOf(vrm.realMachine.PI.ordinal()));
    rmSI.setField(String.valueOf(vrm.realMachine.SI.ordinal()));
    rmIOI.setField(String.valueOf(vrm.realMachine.IOI));
    rmMODE.setField(vrm.realMachine.MODE.name());
    rmBUSY.setField(String.valueOf(vrm.realMachine.BUSY));

    // VM registers
    if (vrm.virtualMachine == null) return;
    vmTMP.setField(vrm.virtualMachine.TMP.toString());
    vmIC.setField(String.valueOf(vrm.virtualMachine.IC));
    vmC.setField(String.valueOf(vrm.virtualMachine.C));
  }

  private void updateIO() {
    // Input
    Word word = vrm.realMachine.keyboard.word;
    if (word == null) {
      input.clear();
    } else {
      input.setText(word.toString());
    }

    // Output
    word = vrm.realMachine.screen.word;
    if (word == null) {
      output.clear();
    } else {
      output.setText(word.toString());
    }

    // Remove focus from both fields
    input.getParent().requestFocus();
    output.getParent().requestFocus();
  }

  /**
   * Called when next command button is pressed
   */
  @FXML
  public void nextCommand() {
    vrm.forward();
  }

}
