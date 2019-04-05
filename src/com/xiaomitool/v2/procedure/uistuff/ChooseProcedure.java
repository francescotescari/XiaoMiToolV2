package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.fetch.GenericFetch;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.resources.ResourceImages;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.rom.chooser.ProcedureChooser;
import com.xiaomitool.v2.rom.chooser.SomethingChooser;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ChooseProcedure {
    private static final String ROM_CATEGORY_ID = "rom_ca_id";

    public static RInstall chooseRom(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.debug("ENTERING CHOOSE ROM");
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                ProcedureChooser pchooser = Procedures.requireProcedureChooser(runner);
                LinkedList<Installable> optionsInstallable = new LinkedList<>();
                LinkedList<ChoosableProcedure> optionsProc = new LinkedList<>();
                LinkedList<ChooserPane.Choice> choices = new LinkedList<>();
                InstallableChooser.IdGroup idGroup = (InstallableChooser.IdGroup) runner.requireContext(ROM_CATEGORY_ID);
                Log.info("Showing rom options to user");
                Device device = Procedures.requireDevice(runner);
                boolean unlocked = UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus());
                Log.debug(chooser.entrySet());
                for (Map.Entry<String, HashMap<Installable.Type, Installable>> entry : chooser.entrySet()){
                    if (!idGroup.hasId(entry.getKey())){
                        continue;
                    }
                    Log.debug(entry.getKey()+" should be in "+idGroup.toString());

                    HashMap<Installable.Type, Installable> map = entry.getValue();
                    if (map == null || map.size() == 0){
                        continue;
                    }
                    Installable installable;
                    if (map.get(Installable.Type.RECOVERY) != null && map.get(Installable.Type.FASTBOOT) != null){
                        installable = unlocked ? map.get(Installable.Type.FASTBOOT) : map.get(Installable.Type.RECOVERY);
                    } else {
                         installable = map.values().iterator().next();
                    }
                    Log.info("Showing installable: "+installable.getChoice().toString());


                    optionsInstallable.add(installable);
                    Log.debug("OptionINSTC: "+installable.getChoice().toString());
                    choices.add(installable.getChoice());
                }
                for (Map.Entry<String, ChoosableProcedure> entry : pchooser.entrySet()){
                    if (!idGroup.hasId(entry.getKey())){
                        continue;
                    }
                    ChoosableProcedure procedure = entry.getValue();
                    Log.debug("OptionPROC: "+procedure.getChoice().toString());
                    Log.info("Showing procedure: "+procedure.getChoice().toString());
                    optionsProc.add(procedure);
                    choices.add(procedure.getChoice());

                }
                ChooserPane chooserPane = new ChooserPane(choices.toArray(new ChooserPane.Choice[]{}));
                Text title = new Text(LRes.CHOOSE_PROCEDURE.toString());
                title.setFont(Font.font(20));
                VBox vBox = new VBox(10,title,chooserPane);
                vBox.setPadding(new Insets(20, 0,0,0));
                vBox.setAlignment(Pos.TOP_CENTER);
                StackPane stackPane = new StackPane(vBox);
                stackPane.setAlignment(Pos.TOP_CENTER);
                WindowManager.setMainContent(stackPane,false);
                int i = chooserPane.getIdClickReceiver().waitClick(), installableLimit = optionsInstallable.size();
                WindowManager.removeTopContent();


                if (i >= installableLimit){
                    ChoosableProcedure proc = optionsProc.get(i-installableLimit);
                    Log.info("The user has choosen this procedure: "+proc.getChoice().toString());
                    RInstall toDoNext = proc.getProcedure();
                    Procedures.pushRInstallOnStack(runner,toDoNext);
                    runner.setContext(IS_CHOOSEN_PROCEDURE, Boolean.TRUE);
                } else {
                    Installable choosenIntallable  = optionsInstallable.get(i);
                    Log.debug("Choosen installable: "+choosenIntallable.toLogString());
                    Procedures.setInstallable(runner, choosenIntallable);
                    Log.debug("CHOOSEN INSTALLABLE: "+choosenIntallable);
                    Log.info("The user has choosen this installable: "+choosenIntallable.getChoice().toString());
                    runner.setContext(IS_CHOOSEN_PROCEDURE, Boolean.FALSE);
                }
                Log.debug("EXITING CHOOSE ROM");

            }
        }.next();

    }
    public static final String IS_CHOOSEN_PROCEDURE = "ischoosenprocedure";

    public static RInstall chooseRomCategory() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Choosing rom category");
                ChooserPane.Choice[] choices = new ChooserPane.Choice[]{
                        new ChooserPane.Choice(LRes.CHOOSE_XIAOMI_TITLE.toString(), LRes.CHOOSE_XIAOMI_SUB.toString(), new Image(DrawableManager.getPng("milogo.png").toString())),
                        new ChooserPane.Choice(LRes.CHOOSE_CUSTOM_TITLE.toString(), LRes.CHOOSE_CUSTOM_SUB.toString(), new Image(DrawableManager.getPng("lineage.png").toString())),
                        new ChooserPane.Choice(LRes.CHOOSE_MOD_TITLE.toString(), LRes.CHOOSE_MOD_SUB.toString(), new Image(DrawableManager.getPng("magisk.png").toString())),
                        new ChooserPane.Choice(LRes.CHOOSE_UNLOCK_TITLE.toString(), LRes.CHOOSE_UNLOCK_SUB.toString(), new Image(DrawableManager.getPng("locker.png").toString()))
                };
                InstallableChooser.IdGroup idGroup = null;
                Device device = Procedures.requireDevice(runner);
                ChooserPane chooserPane = new ChooserPane(choices);
                Text title = new Text(LRes.CHOOSE_PROCEDURE_CATEGORY.toString());
                title.setFont(Font.font(20));
                VBox vBox = new VBox(10,title,chooserPane);
                vBox.setPadding(new Insets(20, 0,0,0));
                vBox.setAlignment(Pos.TOP_CENTER);
                StackPane stackPane = new StackPane(vBox);
                stackPane.setAlignment(Pos.TOP_CENTER);
                WindowManager.setMainContent(stackPane,false);
                int i = chooserPane.getIdClickReceiver().waitClick();
                WindowManager.removeTopContent();
                RInstall toDoNext = null;
                String noInternetMsg = LRes.NO_INTERNET_BEFORE_FETCH.toString();
                String keySkip = "key_skip_no_int";
                switch (i){
                    case 0:
                        toDoNext = RNode.sequence(ConfirmationProcedure.suggestInternetIfMissing(noInternetMsg, keySkip), RNode.conditional(keySkip, null, GenericFetch.fetchAllOfficial(device).next()));
                        idGroup = InstallableChooser.IdGroup.officialRom;
                        break;
                    case 1:
                        toDoNext = RNode.sequence(ConfirmationProcedure.suggestInternetIfMissing(noInternetMsg, keySkip),RNode.conditional(keySkip, null,GenericFetch.fetchAllUnofficial()));
                        idGroup = InstallableChooser.IdGroup.unofficialRoms;
                        break;
                    case 2:
                        toDoNext = RNode.sequence(ConfirmationProcedure.suggestInternetIfMissing(noInternetMsg, keySkip),RNode.conditional(keySkip, null,GenericFetch.fetchAllMods()));
                        idGroup = InstallableChooser.IdGroup.modsAndStuff;
                        break;
                    case 3:
                        toDoNext = Procedures.doNothing();
                        idGroup = InstallableChooser.IdGroup.xiaomiProcedures;
                        break;
                }
                Log.info("Category choosen: "+i+", group: "+idGroup.getName());
                runner.setContext(ROM_CATEGORY_ID, idGroup);
                Procedures.pushRInstallOnStack(runner, toDoNext);
            }
        }.next();
    }
}
