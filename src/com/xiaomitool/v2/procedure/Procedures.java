package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.PopupWindow;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.gui.visual.DragAndDropPane;
import com.xiaomitool.v2.gui.visual.SidePane;
import com.xiaomitool.v2.gui.visual.SortableTableView;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.rom.chooser.ProcedureChooser;
import com.xiaomitool.v2.rom.interfaces.StatedProcedure;
import com.xiaomitool.v2.utility.CommandClass;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import static com.xiaomitool.v2.procedure.install.InstallException.Code.FILE_NOT_FOUND;
import static com.xiaomitool.v2.procedure.install.InstallException.Code.INTERNAL_ERROR;

public class Procedures {
    public static final String SELECTED_DEVICE = "selected_device";
    public static final String INSTALLABLE = "installable";
    public static final String REQUEST_PARAMS = "request_params";
    private static final String TO_DO_STACK = "to_do_stack";
    private static final String INSTALLABLE_CHOOSER = "installable_chooser";
    private static final String PROCEDURE_CHOOSER = "procedure_chooser";
    private static final String SAVED_PROCE = "saved_proce";

    public static String getDeviceProperty(ProcedureRunner runner, String property) throws InstallException {
        Object prop = runner.getContext("prop_" + property);
        if (prop != null) {
            return prop.toString();
        }
        Device device = (Device) runner.requireContext(SELECTED_DEVICE);
        prop = device.getDeviceProperties().get(property);
        return prop == null ? null : prop.toString();
    }

    public static String requireDeviceProperty(ProcedureRunner runner, String property) throws InstallException {
        String prop = getDeviceProperty(runner, property);
        if (prop == null) {
            throw new InstallException("Failed to get device property: " + property, InstallException.Code.INFO_RETRIVE_FAILED);
        }
        return prop;
    }

