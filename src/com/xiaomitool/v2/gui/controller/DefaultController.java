package com.xiaomitool.v2.gui.controller;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.engine.ToolManager;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public abstract class DefaultController {
    public DefaultController(){}
    public DefaultController(Stage primaryStage){
      this.primaryStage = primaryStage;
    }
    protected Stage primaryStage;
    private double stageX, stageY;


    protected void initHeaderDrag(Node HEADER){
        HEADER.setOnMousePressed(event -> {
            HEADER.setCursor(Cursor.CLOSED_HAND);
            stageX = event.getSceneX();
            stageY = event.getSceneY();
        });
        HEADER.setOnMouseReleased(event -> {
            HEADER.setCursor(Cursor.DEFAULT);
            stageX = event.getSceneX();
            stageY = event.getSceneY();
        });
        HEADER.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - stageX);
            primaryStage.setY(event.getScreenY() - stageY);
        });
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected void setCloseImage(ImageView IMG_CLOSE){
        setCloseImage(IMG_CLOSE, false);
    }
    protected void setCloseImage(ImageView IMG_CLOSE, boolean exit){
        IMG_CLOSE.setOnMouseClicked(event -> {
            if (exit) {
                ToolManager.exit(0);
            } else {
                ToolManager.closeStage(primaryStage);
            }

        });
        GuiUtils.tooltip(IMG_CLOSE, LRes.TIP_WINDOW_CLOSE);
        GuiUtils.setViewportChange(IMG_CLOSE, new GuiUtils.GetViewport() {
            @Override
            public Rectangle2D get(int index) {
                return new Rectangle2D(index*11,-1,11,13);
            }
        });
    }
    protected void setMinifyImage(ImageView IMG_MIN){
        IMG_MIN.setOnMouseClicked(event -> primaryStage.setIconified(true));
        GuiUtils.tooltip(IMG_MIN,LRes.TIP_WINDOW_MINIMIZE);
        GuiUtils.setViewportChange(IMG_MIN, new GuiUtils.GetViewport() {
            @Override
            public Rectangle2D get(int index) {
                return new Rectangle2D(index*11,-1,11,13);
            }
        });
    }
    @FXML
    protected abstract void initialize();
}
