package game.gui.util;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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

    public enum Style { INFO, ERROR, CONFIRM }

    private static void show(StackPane host, String title, String body,
                             String yes, String no, Runnable onYes, Runnable onNo, Style style) {
        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 10, 0.55), CornerRadii.EMPTY, Insets.EMPTY)));
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
                Color.web("#0a0e20", 0.97), new CornerRadii(16), Insets.EMPTY)));
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

    private static String toWeb(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
