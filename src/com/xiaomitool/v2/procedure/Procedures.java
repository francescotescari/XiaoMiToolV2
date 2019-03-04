package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.PopupWindow;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.*;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.rom.chooser.ProcedureChooser;
import com.xiaomitool.v2.tasks.UpdateListener;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;


import java.io.File;
import java.util.LinkedList;

import static com.xiaomitool.v2.procedure.GuiListener.Event;
import static com.xiaomitool.v2.procedure.install.InstallException.Code.*;

public class Procedures {
    public static final String SELECTED_DEVICE = "selected_device";
    public static final String INSTALL_FILE_PATH = "install_file_path";
    public static final String INSTALLABLE = "installable";
    public static final String REQUEST_PARAMS = "request_params";
    public static final String REQUEST_RESULT = "request_result";
    public static final String DOWNLOAD_URL = "download_url";
    public static final String DEVICE_CODENAME = "device_codename";
    private static final String TO_DO_STACK = "to_do_stack";
    private static final String INSTALLABLE_CHOOSER = "installable_chooser";
    private static final String PROCEDURE_CHOOSER = "procedure_chooser";

    public static RInstall rebootNoWait(Device.Status toStatus){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Device device = (Device) procedureRunner.requireContext(SELECTED_DEVICE);
                try {
                    device.rebootNoWait(toStatus);
                    procedureRunner.text(LRes.REBOOTING_TO_MODE.toString(toStatus.toString()));
                } catch (AdbException e) {
                    throw e.toInstallException(true);
                }
            }
        };
    }

    public static RInstall rebootIfYouCan(Device.Status toStatus){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                try {
                    rebootNoWait(toStatus).run(procedureRunner);
                } catch (InstallException e){
                    if (!InstallException.Code.INTERNAL_ERROR.equals(e.getCode())){
                        Log.error("Failed to reboot device: "+e.getCode()+", "+e.getMessage());
                    } else {
                        throw e;
                    }
                }
            }
        };

    }

    public static RInstall reboot(Device.Status toStatus){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Device device = (Device) procedureRunner.requireContext(SELECTED_DEVICE);
                procedureRunner.text("Rebooting device to "+toStatus.toString()+" mode");
                boolean result = false;
                try {
                    result = device.reboot(toStatus);
                } catch (AdbException e) {
                    e.printStackTrace();
                }
                if (!result){
                    while (Device.Status.UNAUTHORIZED.equals(device.getStatus()) || Device.Status.OFFLINE.equals(device.getStatus())){
                        procedureRunner.onEvent(Event.DEVICE_UNAUTH_OFFLINE,null);
                        if (CommandClass.Command.ABORT.equals(procedureRunner.waitCommand())){
                            throw new InstallException("Procedure aborted", InstallException.Code.ABORTED, false);
                        }
                    }
                }
                result = toStatus.equals(device.getStatus());
                if (!result){
                    throw new InstallException("Cannot reboot device to "+toStatus.toString(),REBOOT_FAILED,true);
                }
            }
        };
    }
    public static RInstall checkTwrp(){
        return new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Device device = (Device) procedureRunner.requireContext(SELECTED_DEVICE);
                if (YesNoMaybe.NO.equals(device.getAnswers().isInTwrpRecovery())){
                    throw new InstallException("The device is not in twrp", NOT_IN_VALID_TWRP, true);
                }
            }
        };
    }
    public static RInstall rebootTwrp(){
        return RNode.sequence(requireAccessibile(),reboot(Device.Status.RECOVERY),checkTwrp());
    }
    public static RInstall fetchResources(){
        return new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Installable installable = (Installable) procedureRunner.requireContext(INSTALLABLE);
                ProgressPane.DefProgressPane progressPane = new ProgressPane.DefProgressPane();
                UpdateListener downloadListener = progressPane.getUpdateListener(1000);
                downloadListener.addOnStart(new UpdateListener.OnStart() {
                    @Override
                    public void run(long totalSize) {
                        progressPane.setContentText(LRes.DOWNLOADING_ROM_FILE);
                    }
                });
                downloadListener.addOnError(new UpdateListener.OnError() {
                    @Override
                    public void run(Exception e) {
                        Log.error("Download rom task failed: "+e.getMessage());
                        installable.sendCommand(CommandClass.Command.ABORT);
                    }
                });
                UpdateListener extractListener = progressPane.getUpdateListener(500);
                extractListener.addOnStart(new UpdateListener.OnStart() {
                    @Override
                    public void run(long totalSize) {
                        progressPane.setContentText("Extracting rom file...");
                    }
                });
                extractListener.addOnError(new UpdateListener.OnError() {
                    @Override
                    public void run(Exception e) {
                        Log.error("Extract rom task failed: "+e.getMessage());
                    }
                });
                WindowManager.setMainContent(progressPane,false);
                installable.fetchResources(downloadListener, extractListener);

            }
        };
    }

    public static RInstall fetchWaitResources(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                fetchResources().run(procedureRunner);
                waitResources().run(procedureRunner);
            }
        };
    }
    public static RInstall waitResources(){
        return new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Installable installable = (Installable) procedureRunner.requireContext(INSTALLABLE);
                try {
                    Log.debug("Waiting resources");
                    installable.waitReourcesReady();
                    Log.debug("Resources ready");
                } catch (Exception e) {
                    Log.debug("Failed to get resources");
                    throw new InstallException("Failed to get required resources: "+e.getMessage(),RESOURCE_FETCH_FAILED, true);
                } finally {
                    WindowManager.removeTopContent();
                }

            }
        };
    }
    public static RInstall requireAccessibile(){
        return new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Device device = (Device) procedureRunner.requireContext(SELECTED_DEVICE);
                if (device == null){
                    throw new InstallException("Device object is null", INTERNAL_ERROR, false);
                }
                try {
                    device.requireAccessibile();
                } catch (AdbException e) {
                    throw e.toInstallException(true);
                }
            }
        };
    }
    public static RInstall rebootStockRecovery(){
        return new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                Device device = (Device) procedureRunner.requireContext(SELECTED_DEVICE);
                procedureRunner.onEvent(Event.STOCK_RECOVERY_REBOOTING,device);
                try {
                    Procedures.reboot(Device.Status.RECOVERY).run(procedureRunner);
                } catch (InstallException e){
                    if (!Device.Status.SIDELOAD.equals(device.getStatus())){
                        throw e;
                    }
                } finally {
                    procedureRunner.clearEvent();
                }
            }
        };
    }
    public static RInstall rebootNeedDeviceOn(Device device){
        return new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                procedureRunner.onEvent(Event.NEED_DEBUGGING_ACTIVE,device);
                Procedures.reboot( Device.Status.DEVICE).run(procedureRunner);
                procedureRunner.clearEvent();
            }
        };
    }

    public static RInstall findAllDeviceInfo(Device device){
        return RNode.sequence(rebootNeedDeviceOn(device),new RInstall() {
            @Override
            public void run( ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                sleep(2000);
                boolean needRecovery = !device.getDeviceProperties().getSideloadProperties().isParsed() && !UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus());
                boolean needFastboot = !device.getDeviceProperties().getFastbootProperties().isParsed();
                if (needRecovery) {
                    Procedures.rebootStockRecovery().run(procedureRunner);
                    sleep(2000);
                    needRecovery = false;
                }
                if (needFastboot){
                    Procedures.reboot( Device.Status.FASTBOOT).run(procedureRunner);
                    needFastboot = false;
                    sleep(2000);
                    needRecovery  = !UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus());
                }
                if (needRecovery) {
                    Procedures.rebootStockRecovery().run(procedureRunner);
                    sleep(2000);
                    needRecovery = false;
                }
            }
        });
    }




    private static void sleep(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }


    public static String getDeviceProperty(ProcedureRunner runner, String property) throws InstallException {
        Object prop = runner.getContext("prop_"+property);
        if (prop != null){
            return prop.toString();
        }
        Device device = (Device) runner.requireContext(SELECTED_DEVICE);
        prop = device.getDeviceProperties().get(property);
        return prop == null ? null : prop.toString();
    }
    public static String requireDeviceProperty(ProcedureRunner runner, String property) throws InstallException {
        String prop = getDeviceProperty(runner, property);
        if (prop == null){
            throw new InstallException("Failed to get device property: "+property, InstallException.Code.INFO_RETRIVE_FAILED, false);
        }
        return prop;
    }

    public static File selectFileFromPc(String title, String text, FileChooser.ExtensionFilter... filters) throws InterruptedException, InstallException {

        Text tit = new Text(title);
        tit.setFont(Font.font(20));
        tit.setTextAlignment(TextAlignment.CENTER);
        tit.setWrappingWidth(WindowManager.getContentWidth()-100);

        Text  t = new Text(text);
        t.setFont(Font.font(16));
        t.setTextAlignment(TextAlignment.CENTER);
        t.setWrappingWidth(WindowManager.getContentWidth()-100);
        DragAndDropPane dragAndDropPane = new DragAndDropPane(300,300);
        dragAndDropPane.setFilters(filters);

        VBox vBox = new VBox(tit,t,GuiUtils.center(dragAndDropPane));
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.ABORT);
        buttonPane.setContent(vBox);
        WindowManager.setMainContent(buttonPane,false);
        File file = null;
        while (file == null) {
            if (buttonPane.waitClick() == 1){
                throw InstallException.ABORT_EXCEPTION;
            }
            file = dragAndDropPane.getSelectedFile();
            if (file == null){
                WindowManager.popup(LRes.FILE_PLEASE_SELECT_POPUP.toString(), PopupWindow.Icon.WARN);
            }
        }
        WindowManager.removeTopContent();

        return file;

    }

    public static Device requireDevice(ProcedureRunner runner) throws InstallException {
        return (Device) runner.requireContext(SELECTED_DEVICE);
    }
    public static Installable requireInstallable(ProcedureRunner runner) throws InstallException {
        return (Installable) runner.requireContext(INSTALLABLE);
    }
    public static InstallableChooser requireInstallableChooser(ProcedureRunner runner) throws InstallException {
        InstallableChooser chooser =  (InstallableChooser) runner.getContext(INSTALLABLE_CHOOSER);
        if (chooser != null){
            return chooser;
        }
        chooser = new InstallableChooser();
        runner.setContext(INSTALLABLE_CHOOSER, chooser);
        return chooser;
    }

    public static void setInstallable(ProcedureRunner runner, Installable installable) {
        runner.setContext(INSTALLABLE, installable);
    }
    @SuppressWarnings("unchecked")
    public static void pushRInstallOnStack(ProcedureRunner runner, RInstall install){
        LinkedList stack = (LinkedList) runner.getContext(TO_DO_STACK);
        if (stack == null){
            stack = new LinkedList();
            runner.setContext(TO_DO_STACK,stack);
        }
        stack.addLast(install);
    }

    private static final String SAVED_PROCE = "saved_proce";
    public static RInstall runSavedProcedure(String name){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ((RInstall) runner.requireContext(SAVED_PROCE+name)).run(runner);
            }
        };
    }

    public static RInstall saveProcedure(String name, RInstall procedure){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                runner.setContext(SAVED_PROCE+name, procedure);
            }
        };
    }

    public static RInstall runStackedProcedures(){
        return new RInstall() {
            @Override
            @SuppressWarnings("unchecked")
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                LinkedList stack = (LinkedList) runner.requireContext(TO_DO_STACK);
                LinkedList<RInstall> safeList = new LinkedList<>();
                for (Object o : stack){
                    if (o instanceof RInstall){
                        safeList.addLast((RInstall) o);
                    }
                }
                stack.clear();
                if (safeList.size() == 0){
                    return;
                }
                RInstall[] procedureArray  =safeList.toArray(new RInstall[]{});
                RNode.sequence(procedureArray).run(runner);
            }
        };
    }

    public static File getInstallableFile(Installable installable) throws InstallException {
        File file = installable.getFinalFile();
        if (file != null){
            return file;
        }
        file = installable.getDownloadedFile();
        if (file != null){
            return file;
        }
        throw new InstallException("Installable doesn't contain file", FILE_NOT_FOUND, false);
    }

    public static ProcedureChooser requireProcedureChooser(ProcedureRunner runner) throws InstallException {
       ProcedureChooser chooser = (ProcedureChooser) runner.getContext(PROCEDURE_CHOOSER);
       if (chooser == null){
           chooser = new ProcedureChooser();
           runner.setContext(PROCEDURE_CHOOSER, chooser);
       }
       return chooser;
    }

    public static RInstall doNothing(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {

            }
        };
    }

}
