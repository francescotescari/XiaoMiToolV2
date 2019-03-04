package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.engine.ToolManager;
import javafx.application.Application;

import javafx.stage.Stage;

public class MainWindow extends Application {
    private static final String FXML_FILE = "MainFrame.fxml";


    @Override
    public void start(Stage primaryStage) throws Exception {

        ToolManager.init(primaryStage);
/*
        FXMLLoader loader = new FXMLLoader(FxmlManager.getFxml(FXML_FILE));
        loader.setController(new MainWindowController(primaryStage));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();*/
    }
}
