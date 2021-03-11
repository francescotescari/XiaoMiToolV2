package com.xiaomitool.v2.gui.other;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.language.LRes;
import javafx.beans.property.SimpleStringProperty;

public class DeviceTableEntry {
    public SimpleStringProperty serial = new SimpleStringProperty();
    public SimpleStringProperty status = new SimpleStringProperty();
    public SimpleStringProperty brand = new SimpleStringProperty();
    public SimpleStringProperty model = new SimpleStringProperty();
    public SimpleStringProperty codename = new SimpleStringProperty();

    public DeviceTableEntry(String serial, String codename, String status, String brand, String model) {
        String unknown = LRes.UNKNOWN.toString().toLowerCase();
        if (serial == null) {
            serial = unknown;
        }
        if (codename == null) {
            codename = unknown;
        }
        if (status == null) {
            status = unknown;
        }
        if (brand == null) {
            brand = unknown;
        }
        if (model == null) {
            model = unknown;
        }
        setSerial(serial);
        setCodename(codename);
        setStatus(status);
        setBrand(brand);
        setModel(model);
    }

    public DeviceTableEntry(Device device) {
        this(device.getSerial(), device.isConnected() ? device.getStatus() : null, device.getDeviceProperties());
    }

    public DeviceTableEntry(String serial, Device.Status deviceStatus, DeviceProperties deviceProperties) {
        this(serial, deviceProperties.getCodename(true), deviceStatus == null ? "disconnected" : deviceStatus.toString(), (String) deviceProperties.get(DeviceProperties.BRAND), (String) deviceProperties.get(DeviceProperties.MODEL));
    }

    public SimpleStringProperty brandProperty() {
        return brand;
    }

    public SimpleStringProperty codenameProperty() {
        return codename;
    }

    public SimpleStringProperty modelProperty() {
        return model;
    }

    public SimpleStringProperty serialProperty() {
        return serial;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public String getBrand() {
        return brand.get();
    }

    public void setBrand(String brand) {
        this.brand.set(brand);
    }

    public String getCodename() {
        return codename.get();
    }

    public void setCodename(String codename) {
        this.codename.set(codename);
    }

    public String getModel() {
        return model.get();
    }

    public void setModel(String model) {
        this.model.set(model);
    }

    public String getSerial() {
        return serial.get();
    }

    public void setSerial(String serial) {
        this.serial.set(serial);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
