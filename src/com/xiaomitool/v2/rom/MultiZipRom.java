package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.rom.MultiInstallable;
import javafx.scene.image.Image;

import java.io.File;

public abstract class MultiZipRom extends MultiInstallable {

    private static ZipRom[] fromFileZipRom(File... files){
        ZipRom[] zipRoms = new ZipRom[files.length];
        for (int i = 0; i<files.length; ++i){
            zipRoms[i] = new ZipRom(files[i]) {
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

    public MultiZipRom(File... files){
        super(fromFileZipRom(files));
    }
}
