package com.xiaomitool.v2.rom.chooser;

import com.xiaomitool.v2.procedure.uistuff.ChoosableProcedure;

public class ProcedureChooser extends SomethingChooser<ChoosableProcedure> {
  public ProcedureChooser() {
    init();
  }

  private void init() {
    this.add(SomethingChooser.ID_FAKE_OFFICIAL, ChoosableProcedure.OFFICIAL_ROM_INSTALL);
    this.add(SomethingChooser.ID_UNLOCK_DEVICE, ChoosableProcedure.UNLOCK_DEVICE);
    this.add(
        SomethingChooser.ID_FAKE_UNOFFICIAL_ZIP, ChoosableProcedure.UNOFFICIAL_MULTI_ROM_INSTALL);
    this.add(SomethingChooser.ID_FAKE_MOD_ZIP, ChoosableProcedure.GENERIC_MOD_ZIP);
    this.add(SomethingChooser.ID_INSTALL_RECOVERY_IMAGE, ChoosableProcedure.RECOVERY_IMAGE);
    this.add(SomethingChooser.ID_BACK, ChoosableProcedure.BACK_TO_CATEGORIES);
  }
}