    public static List<File> selectFilesFromPc(String title, String text, FileChooser.ExtensionFilter... filters) throws InterruptedException {
        SidePane sidePane = new SidePane();
        SortableTableView<FileEntry> tableView = new SortableTableView<>(new SortableTableView.TableViewColumn<>(LRes.SELECTED_FILE_LIST.toString(), FileEntry::pathProperty, 300d));
        tableView.setPrefSize(300, 250);
        sidePane.setLeft(GuiUtils.center(tableView));
        DragAndDropPane dragAndDropPane = new DragAndDropPane(300, 300);
        dragAndDropPane.setFilters(filters);
        sidePane.setRight(GuiUtils.center(dragAndDropPane));
        Text tit = new Text(title);
        tit.setFont(Font.font(20));
        dragAndDropPane.setOnFileChange(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                if (newValue != null) {
                    tableView.getItems().add(new FileEntry(newValue));
                    dragAndDropPane.reset();
                }
            }
        });
        tit.setTextAlignment(TextAlignment.CENTER);
        tit.setWrappingWidth(WindowManager.getContentWidth() - 100);
        Text tText = new Text(text);
        tText.setFont(Font.font(16));
        tText.setTextAlignment(TextAlignment.CENTER);
        tText.setWrappingWidth(WindowManager.getContentWidth() - 100);
        VBox vBox = new VBox(tit, tText, sidePane);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.CANCEL);
        buttonPane.setContent(vBox);
        WindowManager.setMainContent(buttonPane, false);
        List<File> files = null;
        while (true) {
            int choice = buttonPane.waitClick();
            if (choice == 1) {
                break;
            }
            ObservableList<FileEntry> items = tableView.getItems();
            if (items.isEmpty()) {
                WindowManager.popup(LRes.FILE_PLEASE_SELECT_POPUP.toString(), PopupWindow.Icon.WARN);
                continue;
            }
            files = new LinkedList<>();
            for (FileEntry entry : items) {
                files.add(entry.file);
            }
            break;
        }
        WindowManager.removeTopContent();
        return files;
    }

    public static File selectFileFromPc(String title, String text, FileChooser.ExtensionFilter... filters) throws InterruptedException, InstallException {
        Text tit = new Text(title);
        tit.setFont(Font.font(20));
        tit.setTextAlignment(TextAlignment.CENTER);
        tit.setWrappingWidth(WindowManager.getContentWidth() - 100);
        Text t = new Text(text);
        t.setFont(Font.font(16));
        t.setTextAlignment(TextAlignment.CENTER);
        t.setWrappingWidth(WindowManager.getContentWidth() - 100);
        DragAndDropPane dragAndDropPane = new DragAndDropPane(300, 300);
        dragAndDropPane.setFilters(filters);
        VBox vBox = new VBox(tit, t, GuiUtils.center(dragAndDropPane));
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.CANCEL);
        buttonPane.setContent(vBox);
        WindowManager.setMainContent(buttonPane, false);
        File file = null;
        while (file == null) {
            if (buttonPane.waitClick() == 1) {
                return null;
            }
            file = dragAndDropPane.getSelectedFile();
            if (file == null) {
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
        InstallableChooser chooser = (InstallableChooser) runner.getContext(INSTALLABLE_CHOOSER);
        if (chooser != null) {
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
    public static void pushRInstallOnStack(ProcedureRunner runner, RInstall install) {
        LinkedList stack = (LinkedList) runner.getContext(TO_DO_STACK);
        if (stack == null) {
            stack = new LinkedList();
            runner.setContext(TO_DO_STACK, stack);
        }
        if (stack.size() > 0){
            Log.warn("Multiple procedures on the stack: "+stack);
        }
        stack.addLast(install);
    }
    public static RInstall throwRMessage(CommandClass.Command cmd){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                throw new RMessage(cmd);
            }
        };
    }

    public static RInstall runSavedProcedure(String name) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ((RInstall) runner.requireContext(SAVED_PROCE + name)).run(runner);
            }
        };
    }

    public static RInstall saveProcedure(String name, RInstall procedure) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                runner.setContext(SAVED_PROCE + name, procedure);
            }
        };
    }

    public static RInstall runStackedProcedures() {
        return new RInstall() {
            @Override
            @SuppressWarnings("unchecked")
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                LinkedList stack = (LinkedList) runner.requireContext(TO_DO_STACK);
                LinkedList<RInstall> safeList = new LinkedList<>();
                for (Object o : stack) {
                    if (o instanceof RInstall) {
                        safeList.addLast((RInstall) o);
                    }
                }
                stack.clear();
                if (safeList.size() == 0) {
                    return;
                }
                RInstall[] procedureArray = safeList.toArray(new RInstall[]{});
                RNode.sequence(procedureArray).run(runner);
            }
        };
    }

    public static File getInstallableFile(Installable installable) throws InstallException {
        File file = installable.getFinalFile();
        if (file != null) {
            return file;
        }
        file = installable.getDownloadedFile();
        if (file != null) {
            return file;
        }
        throw new InstallException("Installable doesn't contain file", FILE_NOT_FOUND, "Null downloaded file and final file, url: " + installable.getDownloadUrl());
    }

    public static ProcedureChooser requireProcedureChooser(ProcedureRunner runner) throws InstallException {
        ProcedureChooser chooser = (ProcedureChooser) runner.getContext(PROCEDURE_CHOOSER);
        if (chooser == null) {
            chooser = new ProcedureChooser();
            runner.setContext(PROCEDURE_CHOOSER, chooser);
        }
        return chooser;
    }

    public static RInstall doNothing() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) {
            }
        };
    }

    public static boolean stillNeedUsbDebug(ProcedureRunner runner, StatedProcedure procedure) throws InstallException {
        return getAllRequiredStates(runner, procedure).contains(Device.Status.DEVICE);
    }

    private static LinkedHashSet<Device.Status> getAllRequiredStates(ProcedureRunner runner, StatedProcedure procedure) throws InstallException {
        Device device = Procedures.requireDevice(runner);
        Device.Status status = device.getStatus();
        if (!device.isConnected()) {
            status = null;
        }
        LinkedHashSet<Device.Status> statuses = new LinkedHashSet<>();
        for (Device.Status requiredStatus : procedure.getRequiredStates()) {
            statuses.add(requiredStatus);
            if (!requiredStatus.equals(status)) {
                if (status == null || Device.Status.FASTBOOT.equals(status)) {
                    statuses.add(Device.Status.DEVICE);
                }
            }
            status = requiredStatus;
        }
        return statuses;
    }

    public static RInstall featureNotAvailable() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                throw new InstallException("Feature not available yet", INTERNAL_ERROR, "You should not see this message");
            }
        };
    }

    private static class FileEntry {
        private final File file;
        private final SimpleStringProperty path = new SimpleStringProperty(this, "path");

        public FileEntry(File f) {
            String path = f.getAbsolutePath();
            if (path.length() > 40) {
                path = "..." + path.substring(path.length() - 40);
            }
            this.path.set(path);
            this.file = f;
        }

        public SimpleStringProperty pathProperty() {
            return path;
        }
    }
}
