package game.gui.util;

import java.util.function.Consumer;

import game.engine.cards.Card;
import game.engine.cards.ConfusionCard;
import game.engine.cards.EnergyStealCard;
import game.engine.cards.ShieldCard;
import game.engine.cards.StartOverCard;
import game.engine.cards.SwapperCard;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public final class ThemedAlert {

    private ThemedAlert() { }

    public static void info(StackPane host, String title, String body, Runnable onOk) {
        show(host, title, body, "OK", null, onOk, null, Style.INFO);
    }

    public static void error(StackPane host, String title, String body, Runnable onOk) {
        show(host, title, body, "OK", null, onOk, null, Style.ERROR);
    }

    public static void confirm(StackPane host, String title, String body,
                               String yes, String no, Runnable onYes, Runnable onNo) {
        show(host, title, body, yes, no, onYes, onNo, Style.CONFIRM);
    }

    public static void prompt(StackPane host, String title, String body,
                              String placeholder, Consumer<String> onSubmit) {
        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        veil.setPickOnBounds(true);

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(520);
        card.setMaxHeight(360);

        Color border = Color.web("#6a8aff");
        card.setBackground(new Background(new BackgroundFill(
                cardGradient(), new CornerRadii(16), Insets.EMPTY)));
        card.setStyle(
                "-fx-border-color: " + toWeb(border) + ";"
                + "-fx-border-width: 2.5;"
                + "-fx-border-radius: 16;"
                + "-fx-background-radius: 16;");
        card.setEffect(new DropShadow(28, border));

        Text t = new Text(title);
        t.setFont(Font.font("Impact", FontWeight.BOLD, 30));
        t.setFill(Color.web("#ffd84d"));
        t.setEffect(new DropShadow(8, Color.web("#000")));

        Label msg = new Label(body);
        msg.setWrapText(true);
        msg.setFont(Font.font("Verdana", 14));
        msg.setTextFill(Color.web("#f4f7ff"));
        msg.setMaxWidth(460);

        TextField input = new TextField();
        input.setPromptText(placeholder == null ? "" : placeholder);
        input.setFont(Font.font("Verdana", 15));
        input.setMaxWidth(360);
        input.setStyle(
                "-fx-background-color: #131a30;"
                + "-fx-text-fill: #ffffff;"
                + "-fx-prompt-text-fill: #6a7099;"
                + "-fx-border-color: #2a3866;"
                + "-fx-border-width: 1.5;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 8 12;");

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        Button okBtn = themedButton("OK", true);
        Button cancelBtn = themedButton("Cancel", false);

        Runnable submit = () -> {
            String value = input.getText() == null ? "" : input.getText().trim();
            dismiss(host, veil, () -> {
                if (!value.isEmpty()) onSubmit.accept(value);
            });
        };

        okBtn.setOnAction(e -> submit.run());
        cancelBtn.setOnAction(e -> dismiss(host, veil, null));
        input.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) submit.run();
            else if (e.getCode() == KeyCode.ESCAPE) dismiss(host, veil, null);
            e.consume();
        });

        buttons.getChildren().addAll(okBtn, cancelBtn);

        card.getChildren().addAll(t, msg, input, buttons);
        veil.getChildren().add(card);
        host.getChildren().add(veil);

        FadeTransition fade = new FadeTransition(Duration.millis(220), veil);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setOnFinished(e -> input.requestFocus());
        fade.play();
    }

    public static void card(StackPane host, Card card, Runnable onOk) {
        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        veil.setPickOnBounds(true);

        Color accent;
        String typeName;
        String iconText;
        if (card instanceof ShieldCard) {
            accent = Color.web("#5ab0e8"); typeName = "SHIELD"; iconText = "⛨";
        } else if (card instanceof SwapperCard) {
            accent = Color.web("#60e090"); typeName = "SWAPPER"; iconText = "⇄";
        } else if (card instanceof EnergyStealCard) {
            accent = Color.web("#ec5060"); typeName = "ENERGY STEAL"; iconText = "⚡";
        } else if (card instanceof StartOverCard) {
            accent = Color.web("#ffc040"); typeName = "START OVER"; iconText = "↺";
        } else if (card instanceof ConfusionCard) {
            accent = Color.web("#a060d8"); typeName = "CONFUSION"; iconText = "✺";
        } else {
            accent = Color.web("#cccccc"); typeName = "CARD"; iconText = "?";
        }

        VBox cardBox = new VBox(14);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPadding(new Insets(28, 32, 28, 32));
        cardBox.setMaxWidth(520);
        cardBox.setMaxHeight(560);

        cardBox.setBackground(new Background(new BackgroundFill(
                cardGradient(), new CornerRadii(16), Insets.EMPTY)));
        cardBox.setStyle(
                "-fx-border-color: " + toWeb(accent) + ";"
                + "-fx-border-width: 2.5;"
                + "-fx-border-radius: 16;"
                + "-fx-background-radius: 16;");
        cardBox.setEffect(new DropShadow(28, accent));

        Text type = new Text(typeName);
        type.setFont(Font.font("Impact", FontWeight.BOLD, 32));
        type.setFill(Color.web("#ffd84d"));
        type.setEffect(new DropShadow(6, Color.BLACK));

        Text icon = new Text(iconText);
        icon.setFont(Font.font("Verdana", FontWeight.BOLD, 96));
        icon.setFill(accent);
        icon.setEffect(new Glow(0.6));

        Text name = new Text(card.getName());
        name.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        name.setFill(Color.web("#ffffff"));
        name.setEffect(new DropShadow(5, Color.web("#000c")));

        Label desc = new Label(card.getDescription());
        desc.setWrapText(true);
        desc.setTextAlignment(TextAlignment.CENTER);
        desc.setAlignment(Pos.CENTER);
        desc.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        desc.setTextFill(Color.web("#f4f7ff"));
        desc.setEffect(new DropShadow(4, Color.web("#000c")));
        desc.setMaxWidth(440);

        Button okBtn = themedButton("OK", true);
        okBtn.setOnAction(e -> dismiss(host, veil, onOk));

        HBox buttons = new HBox(okBtn);
        buttons.setAlignment(Pos.CENTER);

        cardBox.getChildren().addAll(type, icon, name, desc, buttons);

        veil.getChildren().add(cardBox);
        host.getChildren().add(veil);

        FadeTransition fade = new FadeTransition(Duration.millis(220), veil);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setOnFinished(e -> okBtn.requestFocus());
        fade.play();
    }

    public enum Style { INFO, ERROR, CONFIRM }

    private static void show(StackPane host, String title, String body,
                             String yes, String no, Runnable onYes, Runnable onNo, Style style) {
        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        veil.setPickOnBounds(true);

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(520);
        card.setMaxHeight(320);

        Color border, glow, titleColor;
        switch (style) {
            case ERROR:
                border = Color.web("#ff5e1a");
                glow = Color.web("#ff5e1a");
                titleColor = Color.web("#ffd84d");
                break;
            case CONFIRM:
                border = Color.web("#00e0ff");
                glow = Color.web("#00b4ff");
                titleColor = Color.web("#ffd84d");
                break;
            default:
                border = Color.web("#6a8aff");
                glow = Color.web("#6a8aff");
                titleColor = Color.web("#ffd84d");
        }

        card.setBackground(new Background(new BackgroundFill(
                cardGradient(), new CornerRadii(16), Insets.EMPTY)));
        card.setStyle(
                "-fx-border-color: " + toWeb(border) + ";"
                + "-fx-border-width: 2.5;"
                + "-fx-border-radius: 16;"
                + "-fx-background-radius: 16;");
        card.setEffect(new DropShadow(28, glow));

        Text t = new Text(title);
        t.setFont(Font.font("Impact", FontWeight.BOLD, 32));
        t.setFill(titleColor);
        t.setEffect(new DropShadow(8, Color.web("#000")));

        Label msg = new Label(body);
        msg.setWrapText(true);
        msg.setFont(Font.font("Verdana", 15));
        msg.setTextFill(Color.web("#f4f7ff"));
        msg.setMaxWidth(460);

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        Button yesBtn = themedButton(yes, true);
        yesBtn.setOnAction(e -> {
            dismiss(host, veil, onYes);
        });
        buttons.getChildren().add(yesBtn);

        if (no != null) {
            Button noBtn = themedButton(no, false);
            noBtn.setOnAction(e -> dismiss(host, veil, onNo));
            buttons.getChildren().add(noBtn);
        }

        card.getChildren().addAll(t, msg, buttons);
        veil.getChildren().add(card);
        host.getChildren().add(veil);

        FadeTransition fade = new FadeTransition(Duration.millis(220), veil);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private static void dismiss(StackPane host, Node veil, Runnable then) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), veil);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            host.getChildren().remove(veil);
            if (then != null) then.run();
        });
        fade.play();
    }

    public static Button themedButton(String text, boolean primary) {
        Button b = new Button(text);
        b.setPrefSize(140, 44);
        b.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        b.setFocusTraversable(false);
        String idle = primary
                ? "-fx-background-color: linear-gradient(to bottom, #2a4a90, #122455);"
                + "-fx-text-fill: #ffffff;"
                + "-fx-border-color: #00e0ff;"
                : "-fx-background-color: linear-gradient(to bottom, #50202a, #281015);"
                + "-fx-text-fill: #ffd0d0;"
                + "-fx-border-color: #ff5e1a;";
        String hover = primary
                ? "-fx-background-color: linear-gradient(to bottom, #3a6abf, #1a347a);"
                + "-fx-text-fill: #ffffff;"
                + "-fx-border-color: #ffffff;"
                : "-fx-background-color: linear-gradient(to bottom, #80303a, #3a181f);"
                + "-fx-text-fill: #ffffff;"
                + "-fx-border-color: #ffd84d;";
        String suffix = "-fx-background-radius: 10;"
                + "-fx-border-radius: 10;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;";
        b.setStyle(idle + suffix);
        b.setOnMouseEntered(e -> b.setStyle(hover + suffix));
        b.setOnMouseExited(e -> b.setStyle(idle + suffix));
        return b;
    }

    public static LinearGradient cardGradient() {
        return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#141a34")),
                new Stop(1, Color.web("#070a18")));
    }

    private static String toWeb(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
