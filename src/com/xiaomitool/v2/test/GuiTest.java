package com.xiaomitool.v2.test;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;

import com.xiaomitool.v2.gui.deviceView.Animatable;
import com.xiaomitool.v2.gui.deviceView.AnimatableDeviceView;
import com.xiaomitool.v2.gui.deviceView.DeviceRecoveryView;
import com.xiaomitool.v2.gui.deviceView.DeviceView;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.*;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

public class GuiTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ToolManager.init(primaryStage);
        String[] texts = new String[]{"Prova1", "Prova2", "Prova3", "Prova4"};
        URL[] imgs = new URL[]{DrawableManager.getPng("usbdbg1"),DrawableManager.getPng("usbdbg2"),DrawableManager.getPng("usbdbg3"),DrawableManager.getPng("usbdbg4")};
        Image[] imgsfx = new Image[imgs.length];
        for (int i = 0; i<imgs.length; ++i){
            imgsfx[i] = new Image(imgs[i].toString(),false);
        }

        Platform.runLater(new Runnable() {
                              @Override
                              public void run() {
                                 /* new Thread(new Runnable() {
                                      @Override
                                      public void run() {
                                          try {
                                              ActionsDynamic.HOW_TO_ENABLE_USB_DEBUGGING(null, false).run();
                                          } catch (InterruptedException e) {
                                              e.printStackTrace();
                                          }
                                      }
                                  }).start();*/
                                  Log.debug("CCASASAS");
                                  /*DeviceRecoveryView deviceRecoveryView = new DeviceRecoveryView(DeviceView.DEVICE_18_9, 400);
                                  WindowManager.setMainContent(deviceRecoveryView);
                                  deviceRecoveryView.selectOption(2);
                                  deviceRecoveryView.animateSelectThird(2000);*/
                                  new Thread(new Runnable() {
                                      @Override
                                      public void run() {
                                          try {
                                              ActionsDynamic.HOWTO_GO_RECOVERY(null).run();
                                          } catch (InterruptedException e) {
                                              e.printStackTrace();
                                          }
                                      }
                                  }).start();


                                 /* DeviceImgInstructionPane imgInstructionPane = new DeviceImgInstructionPane(WindowManager.getContentHeight() + 30, WindowManager.getContentHeight() - 10, texts, imgsfx, null);
                                  WindowManager.setMainContent(imgInstructionPane);
                                  try {
                                      imgInstructionPane.animate();
                                  } catch (InterruptedException e) {
                                      e.printStackTrace();
                                  }
                              }*/
                              }
                          });
                /*SidePane sidePane = new SidePane();
                ButtonPane buttonPane = new ButtonPane("Prev","Next");
                AnimatableDeviceView deviceView = new AnimatableDeviceView(DeviceView.DEVICE_18_9, WindowManager.getContentHeight()+30) {
                    @Override
                    public boolean animate(int step) throws InterruptedException {
                        switch (step){
                            case 0:
                                this.buildCircleTransition(666,1330,-1);
                            default:
                                return false;
                        }

                    }
                };

                sidePane.setRight(GuiUtils.center(DeviceView.crop(deviceView,WindowManager.getContentHeight()-10, 20)));
                Text text = new Text(texts[0]);
                text.setWrappingWidth(400);
                text.setFont(Font.font(15));
                text.setTextAlignment(TextAlignment.CENTER);
                buttonPane.setContent(text);
                StackPane stackPane = new StackPane(buttonPane);
                stackPane.setPadding(new Insets(20,0,40,20));
                sidePane.setLeft(stackPane);
                deviceView.setContent(imgs[0]);
                deviceView.setLenOverlay(WindowManager.requireOverlayPane(), 200, 0.5);
                WindowManager.setMainContent(sidePane);
                try {
                    deviceView.animate(0);
                }catch (Exception e){

                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int click = buttonPane.waitClick();
                            if (click == 1){

                            }

                        } catch (Exception e){

                        }
                    }
                })
            }
        });*/


        /*Pane pane = new StackPane();
        pane.setBackground(WindowManager.DEFAULT_BACKGROUND);
        pane.setPrefSize(1000,700);

        //pane.getChildren().add(GuiUtils.center(GuiUtils.debug(new DragAndDropPane(600,600))));
        pane.getChildren().add(GuiUtils.center(new ChooserPane(new ChooserPane.Choice("THIS IS A TITLE","and here is the subtext",ResourcesManager.b64toImage(ResourcesConst.IMAGE_TEST_B64)), new ChooserPane.Choice("Let's make another to test","with a really beatiful description", ResourcesManager.b64toImage("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")), new ChooserPane.Choice("Let's make another to test","with a really beatiful description, but this one is not only beautiful, but also reallly long. I mean exceptionally long. So long that it doens't fit in the window anymore :) "), new ChooserPane.Choice("Let's make another to test","with a really beatiful description"), new ChooserPane.Choice("Let's make another to test","with a really beatiful description"), new ChooserPane.Choice("Let's make another to test","with a really beatiful description"), new ChooserPane.Choice("Let's make another to test","with a really beatiful description"), new ChooserPane.Choice("Let's make another to test with a longer text","with a really beatiful description"), new ChooserPane.Choice("Let's make another to test","with a really beatiful description"))));
/*
        Label lb = new Label("CCCCCCCCCCCC");

        /*Pane overlay = new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE,null,null)));
        overlay.setPickOnBounds(false);
        ToastPane overlay = new ToastPane(10,2,2,30,1000);

        lb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Log.debug("SSDSA");
                overlay.toast("CIAO");
            }
        });
        pane.getChildren().addAll(lb, overlay);*/
        /*ScrollPane scrollPane = new ScrollPane();
        pane.getChildren().add(scrollPane);
        Text t = new Text(Test.LOREM_IPSUM);
        t.wrappingWidthProperty().bind(scrollPane.widthProperty().subtract(30));

        Log.debug(scrollPane.getLayoutBounds().getWidth());
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(t);*/
        //pane.setBackground(GuiUtils.backgroundFromColor(new LinearGradient(0,0.8,0,1, true, CycleMethod.NO_CYCLE,new Stop(0, Color.TRANSPARENT), new Stop(1,Color.GREEN))));

        /*Image image = new Image(DrawableManager.getPng("device18_9").toString(),300,300,true,true);
        DeviceImage deviceImage = new DeviceImage(image,220,35,1920,1080,2360, 1160, new DeviceView.ButtonPosition(410,1150,10,200),new DeviceView.ButtonPosition(610,1150,10,200),new DeviceView.ButtonPosition(880,1150,10,200));
        DeviceRecoveryView deviceView = new DeviceRecoveryView(DEVICE_18_9, 700);
        //deviceView.animateSelectThird(3000);
        /*setMaxHeight(500);
        setMaxWidth(500);
        prefHeightProperty().bind(pane.heightProperty());
        ImageView imageView = new ImageView(new Image(DrawableManager.getPng("fastboot").toString()));
        deviceView.setContent(imageView);

        //setContent(imageView);
        /*setClickVolumeDown(1);
        setClickPower(1);*/
        /*StackPane anchorPane = new StackPane(deviceView);
        anchorPane.setBackground(GuiUtils.backgroundFromColor(Color.RED));
        anchorPane.setMaxSize(500,500);

        //overlay.setBackground(GuiUtils.backgroundFromColor(Color.RED));
       // setLenOverlay(overlay,200, 0.5);
        HBox hBox = new HBox(deviceView, new DeviceRecoveryView(DEVICE_18_9, 700));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER );
        deviceView.setClickPower(Transition.INDEFINITE);
        pane.getChildren().addAll(hBox);*/


        //pane.getChildren().add(rectangle);
        //pane.setMaxSize(500,500);

       /* DeviceView deviceView = new DeviceView(DeviceView.DEVICE_18_9, 900);
        ImageView image = new ImageView(DrawableManager.getPng(DrawableManager.DEVICE_AUTH).toString());
        image.setViewport(new Rectangle2D(0,image.getImage().getHeight()-2160,1080,2160));
        deviceView.setContent(image);
        deviceView.buildCircleTransition(800,2060,Animation.INDEFINITE);

    Pane pane1 = GuiUtils.crop(deviceView,0,550,500, 350);
    pane1.setPrefSize(-1,400);
*/
      /* TextStackPane textStackPane = new TextStackPane(300,300);

      // GuiUtils.debug(textStackPane);


        RegularTable table = new RegularTable(4,2,200,400);
        GuiUtils.debug(table);
        table.add(new Text("Caio"),0,0, Pos.CENTER_RIGHT);
        pane.getChildren().add(GuiUtils.center(table));
        textStackPane.addText("CAIO");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textStackPane.addText("CAIO2");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                textStackPane.addText("CAIO2\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            }
        }).start();*/

      /*  Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();*/

    }

    public static class RegularTable extends GridPane {
        public RegularTable(int rows, int columns, double height, double width){
            super();
            double rowHeight = height/rows;
            double columnWidth = width/columns;
            super.setPrefSize(width,height);
            RowConstraints[] rowConstraints = new RowConstraints[rows];
            RowConstraints c = new RowConstraints(rowHeight);
            c.setVgrow(Priority.ALWAYS);
            for (int i = 0; i<rows; ++i){
                rowConstraints[i] = c;
            }
            super.getRowConstraints().addAll(rowConstraints);
            ColumnConstraints[] columnConstraints = new ColumnConstraints[columns];
            ColumnConstraints c1 = new ColumnConstraints(columnWidth);
            c1.setHgrow(Priority.ALWAYS);
            for (int i = 0; i<columns; ++i){
                columnConstraints[i] = c1;
            }
            super.getColumnConstraints().addAll(columnConstraints);
        }
        @Override
        public void add(Node node, int columnIndex, int rowIndex){
            add(node,columnIndex,rowIndex,Pos.CENTER);
        }
        public void add(Node node, int columnIndex, int rowIndex,Pos position){
            StackPane stackPane = new StackPane(node);
            stackPane.setAlignment(position);
            super.add(stackPane,columnIndex,rowIndex);
        }
    }



    /*public static class DeviceView extends StackPane {
        private com.xiaomitool.v2.test.GuiTest.DeviceView deviceView;
        private StackPane contentPane;
        private ImageView imageView;
        public DeviceView(com.xiaomitool.v2.test.GuiTest.DeviceView deviceImage){
            this.deviceView = deviceImage;
            build();
        }
        private void build(){
            imageView = new ImageView(getDeviceImage());
            imageView.fitHeightProperty().bind(super.heightProperty());
            imageView.setPreserveRatio(true);
            imageView.setPickOnBounds(false);
            imageView.setMouseTransparent(true);

            contentPane = new StackPane();
            contentPane.setAlignment(Pos.TOP_LEFT);
            contentPane.setBackground(GuiUtils.backgroundFromColor(Color.RED));

            contentPane.paddingProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(new Callable<Insets>() {
                @Override
                public Insets call() throws Exception {

                    Insets insets = new Insets(imageView.getBoundsInParent().getHeight()* getTopOffset()/ getDeviceImage().getHeight(),0,0,imageView.getBoundsInParent().getWidth()* getLeftOffset()/ getDeviceImage().getWidth());
                    Log.debug("Setting padding to: "+insets.toString()+", "+imageView.getBoundsInParent().getHeight());
                    return insets;
                }
            }, super.heightProperty()));
            super.getChildren().addAll(contentPane, imageView);
        }
        public void setContent(ImageView image){
            image.fitHeightProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(new Callable<Number>() {
                @Override
                public Number call() throws Exception {
                    return imageView.getFitHeight()* getHeight()/ getDeviceImage().getHeight();
                }
            }, contentPane.heightProperty()));
            image.setPreserveRatio(true);
            contentPane.getChildren().add(image);
        }
    }*/







    public static class CircleAnimation extends  StackPane {
        private FadeTransition[] transitions;
        public CircleAnimation(double width, double height){
            this(width,height,Color.gray(0.1));
        }
        public CircleAnimation(double width, double height, Paint paint){
            super();
            Pane pane = new Pane();
            pane.setPrefSize(width, height);
            super.getChildren().add(pane);
            double radius = width/14;
            transitions = new FadeTransition[3];
            for (int i = 0; i<3; ++i){
                Circle circle =  new Circle(radius);
                circle.setCenterX((3+4*i)*radius);
                circle.setCenterY(height/2);
                circle.setFill(paint);
                circle.setOpacity(0.3);
                pane.getChildren().add(circle);
                FadeTransition transition = new FadeTransition(Duration.millis(800), circle);
                transition.setFromValue(0.3);
                transition.setToValue(1.0);
                transition.setAutoReverse(true);
                transition.setCycleCount(Transition.INDEFINITE);
                transition.setDelay(Duration.millis(200*i));
                transition.setInterpolator(new Interpolator() {
                    @Override
                    protected double curve(double t) {

                        return t < 0.5 ? 0 : Math.pow((t-0.5)/0.5,3);
                    }
                });
                transitions[i] = transition;
            }
        }
        public CircleAnimation play(){
            for (Transition t : transitions){
                t.play();
            }
            return this;
        }

    }
}
