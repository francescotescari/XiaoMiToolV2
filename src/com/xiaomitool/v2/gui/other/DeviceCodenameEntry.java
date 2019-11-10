package com.xiaomitool.v2.gui.other;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.language.LRes;
import javafx.beans.property.SimpleStringProperty;

public class DeviceCodenameEntry {
    public SimpleStringProperty name = new SimpleStringProperty();
    public  SimpleStringProperty codename = new SimpleStringProperty();
    public DeviceCodenameEntry(String codename, String name){
        String unknown = LRes.UNKNOWN.toString().toLowerCase();
        if (codename == null){codename=unknown;}
        if (name == null){
            name = unknown;
        }
        setCodename(codename);
        setName(name);

    }

    public void setCodename(String codename) {
        this.codename.set(codename);
    }

    public void setName(String status) {
        this.name.set(status);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty codenameProperty() {
        return codename;
    }

    public String getCodename() {
        return codename.get();
    }

    public String getName() {
        return name.get();
    }


}
