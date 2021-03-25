package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.GuiListenerAbstract;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class InstallPane extends StackPane implements GuiListenerAbstract {

    private final CommandClass commandManager = new CommandClass();
    private TextStackPane textStackPane;

    public InstallPane() {
        build();
    }

    private void build() {
        double winWidth = WindowManager.getContentWidth(), winHeight = WindowManager.getContentHeight();
        LoadingAnimation loadingAnimation = new LoadingAnimation(winWidth / 6);
        double animHeight = loadingAnimation.getCircleRadius() + 30;
        textStackPane = new TextStackPane(winWidth, winHeight - animHeight);
        VBox vBox = new VBox(textStackPane, loadingAnimation);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);
        super.getChildren().add(vBox);
    }

    @Override
    public void text(String message) {
        if (textStackPane == null) {
            toast(message);
            return;
        }
        textStackPane.addText(message);
    }

    @Override
    public void toast(String message) {
        WindowManager.toast(message);
    }

    @Override
    public void onException(InstallException exception) {
        ToolManager.setOnExitAskForFeedback(false); //TOO MANY FEEDBACKS
        Log.log("FATAL", exception.toString(), true);
        Log.exc(exception);
        Log.exc(new Exception("TraceBackException"));
        Log.printStackTrace(exception);
        String stackTrace = StrUtils.exceptionToOriginString(exception);
        ErrorPane errorPane = new ErrorPane(LRes.CANCEL, LRes.STEP_BACK, LRes.TRY_AGAIN);
        errorPane.setTitle(LRes.PROCEDURE_EXC_TITLE.toString(), Color.rgb(128, 0, 0));
        Text text_pre = WindowManager.newText(LRes.PROCEDURE_EXC_TEXT.toString(), true);
        Text description = WindowManager.newText(exception.getMessage(), true);
        Pane descPane = new StackPane(description);
        descPane.setPadding(new Insets(5));
        Text text_post = WindowManager.newText(LRes.PROCEDURE_EXC_TEXT_2.toString(exception.getCode().toString(), stackTrace, LRes.TRY_AGAIN, LRes.STEP_BACK, LRes.CANCEL), true);
        description.setFont(Font.font(null, FontWeight.BOLD, 17));
        errorPane.appendContent(text_pre);
        errorPane.appendContent(descPane);
        errorPane.appendContent(text_post);

        WindowManager.setMainContent(errorPane, false);
        int msg;
        try {
            msg = errorPane.waitClick();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WindowManager.removeTopContent();
        if (msg == 0) {
            this.sendCommand(CommandClass.Command.ABORT);
        } else if (msg == 1) {
            this.sendCommand(CommandClass.Command.UPLEVEL);
        } else {
            this.sendCommand(CommandClass.Command.RETRY);
        }
    }

    @Override
    public CommandClass.Command exception(InstallException exception, Runnable beforeWaitCommand) throws InterruptedException {
        onException(exception);
        if (beforeWaitCommand != null) {
            beforeWaitCommand.run();
        }
        return this.waitCommand();
    }

    @Override
    public void sendCommand(CommandClass.Command cmd) {
        commandManager.sendCommand(cmd);
    }

    @Override
    public CommandClass.Command waitCommand() throws InterruptedException {
        return commandManager.waitCommand();
    }

    @Override
    public boolean isWaitingCommand() {
        return commandManager.isWaitingCommand();
    }
}
