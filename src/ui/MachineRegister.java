package ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Machine register container that displays a label and a field for modifiable data.
 * E.g. TMP 40565.
 */
public class MachineRegister extends HBox {

  @FXML
  private Label label;
  @FXML
  private TextField field;

  /**
   * Required c-tor
   */
  public MachineRegister() {
    super();
    final FXMLLoader loader = new FXMLLoader(getClass().getResource("machine_register.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public void setLabel(String label) {
    this.label.setText(label);
  }

  public String getLabel() {
    return label.getText();
  }

  public void setField(String field) {
    this.field.setText(field);
  }

  public String getField() {
    return field.getText();
  }

}