package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.GuiUtils;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class GradientOverlay extends StackPane {
    public GradientOverlay(double fromX, double fromY, double toX, double toY, Stop... stops) {
        build(fromX, fromY, toX, toY, stops);
    }

    private void build(double fromX, double fromY, double toX, double toY, Stop... stops) {
        super.setPickOnBounds(false);
        super.setMouseTransparent(true);
        super.setBackground(GuiUtils.backgroundFromColor(new LinearGradient(fromX, fromY, toX, toY, true, CycleMethod.NO_CYCLE, stops)));
    }
}
