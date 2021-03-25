package com.xiaomitool.v2.gui.controller;

import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.PopupWindow;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.CustomButton;
import com.xiaomitool.v2.gui.visual.OverlayPane;
import com.xiaomitool.v2.gui.visual.ToastPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.language.Lang;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.Pair;
import com.xiaomitool.v2.utility.RunnableMessage;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import javafx.application.Platform;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SettingsController extends DefaultController {
    private static final OverlayPane settingsOverlayPane = new OverlayPane();
    private static final ToastPane settingsToastPane = new ToastPane(settingsOverlayPane);
    private static PopupWindow feedbackPopup;
    @FXML
    private ImageView IMG_CLOSE;
    @FXML
    private HBox HEADER;
    @FXML
    private Label LABEL_DOWNLOAD, LABEL_EXTRACT, TEXT_DOWNLOAD, TEXT_EXTRACT;
    @FXML
    private Button BUTTON_DOWNLOAD, BUTTON_EXTRACT, BUTTON_CLEAR, BUTTON_RESET, BUTTON_FEEDBACK;
    @FXML
    private CheckBox CHECK_SAVE_LOGIN;
    @FXML
    private StackPane WHOLE;
    @FXML
    private ComboBox<String> REGION_COMBO, LANG_COMBO;


    public static PopupWindow getFeedbackPopupWindow() {
        if (feedbackPopup == null) {
            feedbackPopup = new PopupWindow(500, 400);
            Text title = new Text(LRes.SEND_FEEDBACK.toString());
            title.setFont(Font.font(20));
            TextArea textArea = new TextArea();
            textArea.setPrefHeight(180);
            textArea.setFont(Font.font(14));
            textArea.setPromptText("Sending feedback is not available in this build");
            textArea.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 500 ? change : null));
            CustomButton button = new CustomButton(LRes.SEND_FEEDBACK);
            button.setFont(Font.font(15));
            button.setDisable(true);
            textArea.setFocusTraversable(false);
            CheckBox checkBox = new CheckBox(LRes.INCLUDE_LOG_FILES.toString());
            checkBox.setSelected(true);
            checkBox.setFont(Font.font(15));
            VBox vBox = new VBox(18, title, textArea, checkBox, button);
            HBox hBox = new HBox(20, new Pane(), vBox, new Pane());
            hBox.setAlignment(Pos.CENTER);
            vBox.setAlignment(Pos.CENTER);
            feedbackPopup.setContent(hBox);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    feedbackPopup.getController().setOnBeforeClose(new RunnableMessage() {
                        @Override
                        public int run() throws InterruptedException {
                            textArea.setText("");
                            return 0;
                        }
                    });
                    String text = textArea.getText();
                    boolean sendLogFile = checkBox.isSelected();
                    if ((text == null || text.isEmpty()) && !sendLogFile) {
                        textArea.setText("");
                        textArea.setPromptText("You have to either send a text log or include the log file or both");
                    } else {
                        button.setDisable(true);
                        button.setText(LRes.UPLOADING_FEEDBACK.toString());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ToastPane feedbackToast = feedbackPopup.getToastPane();
                                ToastPane settingsToast = settingsToastPane;
                                try {
                                    /* if (!LogSender.uploadFeedback(text, sendLogFile)) {
                                        throw new Exception("Failed to uplaod the feedback, check the log file");
                                    }*/
                                    ToolManager.setOnExitAskForFeedback(false);
                                    feedbackPopup.getController().closeWindow();
                                    Platform.runLater(() -> settingsToast.toast(LRes.FEEDBACK_SENT.toString()));
                                } catch (Exception e) {
                                    Log.error("Failed to send the feedback: " + e.getMessage());
                                    Log.exc(e);
                                    if (feedbackToast != null) {
                                        Platform.runLater(() -> feedbackToast.toast(LRes.FEEDBACK_ERROR.toString()));
                                    }
                                }
                                //LogSender.cooldownCounter(button);
                            }
                        }).start();
                    }
                }
            });
        }
        return feedbackPopup;
    }

    @Override
    protected void initialize() {
        setCloseImage(IMG_CLOSE);
        initHeaderDrag(HEADER);
        initCss();
        initTexts();
        loadSettings();
        initOnClick();
        WHOLE.getChildren().add(settingsOverlayPane);
    }

    private void initCss() {
        TEXT_EXTRACT.setStyle("-fx-text-overrun: leading-ellipsis;");
        TEXT_DOWNLOAD.setStyle("-fx-text-overrun: leading-ellipsis;");
        BUTTON_CLEAR.setDisable(true);
        GuiUtils.specialComboBox(REGION_COMBO, LRes.SELECTED_REGION, 14);
        GuiUtils.specialComboBox(LANG_COMBO, LRes.SELECTED_LANG, 14);
    }

    private List<Pair<String, String>> langEntries;

    private void initTexts() {
        LABEL_DOWNLOAD.setText(LRes.SETTINGS_DOWNLOAD_DIR.toString());
        LABEL_EXTRACT.setText(LRes.SETTINGS_EXTRACT_DIR.toString());
        BUTTON_DOWNLOAD.setText(LRes.CHOOSE.toString());
        BUTTON_CLEAR.setText(LRes.SETTINGS_CLEAR.toString());
        BUTTON_RESET.setText(LRes.SETTINGS_RESET.toString());
        BUTTON_EXTRACT.setText(LRes.CHOOSE.toString());
        CHECK_SAVE_LOGIN.setText(LRes.SETTINGS_SAVE_SESSION.toString());
        BUTTON_FEEDBACK.setText(LRes.SEND_FEEDBACK.toString());
        REGION_COMBO.setPromptText(LRes.PLEASE_SELECT_REGION.toString());
        REGION_COMBO.setItems(new ObservableListBase<String>() {
            @Override
            public int size() {
                return SettingsUtils.Region.values().length;
            }

            @Override
            public String get(int index) {
                return SettingsUtils.Region.values()[index].toHuman();
            }
        });
        langEntries = Lang.getComboChoices();
        LANG_COMBO.setPromptText(LRes.PLEASE_SELECT_LANGUAGE.toString());
        LANG_COMBO.setItems(new ObservableListBase<String>() {
            @Override
            public int size() {
                return langEntries.size();
            }

            @Override
            public String get(int index) {
                return langEntries.get(index).getSecond();
            }
        });
    }

    private void loadSettings() {
        SettingsUtils.load();
        Path downloadDir = SettingsUtils.getDownloadPath();
        Path extractDir = SettingsUtils.getExtractPath();
        String ddir, edir;
        try {
            ddir = downloadDir.toFile().getCanonicalPath();
            edir = extractDir.toFile().getCanonicalPath();
        } catch (IOException e) {
            ddir = downloadDir.toString();
            edir = extractDir.toString();
        }
        TEXT_DOWNLOAD.setText(ddir);
        TEXT_EXTRACT.setText(edir);
        String saveSession = SettingsUtils.getOpt(SettingsUtils.PREF_SAVE_SESSION);
        boolean bool = "true".equalsIgnoreCase(saveSession);
        CHECK_SAVE_LOGIN.setSelected(bool);
    }

    private void initOnClick() {
        SettingsUtils.Region region = SettingsUtils.getRegion();
        if (region != null) {
            int i = 0;
            for (SettingsUtils.Region r : SettingsUtils.Region.values()) {
                if (region.equals(r)) {
                    REGION_COMBO.getSelectionModel().select(i);
                }
                ++i;
            }
        }
        REGION_COMBO.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SettingsUtils.Region[] regions = SettingsUtils.Region.values();
                int index = REGION_COMBO.getSelectionModel().getSelectedIndex();
                SettingsUtils.Region region = regions[index];
                Log.info("Selected region: " + region);
                SettingsUtils.setRegion(region);
            }
        });
        String langCode = SettingsUtils.getLanguage();
        if (langEntries != null && langCode != null){
            int i = 0;
            for (Pair<String, String> le : langEntries){
                if (langCode.equals(le.getFirst())){
                    LANG_COMBO.getSelectionModel().select(i);
                    break;
                }
                i++;
            }
        }
        LANG_COMBO.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int index = LANG_COMBO.getSelectionModel().getSelectedIndex();
                String langCode = langEntries.get(index).getFirst();
                Log.info("Selected language: " + langCode);
                SettingsUtils.setLanguage(langCode);
                closeWindow();
            }
        });
        BUTTON_EXTRACT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(SettingsUtils.getExtractPath().toFile());
                File choice = directoryChooser.showDialog(WindowManager.mainWindow());
                if (choice == null) {
                    return;
                }
                String path;
                try {
                    path = choice.getCanonicalPath();
                } catch (IOException e) {
                    path = choice.getAbsolutePath();
                }
                SettingsUtils.saveOpt(SettingsUtils.PREF_EXTRACT_DIR, path);
                TEXT_EXTRACT.setText(path);
            }
        });
        BUTTON_DOWNLOAD.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(SettingsUtils.getDownloadPath().toFile());
                File choice = directoryChooser.showDialog(WindowManager.mainWindow());
                if (choice == null) {
                    return;
                }
                String path;
                try {
                    path = choice.getCanonicalPath();
                } catch (IOException e) {
                    path = choice.getAbsolutePath();
                }
                SettingsUtils.saveOpt(SettingsUtils.PREF_DOWNLOAD_DIR, path);
                TEXT_DOWNLOAD.setText(path);
            }
        });
        CHECK_SAVE_LOGIN.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String val = CHECK_SAVE_LOGIN.isSelected() ? "true" : "false";
                SettingsUtils.saveOpt(SettingsUtils.PREF_SAVE_SESSION, val);
            }
        });
        BUTTON_FEEDBACK.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WindowManager.launchPopup(getFeedbackPopupWindow());
            }
        });
        BUTTON_RESET.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SettingsUtils.unset(SettingsUtils.PREF_SAVE_SESSION);
                SettingsUtils.unset(SettingsUtils.PREF_DOWNLOAD_DIR);
                SettingsUtils.unset(SettingsUtils.PREF_EXTRACT_DIR);
                String dp, ep;
                try {
                    dp = SettingsUtils.getDownloadPath().toFile().getCanonicalPath();
                    ep = SettingsUtils.getExtractPath().toFile().getCanonicalPath();
                } catch (IOException e) {
                    dp = SettingsUtils.getDownloadPath().toString();
                    ep = SettingsUtils.getExtractPath().toString();
                }
                TEXT_DOWNLOAD.setText(dp);
                TEXT_EXTRACT.setText(ep);
                CHECK_SAVE_LOGIN.setSelected(false);
            }
        });
    }
}
