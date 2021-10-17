package com.xiaomitool.v2.rom;

import java.io.File;
import javafx.scene.image.Image;

public abstract class MultiZipRom extends MultiInstallable {
  public MultiZipRom(File... files) {
    super(fromFileZipRom(files));
  }

  private static ZipRom[] fromFileZipRom(File... files) {
    ZipRom[] zipRoms = new ZipRom[files.length];
    for (int i = 0; i < files.length; ++i) {
      zipRoms[i] =
          new ZipRom(files[i]) {
            @Override
            public String getTitle() {
              return "";
            }

            @Override
            public String getText() {
              return "";
            }

            @Override
            public Image getIcon() {
              return null;
            }
          };
    }
    return zipRoms;
  }
}
