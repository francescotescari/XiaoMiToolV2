package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.logging.Log;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Preloader {
    private static SplashScreen instance;
    private String title;
    private Image image;
    private Stage primaryStage, secondaryStage;
    public SplashScreen(String title, Image image) {
        Log.debug(image);
        this.title = title;
        this.image = image;
    }

    public static SplashScreen getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        Log.debug(this.image.getHeight());
        StackPane root = new StackPane(new ImageView(this.image));
        Scene scene = new Scene(root);
        //scene.setFill(Color.TRANSPARENT);
        primaryStage.setTitle(this.title);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);

        Stage secondaryStage = new Stage(StageStyle.UNDECORATED);
        secondaryStage.initOwner(primaryStage);
        secondaryStage.setScene(scene);
        primaryStage.show();
        secondaryStage.show();
        this.primaryStage = primaryStage;
        this.secondaryStage = secondaryStage;
        instance = this;

    }

    public void stopSplash() {
        this.secondaryStage.close();
        this.primaryStage.close();
        instance = null;
    }


    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        Log.debug(evt);
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            this.primaryStage.hide();
        }
    }
}
