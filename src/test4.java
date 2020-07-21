import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.InstallPane;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import javafx.application.Application;
import javafx.stage.Stage;

public class test4 extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        ToolManager.init(primaryStage, new String[]{});
        ThreadUtils.sleepSilently(1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InstallPane pane = new InstallPane();
                    WindowManager.setMainContent(pane, true);
                    pane.exception(new InstallException("Test message", InstallException.Code.ROM_SELECTION_ERROR), null);
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }
}
