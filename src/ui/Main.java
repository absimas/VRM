package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    final Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
    primaryStage.setTitle("VRM");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();

    // After fitting all the content use those measurements as min screen size
    primaryStage.setMinWidth(primaryStage.getWidth());
    primaryStage.setMinHeight(primaryStage.getHeight());
    primaryStage.sizeToScene();
  }

  public static void main(String[] args) {
    launch(args);
  }

}
