package com.xiaomitool.v2.engine.actions;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.engine.CommonsMessages;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.deviceView.DeviceView;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.other.DeviceTableEntry;
import com.xiaomitool.v2.gui.visual.*;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.GuiListener;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.uistuff.ChooseProcedure;
import com.xiaomitool.v2.utility.Nullable;
import com.xiaomitool.v2.utility.RunnableMessage;

import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static com.xiaomitool.v2.engine.CommonsMessages.NOOP;

public class ActionsDynamic {
    public static RunnableMessage MAIN_SCREEN_LOADING(LRes message){
        return MAIN_SCREEN_LOADING(message.toString());
    }
    public static RunnableMessage MAIN_SCREEN_LOADING(String message){
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                Node node = new LoadingAnimation.WithText(message,150);
                WindowManager.setMainContent(node,false);
                return 0;
            }
        };
    }
    public static RunnableMessage SEARCH_SELECT_DEVICES(@Nullable Device.Status wantedStatus){ return () -> {
        ActionsDynamic.MAIN_SCREEN_LOADING(LRes.SEARCHING_CONNECTED_DEVICES).run();
        DeviceManager.refresh();
        Thread.sleep(1000);
        int connectedDevices = DeviceManager.count(wantedStatus);
        RunnableMessage nextStep;
        if (connectedDevices == 0) {
            nextStep = NO_DEVICE_CONNECTED(wantedStatus);
        } else {
            nextStep = SELECT_DEVICE(wantedStatus);
        }
        return nextStep.run();
    };}

    public static RunnableMessage NO_DEVICE_CONNECTED(@Nullable Device.Status wantedStatus) { return  () -> {
        LRes msg, button;
        RunnableMessage howto = null;
        Device.Status wStatus = wantedStatus;
        if (wStatus == null){
            wStatus = Device.Status.DEVICE;
        }
        switch (wStatus){
            case SIDELOAD:
            case RECOVERY:
                msg = LRes.NO_DEVICE_CONNECTED_RECOVERY;
                button = LRes.HT_GO_RECOVERY;
                howto = ActionsStatic.HOWTO_GO_RECOVERY();
                break;
            case FASTBOOT:
                msg = LRes.NO_DEVICE_CONNECTED_FASTBOOT;
                button = LRes.HT_GO_FASTBOOT;
                break;
            default:
                msg = LRes.NO_DEVICE_CONNECTED;
                button = LRes.HT_ENABLE_USB_DEBUG;

        }
        ButtonPane pane = new ButtonPane(LRes.SEARCH_AGAIN, button);
        ImageView no_connection = new ImageView(new Image(DrawableManager.getPng(DrawableManager.NO_CONNECTION).toString()));
        no_connection.setFitHeight(200);
        no_connection.setPreserveRatio(true);
        Text no = new Text(msg.toString());
        no.setTextAlignment(TextAlignment.CENTER);
        no.setFont(Font.font(16));
        VBox vBox = new VBox(no_connection,no);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        pane.setContent(vBox);
        Log.debug("SDSDSASEW");
        WindowManager.setMainContent(pane);
        DeviceManager.addMessageReceiver(pane.getIdClickReceiver());
        int message = CommonsMessages.NOOP;
        while ((message != CommonsMessages.NEW_DEVICE || DeviceManager.count(wantedStatus) == 0) && message < 0){
            message=pane.waitClick();
            if (message > 0 && howto != null){
                message = howto.run();
                if (message >= 0){
                    message = NOOP;
                }
            }
        }
        DeviceManager.removeMessageReceiver(pane.getIdClickReceiver());

        return ActionsDynamic.SEARCH_SELECT_DEVICES(wantedStatus).run();
    };}

    public static RunnableMessage SELECT_DEVICE(@Nullable Device.Status wantedStatus) { return  () -> {
        TableView<DeviceTableEntry> tableView = new TableView<DeviceTableEntry>(){
            public void requestFocus() { }
        };
        TableColumn<DeviceTableEntry,String> serial = new TableColumn<>(LRes.SERIAL.toString());
        TableColumn<DeviceTableEntry,String> status = new TableColumn<>(LRes.STATUS.toString());
        TableColumn<DeviceTableEntry,String> codename = new TableColumn<>(LRes.CODENAME.toString());
        TableColumn<DeviceTableEntry,String> brand = new TableColumn<>(LRes.BRAND.toString());
        TableColumn<DeviceTableEntry,String> model = new TableColumn<>(LRes.MODEL.toString());
        serial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        codename.setCellValueFactory(new PropertyValueFactory<>("codename"));
        brand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        model.setCellValueFactory(new PropertyValueFactory<>("model"));
        tableView.getColumns().addAll(serial,status,codename,brand,model);
        ObservableList<DeviceTableEntry> observableList = tableView.getItems();
        for (Device device : DeviceManager.getDevices()){
            if (wantedStatus != null && !device.getStatus().equals(wantedStatus)){
                continue;
            }
            DeviceTableEntry tableEntry = new DeviceTableEntry(device);
            observableList.add(tableEntry);
        }
        tableView.getSelectionModel().select(0);
        ButtonPane buttonPane = new ButtonPane(LRes.SELECT, LRes.SEARCH_AGAIN);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (TableColumn column : tableView.getColumns()){
            column.setPrefWidth(120);
            column.setSortable(false);
        }

        tableView.setPrefSize(600,400);
        tableView.setEditable(false);
        String hex = "ffa970";
        tableView.setStyle("-fx-focus-color: #C8C8C8;\n" +
                "-fx-faint-focus-color: #C8C8C8;\n" +
                "-fx-selection-bar: #"+hex+"; -fx-selection-bar-non-focused: #"+hex+";"
        );
        tableView.setBorder(Border.EMPTY);


        buttonPane.setContent(GuiUtils.center(tableView));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WindowManager.setMainContent(buttonPane);
            }
        });
        int message = CommonsMessages.NOOP;
        DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
        boolean newDevice = false;
        while (message == CommonsMessages.NOOP){
            message = buttonPane.waitClick();
            if (message == CommonsMessages.NEW_DEVICE){
                newDevice = true;
            } else if (newDevice && message == CommonsMessages.DEVICE_UPDATE_FINISH){
                observableList.clear();
                for (Device device : DeviceManager.getDevices()){
                    if (wantedStatus != null && !device.getStatus().equals(wantedStatus)){
                        continue;
                    }
                    DeviceTableEntry tableEntry = new DeviceTableEntry(device);
                    observableList.add(tableEntry);

                }
                tableView.getSelectionModel().select(0);
                newDevice = false;
            }
            if (message < 0){
                message = CommonsMessages.NOOP;
            }

            if (message == 0){
                DeviceTableEntry selected = tableView.getSelectionModel().getSelectedItem();

                if (selected == null){
                    message = CommonsMessages.NOOP;
                } else {
                    DeviceManager.setSelectedDevice(selected.getSerial());
                    if (DeviceManager.getSelectedDevice() == null){
                        message = CommonsMessages.NOOP;
                    }
                }
            }
        }

        return message == 0 ? 0 : SEARCH_SELECT_DEVICES(wantedStatus).run();
    };}

    public static RunnableMessage REQUIRE_DEVICE_ON(Device device){
        return () -> {
            if (!device.isTurnedOn()){
                //Need to reboot to device
                REBOOT_DEVICE(device, Device.Status.DEVICE, false).run();
                WAIT_USB_DEBUG_ENABLE(device).run();
            }
            REQUIRE_DEVICE_AUTH(device).run();
            REQUIRE_DEVICE_CONNECTED(device).run();
            return  0;
        };
    }

    public static RunnableMessage REQUIRE_DEVICE_CONNECTED(Device device){
        return () -> {
            if (device.isConnected()){
                return 0;
            }
            ButtonPane pane = new ButtonPane(LRes.TRY_AGAIN);
            ImageView no_connection = new ImageView(new Image(DrawableManager.getPng(DrawableManager.NO_CONNECTION).toString()));
            no_connection.setFitHeight(200);
            no_connection.setPreserveRatio(true);
            Text no = new Text(LRes.DEVICE_NOT_CONNECTED.toString());
            no.setTextAlignment(TextAlignment.CENTER);
            no.setFont(Font.font(16));
            no.setWrappingWidth(600);
            VBox vBox = new VBox(no_connection,no);
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(20);
            pane.setContent(vBox);
            WindowManager.setMainContent(pane);
            IDClickReceiver receiver = pane.getIdClickReceiver();
            DeviceManager.addMessageReceiver(receiver);
            int message = NOOP;
            while (message == NOOP){
                message = pane.waitClick();
                if (message == CommonsMessages.DEVICE_UPDATE_FINISH){
                    if (device.isConnected()){
                        break;
                    }
                }
                if (message == 0){
                    MAIN_SCREEN_LOADING(LRes.SEARCHING_CONNECTED_DEVICES).run();
                    ActionsStatic.RESTART_ADB_SERVER().run();
                    if (device.isConnected()){
                        break;
                    }
                    WindowManager.removeTopContent();
                }
                message = NOOP;
            }
            DeviceManager.removeMessageReceiver(receiver);
            return 1;
        };
    }
    public static RunnableMessage WAIT_USB_DEBUG_ENABLE(Device device){
        return WAIT_USB_DEBUG_ENABLE(device,null);
    }

    public static RunnableMessage WAIT_USB_DEBUG_ENABLE(Device device, String text){
        return () -> {
            ButtonPane pane = new ButtonPane(LRes.HT_ENABLE_USB_DEBUG, LRes.SEARCH_AGAIN);
            WindowManager.setMainContent(pane);
            IDClickReceiver receiver = pane.getIdClickReceiver();
            DeviceManager.addMessageReceiver(receiver);
            int message = NOOP;
            while (message == NOOP){
                message = receiver.waitClick();
                if (message == CommonsMessages.DEVICE_UPDATE_FINISH){
                    if (device.isTurnedOn()){
                        break;
                    }
                }
                if (message == 1){
                    ActionsStatic.RESTART_ADB_SERVER().run();
                    if (device.isTurnedOn()){
                        break;
                    }
                    WindowManager.removeTopContent();

                }

                    message = NOOP;

            }
            DeviceManager.removeMessageReceiver(receiver);
            return  0;};
    }

    public static RunnableMessage FIND_DEVICE_INFO(Device device){
        return () -> {
            Text texts[] = new Text[10];
            Text ktexts[] = new Text[10];
            LRes kstring[] = new LRes[]{LRes.SERIAL, LRes.BRAND, LRes.MODEL, LRes.CODENAME, LRes.MIUI_VERSION, LRes.ANDROID_VERSION, LRes.SERIAL_NUMBER, LRes.BOOTLOADER_STATUS, LRes.FASTBOOT_PARSED, LRes.RECOVERY_PARSED};
            for (int i = 0; i<kstring.length; ++i){
                texts[i] = new Text();
                ktexts[i] = new Text();
                texts[i].setFont(Font.font(14));
                ktexts[i].setFont(Font.font(14));
                ktexts[i].setText(kstring[i].toString()+" : ");
            }
            ActionsUtil.setDevicePropertiesText(device, texts);
            double width = WindowManager.getMainPane().getWidth()-100;
            RegularTable regularTable = new RegularTable(10,2,280,width);
            for (int i = 0; i<kstring.length; ++i){
                regularTable.add(ktexts[i],0,i,Pos.CENTER_RIGHT);
                regularTable.add(texts[i],1,i,Pos.CENTER_LEFT);
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

            WindowManager.setMainContent(GuiUtils.center(vBox));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!device.getDeviceProperties().getFastbootProperties().isParsed()) {
                        try {
                            if (!device.reboot(Device.Status.FASTBOOT)) {
                                throw new Exception("Failed to reboot to fastboot");
                            } else {

                                ActionsUtil.setDevicePropertiesText(device, texts);
                            }
                        } catch (Exception e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    texts[8].setText(e.getMessage());
                                }
                            });
                        }
                    } else {
                        ActionsUtil.setDevicePropertiesText(device, texts);
                    }

                    if (!device.getDeviceProperties().getSideloadProperties().isParsed()) {
                        try {
                            if (UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus())) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        texts[9].setText(LRes.UNIMPORTANT.toString());
                                    }
                                });

                            } else {
                                if (!device.reboot(Device.Status.RECOVERY)) {
                                    throw new Exception("Failed to reboot to recovery");
                                } else {
                                    ActionsUtil.setDevicePropertiesText(device, texts);
                                }
                            }
                        } catch (Exception e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    texts[9].setText(e.getMessage());
                                }
                            });
                        }
                    } else {
                        ActionsUtil.setDevicePropertiesText(device, texts);
                    }

                    try {
                        Thread.sleep(1000);
                        ActionsDynamic.START_PROCEDURE(device).run();
                    } catch (InterruptedException e) {

                    }


                }
            }).start();

            return 0;


        };
    }



    public static RunnableMessage REQUIRE_DEVICE_AUTH(Device device){
        return () -> {
            if (!device.needAuthorization()){
                return 0;
            }
            ButtonPane pane = new ButtonPane( LRes.TRY_AGAIN);

            IDClickReceiver receiver = pane.getIdClickReceiver();
            DeviceManager.addMessageReceiver(receiver);
            DeviceView deviceView = new DeviceView(DeviceView.DEVICE_18_9, 900);
            ImageView image = new ImageView(DrawableManager.getPng(DrawableManager.DEVICE_AUTH).toString());
            image.setViewport(new Rectangle2D(0,image.getImage().getHeight()-2160,1080,2160));
            deviceView.setContent(image);
            deviceView.buildCircleTransition(800,2060,Animation.INDEFINITE);
            Pane cropped = GuiUtils.crop(deviceView,0,550,900/deviceView.getOuterAspectRatio(),350);
            cropped.setPrefSize(900/deviceView.getOuterAspectRatio(),350);
            GuiUtils.debug(cropped);
            Text text = new Text(LRes.AUTH_DEVICE_TEXT.toString());
            text.setTextAlignment(TextAlignment.CENTER);
            text.setFont(Font.font(16));
            text.setWrappingWidth(600);
            VBox vBox = new VBox(GuiUtils.center(cropped),text);
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(20);

            pane.setContent(vBox);

            WindowManager.setMainContent(pane);


            int messsage = NOOP;
            while (messsage == NOOP){
                messsage = receiver.waitClick();
                if(messsage == CommonsMessages.DEVICE_UPDATE_FINISH){
                    if (!device.needAuthorization()){
                        break;
                    }
                }
                if (messsage < 0){
                    messsage = NOOP;
                }
                if (messsage == 0){
                    DeviceManager.refresh();
                    if (device.needAuthorization()) {
                        ActionsStatic.RESTART_ADB_SERVER().run();
                        if (!device.needAuthorization()){
                            break;
                        } else {
                            WindowManager.setMainContent(pane);
                        }
                    }
                    messsage = NOOP;
                }
            }
            DeviceManager.removeMessageReceiver(receiver);
            return 1;
        };
    }


    public static RunnableMessage REBOOT_DEVICE(Device device, Device.Status status){
        return REBOOT_DEVICE(device,status,true); }

    public static RunnableMessage REBOOT_DEVICE(Device device, Device.Status status, boolean wait){
        return () -> {
            try {
                boolean res = wait ? device.reboot(status) : device.rebootNoWait(status);
            }  catch (AdbException e) {
                e.printStackTrace();
            }
            return 0;
        };
    }

    public static RunnableMessage START_PROCEDURE(Device device){
        return new RunnableMessage() {
            @Override
            public int run() throws InterruptedException {
                InstallPane installPane = new InstallPane();
                WindowManager.setMainContent(installPane);
                ProcedureRunner runner = new ProcedureRunner(installPane.getListener());


                runner.init(null,device);

                //runner.setContext("prop_"+DeviceProperties.CODENAME,"whyred");
                //FastbootFetch.findAllLatestFastboot().run(runner);
                try {
                    Log.debug("PRO0 CHOOSE CAT");
                    ChooseProcedure.chooseRomCategory().run(runner);
                    Log.debug("PRO0 CHOOSE ROM");
                    ChooseProcedure.chooseRom().run(runner);
                    Log.debug("PRO0 FETCH RESOURCE");
                    GenericInstall.resourceFetchWait().run(runner);
                    Log.debug("PRO0 INSTALL ROM");
                    GenericInstall.runInstallProcedure().run(runner);
                    Log.debug("PRO0 FINISHED");
                    GenericInstall.installationSuccess().run(runner);
                } catch (Exception e){
                    e.printStackTrace();
                } catch (RMessage rMessage) {
                    rMessage.printStackTrace();
                }


                return 0;
            }
        };
    }

}
