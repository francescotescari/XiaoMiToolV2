package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.engine.actions.ActionsStatic;
import com.xiaomitool.v2.gui.controller.*;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.fxml.FxmlManager;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.visual.OverlayPane;
import com.xiaomitool.v2.gui.visual.ToastPane;
import com.xiaomitool.v2.gui.visual.VisiblePane;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.logging.feedback.LogSender;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.utility.RunnableMessage;
import com.xiaomitool.v2.utility.SilentCompleteFuture;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.io.IOException;

public class WindowManager {
    public static final Color XIAOMI_COLOR = Color.rgb(255,103, 0);
    public static final String XIAOMI_COLOR_HEX = "FF6700";
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.rgb(245,245,245);
    public static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(DEFAULT_BACKGROUND_COLOR, null, null));
    public static final String FRAME_LOGIN = "LoginFrame";
    public static final String FRAME_MAIN = "MainFrame";
    public static final String FRAME_SETTINGS = "SettingsFrame";
    private static final String FRAME_POPUP = "Popup";
    private static final String DEFAULT_TITLE = "XiaoMiTool V2";
    public static final double PREF_WIN_WIDTH = 860;
    public static final double PREF_WIN_HEIGHT = 860;
    private static final Image ICON_IMAGE = DrawableManager.getResourceImage("icon.png");
    private static Stage mainStage;
    private static ToastPane toastPane;
    private static OverlayPane mainOverlay;
    private static VisiblePane mainVisiblePane;
    private static final DropShadow windowDropShadow = new DropShadow(10, 1, 1, Color.gray(0.7));

    public static void setMainVisiblePane(VisiblePane pane){


        mainVisiblePane = pane;
        mainVisiblePane.getPane().setMaxSize(PREF_WIN_WIDTH, PREF_WIN_HEIGHT-90);
    }
    public static void setMainContent(Node node){
        setMainContent(node,true);
    }
    public static void removeTopContent(){
        if (mainVisiblePane == null){
            return;
        }
        if (Platform.isFxApplicationThread()) {
            mainVisiblePane.removeTop();
        } else  {
            Platform.runLater(WindowManager::removeTopContent);
        }
    }
    public static void setMainContent(Node node, boolean deleteUnder){
        if (mainVisiblePane == null){
            return;
        }
        if (Platform.isFxApplicationThread()) {
            mainVisiblePane.add(node);
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                        mainVisiblePane.saveStack(!deleteUnder);
                        mainVisiblePane.add(node);

                }
            });
        }

    }

    public  static void hotKey(){
        mainStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Log.info(event.getCode().getName());
            }
        });
    }

    public static double getContentHeight(){
        if (mainVisiblePane != null && mainVisiblePane.getPane() != null && mainVisiblePane.getPane().getHeight() > 0){
            return mainVisiblePane.getPane().getHeight();
        }
        return PREF_WIN_HEIGHT-100;
    }
    public static double getContentWidth(){
        if (mainVisiblePane != null && mainVisiblePane.getPane() != null && mainVisiblePane.getPane().getWidth() > 0){
            return mainVisiblePane.getPane().getWidth();
        }
        return PREF_WIN_WIDTH;
    }
    public static Stage getMainStage(){
        return mainStage;
    }


    public static Pane getMainPane(){
        return mainVisiblePane.getPane();
    }

    public static Stage mainWindow(){
        return mainStage;
    }

    public static Stage launchSettings(){
        return launchWindow(FRAME_SETTINGS,new SettingsController());
    }



    public static Stage launchLogin(){
        return launchWindow(FRAME_LOGIN, new LoginController());
    }

    private static RunnableMessage ASK_FEEDBACK = new RunnableMessage() {
        @Override
        public int run() throws InterruptedException {
            WindowManager.setOnExitAskForFeedback(false);
            if (LogSender.isLogCooldown()){
                return 0;
            }
            ActionsStatic.ASK_FOR_FEEDBACK().run();
            return 0;
        }
    };

    private static RunnableMessage ON_BEFORE_CLOSE = null;

    public static void setOnExitAskForFeedback(boolean ask){
        ON_BEFORE_CLOSE = ask ? ASK_FEEDBACK : null;
    }

    private static MainWindowController mainWindowController = null;
    public static void launchMain(Stage primaryStage){
        mainStage = primaryStage;
        mainWindowController = new MainWindowController();
        launchWindow(FRAME_MAIN, mainWindowController,primaryStage);
        mainWindowController.setOnBeforeClose(new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                ActionsStatic.CLOSING().run();
                RunnableMessage r = ON_BEFORE_CLOSE;
                if (r != null){
                    return r.run();
                }
                return 0;
            }
        });
        primaryStage.setOnCloseRequest(event -> {
            ToolManager.exit(0);
            // Save file
        });
        //hotKey();
    }
    public static Stage launchWindow(String fxml, DefaultController controller){
        return launchWindow(fxml,controller,null);
    }
    public static void toast(String message){
        if (toastPane == null){
            Log.warn("Cannot toast message: "+message);
            return;
        }
        toastPane.toast(message);
    }
    public static Stage launchPopup(PopupWindow popupWindow){
        SilentCompleteFuture<Stage> future = new SilentCompleteFuture<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Pointer pointer = new Pointer();
                PopupController controller = new PopupController(popupWindow);
                Stage stage = launchWindow(FRAME_POPUP,controller,null,pointer);
                Parent p = stage.getScene().getRoot();
                ((Pane) pointer.pointed).setMinSize(popupWindow.getWidth(),popupWindow.getHeight());

                stage.setTitle(DEFAULT_TITLE);
                stage.sizeToScene();
                centerStage(stage);
                future.complete(stage);
            }
        };
        if (Platform.isFxApplicationThread()){
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
        return future.getSilently();

    }
    private static Stage launchWindow(String fxml, DefaultController controller, Stage primaryStage){
        return launchWindow(fxml,controller,primaryStage,null);
    }
    private static void centerStage(Stage stage){

                double x = mainStage.getX()+(mainStage.getWidth()-stage.getWidth())/2;
                double y = mainStage.getY()+(mainStage.getHeight()-stage.getHeight())/2;
                stage.setX(x);
                stage.setY(y);



    }


    private static Stage launchWindow(String fxml, DefaultController controller, Stage primaryStage, Pointer pointer)  {
        boolean isMain = primaryStage == mainStage && primaryStage != null;
        if (isMain){
            Log.info("Launching main window");
        }
        primaryStage = primaryStage != null ? primaryStage : new Stage();
        FXMLLoader loader = new FXMLLoader(FxmlManager.getFxml(fxml));
        controller.setPrimaryStage(primaryStage);
        loader.setController(controller);
        Parent root = null;
        Parent realRoot = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            Log.error("Failed to load "+fxml+" fxml resource: "+e.getMessage());
            e.printStackTrace();
            return null;
        }



        Background background = DEFAULT_BACKGROUND;
        if (pointer != null) {
            pointer.pointed = root;
        }
        if (root instanceof Pane){
            ((Pane) root).setBackground(background);
            if (isMain){
                mainOverlay = new OverlayPane();
                toastPane = new ToastPane(mainOverlay, 300,20,10,100,1500);

            }

        }  else {
            background = null;
        }
        Scene scene;
        realRoot = root;
        if (background != null) {
            Log.debug("Dropping shadow on: "+root);
            StackPane pane = new StackPane(root);
            if (isMain){
                pane.getChildren().add(mainOverlay);

            }
            pane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.0);");
            root.setEffect(windowDropShadow);
            Log.debug(root.getEffect());
            pane.setPadding(new Insets(10));
            realRoot = pane;
        }

        scene = new Scene(realRoot);
        if (ICON_IMAGE != null) {
            primaryStage.getIcons().add(ICON_IMAGE);
        }
        primaryStage.setTitle(DEFAULT_TITLE);

        scene.setFill(Color.TRANSPARENT);
        if (!isMain){
            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.initOwner(mainStage);
        }
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.setScene(scene);
        Log.debug(primaryStage.getWidth());
        ToolManager.showStage(primaryStage);
        if (!isMain){
            centerStage(primaryStage);
        }
        return primaryStage;
    }

    public static void runNowOrLater(Runnable runnable) {
        if (Platform.isFxApplicationThread()){
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void popup(String string, PopupWindow.Icon icon) {
        runNowOrLater(new Runnable() {
            @Override
            public void run() {
                PopupWindow popupWindow = new PopupWindow.ImageTextPopup(string,icon);
                launchPopup(popupWindow);
            }
        });

    }

    public static OverlayPane requireOverlayPane() {
        return mainOverlay;
    }
}
