package game.gui.view;

import game.engine.cards.Card;
import game.engine.cards.ConfusionCard;
import game.engine.cards.EnergyStealCard;
import game.engine.cards.ShieldCard;
import game.engine.cards.StartOverCard;
import game.engine.cards.SwapperCard;
import game.gui.util.SoundManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class CardDeckView extends Pane {

    public static final double CARD_W = 100;
    public static final double CARD_H = 150;
    public static final double PORTRAIT_W = 220;
    public static final double PORTRAIT_H = 330;

    private final Group deckStack = new Group();
    private Pane blurTarget;
    private final BoxBlur blurEffect = new BoxBlur(0, 0, 2);

    public CardDeckView() {
        setPrefSize(260, 360);
        setMinSize(260, 360);
        setMaxSize(260, 360);

        Text label = new Text("DECK");
        label.setFont(Font.font("Impact", FontWeight.BOLD, 22));
        label.setFill(Color.web("#ffd84d"));
        label.setEffect(new DropShadow(6, Color.BLACK));
        label.setLayoutX(110);
        label.setLayoutY(28);

        getChildren().addAll(label, deckStack);
        rebuildDeckStack();
    }

    public void setBlurTarget(Pane target) {
        this.blurTarget = target;
    }

    private void rebuildDeckStack() {
        deckStack.getChildren().clear();
        for (int i = 0; i < 6; i++) {
            Group card = buildBackCard(null, i % 5);
            card.setTranslateX(75 + i * 2);
            card.setTranslateY(80 + (5 - i) * 3);
            card.setRotate(-3 + i * 1.2);
            deckStack.getChildren().add(card);
        }
    }

    private Group buildBackCard(Card sample, int colorIdx) {
        Group g = new Group();
        Rectangle r = new Rectangle(CARD_W, CARD_H);
        r.setArcWidth(12);
        r.setArcHeight(12);
        Color top, bot, accent;
        String iconText;
        if (sample instanceof ShieldCard) {
            top = Color.web("#3a90d0"); bot = Color.web("#0a2a58"); accent = Color.web("#cfe8ff"); iconText = "⛨";
        } else if (sample instanceof SwapperCard) {
            top = Color.web("#40c070"); bot = Color.web("#0a4020"); accent = Color.web("#d0ffe0"); iconText = "⇄";
        } else if (sample instanceof EnergyStealCard) {
            top = Color.web("#c83040"); bot = Color.web("#400a14"); accent = Color.web("#ffd8d8"); iconText = "⚡";
        } else if (sample instanceof StartOverCard) {
            top = Color.web("#e8a020"); bot = Color.web("#603810"); accent = Color.web("#fff0c0"); iconText = "↺";
        } else if (sample instanceof ConfusionCard) {
            top = Color.web("#8040c0"); bot = Color.web("#28104a"); accent = Color.web("#e8d0ff"); iconText = "✺";
        } else {
            Color[] tops = {Color.web("#3a90d0"), Color.web("#40c070"), Color.web("#c83040"),
                            Color.web("#e8a020"), Color.web("#8040c0")};
            Color[] bots = {Color.web("#0a2a58"), Color.web("#0a4020"), Color.web("#400a14"),
                            Color.web("#603810"), Color.web("#28104a")};
            top = tops[colorIdx]; bot = bots[colorIdx]; accent = Color.web("#fff7c8");
            iconText = "?";
        }
        r.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, top), new Stop(1, bot)));
        r.setStroke(Color.web("#0a0a14"));
        r.setStrokeWidth(2);
        r.setEffect(new DropShadow(10, Color.web("#000a")));

        Rectangle inner = new Rectangle(8, 8, CARD_W - 16, CARD_H - 16);
        inner.setArcWidth(8);
        inner.setArcHeight(8);
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(accent);
        inner.setStrokeWidth(1.2);

        Text icon = new Text(iconText);
        icon.setFont(Font.font("Verdana", FontWeight.BOLD, 38));
        icon.setFill(accent);
        icon.setEffect(new Glow(0.5));
        icon.setLayoutX(CARD_W / 2 - icon.getBoundsInLocal().getWidth() / 2);
        icon.setLayoutY(CARD_H / 2 + 14);

        Text dd = new Text("DD");
        dd.setFont(Font.font("Impact", FontWeight.BOLD, 12));
        dd.setFill(accent);
        dd.setLayoutX(10);
        dd.setLayoutY(20);
        Text dd2 = new Text("DD");
        dd2.setFont(Font.font("Impact", FontWeight.BOLD, 12));
        dd2.setFill(accent);
        dd2.setLayoutX(CARD_W - 26);
        dd2.setLayoutY(CARD_H - 8);

        g.getChildren().addAll(r, inner, icon, dd, dd2);
        return g;
    }

    private Group buildFrontCard(Card card) {
        Group g = new Group();
        Color top, bot, accent, text;
        String typeName;
        String iconText;
        text = Color.web("#ffffff");
        if (card instanceof ShieldCard) {
            top = Color.web("#5ab0e8"); bot = Color.web("#1a4a8a"); accent = Color.web("#cfe8ff");
            typeName = "SHIELD"; iconText = "⛨";
        } else if (card instanceof SwapperCard) {
            top = Color.web("#40c070"); bot = Color.web("#0a4020"); accent = Color.web("#d0ffe0");
            typeName = "SWAPPER"; iconText = "⇄";
        } else if (card instanceof EnergyStealCard) {
            top = Color.web("#ec5060"); bot = Color.web("#601020"); accent = Color.web("#ffd8d0");
            typeName = "ENERGY STEAL"; iconText = "⚡";
        } else if (card instanceof StartOverCard) {
            top = Color.web("#e8a020"); bot = Color.web("#603810"); accent = Color.web("#fff0c0");
            typeName = "START OVER"; iconText = "↺";
        } else if (card instanceof ConfusionCard) {
            top = Color.web("#a060d8"); bot = Color.web("#3a1860"); accent = Color.web("#f0d8ff");
            typeName = "CONFUSION"; iconText = "✺";
        } else {
            top = Color.web("#cccccc"); bot = Color.web("#333333"); accent = Color.web("#ffffff");
            typeName = "CARD"; iconText = "?";
        }

        Rectangle r = new Rectangle(PORTRAIT_W, PORTRAIT_H);
        r.setArcWidth(18);
        r.setArcHeight(18);
        r.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, top), new Stop(1, bot)));
        r.setStroke(Color.web("#0a0a14"));
        r.setStrokeWidth(2.5);
        r.setEffect(new DropShadow(22, Color.web("#000d")));

        Rectangle bevel = new Rectangle(10, 10, PORTRAIT_W - 20, PORTRAIT_H - 20);
        bevel.setArcWidth(12);
        bevel.setArcHeight(12);
        bevel.setFill(Color.TRANSPARENT);
        bevel.setStroke(accent);
        bevel.setStrokeWidth(1.5);

        Text type = new Text(typeName);
        type.setFont(Font.font("Impact", FontWeight.BOLD, 22));
        type.setFill(text);
        type.setEffect(new DropShadow(4, Color.web("#0006")));
        type.setLayoutX(PORTRAIT_W / 2 - type.getBoundsInLocal().getWidth() / 2);
        type.setLayoutY(40);

        Text icon = new Text(iconText);
        icon.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
        icon.setFill(text);
        icon.setEffect(new Glow(0.55));
        icon.setLayoutX(PORTRAIT_W / 2 - icon.getBoundsInLocal().getWidth() / 2);
        icon.setLayoutY(150);

        Text name = new Text(card != null ? card.getName() : "");
        name.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        name.setFill(text);
        name.setEffect(new DropShadow(5, Color.web("#000c")));
        name.setLayoutX(PORTRAIT_W / 2 - name.getBoundsInLocal().getWidth() / 2);
        name.setLayoutY(220);

        Text desc = new Text(card != null ? card.getDescription() : "");
        desc.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        desc.setFill(text);
        desc.setEffect(new DropShadow(4, Color.web("#000c")));
        desc.setWrappingWidth(PORTRAIT_W - 36);
        desc.setTextAlignment(TextAlignment.CENTER);
        desc.setLayoutX(18);
        desc.setLayoutY(250);

        g.getChildren().addAll(r, bevel, type, icon, name, desc);
        return g;
    }

    public void animateShuffle(Runnable onDone) {
        SoundManager.get().playSfx(SoundManager.Sfx.SHUFFLE);
        SequentialTransition seq = new SequentialTransition();
        for (int i = 0; i < deckStack.getChildren().size(); i++) {
            Group card = (Group) deckStack.getChildren().get(i);
            double origX = card.getTranslateX();
            double origY = card.getTranslateY();
            double origR = card.getRotate();
            TranslateTransition tt = new TranslateTransition(Duration.millis(130), card);
            tt.setToX(origX + (i % 2 == 0 ? 30 : -30));
            tt.setToY(origY - 12);
            TranslateTransition back = new TranslateTransition(Duration.millis(130), card);
            back.setToX(origX);
            back.setToY(origY);
            RotateTransition rot = new RotateTransition(Duration.millis(130), card);
            rot.setToAngle(origR + (i % 2 == 0 ? 18 : -18));
            RotateTransition rotBack = new RotateTransition(Duration.millis(130), card);
            rotBack.setToAngle(origR);
            seq.getChildren().addAll(
                    new ParallelTransition(tt, rot),
                    new ParallelTransition(back, rotBack));
        }
        seq.setOnFinished(e -> {
            if (onDone != null) onDone.run();
        });
        seq.play();
    }

    public void animateDrawAndShow(Card card, Runnable onDone) {
        SoundManager.get().playSfx(SoundManager.Sfx.DRAW_CARD);

        Group back = buildBackCard(card, 0);
        back.setTranslateX(75);
        back.setTranslateY(80);
        getChildren().add(back);

        Group front = buildFrontCard(card);
        front.setOpacity(0);
        getChildren().add(front);

        double centerX = getPrefWidth() / 2 - PORTRAIT_W / 2;
        double centerY = -PORTRAIT_H / 2 + 80;

        Timeline rise = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(back.translateXProperty(), back.getTranslateX()),
                        new KeyValue(back.translateYProperty(), back.getTranslateY()),
                        new KeyValue(back.scaleXProperty(), 1.0),
                        new KeyValue(back.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(back.translateXProperty(), centerX + (PORTRAIT_W - CARD_W) / 2),
                        new KeyValue(back.translateYProperty(), centerY + (PORTRAIT_H - CARD_H) / 2),
                        new KeyValue(back.scaleXProperty(), PORTRAIT_W / CARD_W * 0.6),
                        new KeyValue(back.scaleYProperty(), PORTRAIT_H / CARD_H * 0.6)));

        front.setTranslateX(centerX);
        front.setTranslateY(centerY);

        Timeline flip1 = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(back.scaleXProperty(), back.getScaleX())),
                new KeyFrame(Duration.millis(220), new KeyValue(back.scaleXProperty(), 0)));

        Timeline flip2 = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(front.scaleXProperty(), 0),
                        new KeyValue(front.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(220), new KeyValue(front.scaleXProperty(), 1)));

        PauseTransition show = new PauseTransition(Duration.millis(1500));

        TranslateTransition fallOut = new TranslateTransition(Duration.millis(550), front);
        fallOut.setToY(getPrefHeight() + 80);
        RotateTransition spinOut = new RotateTransition(Duration.millis(550), front);
        spinOut.setByAngle(20);
        ScaleTransition fadeShrink = new ScaleTransition(Duration.millis(550), front);
        fadeShrink.setToX(0.7);
        fadeShrink.setToY(0.7);

        ParallelTransition exit = new ParallelTransition(fallOut, spinOut, fadeShrink);

        SequentialTransition full = new SequentialTransition(rise, flip1, flip2, show, exit);
        full.setOnFinished(e -> {
            getChildren().removeAll(back, front);
            if (onDone != null) onDone.run();
        });

        Timeline flipRollback = new Timeline(
                new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.millis(0), new KeyValue(back.opacityProperty(), 1)));
        flip1.setOnFinished(e -> back.setOpacity(0));

        full.play();
    }
}
