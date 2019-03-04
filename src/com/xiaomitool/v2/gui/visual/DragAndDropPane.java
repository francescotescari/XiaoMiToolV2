package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;

import com.xiaomitool.v2.utility.utils.FileUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;

public class DragAndDropPane extends StackPane {
    private double cWidth, cHeight;
    private FileChooser.ExtensionFilter[] filters;
    private static final String selectText = LRes.FILE_SELECT_DRAGDROP.toString();



    private SimpleObjectProperty<File> inputFile = new SimpleObjectProperty<>();

    public DragAndDropPane(double width, double height){
        this.cWidth = width;
        this.cHeight = height;
        super.setPrefWidth(width);
        super.setPrefHeight(height);
        build();
    }

    public File getSelectedFile(){
        return inputFile.getValue();
    }
    private Text insideString;
    private Canvas canvas;
    private VBox content;
    private StackPane empty, border;

    private Border getBorder(boolean dashed){
        return new Border(new BorderStroke(Color.gray(0.4), dashed ? BorderStrokeStyle.DASHED : BorderStrokeStyle.SOLID, new CornerRadii(0.1,true), new BorderWidths((cWidth+cHeight)/100)));
    }

    private void build(){
        insideString = new Text(selectText);
        border = new StackPane();
        border.setBorder(new Border(new BorderStroke(Color.gray(0.4), BorderStrokeStyle.DASHED, new CornerRadii(0.1,true), new BorderWidths((cWidth+cHeight)/100))));
        double canvasSize = (cWidth+cHeight)/8;
        canvas = new Canvas(canvasSize,canvasSize);
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(Color.gray(0.2));
        context.fillRect(4d/9*canvasSize,0,1d/9*canvasSize,canvasSize);
        context.fillRect(0,4d/9*canvasSize,canvasSize,1d/9*canvasSize);
        super.setPadding(new Insets(cHeight/12,cWidth/12,cHeight/12,cWidth/12));

        insideString.setWrappingWidth(cWidth*7d/10);
        insideString.setTextAlignment(TextAlignment.CENTER);
        double fontSize = cHeight/20;
        if (fontSize > 19){
            fontSize = 19;
        }
        insideString.setFont(Font.font(fontSize));
        double th = insideString.getLayoutBounds().getHeight();
        empty = new StackPane();
        empty.setPrefHeight(th);
        content = new VBox(insideString,canvas, empty);
        content.setAlignment(Pos.CENTER);
        content.setSpacing(cHeight/30);
        border.getChildren().add(content);
        super.getChildren().add(border);
        super.setCursor(Cursor.HAND);
        super.setOpacity(0.6);
        super.setOnDragOver(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != this
                        && event.getDragboard().hasFiles()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });
        super.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    File path = db.getFiles().get(0);
                    if (path != null) {
                        String filePath = path.toString();
                        if (filters != null) {
                            for (FileChooser.ExtensionFilter filter : filters) {
                                for (String ext : filter.getExtensions()) {
                                    if (filePath.toLowerCase().endsWith(ext.toLowerCase())){
                                        success = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (success) {
                            inputFile.set(path);
                            Log.debug(path.toString());
                        }

                    }
                }
                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(success);

                event.consume();
            }
        });
        super.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                File file = FileUtils.selectFile(LRes.FILE_PLEASE_SELECT.toString("twrp"), filters);
                if (file != null){
                    inputFile.set(file);
                    Log.debug(file);
                }
            }
        });
        this.setOnFileChange(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String text;
                        if (newValue == null){
                            text = selectText;
                            content.getChildren().clear();
                            content.getChildren().addAll(insideString,canvas,empty);
                            border.setBorder(getBorder(true));
                            DragAndDropPane.super.setOpacity(0.6);

                        } else {
                            text = LRes.FILE_SELECTED+":\n"+FilenameUtils.getName(newValue.toString());
                            content.getChildren().clear();
                            content.getChildren().addAll(insideString);
                            border.setBorder(getBorder(false));
                            DragAndDropPane.super.setOpacity(0.75);

                        }
                        insideString.setText(text);
                    }
                };
                WindowManager.runNowOrLater(runnable);


            }
        });
    }

    public void setOnFileChange(ChangeListener<? super File> listener){
        inputFile.addListener(listener);
    }

    public void setFilters(FileChooser.ExtensionFilter[] filters) {
        this.filters = filters;
    }
}
