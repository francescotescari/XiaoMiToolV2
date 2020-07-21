import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.adb.AdbUtils;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import javafx.application.Application;
import javafx.stage.Stage;

public class test2 extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        ToolManager.init(primaryStage, new String[]{});
        ThreadUtils.sleepSilently(1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AdbCommunication.registerAutoScanDevices();
                Device device = null;
                while (device == null){
                    device = DeviceManager.getFirstDevice();
                    ThreadUtils.sleepSilently(2000);
                }
                try {
                    ActionsDynamic.START_PROCEDURE(device,  GenericInstall.main(), null, GenericInstall.selectRomAndGo()).run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
