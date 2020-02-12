package com.xiaomitool.v2.gui.visual;


import com.xiaomitool.v2.engine.actions.ActionsStatic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.GuiListener;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class InstallPane extends StackPane {
    private GuiListener listener;
    private TextStackPane textStackPane;
    public InstallPane(){
        build();
    }



    private void build(){
        double winWidth = WindowManager.getContentWidth(), winHeight =WindowManager.getContentHeight();
        LoadingAnimation loadingAnimation = new LoadingAnimation(winWidth / 6);
        double animHeight = loadingAnimation.getCircleRadius()+30;
        textStackPane = new TextStackPane(winWidth, winHeight-animHeight);
        VBox vBox = new VBox(textStackPane, loadingAnimation);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);
        listener = new GuiListener() {
            @Override
            public void toast(String message) {
                WindowManager.toast(message);
            }

            @Override
            public void text(String message) {
                if (textStackPane == null){
                    toast(message);
                    return;
                }
                textStackPane.addText(message);

            }

            @Override
            protected void onException(InstallException exception) {
                WindowManager.setOnExitAskForFeedback(true);
                Log.log("FATAL",exception.toString(),true);
                Log.exc(exception);
                Log.exc(new Exception("TraceBackException"));
                /*Log.debug(StrUtils.exceptionToString(new Exception()));*/
                Log.printStackTrace(exception);
                //exception.printStackTrace();
                /*Log.debug("GUI LISTENER EXCEPTION:");*/
                /*Log.debug(exception.getMessage());*/
                /*Log.debug(exception.getCode().toString());*/
                String stackTrace = StrUtils.exceptionToString(exception);
                int len = stackTrace.length();
                stackTrace = StrUtils.firstNLines(stackTrace,5);
                ErrorPane errorPane = new ErrorPane(LRes.CANCEL, LRes.STEP_BACK, LRes.TRY_AGAIN);
                errorPane.setTitle(LRes.PROCEDURE_EXC_TITLE.toString(), Color.rgb(128,0,0));
                errorPane.setText(LRes.PROCEDURE_EXC_TEXT.toString(LRes.PROCEDURE_EXC_DETAILS.toString(exception.getCode().toString(), exception.getMessage())+"\n",LRes.TRY_AGAIN, LRes.STEP_BACK, LRes.CANCEL));
                Text t2 = new Text(LRes.PROCEDURE_EXC_ADV_DETAILS.toString()+": "+exception.getMessage()+"\n"+stackTrace+(stackTrace.length() != len ? "\n..." : ""));
                t2.setTextAlignment(TextAlignment.CENTER);
                t2.setWrappingWidth(WindowManager.getContentWidth()-100);
                t2.setFont(Font.font(14));
                t2.setFill(Color.gray(0.15));
                errorPane.appendContent(t2);
                WindowManager.setMainContent(errorPane,false);
                int msg;
                try {
                    msg = errorPane.waitClick();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                WindowManager.removeTopContent();
                if (msg == 0){
                    this.sendCommand(Command.ABORT);
                } else if (msg == 1){
                    this.sendCommand(Command.UPLEVEL);
                } else {
                    this.sendCommand(Command.RETRY);
                }

                //exception.printStackTrace();
            }
        };
        super.getChildren().add(vBox);
    }

    public GuiListener getListener() {
        return listener;
    }
}
