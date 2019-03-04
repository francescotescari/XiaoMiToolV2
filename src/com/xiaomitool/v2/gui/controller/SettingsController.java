package com.xiaomitool.v2.gui.controller;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SettingsController extends DefaultController {
    @FXML
    private ImageView IMG_CLOSE;
    @FXML
    private HBox HEADER;
    @FXML
    private Label LABEL_DOWNLOAD, LABEL_EXTRACT, TEXT_DOWNLOAD, TEXT_EXTRACT;
    @FXML
    private Button BUTTON_DOWNLOAD, BUTTON_EXTRACT, BUTTON_CLEAR, BUTTON_RESET;
    @FXML
    private CheckBox CHECK_SAVE_LOGIN;

    @Override
    protected void initialize() {
        setCloseImage(IMG_CLOSE);
        initHeaderDrag(HEADER);
        initCss();
        initTexts();
        loadSettings();
        initOnClick();
    }

    private void initCss(){
        TEXT_EXTRACT.setStyle("-fx-text-overrun: leading-ellipsis;");
        TEXT_DOWNLOAD.setStyle("-fx-text-overrun: leading-ellipsis;");
        BUTTON_CLEAR.setDisable(true);
    }
    private void initTexts(){
        LABEL_DOWNLOAD.setText(LRes.SETTINGS_DOWNLOAD_DIR.toString());
        LABEL_EXTRACT.setText(LRes.SETTINGS_EXTRACT_DIR.toString());
        BUTTON_DOWNLOAD.setText(LRes.CHOOSE.toString());
        BUTTON_CLEAR.setText(LRes.SETTINGS_CLEAR.toString());
        BUTTON_EXTRACT.setText(LRes.CHOOSE.toString());
        CHECK_SAVE_LOGIN.setText(LRes.SETTINGS_SAVE_SESSION.toString());
    }

    private void loadSettings(){
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
        Log.debug(TEXT_DOWNLOAD.getLayoutBounds().getWidth());
        TEXT_EXTRACT.setText(edir);
        String saveSession = SettingsUtils.getOpt(SettingsUtils.PREF_SAVE_SESSION);
        boolean bool = "true".equalsIgnoreCase(saveSession);
        CHECK_SAVE_LOGIN.setSelected(bool);


    }

    private void initOnClick(){
        BUTTON_EXTRACT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(SettingsUtils.getExtractPath().toFile());
                File choice = directoryChooser.showDialog(WindowManager.mainWindow());
                if (choice == null){
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
                if (choice == null){
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
                SettingsUtils.saveOpt(SettingsUtils.PREF_SAVE_SESSION,val);
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
