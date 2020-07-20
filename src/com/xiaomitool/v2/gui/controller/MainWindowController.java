package com.xiaomitool.v2.gui.controller;

import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.engine.ToolRunner;
import com.xiaomitool.v2.gui.GuiObjects;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.VisiblePane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.utility.utils.InetUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainWindowController extends DefaultController {
    private Stage primaryStage;
    private double stageX, stageY;
    @FXML
    private HBox HEADER;
    @FXML
    private ImageView IMG_SETTINGS, IMG_CLOSE, IMG_MIN;
    @FXML
    private Hyperlink TRANSLATED_LINK, LOGIN_LINK;
    @FXML
    private Label LOGIN_NUMBER, VERSION_NUMBER, TEXT_TRANSLATED_BY;
    @FXML
    private BorderPane BORDER_PANE;

    public MainWindowController() {
    }

    private String translateUrl = null;

    public MainWindowController(Stage primaryStage) {
        super(primaryStage);
        this.primaryStage = primaryStage;
    }

    private static MainWindowController instance = null;

    public static MainWindowController getInstance(){
        return instance;
    }

    @FXML
    protected void initialize() {
        saveNodes();
        initHeaderDrag(HEADER);
        setSettingsImage(IMG_SETTINGS);
        setCloseImage(IMG_CLOSE, true);
        setMinifyImage(IMG_MIN);
        initOnClick();
        initVisiblePane();
        initDisclaimer();
        initTranslateClick();
        initText();
        instance = this;
    }

    public void retext(){
        initText();
    }

    private void initText() {
        TEXT_TRANSLATED_BY.setText(LRes.TRANSLATED_BY.toString());
        String version = "V" + ToolManager.TOOL_VERSION;
        if (ToolManager.TOOL_VERSION_EX != null && !ToolManager.TOOL_VERSION_EX.isEmpty()) {
            version += " (" + ToolManager.TOOL_VERSION_EX + ")";
        }
        VERSION_NUMBER.setText(version);
        GuiUtils.tooltip(IMG_SETTINGS, LRes.TIP_WINDOW_SETTINGS);
        translateUrl = LRes.TRANSLATED_URL.toString();
    }

    private void initVisiblePane() {
        VisiblePane pane = new VisiblePane();
        WindowManager.setMainVisiblePane(pane);
        BORDER_PANE.setCenter(pane.getPane());
    }

    private void initDisclaimer() {
        ToolRunner.start();
    }

    private void saveNodes() {
        GuiObjects.set(GuiObjects.IMG_CLOSE, IMG_CLOSE);
        GuiObjects.set(GuiObjects.IMG_MINIFY, IMG_MIN);
        GuiObjects.set(GuiObjects.IMG_SETTINGS, IMG_SETTINGS);
        GuiObjects.set(GuiObjects.LOGIN_LINK, LOGIN_LINK);
        GuiObjects.set(GuiObjects.LOGIN_NUMBER, LOGIN_NUMBER);
    }

    private void setSettingsImage(ImageView IMG_SETTINGS) {
        GuiUtils.setViewportChange(IMG_SETTINGS, new GuiUtils.GetViewport() {
            @Override
            public Rectangle2D get(int index) {
                return new Rectangle2D(5 + index * 24, 0, 14, 14);
            }
        });

        IMG_SETTINGS.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                WindowManager.launchSettings();
            }
        });
    }

    private void initOnClick() {
        LOGIN_LINK.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                LoginController.loginClick();
            }
        });
    }

    private void initTranslateClick(){
        TRANSLATED_LINK.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (translateUrl != null && translateUrl.startsWith("http")){
                    InetUtils.openUrlInBrowser(translateUrl);
                }
            }
        });
    }
}
