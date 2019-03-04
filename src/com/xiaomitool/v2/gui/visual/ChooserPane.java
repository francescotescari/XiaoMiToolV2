package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class ChooserPane extends StackPane {

    private static final Background CHOICE_BG =  GuiUtils.backgroundFromColor(Color.web("#ffd1b3"));
    private static final Background CHOICE_BG_HOVER = GuiUtils.backgroundFromColor(Color.web("#ffb380"));
    private static final DropShadow CHOICE_DROPSHADOW = new DropShadow(10,Color.gray(0.9));
    private static final DropShadow CHOICE_DROPSHADOW_HOVER = new DropShadow(10,Color.gray(0.7));
    private IDClickReceiver idClickReceiver = new IDClickReceiver();

    public IDClickReceiver getIdClickReceiver(){
        return idClickReceiver;
    }
    private double maxChoicesWidth  = WindowManager.getContentWidth()-100;


    public static class Choice{
        private String title, subtext;
        private Image image;

        public Image getImage() {
            return image;
        }

        public Choice(String title, String subtext){
            if (title == null){
                title = "";
            }
            if (subtext == null){
                subtext = "";
            }
            this.title = title;
            this.subtext = subtext;
        }

        public Choice(String title, String subtext, Image image){
            this(title,subtext);
            this.image = image;

        }

        @Override
        public String toString(){
            return "Choice: "+this.title+" - "+this.subtext;
        }

        public String getSubtext() {
            return subtext;
        }

        public String getTitle() {
            return title;
        }
    }
    public ChooserPane(Choice... choices){
        this.choices = choices;
        build();
    }


    private Choice[] choices;
    private void build(){
        VBox vBox = new VBox();
       // vBox.setPrefSize(300,300);
        vBox.setPadding(new Insets(20));
       // GuiUtils.debug(vBox, Color.YELLOW);
        ObservableList<Node> children = vBox.getChildren();
        for (Choice choice : choices){
            children.add(buildChoice(choice));
        }
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setContent(GuiUtils.center(vBox));
        scrollPane.setPrefWidth(5000);
        scrollPane.setPrefViewportWidth(5000);
        super.setPrefWidth(5000);
        //GuiUtils.debug(this);

        scrollPane.setFitToWidth(true);

        /*scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);*/
        scrollPane.setStyle("-fx-background: rgba(245,245,245,0); -fx-background-color: rgba(245,245,245,0);");
        super.getChildren().add(scrollPane);

    }

    private Node buildChoice(Choice choice){


        Pane iconPane = new Pane();
        Pane textPane = new VBox();
        ((VBox) textPane).setAlignment(Pos.CENTER_LEFT);
        //iconPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null,BorderStroke.MEDIUM)));
        iconPane.setPrefSize(50,50);
        HBox hBox = new HBox(iconPane, textPane);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10);
        Text title = new Text(choice.getTitle());
        title.setFont(Font.font(null, FontWeight.SEMI_BOLD, 18));
        Text subtext = new Text(choice.getSubtext());
        subtext.setFont(Font.font(14));
        Image image = choice.getImage();
        if (image!=null){
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
            iconPane.getChildren().add(imageView);
        }
        double titWidth = title.getLayoutBounds().getWidth(), subWidth = subtext.getLayoutBounds().getWidth();
        if (titWidth > maxChoicesWidth || subWidth > maxChoicesWidth){
            title.setWrappingWidth(maxChoicesWidth);
            subtext.setWrappingWidth(maxChoicesWidth);
        }
        hBox.setMinWidth(230);
        textPane.getChildren().addAll(title,subtext);
        hBox.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hBox.setBackground(CHOICE_BG_HOVER);
                hBox.setEffect(CHOICE_DROPSHADOW_HOVER);
            }
        });
        hBox.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hBox.setBackground(CHOICE_BG);
                hBox.setEffect(CHOICE_DROPSHADOW);
            }
        });
        hBox.setCursor(Cursor.HAND);
        hBox.setPadding(new Insets(10));
        hBox.setBackground(CHOICE_BG);
        hBox.setEffect(CHOICE_DROPSHADOW);
        this.idClickReceiver.addNode(hBox);
        return hBox;
    }
}
