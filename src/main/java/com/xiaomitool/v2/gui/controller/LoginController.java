package com.xiaomitool.v2.gui.controller;

import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.GuiObjects;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.LoadingAnimation;
import com.xiaomitool.v2.gui.visual.TextScrollPane;
import com.xiaomitool.v2.gui.visual.VisiblePane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.utility.WaitSemaphore;
import com.xiaomitool.v2.utility.utils.CookieUtils;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.HttpCookie;
import java.net.URI;
import java.util.Locale;

public class LoginController extends DefaultController {
    private static final String LOGIN_URL = "https://account.xiaomi.com/pass/serviceLogin?sid=unlockApi&json=false&passive=true&hidden=false&_snsDefault=facebook&checkSafePhone=true&_locale=" + Locale.getDefault().getLanguage().toLowerCase();    
    private static boolean loggedIn = false;
    private static Thread loginThread = null;
    @FXML
    private HBox HEADER;
    @FXML
    private ImageView IMG_CLOSE;
    @FXML
    private WebView BROWSER;
    @FXML
    private Hyperlink WHY_LOGIN;
    @FXML
    private StackPane CONTENT;
    private WebEngine ENGINE;
    private VisiblePane BROWSER_AREA;
    private Node LOADING_NODE = new LoadingAnimation.WithText(LRes.LOADING, 70), CURRENT_NODE;
    private String passToken = null, userId = null, deviceId = null;
    private boolean isWhyLogin = false;
    private TextScrollPane WHY_LOGIN_TEXT = null;
    private boolean loadingLocalContent = false;

    public static boolean isLogged() {
        return loggedIn;
    }

    public static void loginClick() {
        if (loggedIn) {
            logout();
        } else {
            if (loginThread != null && loginThread.isAlive()) {
                return;
            }
            Runnable runnable = loginRunnable();
            loginThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (isLogged()) {
                                WindowManager.toast(LRes.LOGIN_SUCCESS.toString());
                            } else {
                                WindowManager.toast(LRes.LOGIN_CANCELED.toString());
                            }
                        }
                    });
                }
            });
            loginThread.start();
        }
    }

    public static void logout() {
        CookieUtils.clear();
        XiaomiKeystore.clear();
        setLoginNumber(null);
    }

    public static Runnable loginRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                String userId = XiaomiKeystore.getInstance().getUserId();
                if (userId != null && !userId.isEmpty()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setLoginNumber(userId);
                        }
                    });
                    return;
                }
                WaitSemaphore semaphore = new WaitSemaphore(0);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Stage stage = WindowManager.launchLogin();
                            stage.setOnHidden(new EventHandler<WindowEvent>() {
                                @Override
                                public void handle(WindowEvent event) {
                                    semaphore.increase();
                                }
                            });
                        } catch (Exception e) {
                            semaphore.increase();
                        }
                    }
                });
                try {
                    semaphore.waitOnce();
                } catch (InterruptedException e) {
                    Log.error("Login thread interrupted: " + e.getMessage());
                }
            }
        };
    }

    public static void setLoginNumber(String userId) {
        if (userId == null) {
            userId = "";
        }
        final String uid = userId;

        GuiObjects.runOnReady(GuiObjects.LOGIN_NUMBER, node -> {
            Label uidLabel = (Label) node;
            if (uidLabel == null) {
                Log.error("Cannot set userId label: null object");
            } else {
                uidLabel.setText(uid);
            }
            return true;
        });

        loggedIn = !userId.isEmpty();
        final String text = loggedIn ? LRes.LOGOUT.toString() : LRes.LOGIN.toString();
        GuiObjects.runOnReady(GuiObjects.LOGIN_LINK, node -> {
            Hyperlink link = (Hyperlink) node;
            if (link == null) {
                Log.error("Cannot set login hyperLink: null object");
            } else {
                link.setText(text);
            }
            return true;
        });

    }

    @Override
    protected void initialize() {
        setCloseImage(IMG_CLOSE);
        initHeaderDrag(HEADER);
        initBrowser();
        initOnClick();
        initText();
    }

    private void initText() {
        WHY_LOGIN.setText(LRes.LOGIN_WHY_LOGIN.toString());
    }

    private void initOnClick() {
        WHY_LOGIN.setOnMouseClicked(event -> {
            if (isWhyLogin) {
                BROWSER_AREA.add(CURRENT_NODE);
                WHY_LOGIN.setText(LRes.LOGIN_WHY_LOGIN.toString());
            } else {
                if (WHY_LOGIN_TEXT == null) {
                    Text loginText = new Text(LRes.LOGIN_WHY_LOGIN_TEXT.toString());
                    loginText.setFont(Font.font(14));
                    loginText.setTextAlignment(TextAlignment.CENTER);
                    WHY_LOGIN_TEXT = new TextScrollPane(loginText);
                    WHY_LOGIN_TEXT.setPadding(new Insets(30, 60, 30, 60));
                }
                BROWSER_AREA.add(WHY_LOGIN_TEXT);
                WHY_LOGIN.setText(LRes.OK_UNDERSTAND.toString());
            }
            isWhyLogin = !isWhyLogin;
        });
    }

    private void initBrowser() {
        BROWSER_AREA = new VisiblePane(CONTENT);
        BROWSER_AREA.add(LOADING_NODE);
        ENGINE = BROWSER.getEngine();
        ENGINE.load(LOGIN_URL);
        Pointer pointer = new Pointer();
        pointer.pointed = new CookieUtils.EventCookieAdd() {
            @Override
            public boolean run(URI url, HttpCookie cookie) {
                String name = cookie.getName();
                if ("passToken".equals(name)) {
                    passToken = cookie.getValue();
                } else if ("deviceId".equals(name)) {
                    deviceId = cookie.getValue();
                } else if ("userId".equals(name)) {
                    userId = cookie.getValue();
                }
                if (passToken != null && userId != null && deviceId != null && !passToken.isEmpty() && !userId.isEmpty() && !deviceId.isEmpty()) {
                    loginDone();
                    return false;
                }
                return true;
            }
        };
        CookieUtils.addCookieListener((CookieUtils.EventCookieAdd) pointer.pointed);
        ENGINE.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (loadingLocalContent) {
                return;
            }
            if (BROWSER_AREA == null) {
                return;
            }
            if (Worker.State.RUNNING.equals(newValue)) {
                setLoadingPage();
            } else if (Worker.State.FAILED.equals(newValue)) {
                setErrorPage();
            } else if (Worker.State.SUCCEEDED.equals(newValue)) {
                setBrowserPage();
            }
        });
    }

    private void loginDone() {
        Log.info("Logged in succesfulyl: " + userId);
        XiaomiKeystore.getInstance().setCredentials(userId, passToken, deviceId);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setLoginNumber(userId);
                ToolManager.closeStage(primaryStage);
            }
        });
    }

    private void setLoadingPage() {
        CURRENT_NODE = LOADING_NODE;
        if (!isWhyLogin) {
            BROWSER_AREA.add(CURRENT_NODE);
        }
    }

    private void setErrorPage() {
        loadingLocalContent = true;
        ENGINE.loadContent("Connection error!");
        setBrowserPage();
        loadingLocalContent = false;
    }

    private void setBrowserPage() {
        CURRENT_NODE = BROWSER;
        if (!isWhyLogin) {
            BROWSER_AREA.add(CURRENT_NODE);
        }
    }
}
