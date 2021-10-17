package com.xiaomitool.v2.engine.actions;

import static com.xiaomitool.v2.engine.CommonsMessages.NOOP;

import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.PopupWindow;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.controller.SettingsController;
import com.xiaomitool.v2.gui.deviceView.DeviceView;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.raw.RawManager;
import com.xiaomitool.v2.gui.visual.*;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.language.Lang;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.tasks.DownloadTask;
import com.xiaomitool.v2.tasks.TaskManager;
import com.xiaomitool.v2.tasks.UpdateListener;
import com.xiaomitool.v2.utility.DriverUtils;
import com.xiaomitool.v2.utility.RunnableMessage;
import com.xiaomitool.v2.utility.utils.InetUtils;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.UpdateUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class ActionsStatic {
  public static final String XMT_UPDATE_FILENAME = "XiaoMiTool_update.jar";

  public static RunnableMessage MAIN() {
    return () -> {
      int message;
      message = FIRST_DISCLAIMER().run();
      if (message != 1) {
        ToolManager.exit(0);
        System.exit(0);
        return 0;
      }
      Log.info("Disclaimer accepted");
      CHECK_FOR_UPDATES_V2().run();
      REQUIRE_REGION().run();
      INSTALL_DRIVERS().run();
      return MOD_CHOOSE_SCREEN().run();
    };
  }

  public static RunnableMessage OVERRIDE_UNLOCK() {
    return () -> {
      try {
        UpdateUtils.overrideUnlockOptions(ToolManager.XMT_HOST);
      } catch (Exception e) {
        Log.error("Failed to override unlock options: " + e.getMessage());
        Log.exc(e);
        return 1;
      }
      return 0;
    };
  }

  public static RunnableMessage MOD_CHOOSE_SCREEN() {
    return () -> {
      RunnableMessage nextMain = MAIN_MOD_DEVICE();
      int message = NOOP;
      while (message == NOOP) {
        message = MOD_RECOVER_CHOICE().run();
        /*if (message == 1) {
            message = FEATURE_NOT_AVAILABLE().run();
        }*/
        nextMain = message == 0 ? MAIN_MOD_DEVICE() : MAIN_RECOVER_DEVICE();
      }
      return nextMain.run();
    };
  }

  public static RunnableMessage REQUIRE_FIXANDROID() {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        if (ResourcesConst.isWindows()) {
          try {
            DriverUtils.requireFixAndroidService(ResourcesManager.getAndroidDriverPath());
          } catch (IOException e) {
            Log.error("Failed to register auto driver fix: " + e.getMessage());
            Log.exc(e);
          }
        }
        return 0;
      }
    };
  }

  public static RunnableMessage STOP_FIXANDROID() {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        if (ResourcesConst.isWindows()) {
          DriverUtils.stopFixAndroidService();
        }
        return 0;
      }
    };
  }

  public static RunnableMessage INSTALL_DRIVERS() {
    return () -> {
      if (!ResourcesConst.isWindows()) {
        return 0;
      }
      AdbCommunication.killServer();
      List<Path> driverPath;
      try {
        driverPath = ResourcesManager.getAllInfPaths();
      } catch (IOException e) {
        Log.error("Failed to find inf files: " + e.getMessage());
        return 1;
      }
      int totalFiles = driverPath.size();
      if (totalFiles == 0) {
        return 1;
      }
      LoadingAnimation.WithText loadingAnimation = new LoadingAnimation.WithText("", 150);
      WindowManager.setMainContent(loadingAnimation, false);
      int i = 1;
      for (Path inf : driverPath) {
        String fn = FilenameUtils.getName(inf.toString());
        loadingAnimation.setText(
            LRes.DRIVER_INSTALLING.toString(fn) + " (" + i + "/" + totalFiles + ")");
        Log.info("Installing driver: " + inf);
        boolean result = DriverUtils.installDriver(inf);
        if (result) {
          Log.info("Install driver success");
        } else {
          Log.warn("Failed to install driver");
        }
        ++i;
      }
      Thread.sleep(1500);
      Log.info("Refreshing connected devices");
      DriverUtils.refresh();
      WindowManager.removeTopContent();
      return 0;
    };
  }

  public static RunnableMessage MAIN_MOD_DEVICE() {
    return () -> {
      ActionsDynamic.MAIN_SCREEN_LOADING(LRes.SEARCHING_CONNECTED_DEVICES).run();
      AdbCommunication.restartServer();
      AdbCommunication.registerAutoScanDevices();
      ActionsDynamic.SEARCH_SELECT_DEVICES().run();
      ActionsDynamic.REQUIRE_DEVICE_ON(DeviceManager.getSelectedDevice()).run();
      ActionsDynamic.FIND_DEVICE_INFO(DeviceManager.getSelectedDevice()).run();
      return 0;
    };
  }

  public static RunnableMessage FEATURE_NOT_AVAILABLE() {
    return () -> {
      Platform.runLater(
          new Runnable() {
            @Override
            public void run() {
              WindowManager.launchPopup(
                  new PopupWindow.ImageTextPopup(
                      LRes.FEATURE_NOT_AVAILABLE.toString(), PopupWindow.Icon.INFO));
            }
          });
      return NOOP;
    };
  }

  public static RunnableMessage RESTART_ADB_SERVER() {
    return () -> {
      Log.info("Restarting adb server");
      ActionsDynamic.MAIN_SCREEN_LOADING(LRes.LOADING).run();
      AdbCommunication.restartServer();
      DeviceManager.refresh(true);
      WindowManager.removeTopContent();
      return 0;
    };
  }

  public static RunnableMessage MAIN_RECOVER_DEVICE() {
    return () -> {
      int accept = RECOVERY_MODE_EXP().run();
      if (accept == 0) {
        return MOD_CHOOSE_SCREEN().run();
      }
      return ActionsDynamic.START_PROCEDURE(
              null, GenericInstall.recoverMain(), null, GenericInstall.goBackToHome())
          .run();
    };
  }

  private static RunnableMessage RECOVERY_MODE_EXP() {
    return () -> {
      ButtonPane buttonPane = new ButtonPane(LRes.CANCEL, LRes.OK_UNDERSTAND);
      buttonPane.setContentText(LRes.RECOVERY_MODE_EXP);
      WindowManager.setMainContent(buttonPane, false);
      int click = buttonPane.waitClick();
      WindowManager.removeTopContent(false);
      return click;
    };
  }

  public static RunnableMessage REQUIRE_INTERNET_CONNECTION() {
    return () -> {
      ActionsDynamic.MAIN_SCREEN_LOADING(LRes.LOADING).run();
      Log.info("Checking internet connection");
      boolean connected = InetUtils.isInternetAvailable();
      if (connected) {
        return NOOP;
      }
      ErrorPane errorPane = new ErrorPane(LRes.TRY_AGAIN);
      errorPane.setTitle(LRes.INET_CONNECTION_ERROR_TITLE);
      errorPane.setText(LRes.INET_CONNECTION_ERROR_TEXT);
      while (!connected) {
        WindowManager.setMainContent(errorPane, false);
        errorPane.waitClick();
        WindowManager.removeTopContent();
        Thread.sleep(1000);
        connected = InetUtils.isInternetAvailable();
      }
      return NOOP;
    };
  }

  public static RunnableMessage CHECK_FOR_UPDATES_V2() {
    return () -> {
      Log.info("Action check for updates v2");
      ActionsStatic.REQUIRE_INTERNET_CONNECTION().run();
      UpdateUtils.UpdateStatus updateStatus;
      try {
        updateStatus =
            UpdateUtils.checkForUpdatesV2(
                ToolManager.URL_UPDATE_V2,
                ToolManager.TOOL_VERSION,
                SettingsUtils.requireHashedPCId());
      } catch (Exception e) {
        Log.error("Failed to check for updates: " + e.getMessage());
        return 1;
      }
      Log.info("Update status: " + updateStatus);
      if (UpdateUtils.UpdateStatus.UPDATED.equals(updateStatus)) {
        Log.info("Tool is updated");
        return 0;
      }
      boolean isFullUpdate = UpdateUtils.UpdateStatus.FULL_UPDATE.equals(updateStatus);
      if (UpdateUtils.UpdateStatus.QUICK_UPDATE.equals(updateStatus)) {
        Log.info(
            "Quick update available: "
                + updateStatus.getLatestVersion()
                + " - "
                + updateStatus.getQuickUrl());
        if (!ResourcesManager.isQuickUpdatedSupported()) {
          isFullUpdate = true;
        } else {
          return DOWNLOAD_INSTALL_QUICK_UPDATE(
                  updateStatus.getQuickUrl(),
                  updateStatus.getLatestVersion(),
                  updateStatus.getQuickSize())
              .run();
        }
      }
      if (isFullUpdate) {
        return SHOW_UPDATE_AVAILABLE(
                LRes.UPDATE_AVAILABLE_TEXT.toString(
                    LRes.UPDATE_FULL_UP_AVAIL, updateStatus.getLatestVersion()),
                true)
            .run();
      }
      if (UpdateUtils.UpdateStatus.BLOCK.equals(updateStatus)) {
        ToolManager.exit(1);
        return -1;
      }
      Log.warn("Unknown update status: " + updateStatus);
      return 2;
    };
  }

  private static RunnableMessage SHOW_UPDATE_AVAILABLE(String message, boolean fullUpdate) {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        ButtonPane buttonPane = new ButtonPane(LRes.IGNORE, LRes.UPDATE_WILL_UPDATE);
        buttonPane.setContentText(message);
        WindowManager.setMainContent(buttonPane, false);
        int msg = buttonPane.waitClick();
        WindowManager.removeTopContent();
        if (msg != 1) {
          Log.warn("Update ignored");
        } else if (fullUpdate) {
          Log.info("Opening url for full update");
          InetUtils.openUrlInBrowser(ToolManager.URL_LATEST + "?p=" + ResourcesConst.getOSName());
          ToolManager.exit(0);
          return -1;
        }
        return msg;
      }
    };
  }

  private static RunnableMessage DOWNLOAD_INSTALL_QUICK_UPDATE(
      String downloadUrl, String version, String size) {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        Log.info("Showing quick update prompt");
        final String failedMessage = LRes.UPDATE_QUICK_UP_FAILED.toString();
        try {
          if (!ResourcesManager.isQuickUpdatedSupported()) {
            throw new Exception("Quick update not supported");
          }
          int click =
              SHOW_UPDATE_AVAILABLE(
                      LRes.UPDATE_AVAILABLE_TEXT.toString(
                          LRes.UPDATE_QUICK_UP_AVAIL.toString(size), version),
                      false)
                  .run();
          if (click != 1) {
            return 0;
          }
          ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
          defProgressPane.setContentText(
              LRes.DOWNLOADING_UPDATE.toString("XiaoMiTool V2", '(' + version + ')'));
          UpdateListener listener = defProgressPane.getUpdateListener(500);
          String fn = XMT_UPDATE_FILENAME;
          if (downloadUrl.endsWith(".gz")) {
            fn += ".gz";
          }
          DownloadTask downloadTask =
              new DownloadTask(listener, downloadUrl, SettingsUtils.getDownloadFile(fn));
          WindowManager.setMainContent(defProgressPane, false);
          TaskManager.getInstance().startSameThread(downloadTask);
          WindowManager.removeTopContent();
          if (downloadTask.getError() != null) {
            throw downloadTask.getError();
          }
          File downloadedFile = (File) downloadTask.getResult();
          if (downloadedFile == null) {
            throw new Exception("Null file object");
          }
          boolean isCompressed = downloadedFile.toString().endsWith(".gz");
          ActionsDynamic.MAIN_SCREEN_LOADING(LRes.INSTALLING_UPDATED).run();
          Path extractedFile;
          if (isCompressed) {
            GZIPInputStream inputStream =
                new GZIPInputStream(new BufferedInputStream(new FileInputStream(downloadedFile)));
            Path dlPath = downloadedFile.toPath();
            extractedFile = dlPath.getParent();
            if (extractedFile == null) {
              throw new Exception("Failed to get downlaod file dir");
            }
            String ffn = FilenameUtils.getName(dlPath.toString().replace(".gz", ""));
            extractedFile = extractedFile.resolve(ffn);
            OutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream(extractedFile.toFile()));
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            if (downloadedFile.exists()) {
              downloadedFile.delete();
            }
          } else {
            extractedFile = downloadedFile.toPath();
          }
          Path installDir = ResourcesManager.getCurrentJarDirPath();
          Path dstFile = installDir.resolve(XMT_UPDATE_FILENAME);
          if (Files.exists(dstFile)) {
            Files.delete(dstFile);
          }
          Files.move(extractedFile, dstFile, StandardCopyOption.REPLACE_EXISTING);
          Thread.sleep(500);
          if (!UpdateUtils.checkIfAlive(ResourcesManager.getJavaLaunchExe(), dstFile)) {
            throw new Exception("Failed to start new jar file: not alive");
          }
          if (!UpdateUtils.startUpdateProcess(ResourcesManager.getJavaLaunchExe(), dstFile, true)) {
            throw new Exception("Failed to start new jar file: update process fail");
          }
          ToolManager.exit(0);
          return 0;
        } catch (Exception e) {
          Log.error("Failed to download install quick update: " + e.getMessage());
          Log.printStackTrace(e);
          return QUICK_UPDATE_FAILED(failedMessage).run();
        }
      }
    };
  }

  private static RunnableMessage QUICK_UPDATE_FAILED(String message) {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        return SHOW_UPDATE_AVAILABLE(message, true).run();
      }
    };
  }

  public static RunnableMessage REQUIRE_REGION() {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        SettingsUtils.Region region = SettingsUtils.getRegion();
        if (region != null) {
          return 0;
        }
        SettingsUtils.Region[] regions = SettingsUtils.Region.values();
        ChooserPane.Choice[] choices = new ChooserPane.Choice[regions.length];
        for (int i = 0; i < regions.length; ++i) {
          region = regions[i];
          String hum = region.toHuman(), t = LRes.SELECT_IF_YOURE_FROM.toString(hum);
          if (SettingsUtils.Region.GLOBAL.equals(region)) {
            t = t.toLowerCase();
          }
          Image img = DrawableManager.getResourceImage(region.getDrawable());
          choices[i] = new ChooserPane.Choice(hum, t, img);
        }
        Text title = new Text(LRes.PLEASE_SELECT_REGION.toString());
        title.setFont(Font.font(20));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrappingWidth(WindowManager.getContentWidth() - 100);
        Text text = new Text(LRes.PLEASE_SELECT_REGION_TEXT.toString());
        text.setFont(Font.font(16));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(WindowManager.getContentWidth() - 100);
        ChooserPane chooserPane = new ChooserPane(choices);
        VBox vBox = new VBox(20, title, text, chooserPane);
        vBox.setAlignment(Pos.CENTER);
        WindowManager.setMainContent(vBox, false);
        int choice = chooserPane.getIdClickReceiver().waitClick();
        WindowManager.removeTopContent();
        region = regions[choice];
        Log.info("Selected region: " + region.toString());
        SettingsUtils.setRegion(region);
        return 0;
      }
    };
  }

  public static RunnableMessage FIRST_DISCLAIMER() {
    return () -> {
      ButtonPane buttonPane = new ButtonPane(LRes.DONT_AGREE.toString(), LRes.AGREE.toString());
      Text t2 = new Text(RawManager.getDisclaimer());
      t2.setFont(Font.font(15));
      Pane p = new TextScrollPane(t2, LRes.DISCLAIMER.toString());
      p.setPadding(new Insets(20, 100, 20, 100));
      buttonPane.setContent(p);
      WindowManager.setMainContent(buttonPane, true);
      return buttonPane.waitClick();
    };
  }

  public static RunnableMessage MOD_RECOVER_CHOICE() {
    return () -> {
      IDClickReceiver idClickReceiver = new IDClickReceiver();
      Platform.runLater(
          new Runnable() {
            @Override
            public void run() {
              double height = WindowManager.getMainPane().getHeight() * 2 / 3;
              DeviceView
                  running = new DeviceView(DeviceView.DEVICE_18_9, height, Color.WHITE, null),
                  recover = new DeviceView(DeviceView.DEVICE_18_9, height, Color.BLACK, null);
              running.getImagePane().setCursor(Cursor.HAND);
              recover.getImagePane().setCursor(Cursor.HAND);
              recover.setContent(DrawableManager.getPng(DrawableManager.FASTBOOT_LOGO), true);
              running.setContent(DrawableManager.getPng(DrawableManager.MIUI10));
              Button b1 = new CustomButton(LRes.CHOOSE);
              Button b2 = new CustomButton(LRes.CHOOSE);
              b1.setMinWidth(100);
              b1.setFont(Font.font(14));
              b2.setMinWidth(100);
              b2.setFont(Font.font(14));
              Text t2 = new Text(LRes.CHOOSE_RECOVER_DEVICE.toString());
              Text t1 = new Text(LRes.CHOOSE_MOD_DEVICE.toString());
              t1.setFont(Font.font(14));
              t2.setFont(Font.font(14));
              t1.setTextAlignment(TextAlignment.CENTER);
              t2.setTextAlignment(TextAlignment.CENTER);
              VBox vBox1 = new VBox(GuiUtils.center(running), t1, b1);
              VBox vBox2 = new VBox(GuiUtils.center(recover), t2, b2);
              idClickReceiver.addNodes(b1, b2, running.getImagePane(), recover.getImagePane());
              vBox1.setAlignment(Pos.CENTER);
              vBox2.setAlignment(Pos.CENTER);
              HBox hBox = new HBox(vBox1, vBox2);
              hBox.setAlignment(Pos.CENTER);
              hBox.setSpacing(100);
              vBox1.setSpacing(20);
              vBox2.setSpacing(20);
              WindowManager.setMainContent(hBox, true);
            }
          });
      return idClickReceiver.waitClick() % 2;
    };
  }

  public static RunnableMessage ASK_FOR_FEEDBACK() {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        ButtonPane buttonPane = new ButtonPane(LRes.YES, LRes.NO);
        buttonPane.setContentText(LRes.FEEDBACK_ASK_TO_LEAVE);
        WindowManager.setMainContent(buttonPane, false);
        int click = buttonPane.waitClick();
        if (click != 0) {
          WindowManager.removeTopContent();
          return 0;
        }
        PopupWindow window = SettingsController.getFeedbackPopupWindow();
        WindowManager.launchPopup(window);
        window.waitForClose();
        WindowManager.removeTopContent();
        return 1;
      }
    };
  }

  public static RunnableMessage CLOSING() {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        return ActionsDynamic.MAIN_SCREEN_LOADING(LRes.CLOSING).run();
      }
    };
  }

  public static RunnableMessage LOAD_ONLINE_LANGS(String host) {
    return new RunnableMessage() {
      @Override
      public int run() throws InterruptedException {
        try {
          Lang.initOnlineLangs(host);
        } catch (Exception e) {
          Log.error("Failed to load online languages");
          Log.exc(e);
        }
        return 0;
      }
    };
  }
}
