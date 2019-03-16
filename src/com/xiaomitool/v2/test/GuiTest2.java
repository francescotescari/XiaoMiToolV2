package com.xiaomitool.v2.test;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.visual.*;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.GuiListener;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.procedure.install.*;
import com.xiaomitool.v2.procedure.uistuff.ChooseProcedure;
import com.xiaomitool.v2.procedure.uistuff.ConfirmationProcedure;
import com.xiaomitool.v2.utility.BezierInterpolator;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;



public class GuiTest2 extends Application{
    private static double[] coor = null;
    private static Transition buildTransition(Node circle){
        TranslateTransition transition = new TranslateTransition(Duration.millis(2000), circle);
        transition.setFromX(-200);
        transition.setToX(200);
        transition.setAutoReverse(false);
        transition.setCycleCount(-1);
        transition.setInterpolator(new BezierInterpolator.WaitBezierInterpolator(0,0.6,0.4,1,0.2,0.2));
        FadeTransition transition1 = new FadeTransition(Duration.millis(1000),circle);
        transition1.setFromValue(0);
        transition1.setToValue(1);
        transition1.setAutoReverse(true);
        transition1.setCycleCount(-1);
        transition1.setInterpolator(new BezierInterpolator.WaitBezierInterpolator(0,0.8,1,1,0.4,0));
        ParallelTransition parallelTransition = new ParallelTransition(transition, transition1);
        return parallelTransition;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ToolManager.init(primaryStage);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InstallPane pane = new InstallPane();
                    WindowManager.setMainContent(pane);
                    ProcedureRunner runner = new ProcedureRunner(pane);
                    runner.setAfterExceptionPane(pane);
                    DeviceManager.refresh();
                    Device device = null;
                    for (Device d : DeviceManager.getDevices()){
                        if (d.getSerial().equals("88eec8b5")){
                            device = d;
                        }
                    }
                    if (device == null){
                        device = new Device("88eec8b5");
                        DeviceManager.addDevice(device);
                    }
                    runner.init(null,device);
                    device.getDeviceProperties().getAdbProperties().put(DeviceProperties.CODENAME, "cepheus");
                    device.getDeviceProperties().getAdbProperties().put(DeviceProperties.FULL_VERSION, "9.3.9");
                    device.getDeviceProperties().getAdbProperties().put(DeviceProperties.X_SERIAL_NUMBER, 1);
                    device.getDeviceProperties().getAdbProperties().put(DeviceProperties.CODEBASE, "9.0");
                    device.getDeviceProperties().getSideloadProperties().put(DeviceProperties.ROMZONE,"1");
                    //runner.setContext("prop_"+DeviceProperties.CODENAME,"whyred");
                    GenericInstall.main().run(runner);
                    //FastbootFetch.findAllLatestFastboot().run(runner);
                    /*ChooseProcedure.chooseRomCategory().run(runner);
                    ChooseProcedure.chooseRom().run(runner);
                    ConfirmationProcedure.confirmInstallableProcedure().run(runner);
                    ConfirmationProcedure.confirmInstallationStart().run(runner);
                    DeviceManager.initScanThreads();
                    GenericInstall.satisfyAllRequirements().run(runner);
                    GenericInstall.resourceFetchWait().run(runner);
                    DeviceManager.refresh();
                    GenericInstall.runInstallProcedure().run(runner);
                    GenericInstall.installationSuccess().run(runner);*/
                  //FastbootInstall.findBuildRunFlashAll().run(runner);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InstallException e) {
                    e.printStackTrace();
                } catch (RMessage rMessage) {
                    rMessage.printStackTrace();
                }
            }
        }).start();

        if (true){
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();

        if (true){
            return;
        }


        SidePane sidePane = new SidePane();
        sidePane.setRight(new Text("CIAO"));
        sidePane.setLeft(new Text("CIAO2"));
        double size = 400;
        ImageView birrapiena = new ImageView(new Image(DrawableManager.getPng("beerfull").toString()));
        ImageView birravuota = new ImageView(new Image(DrawableManager.getPng("beerempty").toString()));
        birrapiena.setFitWidth(size);
        birravuota.setFitHeight(size);
        birrapiena.setFitHeight(size);
        birravuota.setFitWidth(size);

        StackPane birra = new StackPane( birrapiena);
        ButtonPane buttonPane = new ButtonPane("Non donare", "Effettua una donazione");

        //birra.setClip(new Rectangle(0,256,600,256));
        StackPane img = new StackPane(birravuota,birra);
        buttonPane.setContent(img);
        SmilePane pane = new SmilePane(100,100);
        pane.getContext().fillRect(0,0,50,50);
        pane.setHappiness(75);
        DonationPane donationPane = new DonationPane();

        /*WindowManager.getMainStage().getScene().addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double x = (mouseEvent.getSceneX()-350)/(WindowManager.getContentWidth()-700);
                if (x < 0){
                    x = 0;
                }
                if (coor != null) {
                    double d = GuiUtils.distanceBetween(coor[0], coor[1], mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    Log.debug(d);
                    pane.setHappiness((d-50)/2);
                } else {
                    coor = GuiUtils.getCenterCoordinates(pane);
                }

                double h = x*size;
                birra.setClip(new Rectangle(0,size-h,size+200,h+200));
            }
        });*/


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {


                            WindowManager.setMainContent(donationPane);
                            coor = GuiUtils.getCenterCoordinates(pane);
                            if (true){
                                return;
                            }
                            for (int i = 0; i<100; ++i){
                                pane.setHappiness(i);
                                Thread.sleep(30);
                            }





                            Pointer p = new Pointer();
                            InstallPane installPane = new InstallPane();
                            WindowManager.setMainContent(installPane);
                            ProcedureRunner runner = new ProcedureRunner(installPane.getListener());
                            runner.setContext(Procedures.SELECTED_DEVICE, new Device("88eec8b5"));
                            /*DeviceRequestParams params = new DeviceRequestParams("whyred_global","8.9.13","8.1.0",Branch.DEVELOPER,"0x1c01702d", 1);
                            HashMap<MiuiRom.Kind, MiuiZipRom> x = MiuiRomOta.otaV3_request(params);
                            Installable installable = null;
                            for (MiuiZipRom rom : x.values()){
                                if (rom != null){
                                    installable = rom;
                                            break;
                                }
                            }*/
                            // = new MiuiZipRom("miui_MI8_8.8.31_b99e4f5263_8.1.zip", new MiuiVersion("8.8.31"), Branch.DEVELOPER, new Codebase("8.1"), "", "ciao", MiuiRom.Kind.LATEST, "");
                            //runner.init(installable);

                            runner.run(StockRecoveryInstall.recoverStuckDevice());
                            //runner.run(StockRecoveryInstall.sendFileViaMTP(Paths.get("F:\\Download\\sdattest\\whyred_images_8.9.6_20180906.0000.00_8.1_cn_efc57c7ee22\\whyred_images_8.9.6_20180906.0000.00_8.1_cn\\images\\system.img")));
                            Log.debug(runner.getContext(StockRecoveryInstall.SELECTED_MTP_DEVICE));
                        } catch (Throwable t){
                            t.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        /*Color color = Color.rgb(255,103, 0);
        Effect effect = new DropShadow(2,1,1, Color.BLACK);
        Circle circle = new Circle(5);

        circle.setFill(color);
        Circle circle2 = new Circle(5);
        circle2.setFill(color);
        Transition transition = buildTransition(circle);

        transition.play();
        Transition transition1 = buildTransition(circle2);
        transition1.setDelay(Duration.millis(150));
        transition1.play();
        Circle circle3 = new Circle(5);
        circle3.setFill(color);
        Transition transition3 = buildTransition(circle3);
        transition3.setDelay(Duration.millis(300));
        transition3.play();
        Circle circle4 = new Circle(5);
        circle4.setFill(color);
        Transition transition4 = buildTransition(circle4);
        transition4.setDelay(Duration.millis(450));
        transition4.play();
        circle.setEffect(effect);
        circle2.setEffect(effect);
        circle3.setEffect(effect);
        circle4.setEffect(effect);*/


        /*Pane drawPane = new Pane();
        BezierInterpolator bezierInterpolator =
        for (double i = 0; i<100; ++i){
            Pane p = new Pane();
            p.setPrefSize(1,100);
            p.setBackground(GuiUtils.backgroundFromColor(Color.CADETBLUE));
            p.setLayoutX(i);
            p.setLayoutY(bezierInterpolator.curve(i/100)*100);
            drawPane.getChildren().add(p);
        }*/

       /* Text text = new Text(Test.LOREM_IPSUM);

        text.setWrappingWidth(200);
        Bounds box = text.getLayoutBounds();
        if (4*box.getHeight() > 3*box.getWidth()){
            text.setWrappingWidth(Math.sqrt(box.getHeight()*box.getWidth()*4/3));
        }
        Log.debug(text.getLayoutBounds());



        PopupWindow popupWindow = new PopupWindow(text.getLayoutBounds().getWidth()+20,text.getLayoutBounds().getHeight()+60);*/
       /*PopupWindow popupWindow = new PopupWindow.ImageTextPopup("This feature is not available yet", PopupWindow.Icon.INFO);
       // popupWindow.setContent(text);
        //WindowManager.launchPopup(popupWindow);
        TableView<DeviceTableEntry> testPane = new TableView<>();
        testPane.setPrefSize(300,200);
        TableColumn<DeviceTableEntry,String> ser = new TableColumn<>("Serial");
        ser.setCellValueFactory(new PropertyValueFactory<DeviceTableEntry,String>("serial"));
        testPane.getColumns().add(ser);
        WindowManager.setMainContent(GuiUtils.center(testPane));
        testPane.getItems().add(new DeviceTableEntry(null,null,null,null,null));*/


        //WindowManager.setMainContent(pane);
        //testPane.setBackground(GuiUtils.backgroundFromColor(Color.RED));


    }



}
