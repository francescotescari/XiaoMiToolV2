package com.xiaomitool.v2.rom.chooser;

import com.xiaomitool.v2.adb.device.*;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RNode;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.procedure.fetch.TwrpFetch;
import com.xiaomitool.v2.procedure.install.FastbootInstall;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.StockRecoveryInstall;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class   InstallationRequirement {

    public static final InstallationRequirement USB_DEBUG_ENABLED = new InstallationRequirement("usb debug enabled") {
        @Override
        public boolean isSatisfied(Device device) {
                if (YesNoMaybe.YES.equals(device.getAnswers().isRebootManualMode()) && !YesNoMaybe.YES.equals(device.getAnswers().isNeedDeviceDebug())){
                    return true;
                }
             Properties props = device.getDeviceProperties().getAdbProperties();
             return props.isParsed() || props.isFailed();
        }

        @Override
        public RInstall satisfyProcedure() {
            return ManageDevice.requireDeviceUsbDebug();
        }

        @Override
        public String getHumanName(Device device) {
            return LRes.ENABLE_USB_DEBUG_IF_NOT.toString();
        }
    };
    public static final InstallationRequirement STOCKRECOVERY_REACHABLE = new InstallationRequirement("stock recovery reachable", USB_DEBUG_ENABLED) {
        @Override
        public boolean isSatisfied(Device device) {
            return  !YesNoMaybe.YES.equals(device.getAnswers().hasTwrpRecovery()) && device.getDeviceProperties().getSideloadProperties().isParsed();
        }

        @Override
        public RInstall satisfyProcedure() {
            return RNode.sequence(RebootDevice.requireStockRecovery(), StockRecoveryInstall.getStockRecoveryInfo());
        }

        @Override
        public String getHumanName(Device device) {
            return LRes.REQ_CHECK_STOCKRECOVERY.toString();
        }
    };

    public static final InstallationRequirement CHECK_IF_TWRP_INSTALLED = new InstallationRequirement("check if twrp installed",USB_DEBUG_ENABLED) {
        @Override
        public boolean isSatisfied(Device device) {
            if (UnlockStatus.LOCKED.equals(device.getAnswers().getUnlockStatus())){
                device.getAnswers().setAnswer(DeviceAnswers.HAS_TWRP, YesNoMaybe.NO);
                return true;
            }
            Log.debug("Checking if we know if twrp is installed");
            YesNoMaybe answer = device.getAnswers().hasTwrpRecovery();
            Log.debug("Is it? "+answer);
            return !(answer == null || YesNoMaybe.MAYBE.equals(answer));
        }

        @Override
        public RInstall satisfyProcedure() {
            return ManageDevice.checkIfTwrpInstalled();
        }

        @Override
        public String getHumanName(Device device) {
            return LRes.REQ_CHECK_TWRPINSTALL.toString();
        }
    };


    public static final InstallationRequirement UNLOCKED_BOOTLOADER = new InstallationRequirement("bootloader unlock") {
        @Override
        public boolean isSatisfied(Device device) {
            return UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus());
        }

        @Override
        public RInstall satisfyProcedure() {
            return RNode.sequence(FastbootInstall.unlockBootloader());
        }

        @Override
        public String getHumanName(Device device) {
            return DeviceGroups.isAndroidOneDevice(device.getDeviceProperties().getCodename(true)) ? LRes.REQ_BOOTLOADER_UNLOCKED.toString() : LRes.REQ_BOOTLOADER_UNLOCKED_MIUI.toString();
        }
    };
    public static final InstallationRequirement TWRP_INSTALLED = new InstallationRequirement("twrp install", UNLOCKED_BOOTLOADER, CHECK_IF_TWRP_INSTALLED) {
        @Override
        public boolean isSatisfied(Device device) {
            return YesNoMaybe.YES.equals(device.getAnswers().hasTwrpRecovery());
        }

        @Override
        public RInstall satisfyProcedure(){
            return RNode.sequence(TwrpFetch.fetchTwrpFallbackToPc(), GenericInstall.resourceFetchWait(), TwrpInstall.flashTwrp(), TwrpInstall.checkIfIsInTwrp());
        }

        @Override
        public String getHumanName(Device device) {
            String text = LRes.REQ_INSTALL_TWRP.toString();
            if (!CHECK_IF_TWRP_INSTALLED.isSatisfied(device)){
                text+=" ("+LRes.IF_NOT_INSTALLED.toString().toLowerCase()+")";
            }
            return text;
        }
    };

    private InstallationRequirement[] subRequirements;
    private String id;

    private InstallationRequirement(String id, InstallationRequirement... subRequirements){
        this.id = id;
        this.subRequirements = subRequirements != null ? subRequirements : new InstallationRequirement[]{};
    }

    @Override
    public int hashCode(){
        return this.id == null ? 0 : id.hashCode();
    }
    @Override
    public String toString(){
        return id;
    }

    public InstallationRequirement[] getSubRequirements() {
        return subRequirements;
    }

    public abstract boolean isSatisfied(Device device);
    public abstract RInstall satisfyProcedure();
    public abstract String getHumanName(Device device);
    public static LinkedList<InstallationRequirement> toSatisfyRequirements(Device device, InstallationRequirement... requirements){
        LinkedList<InstallationRequirement> list = new LinkedList<>();
        for (InstallationRequirement req : requirements){
            toSatisfyInternal(device,req,list);
        }
        list.sort(Comparator.comparingInt(o -> o.getSubRequirements().length));
        return list;
    }
    private static void toSatisfyInternal(Device device, InstallationRequirement requirement, LinkedList<InstallationRequirement> list){
        if (!requirement.isSatisfied(device)){
            if (!list.contains(requirement)) {
                list.addFirst(requirement);
            }
            for (InstallationRequirement req : requirement.getSubRequirements()){
                toSatisfyInternal(device,req,list);
            }
        }
    }

    public static RInstall satisfyNextRequirement(Device device, Installable installable){
        InstallationRequirement[] requirements = getInstallableRequirements(installable, device);
        if (requirements.length == 0){
            return null;
        }
        LinkedList<InstallationRequirement> toSatisfy = toSatisfyRequirements(device, requirements);
        if (toSatisfy.isEmpty()){
            return null;
        }
        return toSatisfy.get(0).satisfyProcedure();
    }

    public static List<InstallationRequirement> getAllInstallableRequirements(Installable installable, Device device){
        return toSatisfyRequirements(device,getInstallableRequirements(installable,device));
    }

    public static InstallationRequirement[] getInstallableRequirements(Installable installable, Device device){
        List<InstallationRequirement> requirementList = new LinkedList<>();
        switch (installable.getType()){
            case RECOVERY:
                Log.debug("Trying get install recovery requirements...");
                Log.debug("Has iToken? "+installable.hasInstallToken());
                Log.debug("Is official? "+installable.isOfficial());
                Log.debug("Bootloader status: "+device.getAnswers().getUnlockStatus());
                Log.debug("Stock recovery reachable? "+STOCKRECOVERY_REACHABLE.isSatisfied(device));
                boolean hasStockRecovery = (!UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus()) || STOCKRECOVERY_REACHABLE.isSatisfied(device));
                boolean isStockRecoveryInstallable = (installable.hasInstallToken() || installable.isOfficial()) && (installable instanceof MiuiZipRom);
                boolean isUnsafeCrossRegionInstallation = false;
                if (isStockRecoveryInstallable){
                    MiuiZipRom zipRom = (MiuiZipRom) installable;
                    MiuiRom.Specie wantToInstallSpecie = zipRom.getSpecie();
                    MiuiRom.Specie currentSpecie = device.getAnswers().getCurrentSpecie();
                    if (currentSpecie.getZone() != wantToInstallSpecie.getZone()){
                        Log.debug("Different rom zone installation, this might be an unsafe cross region installation");
                        String product = device.getDeviceProperties().getCodename(true);
                        isUnsafeCrossRegionInstallation = !DeviceGroups.isSafeToChangeRecoveryLocked(product);
                    }
                }
                if (!isStockRecoveryInstallable){
                    requirementList.add(TWRP_INSTALLED);
                } else {
                    if (isUnsafeCrossRegionInstallation) {
                        requirementList.add(UNLOCKED_BOOTLOADER);
                    }
                    if (!hasStockRecovery) {
                        requirementList.add(STOCKRECOVERY_REACHABLE);
                    }
                }
                break;
            case IMAGE:
            case FASTBOOT:
                requirementList.add(UNLOCKED_BOOTLOADER);
                break;

        }
        return requirementList.toArray(new InstallationRequirement[]{});
    }
}
