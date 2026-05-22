package game.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MainMenu extends Application {

    private static final String MENU_IMAGE = "menu.png";
    private static final String MENU_MUSIC_DIR = "Menu";
    private static final double SCENE_W = 1200;
    private static final double SCENE_H = 800;

    private MediaPlayer musicPlayer;
    private final List<String> playlist = new ArrayList<>();
    private int trackIndex = 0;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        ImageView background = buildBackground();
        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 20, 0.35), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox content = buildContent(stage);

        root.getChildren().addAll(background, veil, content);

        Scene scene = new Scene(root, SCENE_W, SCENE_H);
        if (background.getImage() != null) {
            background.fitWidthProperty().bind(scene.widthProperty());
            background.fitHeightProperty().bind(scene.heightProperty());
        }

        stage.setScene(scene);
        stage.setTitle("DoorDasH: Scare vs Laugh Touchdown");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        fadeIn(content);
        startPlaylist();
    }

    private ImageView buildBackground() {
        ImageView view = new ImageView();
        File f = resolveResource("png files", MENU_IMAGE);
        if (f != null && f.exists()) {
            view.setImage(new Image(f.toURI().toString()));
        }
        view.setPreserveRatio(false);
        view.setSmooth(true);
        return view;
    }

    private VBox buildContent(Stage stage) {
        Text title = new Text("DoorDasH");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 92));
        title.setFill(Color.web("#ffd84d"));
        DropShadow titleShadow = new DropShadow(18, Color.web("#ff5e1a"));
        titleShadow.setSpread(0.35);
        title.setEffect(titleShadow);

        Text subtitle = new Text("Scare vs Laugh — Touchdown");
        subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        subtitle.setFill(Color.web("#e8f1ff"));
        subtitle.setEffect(new DropShadow(8, Color.web("#001428")));

        Button solo = themedButton("Solo Game");
        solo.setOnAction(e -> startSoloGame(stage));

        Button multi = themedButton("Multiplayer Game");
        multi.setOnAction(e -> startMultiplayerGame(stage));

        Button how = themedButton("How to Play");
        how.setOnAction(e -> showHowToPlay(stage));

        Button quit = themedButton("Quit");
        quit.setOnAction(e -> confirmQuit(stage));

        VBox buttons = new VBox(18, solo, multi, how, quit);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, title, subtitle, buttons);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 60, 0));
        return box;
    }

    private Button themedButton(String label) {
        Button b = new Button(label);
        b.setPrefSize(340, 64);
        b.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        b.setFocusTraversable(false);
        b.setStyle(idleStyle());

        DropShadow glow = new DropShadow(0, Color.web("#00e0ff"));
        glow.setSpread(0.4);
        Glow extraGlow = new Glow(0);
        glow.setInput(extraGlow);
        b.setEffect(glow);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(180), b);
        scaleIn.setToX(1.08);
        scaleIn.setToY(1.08);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(180), b);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        Timeline glowIn = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(glow.radiusProperty(), 28),
                        new KeyValue(extraGlow.levelProperty(), 0.55)));

        Timeline glowOut = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(glow.radiusProperty(), 0),
                        new KeyValue(extraGlow.levelProperty(), 0)));

        TranslateTransition nudge = new TranslateTransition(Duration.millis(120), b);
        nudge.setFromX(0);
        nudge.setByX(6);
        nudge.setAutoReverse(true);
        nudge.setCycleCount(2);

        b.setOnMouseEntered(e -> {
            b.setStyle(hoverStyle());
            scaleOut.stop();
            glowOut.stop();
            scaleIn.playFromStart();
            glowIn.playFromStart();
            nudge.playFromStart();
        });
        b.setOnMouseExited(e -> {
            b.setStyle(idleStyle());
            scaleIn.stop();
            glowIn.stop();
            scaleOut.playFromStart();
            glowOut.playFromStart();
        });
        b.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(70), b);
            press.setToX(1.02);
            press.setToY(1.02);
            press.play();
        });
        b.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(90), b);
            release.setToX(1.08);
            release.setToY(1.08);
            release.play();
        });
        return b;
    }

    private String idleStyle() {
        return "-fx-background-color: linear-gradient(to bottom, rgba(20,28,60,0.92), rgba(8,10,28,0.92));"
                + "-fx-text-fill: #f4f7ff;"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: #6a8aff;"
                + "-fx-border-radius: 14;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;";
    }

    private String hoverStyle() {
        return "-fx-background-color: linear-gradient(to bottom, rgba(50,80,180,0.95), rgba(20,30,90,0.95));"
                + "-fx-text-fill: #ffffff;"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: #00e0ff;"
                + "-fx-border-radius: 14;"
                + "-fx-border-width: 2.5;"
                + "-fx-cursor: hand;";
    }

    private void fadeIn(VBox content) {
        FadeTransition fade = new FadeTransition(Duration.millis(900), content);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(900), content);
        slide.setFromY(40);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }

    private void startPlaylist() {
        File dir = resolveResource("mp3 files", MENU_MUSIC_DIR);
        if (dir == null || !dir.isDirectory()) {
            System.err.println("Menu music folder not found: " + MENU_MUSIC_DIR);
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files == null || files.length == 0) {
            System.err.println("No mp3 files found in: " + dir.getAbsolutePath());
            return;
        }
        Arrays.sort(files);
        for (File f : files) {
            playlist.add(f.toURI().toString());
        }
        Collections.shuffle(playlist);
        trackIndex = 0;
        playCurrentTrack();
    }

    private void playCurrentTrack() {
        if (playlist.isEmpty()) return;
        try {
            if (musicPlayer != null) {
                musicPlayer.stop();
                musicPlayer.dispose();
            }
            Media media = new Media(playlist.get(trackIndex));
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setVolume(0.55);
            musicPlayer.setOnEndOfMedia(this::advanceTrack);
            musicPlayer.setOnError(() -> {
                System.err.println("Media error: " + musicPlayer.getError());
                advanceTrack();
            });
            musicPlayer.play();
        } catch (Exception ex) {
            System.err.println("Failed to play track: " + ex.getMessage());
        }
    }

    private void advanceTrack() {
        trackIndex = (trackIndex + 1) % playlist.size();
        playCurrentTrack();
    }

    private void startSoloGame(Stage stage) {
        showRoleSelection(stage, "Solo Game");
    }

    private void startMultiplayerGame(Stage stage) {
        showRoleSelection(stage, "Multiplayer Game");
    }

    private void showRoleSelection(Stage stage, String mode) {
        game.gui.view.GameView.Mode m =
                mode.startsWith("Solo")
                        ? game.gui.view.GameView.Mode.SOLO
                        : game.gui.view.GameView.Mode.MULTIPLAYER;
        RoleSelectionView view = new RoleSelectionView(role -> {
            shutdown();
            startGameplay(stage, role, m);
        });
        stage.setScene(view.buildScene());
        stage.setTitle("DoorDasH — " + mode + " — Choose Your Side");
    }

    private void startGameplay(Stage stage, game.engine.Role role,
                               game.gui.view.GameView.Mode m) {
        game.gui.util.SoundManager.get().preload();
        game.gui.view.GameView gv = new game.gui.view.GameView(stage, role, m, () -> {
            // Return to main menu — relaunch MainMenu by calling start again
            game.gui.util.SoundManager.get().stopAll();
            try {
                new MainMenu().start(stage);
            } catch (Exception ignored) { }
        });
        stage.setScene(gv.buildScene());
        stage.setTitle("DoorDasH — Gameplay");
    }

    private void confirmQuit(Stage stage) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to quit DoorDasH?",
                ButtonType.YES, ButtonType.NO);
        a.setTitle("Quit");
        a.setHeaderText("Leaving Monstropolis?");
        a.initOwner(stage);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                shutdown();
                Platform.exit();
            }
        });
    }

    private void info(Stage owner, String header, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setTitle(header);
        a.setHeaderText(header);
        a.initOwner(owner);
        a.showAndWait();
    }

    private void showHowToPlay(Stage owner) {
        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.WINDOW_MODAL);
        popup.setTitle("How to Play");

        Text header = new Text("How to Play DoorDasH");
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        header.setFill(Color.web("#ffd84d"));
        header.setEffect(new DropShadow(8, Color.web("#000")));

        Label body = new Label(howToPlayText());
        body.setWrapText(true);
        body.setFont(Font.font("Verdana", 15));
        body.setTextFill(Color.web("#f4f7ff"));
        body.setTextAlignment(TextAlignment.LEFT);
        body.setMaxWidth(700);

        VBox inner = new VBox(18, header, body);
        inner.setPadding(new Insets(28));
        inner.setAlignment(Pos.TOP_LEFT);
        inner.setBackground(new Background(new BackgroundFill(
                Color.rgb(10, 14, 32, 0.95), new CornerRadii(14), Insets.EMPTY)));

        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a0e20; -fx-background-color: #0a0e20;");

        Button close = themedButton("Close");
        close.setPrefSize(180, 48);
        close.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        close.setOnAction(e -> popup.close());

        VBox layout = new VBox(16, scroll, close);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setBackground(new Background(new BackgroundFill(
                Color.web("#05071a"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene s = new Scene(layout, 780, 620);
        popup.setScene(s);
        popup.show();
    }

    private String howToPlayText() {
        return ""
                + "GOAL\n"
                + "Race your monster across the 100-cell snake board and slam-dunk the most energy "
                + "into doors of your role (SCARER or LAUGHER) before your opponent does.\n\n"

                + "PICKING A SIDE\n"
                + "Choose SCARER or LAUGHER on the start screen. Your role decides which doors give "
                + "you energy and which drain it.\n\n"

                + "ON YOUR TURN\n"
                + "  1. (Optional) Activate your monster's powerup before rolling.\n"
                + "  2. Roll the dice and move that many cells along the snake path.\n"
                + "  3. The cell you land on triggers its effect.\n\n"

                + "CELL TYPES\n"
                + "  • SCARER Door  — Scarers gain energy here, Laughers lose energy.\n"
                + "  • LAUGHER Door — Laughers gain energy here, Scarers lose energy.\n"
                + "  • Monster Cell — A stationed monster reacts to whoever lands here.\n"
                + "  • Card Cell    — Draw one of 25 shuffled cards (Shield, Swapper, Start Over,\n"
                + "                   Energy Steal, Confusion).\n"
                + "  • Conveyor Belt — Automatically slides your monster a few cells.\n"
                + "  • Contamination Sock — Negative effect; watch your step.\n"
                + "  • Normal Cell  — Safe; no effect.\n\n"

                + "MONSTER TYPES\n"
                + "  • Dasher      — Faster movement.\n"
                + "  • Dynamo      — Higher energy reserves.\n"
                + "  • Multitasker — Performs multiple actions per turn.\n"
                + "  • Schemer     — Strategic powerups.\n\n"

                + "STATUS EFFECTS\n"
                + "  • Shield        — Blocks the next energy loss.\n"
                + "  • Confusion     — Your role temporarily swaps.\n"
                + "  • Momentum Rush — Move further than the dice shows.\n"
                + "  • Focus Mode    — Enhanced abilities for a few turns.\n"
                + "  • Freeze        — You skip your next turn.\n\n"

                + "WINNING\n"
                + "Reach the touchdown cell with the most energy, or push your opponent to zero. "
                + "The winning monster's name, role and final energy are shown on the end screen.\n\n"

                + "HOTKEYS (Main Menu)\n"
                + "  • Click any button — themed hover animation, glow, and a snappy scale.\n"
                + "  • Close window (X) — quits at any time.\n";
    }

    private void shutdown() {
        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                musicPlayer.dispose();
            } catch (Exception ignored) { }
            musicPlayer = null;
        }
    }

    private File resolveResource(String subdir, String name) {
        String[] roots = {
                "src/game/gui/resources/",
                "game/gui/resources/",
                "../src/game/gui/resources/",
                System.getProperty("user.dir") + "/src/game/gui/resources/"
        };
        for (String root : roots) {
            File candidate = new File(root + subdir + "/" + name);
            if (candidate.exists()) return candidate;
        }
        return new File("src/game/gui/resources/" + subdir + "/" + name);
    }

    @Override
    public void stop() {
        shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
