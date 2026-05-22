package game.gui.view;

import game.engine.Role;
import game.gui.util.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class EndSequence {

    private static final String CREDITS =
            "A Katakito Schocolade production.\n\n"
                    + "Team members:\n"
                    + "Carlos Emad\n"
                    + "Karim Tarek\n"
                    + "Omar Ahmed\n"
                    + "Mohamed Mostafa\n\n"
                    + "Under the supervision of:\n"
                    + "Dr. Prof. Slim Abdennadher\n"
                    + "Dr. Nada Farid\n\n"
                    + "Press ESC to return to main menu";

    public static void play(StackPane host, Role winnerRole, String winnerName,
                            Runnable onReturnToMenu) {
        SoundManager.get().stopAll();
        SoundManager.get().playSfx(
                winnerRole == Role.SCARER
                        ? SoundManager.Sfx.SCARER_WIN
                        : SoundManager.Sfx.LAUGHER_WIN);

        Rectangle blackout = new Rectangle();
        blackout.widthProperty().bind(host.widthProperty());
        blackout.heightProperty().bind(host.heightProperty());
        blackout.setFill(Color.BLACK);
        blackout.setOpacity(0);
        host.getChildren().add(blackout);

        // Gradual dim during winner audio (~4-6s)
        Timeline dim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blackout.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(5.5), new KeyValue(blackout.opacityProperty(), 1)));

        Text winText = new Text(winnerRole == Role.SCARER ? "SCARERS WIN" : "LAUGHERS WIN");
        winText.setFont(Font.font("Impact", FontWeight.BOLD, 96));
        winText.setFill(winnerRole == Role.SCARER
                ? Color.web("#ff4a4a") : Color.web("#ffd84d"));
        winText.setEffect(new DropShadow(40, winnerRole == Role.SCARER
                ? Color.web("#ff5e1a") : Color.web("#ffd84d")));
        winText.setOpacity(0);
        winText.setTextAlignment(TextAlignment.CENTER);

        StackPane winContainer = new StackPane(winText);
        host.getChildren().add(winContainer);

        Text subWin = new Text("Champion: " + winnerName);
        subWin.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        subWin.setFill(Color.web("#ffffff"));
        subWin.setEffect(new DropShadow(8, Color.BLACK));
        subWin.setOpacity(0);

        VBox winBlock = new VBox(20, winText, subWin);
        winBlock.setAlignment(Pos.CENTER);
        winContainer.getChildren().setAll(winBlock);

        PauseTransition afterDim = new PauseTransition(Duration.seconds(5.6));
        afterDim.setOnFinished(e -> {
            SoundManager.get().playSfx(SoundManager.Sfx.END);

            FadeTransition fadeInText = new FadeTransition(Duration.seconds(1.2), winText);
            fadeInText.setFromValue(0);
            fadeInText.setToValue(1);

            FadeTransition fadeInSub = new FadeTransition(Duration.seconds(1.0), subWin);
            fadeInSub.setFromValue(0);
            fadeInSub.setToValue(1);
            fadeInSub.setDelay(Duration.millis(400));

            PauseTransition hold = new PauseTransition(Duration.seconds(3.2));

            FadeTransition fadeOutText = new FadeTransition(Duration.seconds(1.5), winText);
            fadeOutText.setFromValue(1);
            fadeOutText.setToValue(0);
            FadeTransition fadeOutSub = new FadeTransition(Duration.seconds(1.5), subWin);
            fadeOutSub.setFromValue(1);
            fadeOutSub.setToValue(0);

            SequentialTransition seq = new SequentialTransition(
                    fadeInText, hold, fadeOutText);
            fadeInSub.play();
            fadeOutSub.setDelay(Duration.seconds(4.4));
            fadeOutSub.play();
            seq.setOnFinished(ev -> {
                host.getChildren().remove(winContainer);
                rollCredits(host, onReturnToMenu);
            });
            seq.play();
        });

        dim.play();
        afterDim.play();
    }

    private static void rollCredits(StackPane host, Runnable onReturnToMenu) {
        Text credits = new Text(CREDITS);
        credits.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        credits.setFill(Color.web("#f4f7ff"));
        credits.setEffect(new DropShadow(6, Color.BLACK));
        credits.setTextAlignment(TextAlignment.CENTER);
        credits.setWrappingWidth(800);

        VBox container = new VBox(credits);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(40));

        StackPane wrap = new StackPane(container);
        wrap.setAlignment(Pos.BOTTOM_CENTER);

        host.getChildren().add(wrap);

        double textHeight = credits.getLayoutBounds().getHeight() + 80;
        double sceneHeight = host.getHeight() > 0 ? host.getHeight() : 800;

        container.setTranslateY(sceneHeight);
        TranslateTransition scroll = new TranslateTransition(
                Duration.seconds(28), container);
        scroll.setToY(-textHeight);
        scroll.play();

        host.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                scroll.stop();
                host.getChildren().remove(wrap);
                if (onReturnToMenu != null) onReturnToMenu.run();
            }
        });
        host.requestFocus();
    }
}
