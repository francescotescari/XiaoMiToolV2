package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class DonationPane extends ButtonPane {
    private double[] dontCoor, donaCoor;
    private boolean showInstallSuccess;
    public DonationPane(boolean showInstallSuccess){
        super(LRes.DONATE_DONT, LRes.DONATE);
        this.showInstallSuccess = showInstallSuccess;
        build();
    }
    public DonationPane(){
        this(true);
    }
    private void build(){
        SmilePane smilePane = new SmilePane(100);
        smilePane.setHappiness(90);
        Text text = new Text(LRes.DONATE_TEXT.toString());
        text.setFont(Font.font(15));
        text.setWrappingWidth(WindowManager.getContentWidth()-150);
        text.setTextAlignment(TextAlignment.CENTER);
        VBox vBox = new VBox(smilePane);
        if (showInstallSuccess){
            Text suc = new Text(LRes.INSTALL_SUCCESS.toString());
            suc.setTextAlignment(TextAlignment.CENTER);
            suc.setFont(Font.font(20));
            suc.setFill(Color.rgb(0,51,0));
            vBox.getChildren().add(suc);
        }
        vBox.getChildren().add(text);
        vBox.setSpacing(30);
        vBox.setAlignment(Pos.CENTER);
        List<CustomButton> buttons = super.getButtons();
        Button dontDonate = buttons.get(0), donate = buttons.get(1);

        WindowManager.getMainStage().getScene().addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double distanceFromDona = 10000, distanceFromDont = 10000;
                if (donaCoor == null){
                    donaCoor = GuiUtils.getCenterCoordinates(donate);
                } else {
                    distanceFromDona = GuiUtils.distanceBetween(event.getSceneX(), event.getSceneY(), donaCoor[0], donaCoor[1])+1;
                }
                if (dontCoor == null){
                    dontCoor = GuiUtils.getCenterCoordinates(dontDonate);
                    if (dontCoor != null) {
                        Log.debug("Dont coor: " +dontCoor[0]+" . "+dontCoor[1]);
                    }
                } else {
                    distanceFromDont = GuiUtils.distanceBetween(event.getSceneX(), event.getSceneY(), dontCoor[0], dontCoor[1])+1;
                }

                    smilePane.setHappiness(75+((Math.pow(Math.abs(distanceFromDont-7),1.2)-Math.pow(Math.abs(distanceFromDona-7),1.2)))*(3200/Math.pow(distanceFromDona+distanceFromDont-70,1.9)));


                Log.debug(distanceFromDona+" - "+distanceFromDont);

            }
        });
        super.setContent(vBox);
    }
}
