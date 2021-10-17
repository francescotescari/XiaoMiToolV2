package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.language.LRes;
import java.io.File;
import javafx.scene.image.Image;

public class LocalZipRomFile extends ZipRom {
  public LocalZipRomFile(File file) {
    super(file);
  }

  @Override
  public String getTitle() {
    return LRes.ROM_LOCAL.toString();
  }

  @Override
  public String getText() {
    return LRes.ROM_LOCAL_TEXT.toString();
  }

  @Override
  public Image getIcon() {
    return DrawableManager.getResourceImage(DrawableManager.LOCAL_PC);
  }
}
