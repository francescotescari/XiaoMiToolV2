package com.xiaomitool.v2.engine.actions;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceAnswers;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.engine.CommonsMessages;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.PopupWindow;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.deviceView.Animatable;
import com.xiaomitool.v2.gui.deviceView.DeviceFastbootView;
import com.xiaomitool.v2.gui.deviceView.DeviceRecoveryView;
import com.xiaomitool.v2.gui.deviceView.DeviceView;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.other.DeviceCodenameEntry;
import com.xiaomitool.v2.gui.other.DeviceTableEntry;
import com.xiaomitool.v2.gui.visual.*;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.logging.feedback.LiveFeedbackEasy;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.*;
import com.xiaomitool.v2.utility.utils.ArrayUtils;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.xiaomitool.v2.engine.CommonsMessages.NOOP;
import static com.xiaomitool.v2.procedure.install.GenericInstall.main;

public class ActionsDynamic {
    public static RunnableMessage MAIN_SCREEN_LOADING() {
        return MAIN_SCREEN_LOADING(LRes.LOADING);
    }

    public static RunnableMessage MAIN_SCREEN_LOADING(LRes message) {
        return MAIN_SCREEN_LOADING(message.toString());
    }

    public static RunnableMessage MAIN_SCREEN_LOADING(String message) {
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                Node node = new LoadingAnimation.WithText(message, 150);
                WindowManager.setMainContent(node, false);
                return 0;
            }
        };
    }

    public static RunnableMessage SEARCH_SELECT_DEVICES(@Nullable Device.Status... wantedStatus) {
        return () -> {
            Log.info("Searching at least one device");
            ActionsDynamic.MAIN_SCREEN_LOADING(LRes.SEARCHING_CONNECTED_DEVICES).run();
            DeviceManager.refresh(true);
            Thread.sleep(1000);
            int connectedDevices = DeviceManager.count(wantedStatus);
            Log.info("Total connected device found: " + connectedDevices);
            RunnableMessage nextStep;
            if (connectedDevices == 0) {
                if (ResourcesConst.isWindows()) {
                    new Thread(() -> {
                        try {
                            DriverUtils.fixAndroidDevices(ResourcesManager.getAndroidDriverPath());
                        } catch (IOException e) {
                            Log.error("Cannot try to fix drivers: " + e.getMessage());
                        }
                    }).start();
                }
                nextStep = NO_DEVICE_CONNECTED(wantedStatus);
            } else {
                nextStep = SELECT_DEVICE(wantedStatus);
            }
            return nextStep.run();
        };
    }

    public static RunnableMessage NO_DEVICE_CONNECTED(Device.Status... wantedStatus) {
        return () -> {
            Log.info("Showing no devices visual");
            LRes button;
            String msg;
            RunnableMessage howto = null;
            Device.Status wStatus = (wantedStatus == null || wantedStatus.length == 0) ? null : wantedStatus[0];
            if (wStatus == null) {
                wStatus = Device.Status.DEVICE;
            }
            switch (wStatus) {
                case SIDELOAD:
                case RECOVERY:
                    msg = LRes.NO_DEVICE_CONNECTED_MODE.toString(Device.Status.RECOVERY.toString(), Device.Status.RECOVERY.toString());
                    button = LRes.HT_GO_RECOVERY;
                    howto = HOWTO_GO_RECOVERY(null);
                    break;
                case FASTBOOT:
                    msg = LRes.NO_DEVICE_CONNECTED_MODE.toString(Device.Status.FASTBOOT.toString(), Device.Status.FASTBOOT.toString());
                    button = LRes.HT_GO_FASTBOOT;
                    howto = HOWTO_GO_FASTBOOT(null, false);
                    break;
                default:
                    msg = LRes.NO_DEVICE_CONNECTED.toString();
                    button = LRes.HT_ENABLE_USB_DEBUG;
                    howto = ActionsDynamic.HOW_TO_ENABLE_USB_DEBUGGING(null, DeviceManager.getDevices().isEmpty());
            }
            ButtonPane pane = new ButtonPane(LRes.SEARCH_AGAIN, button);
            ImageView no_connection = new ImageView(new Image(DrawableManager.getPng(DrawableManager.NO_CONNECTION).toString()));
            no_connection.setFitHeight(200);
            no_connection.setPreserveRatio(true);
            Text no = new Text(msg);
            no.setTextAlignment(TextAlignment.CENTER);
            no.setFont(Font.font(16));
            no.setWrappingWidth(WindowManager.getContentWidth() - 80);
            VBox vBox = new VBox(no_connection, no);
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(20);
            pane.setContent(vBox);
            WindowManager.setMainContent(pane, false);
            DeviceManager.addMessageReceiver(pane.getIdClickReceiver());
            int message = CommonsMessages.NOOP;
            while ((message != CommonsMessages.NEW_DEVICE || DeviceManager.count(wantedStatus) == 0) && message < 0) {
                message = pane.waitClick();
                if (message > 0 && howto != null) {
                    message = howto.run();
                    if (message == 0) {
                        message = NOOP;
                    }
                }
            }
            WindowManager.removeTopContent();
            DeviceManager.removeMessageReceiver(pane.getIdClickReceiver());
            return ActionsDynamic.SEARCH_SELECT_DEVICES(wantedStatus).run();
        };
    }

    public static RunnableMessage SELECT_DEVICE(@Nullable Device.Status... wantedStatus) {
        return () -> {
            Log.info("Displaying found devices to choose");
            TableView<DeviceTableEntry> tableView = new TableView<DeviceTableEntry>() {
                public void requestFocus() {
                }
            };
            TableColumn<DeviceTableEntry, String> serial = new TableColumn<>(LRes.SERIAL.toString());
            TableColumn<DeviceTableEntry, String> status = new TableColumn<>(LRes.STATUS.toString());
            TableColumn<DeviceTableEntry, String> codename = new TableColumn<>(LRes.CODENAME.toString());
            TableColumn<DeviceTableEntry, String> brand = new TableColumn<>(LRes.BRAND.toString());
            TableColumn<DeviceTableEntry, String> model = new TableColumn<>(LRes.MODEL.toString());
            serial.setCellValueFactory(new PropertyValueFactory<>("serial"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));
            codename.setCellValueFactory(new PropertyValueFactory<>("codename"));
            brand.setCellValueFactory(new PropertyValueFactory<>("brand"));
            model.setCellValueFactory(new PropertyValueFactory<>("model"));
            tableView.getColumns().addAll(serial, status, codename, brand, model);
            ObservableList<DeviceTableEntry> observableList = tableView.getItems();
            for (Device device : DeviceManager.getDevices()) {
                if (wantedStatus != null && wantedStatus.length > 0 && !ArrayUtils.in(wantedStatus, device.getStatus())) {
                    continue;
                }
                DeviceTableEntry tableEntry = new DeviceTableEntry(device);
                observableList.add(tableEntry);
            }
            tableView.getSelectionModel().select(0);
            ButtonPane buttonPane = new ButtonPane(LRes.SELECT, LRes.SEARCH_AGAIN);
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            for (TableColumn column : tableView.getColumns()) {
                column.setPrefWidth(120);
                column.setSortable(false);
            }
            tableView.setPrefSize(600, 400);
            tableView.setEditable(false);
            String hex = "ffa970";
            tableView.setStyle("-fx-focus-color: #C8C8C8;\n" +
                    "-fx-faint-focus-color: #C8C8C8;\n" +
                    "-fx-selection-bar: #" + hex + "; -fx-selection-bar-non-focused: #" + hex + ";"
            );
            tableView.setBorder(Border.EMPTY);
            buttonPane.setContent(GuiUtils.center(tableView));
            WindowManager.setMainContent(buttonPane, true);
            int message = CommonsMessages.NOOP;
            DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
            boolean newDevice = false;
            while (message == CommonsMessages.NOOP) {
                message = buttonPane.waitClick();
                if (message == CommonsMessages.NEW_DEVICE) {
                    newDevice = true;
                } else if (newDevice && message == CommonsMessages.DEVICE_UPDATE_FINISH) {
                    observableList.clear();
                    for (Device device : DeviceManager.getDevices()) {
                        if (wantedStatus != null && !ArrayUtils.in(wantedStatus, device.getStatus())) {
                            continue;
                        }
                        DeviceTableEntry tableEntry = new DeviceTableEntry(device);
                        observableList.add(tableEntry);
                    }
                    tableView.getSelectionModel().select(0);
                    newDevice = false;
                }
                if (message < 0) {
                    message = CommonsMessages.NOOP;
                }
                if (message == 0) {
                    DeviceTableEntry selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected == null) {
                        message = CommonsMessages.NOOP;
                    } else {
                        DeviceManager.setSelectedDevice(selected.getSerial());
                        if (DeviceManager.getSelectedDevice() == null) {
                            message = CommonsMessages.NOOP;
                        }
                    }
                }
            }
            return message == 0 ? 0 : SEARCH_SELECT_DEVICES(wantedStatus).run();
        };
    }

    public static RunnableMessage REQUIRE_DEVICE_ON(Device device) {
        return () -> {
            Log.info("Rebooting and expecting device " + device.getSerial() + " turned on with usb debug enabled, auth and connected");
            if (!device.isTurnedOn()) {
                REBOOT_DEVICE(device, Device.Status.DEVICE, false).run();
                WAIT_USB_DEBUG_ENABLE(device).run();
            }
            REQUIRE_DEVICE_AUTH(device).run();
            REQUIRE_DEVICE_CONNECTED(device).run();
            return 0;
        };
    }

    public static RunnableMessage REQUIRE_DEVICE_CONNECTED(Device device) {
        return () -> {
            Log.info("Expecting device connected to the pc");
            if (device.isConnected()) {
                return 0;
            }
            Log.warn("It is not, please connect device to the pc");
            ButtonPane pane = new ButtonPane(LRes.TRY_AGAIN);
            ImageView no_connection = new ImageView(new Image(DrawableManager.getPng(DrawableManager.NO_CONNECTION).toString()));
            no_connection.setFitHeight(200);
            no_connection.setPreserveRatio(true);
            Text no = new Text(LRes.DEVICE_NOT_CONNECTED.toString());
            no.setTextAlignment(TextAlignment.CENTER);
            no.setFont(Font.font(16));
            no.setWrappingWidth(600);
            VBox vBox = new VBox(no_connection, no);
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(20);
            pane.setContent(vBox);
            WindowManager.setMainContent(pane, false);
            IDClickReceiver receiver = pane.getIdClickReceiver();
            DeviceManager.addMessageReceiver(receiver);
            int message = NOOP;
            while (message == NOOP) {
                message = pane.waitClick();
                if (message == CommonsMessages.DEVICE_UPDATE_FINISH) {
                    if (device.isConnected()) {
                        break;
                    }
                }
                if (message == 0) {
                    MAIN_SCREEN_LOADING(LRes.SEARCHING_CONNECTED_DEVICES).run();
                    ActionsStatic.RESTART_ADB_SERVER().run();
                    WindowManager.removeTopContent();
                    if (device.isConnected()) {
                        break;
                    }
                }
                message = NOOP;
            }
            DeviceManager.removeMessageReceiver(receiver);
            WindowManager.removeTopContent();
            return 1;
        };
    }

    public static RunnableMessage WAIT_USB_DEBUG_ENABLE(Device device) {
        return WAIT_USB_DEBUG_ENABLE(device, null);
    }

    public static RunnableMessage WAIT_USB_DEBUG_ENABLE(Device device, String text) {
        return () -> {
            Log.info("Waiting for device usb connection");
            ButtonPane pane = new ButtonPane(LRes.HT_ENABLE_USB_DEBUG, LRes.SEARCH_AGAIN);
            pane.setContentText(LRes.WAITING_USB_ENABLE.toString(LRes.SEARCH_AGAIN));
            WindowManager.setMainContent(pane, false);
            IDClickReceiver receiver = pane.getIdClickReceiver();
            DeviceManager.addMessageReceiver(receiver);
            int message = NOOP;
            while (message == NOOP) {
                message = receiver.waitClick();
                if (message == CommonsMessages.DEVICE_UPDATE_FINISH) {
                    if (device.isTurnedOn()) {
                        break;
                    }
                }
                if (message == 1) {
                    ActionsStatic.RESTART_ADB_SERVER().run();
                    WindowManager.removeTopContent();
                    if (device.isTurnedOn() || Device.Status.UNAUTHORIZED.equals(device.getStatus())) {
                        break;
                    }
                } else if (message == 0) {
                    if (HOW_TO_ENABLE_USB_DEBUGGING(device, true).run() > 0) {
                        if (device.isTurnedOn() || Device.Status.UNAUTHORIZED.equals(device.getStatus())) {
                            break;
                        }
                    }
                }
                message = NOOP;
            }
            WindowManager.removeTopContent();
            DeviceManager.removeMessageReceiver(receiver);
            return 0;
        };
    }

    public static RunnableMessage REBOOT_STOCK_RECOVERY(Device device, boolean force) {
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                if (device == null) {
                    return 0;
                }
                Log.info("Starting reboot to stock recovery visual procedure");
                if (!force) {
                    if (Device.Status.SIDELOAD.equals(device.getStatus())) {
                        return 1;
                    }
                }
                REQUIRE_DEVICE_CONNECTED(device).run();
                REQUIRE_DEVICE_AUTH(device).run();
                if (Device.Status.FASTBOOT.equals(device.getStatus())) {
                    try {
                        if (!device.reboot(Device.Status.DEVICE)) {
                            throw new AdbException("Failed to reboot device to device mode");
                        }
                    } catch (AdbException e) {
                        REQUIRE_DEVICE_CONNECTED(device).run();
                        REQUIRE_DEVICE_AUTH(device).run();
                        if (!device.isTurnedOn()) {
                            return 0;
                        }
                    }
                }
                Thread.sleep(1000);
                try {
                    if (!device.rebootNoWait(Device.Status.SIDELOAD, force)) {
                        throw new AdbException("Failed to reboot device to recovery mode");
                    }
                    HOWTO_GO_RECOVERY(device).run();
                    if (!device.waitStatus(Device.Status.SIDELOAD, 5)) {
                        throw new AdbException("Failed to reboot device to recovery mode");
                    }
                } catch (AdbException e) {
                    return 0;
                }
                return 1;
            }
        };
    }

    public static RunnableMessage FIND_DEVICE_INFO(Device device) {
        return () -> {
            ActionsStatic.REQUIRE_FIXANDROID().run();
            device.getAnswers().setAnswer(DeviceAnswers.HAS_TWRP, YesNoMaybe.MAYBE);
            device.getDeviceProperties().getRecoveryProperties().reset();
            Log.info("Starting find_device_info visual procedure");
            Text[] texts = new Text[10];
            Text[] ktexts = new Text[10];
            LRes[] kstring = new LRes[]{LRes.SERIAL, LRes.BRAND, LRes.MODEL, LRes.CODENAME, LRes.MIUI_VERSION, LRes.ANDROID_VERSION, LRes.SERIAL_NUMBER, LRes.BOOTLOADER_STATUS, LRes.FASTBOOT_PARSED, LRes.RECOVERY_PARSED};
            for (int i = 0; i < kstring.length; ++i) {
                texts[i] = new Text();
                ktexts[i] = new Text();
                texts[i].setFont(Font.font(14));
                ktexts[i].setFont(Font.font(14));
                ktexts[i].setText(kstring[i].toString() + " : ");
            }
            ActionsUtil.setDevicePropertiesText(device, texts);
            double width = WindowManager.getMainPane().getWidth() - 100;
            RegularTable regularTable = new RegularTable(10, 2, 280, width);
            for (int i = 0; i < kstring.length; ++i) {
                regularTable.add(ktexts[i], 0, i, Pos.CENTER_RIGHT);
                regularTable.add(texts[i], 1, i, Pos.CENTER_LEFT);
            }
            Text title = new Text(LRes.FINDING_INFO_TEXT.toString());
            title.setFont(Font.font(16));
            title.setWrappingWidth(width);
            title.setTextAlignment(TextAlignment.CENTER);
            LoadingAnimation loadingAnimation = new LoadingAnimation(100);
            StackPane footer = new StackPane(loadingAnimation);
            footer.setPrefHeight(50);
            VBox vBox = new VBox(title, regularTable, footer);
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(10);
            WindowManager.setMainContent(GuiUtils.center(vBox), true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.info("Starting find_device_info reboot thread");
                    if (!device.getDeviceProperties().getFastbootProperties().isParsed()) {
                        Log.info("Device fastboot properties are not parsed yet");
                        try {
                            if (!device.reboot(Device.Status.FASTBOOT)) {
                                throw new Exception("Failed to reboot to fastboot");
                            } else {
                                Log.info("Device rebooted to fastboot, parsing starting soon");
                                Thread.sleep(1000);
                                ActionsUtil.setDevicePropertiesText(device, texts);
                            }
                        } catch (Exception e) {
                            Log.warn("Failed to reboot device to fastboot, skipping: " + e.getMessage());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    texts[8].setText(e.getMessage());
                                }
                            });
                        }
                    } else {
                        Log.info("Device fastboot properties already parsed, skipping");
                        ActionsUtil.setDevicePropertiesText(device, texts);
                    }
                    if (!device.getDeviceProperties().getSideloadProperties().isParsed() && !device.getDeviceProperties().getRecoveryProperties().isParsed()) {
                        try {
                            Log.info("Device sideload and recovery properties are not parsed yet");
                            if (UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus())) {
                                Log.info("Bootloader is unlocked, we dont care about recovery properties now");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        texts[9].setText(LRes.IRRELEVANT.toString());
                                    }
                                });
                            } else {
                                Log.info("Bootloader il locked, we go to stock recovery now");
                                boolean result = REBOOT_STOCK_RECOVERY(device, true).run() != 0;
                                if (!result) {
                                    throw new Exception("Failed to reboot to stock recovery");
                                }
                                Log.info("Device rebooted to stock recovery mode, parsing starting soon");
                                Thread.sleep(1000);
                                ActionsUtil.setDevicePropertiesText(device, texts);
                            }
                        } catch (Exception e) {
                            Log.warn("Failed to reboot to recovery mode, skipping: " + e.getMessage());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    texts[9].setText(e.getMessage());
                                }
                            });
                        }
                    } else {
                        Log.info("Either stock recovery or custom recovery properties are already found, we dont need more fore now, skipping");
                        ActionsUtil.setDevicePropertiesText(device, texts);
                    }
                    try {
                        ActionsStatic.STOP_FIXANDROID().run();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(1200);
                        if (UnlockStatus.LOCKED.equals(device.getAnswers().getUnlockStatus())) {
                            device.getAnswers().setAnswer(DeviceAnswers.HAS_STOCK_MIUI, YesNoMaybe.YES);
                        }
                        ActionsDynamic.START_PROCEDURE(device, GenericInstall.main(), null, GenericInstall.selectRomAndGo()).run();
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
            return 0;
        };
    }

    public static RunnableMessage REQUIRE_DEVICE_AUTH(Device device) {
        return () -> {
            Log.info("Expecting device authorized");
            if (!device.needAuthorization()) {
                return 0;
            }
            Log.warn("It is not, please authorize it");
            ButtonPane pane = new ButtonPane(LRes.TRY_AGAIN);
            IDClickReceiver receiver = pane.getIdClickReceiver();
            DeviceManager.addMessageReceiver(receiver);
            DeviceView deviceView = new DeviceView(DeviceView.DEVICE_18_9, 880);
            ImageView image = new ImageView(DrawableManager.getPng(DrawableManager.DEVICE_AUTH).toString());
            deviceView.setContent(image);
            deviceView.buildCircleTransition(800, 2060, Animation.INDEFINITE);
            Text text = new Text(LRes.AUTH_DEVICE_TEXT.toString());
            text.setTextAlignment(TextAlignment.CENTER);
            text.setFont(Font.font(16));
            text.setWrappingWidth(600);
            VBox vBox = new VBox(GuiUtils.center(DeviceView.crop(deviceView, 310, 570)), text);
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(20);
            pane.setContent(vBox);
            WindowManager.setMainContent(pane, false);
            int messsage = NOOP;
            while (messsage == NOOP) {
                messsage = receiver.waitClick();
                if (messsage == CommonsMessages.DEVICE_UPDATE_FINISH) {
                    if (!device.needAuthorization() && device.isConnected()) {
                        break;
                    }
                }
                if (messsage < 0) {
                    messsage = NOOP;
                }
                if (messsage == 0) {
                    DeviceManager.refresh(true);
                    if (device.needAuthorization()) {
                        ActionsStatic.RESTART_ADB_SERVER().run();
                        if (!device.needAuthorization() && device.isConnected()) {
                            break;
                        } else {
                            WindowManager.setMainContent(pane, false);
                        }
                    } else {
                        break;
                    }
                    messsage = NOOP;
                }
            }
            DeviceManager.removeMessageReceiver(receiver);
            WindowManager.removeTopContent();
            return 1;
        };
    }

    public static RunnableMessage REBOOT_DEVICE(Device device, Device.Status status) {
        return REBOOT_DEVICE(device, status, true);
    }

    public static RunnableMessage REBOOT_DEVICE(Device device, Device.Status status, boolean wait) {
        return () -> {
            Log.info("Rebooting device visual");
            boolean res = false;
            try {
                res = wait ? device.reboot(status) : device.rebootNoWait(status);
            } catch (AdbException e) {
                Log.error("Failed to reboot the device: " + e.getMessage());
            }
            return res ? 1 : 0;
        };
    }

    public static RunnableMessage START_PROCEDURE(Device device, RInstall startFromHere, ProcedureRunner runner, RInstall onRestart) {
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                if (startFromHere == null && device != null) {
                    try {
                        DeviceProperties properties = device.getDeviceProperties();
                        HashMap<String, String> logginProps = new HashMap<>();
                        logginProps.put("d", String.valueOf(properties.getCodename(false)));
                        logginProps.put("c", String.valueOf(properties.get(DeviceProperties.CODEBASE)));
                        logginProps.put("v", String.valueOf(properties.get(DeviceProperties.FULL_VERSION)));
                        logginProps.put("bs", String.valueOf(device.getAnswers().getUnlockStatus()));
                        logginProps.put("sn", String.valueOf(device.getAnswers().getSerialNumber()));
                        logginProps.put("rg", String.valueOf(SettingsUtils.getRegion()));
                        LiveFeedbackEasy.sendLog("DATA", new JSONObject(logginProps).toString());
                    } catch (Exception ignored) {
                    }
                }
                Log.info("Starting installation part main procedure");
                InstallPane installPane;
                ProcedureRunner thisRunner;
                if (runner == null) {
                    installPane = new InstallPane();
                    thisRunner = new ProcedureRunner(installPane.getListener());
                } else {
                    installPane = runner.getInstallPane();
                    if (installPane == null) {
                        installPane = new InstallPane();
                        runner.setInstallPane(installPane);
                    }
                    thisRunner = runner;
                }
                thisRunner.setRestarter(onRestart);
                WindowManager.setOnEmpty(installPane);
                WindowManager.setMainContent(installPane, true);
                thisRunner.init(null, device);
                RInstall main = main();
                try {
                    try {
                        Log.info("Main procedure loaded, starting now");
                        if (startFromHere == null) {
                            throw new InstallException("Null procedure to start from", InstallException.Code.INTERNAL_ERROR);
                        }
                        startFromHere.run(thisRunner);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            if (e instanceof InstallException) {
                                throw (InstallException) e;
                            } else if (e instanceof InterruptedException) {
                                throw (InterruptedException) e;
                            }
                            Log.printStackTrace(e);
                            thisRunner.handleException(new InstallException(e.getMessage(), InstallException.Code.INTERNAL_ERROR, e), main);
                        } catch (InstallException e1) {
                            throw new RMessage(e1);
                        }
                    }
                } catch (RMessage rMessage) {
                    rMessage.printStackTrace();
                    if (CommandClass.Command.ABORT.equals(rMessage.getCmd())) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ActionsStatic.ASK_FOR_FEEDBACK().run();
                                    ActionsStatic.MOD_CHOOSE_SCREEN().run();
                                } catch (InterruptedException e) {
                                }
                            }
                        }).start();
                        return 0;
                    }
                    Log.error("Fatal uncaught rmessage: " + rMessage.getCmd());
                    PopupWindow popupWindow = new PopupWindow.ImageTextPopup("A fatal error has occured: type: " + rMessage.getCmd() + "\nThe tool will now exit", PopupWindow.Icon.ERROR);
                    WindowManager.launchPopup(popupWindow);
                    popupWindow.waitForClose();
                    ActionsStatic.ASK_FOR_FEEDBACK().run();
                    ToolManager.exit(1);
                    System.exit(1);
                }
                return 0;
            }
        };
    }

    public static RunnableMessage HOWTO_GO_RECOVERY(Device device) {
        return HOWTO_GO_RECOVERY(true, device);
    }

    public static RunnableMessage HOWTO_GO_FASTBOOT(Device device, boolean canFail) {
        return () -> {
            ButtonPane buttonPane = canFail ? new ButtonPane(LRes.OK_FINISHED, LRes.FAILED_TO_DO_THAT) : new ButtonPane(LRes.OK_FINISHED);
            DeviceFastbootView deviceFastbootView = new DeviceFastbootView(DeviceView.DEVICE_18_9, 640);
            deviceFastbootView.animate(-1, 3000);
            SidePane sidePane = new SidePane();
            Text t = new Text(LRes.ENTER_FASTBOOT_MODE.toString());
            t.setWrappingWidth(400);
            t.setFont(Font.font(15));
            sidePane.setLeft(t);
            Pane p = DeviceView.crop(deviceFastbootView, 410);
            sidePane.setRight(GuiUtils.center(p));
            buttonPane.setContent(sidePane);
            WindowManager.setMainContent(buttonPane, false);
            DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
            int msg = NOOP;
            int exitcode = 0;
            while (msg < 0) {
                if (device != null && device.isConnected() && (Device.Status.FASTBOOT.equals(device.getStatus()))) {
                    exitcode = 1;
                    break;
                }
                if (device == null) {
                    for (Device d : DeviceManager.getDevices()) {
                        if (d.isConnected() && Device.Status.FASTBOOT.equals(d.getStatus())) {
                            exitcode = 2;
                        }
                    }
                }
                msg = buttonPane.waitClick();
            }
            if (msg != 0 && exitcode == 0) {
                exitcode = -1;
            }
            DeviceManager.removeMessageReceiver(buttonPane.getIdClickReceiver());
            WindowManager.removeTopContent();
            return exitcode;
        };
    }

    public static RunnableMessage HOWTO_GO_RECOVERY(boolean rebootingText, Device device) {
        return () -> {
            ButtonPane buttonPane = new ButtonPane(LRes.OK_FINISHED);
            SidePane sidePane = new SidePane();
            DeviceRecoveryView deviceRecoveryView = new DeviceRecoveryView(DeviceView.DEVICE_18_9, 640);
            deviceRecoveryView.animateSelectThird(3000);
            String nText;
            if (!rebootingText) {
                nText = "1) " + LRes.BTN_VOLUP_POW.toString() + "\n2) " + LRes.HT_RECOVERY_TEXT_1.toString();
            } else {
                nText = LRes.ENTER_STOCK_RECOVERY_MODE.toString(LRes.OK_FINISHED.toString());
            }
            Text t = new Text(nText);
            t.setWrappingWidth(400);
            t.setFont(Font.font(15));
            sidePane.setLeft(t);
            Pane p = DeviceView.crop(deviceRecoveryView, 410);
            sidePane.setRight(GuiUtils.center(p));
            buttonPane.setContent(sidePane);
            WindowManager.setMainContent(buttonPane, false);
            DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
            int msg = NOOP;
            int exitcode = 0;
            while (msg < 0) {
                exitcode = 0;
                if (device != null && device.isConnected() && (Device.Status.SIDELOAD.equals(device.getStatus()) || Device.Status.RECOVERY.equals(device.getStatus()))) {
                    exitcode = 1;
                    break;
                }
                msg = buttonPane.waitClick();
            }
            DeviceManager.removeMessageReceiver(buttonPane.getIdClickReceiver());
            WindowManager.removeTopContent();
            return exitcode;
        };
    }

    public static RunnableMessage HOW_TO_ENABLE_USB_DEBUGGING(Device device, boolean search) {
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                String[] text = new String[]{LRes.HT_ENABLE_USB_1.toString(), LRes.HT_ENABLE_USB_2.toString(), LRes.HT_ENABLE_USB_3.toString(), LRes.HT_ENABLE_USB_4.toString(), LRes.HT_ENABLE_USB_5.toString(), LRes.HT_ENABLE_USB_6.toString(), LRes.HT_ENABLE_USB_7.toString(),};
                Image[] images = new Image[7];
                for (int i = 0; i < 7; i++) {
                    images[i] = new Image(DrawableManager.getPng("usbdbg" + (i + 1)).toString(), false);
                }
                HashMap<Integer, Animatable.AnimationPayload> animations = new HashMap<>();
                animations.put(0, new Animatable.AnimationPayload(664, 1323, Animation.INDEFINITE, true));
                animations.put(1, new Animatable.AnimationPayload(400, 435, Animation.INDEFINITE, true));
                animations.put(2, new Animatable.AnimationPayload(400, 1050, Animation.INDEFINITE, true));
                animations.put(3, new Animatable.AnimationPayload(400, 1954, Animation.INDEFINITE, true));
                animations.put(4, new Animatable.AnimationPayload(400, 1746, Animation.INDEFINITE, true));
                animations.put(5, new Animatable.AnimationPayload(950, 1082, Animation.INDEFINITE, true));
                animations.put(6, new Animatable.AnimationPayload(800, 2060, Animation.INDEFINITE, true));
                DeviceImgInstructionPane instructionPane = new DeviceImgInstructionPane(WindowManager.getContentHeight() + 30, WindowManager.getContentHeight() - 10, text, images, animations);
                IDClickReceiver receiver = instructionPane.getIdClickReceiver();
                WindowManager.setMainContent(instructionPane, false);
                if (search) {
                    DeviceManager.addMessageReceiver(receiver);
                }
                int message = CommonsMessages.NOOP;
                int exitcode = 0;
                while (message < 0) {
                    exitcode = 0;
                    if (device != null) {
                        if (Device.Status.DEVICE.equals(device.getStatus()) || Device.Status.UNAUTHORIZED.equals(device.getStatus())) {
                            exitcode = 1;
                            break;
                        }
                    } else {
                        if (message == CommonsMessages.NEW_DEVICE && !DeviceManager.getDevices().isEmpty()) {
                            Device d = DeviceManager.getFirstDevice();
                            if (d != null && (Device.Status.DEVICE.equals(d.getStatus()) || Device.Status.UNAUTHORIZED.equals(d.getStatus()))) {
                                exitcode = 1;
                                break;
                            }
                        }
                    }
                    message = instructionPane.waitClick();
                }
                DeviceManager.removeMessageReceiver(receiver);
                WindowManager.removeTopContent();
                return exitcode;
            }
        };
    }

    public static RunnableMessage SELECT_DEVICE_CODENAME(Pointer result, HashMap<String, String> devices) {
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                Log.info("Displaying codename choose table");
                TableView<DeviceCodenameEntry> tableView = new TableView<DeviceCodenameEntry>() {
                    public void requestFocus() {
                    }
                };
                TableColumn<DeviceCodenameEntry, String> codename = new TableColumn<>(LRes.CODENAME.toString());
                TableColumn<DeviceCodenameEntry, String> model = new TableColumn<>(LRes.MODEL.toString());
                codename.setCellValueFactory(new PropertyValueFactory<>("codename"));
                model.setCellValueFactory(new PropertyValueFactory<>("name"));
                tableView.getColumns().addAll(codename, model);
                ObservableList<DeviceCodenameEntry> observableList = tableView.getItems();
                ArrayList<Map.Entry<String, String>> sortingList = new ArrayList<>(devices.entrySet());
                sortingList.sort(new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                        return Collator.getInstance().compare(o1.getValue(), o2.getValue());
                    }
                });
                for (Map.Entry<String, String> device : sortingList) {
                    DeviceCodenameEntry tableEntry = new DeviceCodenameEntry(device.getKey(), device.getValue());
                    observableList.add(tableEntry);
                }
                ButtonPane buttonPane = new ButtonPane(LRes.SELECT, LRes.ABORT);
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                for (TableColumn column : tableView.getColumns()) {
                    column.setPrefWidth(300);
                    column.setSortable(false);
                }
                tableView.setPrefSize(600, 400);
                tableView.setEditable(false);
                String hex = "ffa970";
                tableView.setStyle("-fx-focus-color: #C8C8C8;\n" +
                        "-fx-faint-focus-color: #C8C8C8;\n" +
                        "-fx-selection-bar: #" + hex + "; -fx-selection-bar-non-focused: #" + hex + ";"
                );
                tableView.setBorder(Border.EMPTY);
                Text text = new Text(LRes.PLEASE_SELECT_DEVICE.toString());
                text.setFont(Font.font(20));
                text.setTextAlignment(TextAlignment.CENTER);
                VBox vBox = new VBox(20, GuiUtils.center(text), tableView);
                buttonPane.setContent(GuiUtils.center(vBox));
                WindowManager.setMainContent(buttonPane, false);
                int message = CommonsMessages.NOOP;
                while (message == CommonsMessages.NOOP) {
                    message = buttonPane.waitClick();
                    DeviceCodenameEntry selected = tableView.getSelectionModel().getSelectedItem();
                    if (message == 0) {
                        if (selected == null) {
                            WindowManager.popup(LRes.PLEASE_SELECT_DEVICE.toString(), PopupWindow.Icon.WARN);
                            message = CommonsMessages.NOOP;
                        } else {
                            result.pointed = selected.getCodename();
                        }
                    }
                }
                WindowManager.removeTopContent();
                return message == 0 ? 1 : 0;
            }
        };
    }
}
