package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.engine.ToolManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainWindow extends Application {
  private static final String FXML_FILE = "MainFrame.fxml";
  private static String[] arguments;

  public static void main(String[] args) {
    arguments = args;
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  ToolManager.init(primaryStage, arguments);
                } catch (Exception e) {
                  e.printStackTrace();
                  System.exit(1);
                }
              }
            })
        .start();
  }
}
