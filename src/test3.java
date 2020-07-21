import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.InstallPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.language.Lang;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Paths;

public class test3  {
    public static void main(String... args) throws Exception {
        Exception e = new Exception();
        System.out.println(LRes.REBOOT_STATUS_FAILED.toString(Device.Status.RECOVERY.toString(), "+", Device.Status.RECOVERY.toString(), e.getMessage()));
        Lang.saveToXmlFile(Paths.get("./lang.xml"));
    }



}
