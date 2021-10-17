package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.fetch.GenericFetch;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.rom.chooser.ProcedureChooser;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ChooseProcedure {
  public static final String IS_CHOOSEN_PROCEDURE = "ischoosenprocedure";
  private static final String ROM_CATEGORY_ID = "rom_ca_id";

  public static RInstall chooseRom() {
    return new RInstall() {
      @Override
      public void run(ProcedureRunner runner)
          throws InstallException, RMessage, InterruptedException {
        InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
        ProcedureChooser pchooser = Procedures.requireProcedureChooser(runner);
        LinkedList<Installable> optionsInstallable = new LinkedList<>();
        LinkedList<ChoosableProcedure> optionsProc = new LinkedList<>();
        LinkedList<ChooserPane.Choice> choices = new LinkedList<>();
        InstallableChooser.IdGroup idGroup =
            (InstallableChooser.IdGroup) runner.requireContext(ROM_CATEGORY_ID);
        Log.info("Showing rom options to user");
        Device device = Procedures.requireDevice(runner);
        boolean unlocked = UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus());
        for (Map.Entry<String, HashMap<Installable.Type, Installable>> entry : chooser.entrySet()) {
          if (!idGroup.hasId(entry.getKey())) {
            continue;
          }
          HashMap<Installable.Type, Installable> map = entry.getValue();
          if (map == null || map.size() == 0) {
            continue;
          }
          Installable installable;
          if (map.get(Installable.Type.RECOVERY) != null
              && map.get(Installable.Type.FASTBOOT) != null) {
            installable =
                unlocked ? map.get(Installable.Type.FASTBOOT) : map.get(Installable.Type.RECOVERY);
          } else {
            installable = map.values().iterator().next();
          }
          Log.info("Showing installable: " + installable.getChoice().toString());
          optionsInstallable.add(installable);
          choices.add(installable.getChoice());
        }
        for (Map.Entry<String, ChoosableProcedure> entry : pchooser.entrySet()) {
          if (!idGroup.hasId(entry.getKey())) {
            continue;
          }
          ChoosableProcedure procedure = entry.getValue();
          Log.info("Showing procedure: " + procedure.getChoice().toString());
          optionsProc.add(procedure);
          choices.add(procedure.getChoice());
        }
        ChooserPane chooserPane = new ChooserPane(choices.toArray(new ChooserPane.Choice[] {}));
        Text title = new Text(LRes.CHOOSE_PROCEDURE.toString());
        title.setFont(Font.font(20));
        VBox vBox = new VBox(10, title, chooserPane);
        vBox.setPadding(new Insets(20, 0, 0, 0));
        vBox.setAlignment(Pos.TOP_CENTER);
        StackPane stackPane = new StackPane(vBox);
        stackPane.setAlignment(Pos.TOP_CENTER);
        WindowManager.setMainContent(stackPane, false);
        int i = chooserPane.getIdClickReceiver().waitClick(),
            installableLimit = optionsInstallable.size();
        WindowManager.removeTopContent();
        if (i >= installableLimit) {
          ChoosableProcedure proc = optionsProc.get(i - installableLimit);
          Log.info("The user has choosen this procedure: " + proc.getChoice().toString());
          RInstall toDoNext = proc.getInstallProcedure();
          Procedures.pushRInstallOnStack(runner, toDoNext);
          runner.setContext(IS_CHOOSEN_PROCEDURE, Boolean.TRUE);
        } else {
          Installable choosenIntallable = optionsInstallable.get(i);
          Procedures.setInstallable(runner, choosenIntallable);
          Log.info(
              "The user has choosen this installable: " + choosenIntallable.getChoice().toString());
          runner.setContext(IS_CHOOSEN_PROCEDURE, Boolean.FALSE);
        }
      }
    }.next();
  }

  public static RInstall chooseRomCategory() {
    return new RInstall() {
      @Override
      public void run(ProcedureRunner runner)
          throws InstallException, RMessage, InterruptedException {
        Log.info("Choosing rom category");
        ChooserPane.Choice[] choices =
            new ChooserPane.Choice[] {
              new ChooserPane.Choice(
                  LRes.CHOOSE_XIAOMI_TITLE.toString(),
                  LRes.CHOOSE_XIAOMI_SUB.toString(),
                  new Image(DrawableManager.getPng("milogo.png").toString())),
              new ChooserPane.Choice(
                  LRes.CHOOSE_CUSTOM_TITLE.toString(),
                  LRes.CHOOSE_CUSTOM_SUB.toString(),
                  new Image(DrawableManager.getPng("lineage.png").toString())),
              new ChooserPane.Choice(
                  LRes.CHOOSE_MOD_TITLE.toString(),
                  LRes.CHOOSE_MOD_SUB.toString(),
                  new Image(DrawableManager.getPng("magisk.png").toString())),
              new ChooserPane.Choice(
                  LRes.CHOOSE_UNLOCK_TITLE.toString(),
                  LRes.CHOOSE_UNLOCK_SUB.toString(),
                  new Image(DrawableManager.getPng("locker.png").toString()))
            };
        InstallableChooser.IdGroup idGroup = null;
        Device device = Procedures.requireDevice(runner);
        ChooserPane chooserPane = new ChooserPane(choices);
        Text title = new Text(LRes.CHOOSE_PROCEDURE_CATEGORY.toString());
        title.setFont(Font.font(20));
        VBox vBox = new VBox(10, title, chooserPane);
        vBox.setPadding(new Insets(20, 0, 0, 0));
        vBox.setAlignment(Pos.TOP_CENTER);
        StackPane stackPane = new StackPane(vBox);
        stackPane.setAlignment(Pos.TOP_CENTER);
        WindowManager.setMainContent(stackPane, false);
        int i = chooserPane.getIdClickReceiver().waitClick();
        WindowManager.removeTopContent();
        RInstall toDoNext = null;
        String noInternetMsg = LRes.NO_INTERNET_BEFORE_FETCH.toString();
        String keySkip = "key_skip_no_int";
        switch (i) {
          case 0:
            toDoNext =
                RNode.sequence(
                    ConfirmationProcedure.suggestInternetIfMissing(noInternetMsg, keySkip),
                    RNode.conditional(keySkip, null, GenericFetch.fetchAllOfficial(device).next()));
            idGroup = InstallableChooser.IdGroup.officialRom;
            break;
          case 1:
            toDoNext =
                RNode.sequence(
                    ConfirmationProcedure.suggestInternetIfMissing(noInternetMsg, keySkip),
                    RNode.conditional(keySkip, null, GenericFetch.fetchAllUnofficial()));
            idGroup = InstallableChooser.IdGroup.unofficialRoms;
            break;
          case 2:
            toDoNext =
                RNode.sequence(
                    ConfirmationProcedure.suggestInternetIfMissing(noInternetMsg, keySkip),
                    RNode.conditional(keySkip, null, GenericFetch.fetchAllMods()));
            idGroup = InstallableChooser.IdGroup.modsAndStuff;
            break;
          case 3:
            toDoNext = Procedures.doNothing();
            idGroup = InstallableChooser.IdGroup.xiaomiProcedures;
            break;
        }
        Log.info("Category choosen: " + i + ", group: " + idGroup.getName());
        runner.setContext(ROM_CATEGORY_ID, idGroup);
        Procedures.pushRInstallOnStack(runner, toDoNext);
      }
    }.next();
  }

  public static RInstall alternativeBackupMethod(
      RInstall originalProcedure, RInstall backupProcedure, String text) {
    return new RInstall() {
      @Override
      public void run(ProcedureRunner runner)
          throws InstallException, RMessage, InterruptedException {
        try {
          originalProcedure.setFlag(RNode.FLAG_THROWRAWEXCEPTION, true).run(runner);
        } catch (InstallException exception) {
          ButtonPane buttonPane = new ButtonPane(LRes.DETAILS, LRes.ALTERNATIVE);
          buttonPane.setContentText(
              text
                  + "\n\n"
                  + LRes.PROCEDURE_EXC_DETAILS.toString(
                      exception.getCode().toString(), exception.getMessage())
                  + "\n\n"
                  + LRes.ALTERNATIVE_PROCEDURE_EXP.toString(LRes.DETAILS, LRes.ALTERNATIVE));
          WindowManager.setMainContent(buttonPane, false);
          int click = buttonPane.waitClick();
          WindowManager.removeTopContent();
          if (click == 0) {
            throw exception;
          } else {
            backupProcedure.run(runner);
          }
        } catch (RMessage msg) {
          if (!CommandClass.Command.ALTERNATIVE.equals(msg.getCmd())) {
            throw msg;
          }
          backupProcedure.run(runner);
        }
      }
    };
  }
}
