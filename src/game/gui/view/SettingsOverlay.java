package game.gui.view;

import game.gui.util.SoundManager;
import game.gui.util.ThemedAlert;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SettingsOverlay {

    private final StackPane host;
    private final Runnable onExitToMenu;
    private final Runnable onRestart;
    private StackPane panel;

    public SettingsOverlay(StackPane host, Runnable onExitToMenu, Runnable onRestart) {
        this.host = host;
        this.onExitToMenu = onExitToMenu;
        this.onRestart = onRestart;
    }

    public Group buildGearButton() {
        Group g = new Group();
        Circle ring = new Circle(20);
        ring.setFill(Color.web("#0e1428", 0.85));
        ring.setStroke(Color.web("#6a8aff"));
        ring.setStrokeWidth(2);
        ring.setEffect(new DropShadow(8, Color.web("#000")));

        Text gear = new Text("⚙");
        gear.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        gear.setFill(Color.web("#ffd84d"));
        gear.setEffect(new Glow(0.4));
        gear.setX(-8);
        gear.setY(8);

        g.getChildren().addAll(ring, gear);
        g.setOnMouseEntered(e -> {
            ring.setStroke(Color.web("#ffd84d"));
            gear.setRotate(gear.getRotate() + 45);
        });
        g.setOnMouseExited(e -> ring.setStroke(Color.web("#6a8aff")));
        g.setOnMouseClicked(e -> show());
        g.setStyle("-fx-cursor: hand;");
        return g;
    }

    public void show() {
        if (panel != null) return;

        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        veil.setPickOnBounds(true);
        veil.setOnMouseClicked(e -> {
            if (e.getTarget() == veil) hide(veil);
        });

        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(440);
        card.setMaxHeight(480);
        card.setBackground(new Background(new BackgroundFill(
                ThemedAlert.cardGradient(), new CornerRadii(16), Insets.EMPTY)));
        card.setStyle("-fx-border-color: #6a8aff;"
                + "-fx-border-width: 2.5;"
                + "-fx-border-radius: 16;"
                + "-fx-background-radius: 16;");
        card.setEffect(new DropShadow(24, Color.web("#6a8aff66")));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("Settings");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 30));
        title.setFill(Color.web("#ffd84d"));
        title.setEffect(new DropShadow(6, Color.BLACK));

        Group closeBtn = closeIcon(() -> hide(veil));
        HBox.setHgrow(title, javafx.scene.layout.Priority.ALWAYS);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, closeBtn);

        Label musicLbl = sectionLabel("Background Music Volume");
        Slider musicSlider = makeSlider();
        musicSlider.valueProperty().bindBidirectional(SoundManager.get().musicVolumeProperty());

        Label sfxLbl = sectionLabel("Game Sounds Volume");
        Slider sfxSlider = makeSlider();
        sfxSlider.valueProperty().bindBidirectional(SoundManager.get().sfxVolumeProperty());

        Button restart = ThemedAlert.themedButton("Restart Game", true);
        restart.setPrefSize(360, 44);
        restart.setOnAction(e -> ThemedAlert.confirm(host,
                "Restart Game",
                "Are you sure you want to restart the game from the beginning? Current progress will be lost.",
                "Restart", "Cancel",
                () -> {
                    hide(veil);
                    if (onRestart != null) onRestart.run();
                },
                null));

        Button exit = ThemedAlert.themedButton("Exit to Main Menu", false);
        exit.setPrefSize(360, 44);
        exit.setOnAction(e -> ThemedAlert.confirm(host,
                "Exit to Main Menu",
                "Are you sure you want to leave the game? Your progress will be lost.",
                "Exit", "Cancel",
                () -> {
                    hide(veil);
                    if (onExitToMenu != null) onExitToMenu.run();
                },
                null));

        card.getChildren().addAll(header, musicLbl, musicSlider,
                sfxLbl, sfxSlider, restart, exit);

        veil.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        host.getChildren().add(veil);
        panel = veil;

        FadeTransition fade = new FadeTransition(Duration.millis(220), veil);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void hide(StackPane veil) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), veil);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            host.getChildren().remove(veil);
            panel = null;
        });
        fade.play();
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        l.setTextFill(Color.web("#cfd8ff"));
        return l;
    }

    private Slider makeSlider() {
        Slider s = new Slider(0, 1, 0.5);
        s.setBlockIncrement(0.05);
        s.setShowTickMarks(true);
        s.setShowTickLabels(true);
        s.setMajorTickUnit(0.25);
        s.setStyle("-fx-control-inner-background: #1a2240;");
        return s;
    }

    private Group closeIcon(Runnable onClose) {
        Group g = new Group();
        Circle bg = new Circle(16);
        bg.setFill(Color.web("#3a1018", 0.9));
        bg.setStroke(Color.web("#ff5e1a"));
        bg.setStrokeWidth(2);
        Line a = new Line(-7, -7, 7, 7);
        Line b = new Line(-7, 7, 7, -7);
        a.setStroke(Color.web("#ffe0c0"));
        b.setStroke(Color.web("#ffe0c0"));
        a.setStrokeWidth(2.5);
        b.setStrokeWidth(2.5);
        g.getChildren().addAll(bg, a, b);
        g.setOnMouseEntered(e -> bg.setStroke(Color.web("#ffd84d")));
        g.setOnMouseExited(e -> bg.setStroke(Color.web("#ff5e1a")));
        g.setOnMouseClicked(e -> onClose.run());
        g.setStyle("-fx-cursor: hand;");
        return g;
    }
}
