package game.gui.view;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Monster;
import game.gui.util.ResourceLocator;
import game.gui.util.SoundManager;
import game.gui.util.ThemedAlert;
import game.gui.util.ScreenScaler; // <--- NEW IMPORT TO BYPASS ECLIPSE BUG

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class GameView {

    public enum Mode { SOLO, MULTIPLAYER }

    private final Stage stage;
    private final Role playerRole;
    private final Mode mode;
    private final Runnable onReturnToMenu;

    private Game game;
    private BoardView boardView;
    private CardDeckView deckView;
    private DiceView diceView;
    private PlayerPanel playerPanel;
    private PlayerPanel opponentPanel;
    private StackPane root;
    private StackPane centerPane;
    private VBox logBox;
    private boolean turnInProgress = false;
    private boolean gameEnded = false;
    
    private static final double SCENE_W = 1200;
    private static final double SCENE_H = 800;

    public GameView(Stage stage, Role playerRole, Mode mode, Runnable onReturnToMenu) {
        this.stage = stage;
        this.playerRole = playerRole;
        this.mode = mode;
        this.onReturnToMenu = onReturnToMenu;
    }

    public Scene buildScene() {
        try {
            game = new Game(playerRole);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load game data", ex);
        }

        root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0a0e22")),
                        new Stop(1, Color.web("#03050e"))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(14));

        playerPanel = new PlayerPanel(game.getPlayer(), true, "SPACE", "P");
        opponentPanel = new PlayerPanel(game.getOpponent(), false,
                mode == Mode.MULTIPLAYER ? "ENTER" : "(Bot)",
                mode == Mode.MULTIPLAYER ? "O" : "(Bot)");

        boardView = new BoardView(game);
        deckView = new CardDeckView();
        diceView = new DiceView();

        centerPane = new StackPane();

        VBox boardColumn = new VBox(12, boardView);
        boardColumn.setAlignment(Pos.CENTER);

        VBox rightOfBoard = new VBox(16, deckView, diceView);
        rightOfBoard.setAlignment(Pos.CENTER);

        HBox centerInner = new HBox(20, boardColumn, rightOfBoard);
        centerInner.setAlignment(Pos.CENTER);

        logBox = new VBox(2);
        logBox.setAlignment(Pos.TOP_LEFT);
        logBox.setMaxWidth(540);
        logBox.setPrefHeight(80);

        VBox center = new VBox(10, centerInner, logBox);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(12));
        center.setBackground(new Background(new BackgroundFill(
                Color.web("#06091a", 0.65), new CornerRadii(16), Insets.EMPTY)));
        center.setStyle("-fx-border-color: #2a3866; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16;");

        centerPane.getChildren().add(center);
        deckView.setBlurTarget(centerPane);

        HBox.setHgrow(centerPane, Priority.ALWAYS);

        HBox row = new HBox(14, playerPanel, centerPane, opponentPanel);
        row.setAlignment(Pos.CENTER);
        layout.setCenter(row);

        // Top bar (gear icon + title)
        SettingsOverlay settings = new SettingsOverlay(root,
                () -> returnToMenu(),
                () -> restartGame());
        Group gear = settings.buildGearButton();

        Text title = new Text("DoorDasH — " + (mode == Mode.SOLO ? "Solo" : "Multiplayer"));
        title.setFont(Font.font("Impact", FontWeight.BOLD, 26));
        title.setFill(Color.web("#ffd84d"));
        title.setEffect(new javafx.scene.effect.DropShadow(6, Color.BLACK));

        HBox topBar = new HBox(14, gear, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 14, 8, 14));
        layout.setTop(topBar);

        root.getChildren().add(layout);

        // MODIFIED LINE: Uses the new bypassed scaler class
        Scene scalableScene = ScreenScaler.createScalableScene(root, SCENE_W, SCENE_H);
        scalableScene.setFill(Color.web("#03050e"));

        wireActions(scalableScene);
        beginGame();
        return scalableScene;
    }

    private void wireActions(Scene scene) {
        playerPanel.getRollButton().setOnAction(e -> attemptRoll(game.getPlayer()));
        playerPanel.getPowerupButton().setOnAction(e -> attemptPowerup(game.getPlayer()));

        if (mode == Mode.MULTIPLAYER) {
            opponentPanel.getRollButton().setOnAction(e -> attemptRoll(game.getOpponent()));
            opponentPanel.getPowerupButton().setOnAction(e -> attemptPowerup(game.getOpponent()));
        } else {
            opponentPanel.setControlsEnabled(false);
        }

        scene.setOnKeyPressed(e -> {
            if (turnInProgress || gameEnded) return;
            Monster current = game.getCurrent();
            if (current == game.getPlayer()) {
                if (e.getCode() == KeyCode.SPACE) attemptRoll(game.getPlayer());
                else if (e.getCode() == KeyCode.P) attemptPowerup(game.getPlayer());
            } else if (mode == Mode.MULTIPLAYER) {
                if (e.getCode() == KeyCode.ENTER) attemptRoll(game.getOpponent());
                else if (e.getCode() == KeyCode.O) attemptPowerup(game.getOpponent());
            }
        });
    }

    private void beginGame() {
        SoundManager.get().stopMusic();
        SoundManager.get().playTheme();
        deckView.animateShuffle(() -> { });
        refreshTurnUi();
        log("Game started. " + game.getCurrent().getName() + " goes first.");
    }

    private void refreshTurnUi() {
        Monster cur = game.getCurrent();
        boolean playerTurn = (cur == game.getPlayer());
        playerPanel.setActive(playerTurn);
        opponentPanel.setActive(!playerTurn);
        playerPanel.setControlsEnabled(playerTurn && !turnInProgress && !gameEnded);
        if (mode == Mode.MULTIPLAYER) {
            opponentPanel.setControlsEnabled(!playerTurn && !turnInProgress && !gameEnded);
        }
        playerPanel.refresh();
        opponentPanel.refresh();
        boardView.refreshDoorStates();
    }

    private void attemptPowerup(Monster who) {
        if (turnInProgress || gameEnded) return;
        if (game.getCurrent() != who) {
            popError("Wait Your Turn", "It's not your turn yet.");
            return;
        }
        try {
            int oppPre = otherOf(who).getEnergy();
            int selfPre = who.getEnergy();
            game.usePowerup();
            int selfDelta = who.getEnergy() - selfPre;
            int oppDelta = otherOf(who).getEnergy() - oppPre;
            log(who.getName() + " used powerup. (self " + signed(selfDelta)
                    + ", opp " + signed(oppDelta) + ")");
            playEnergyDeltaSfx(selfDelta);
            playEnergyDeltaSfx(oppDelta);
            refreshTurnUi();
            checkWinner();
        } catch (OutOfEnergyException ex) {
            popError("Not Enough Energy", ex.getMessage());
        } catch (Exception ex) {
            popError("Game Error", ex.getMessage() == null ? ex.toString() : ex.getMessage());
        }
    }

    private void attemptRoll(Monster who) {
        if (turnInProgress || gameEnded) return;
        if (game.getCurrent() != who) {
            popError("Wait Your Turn", "It's not your turn yet.");
            return;
        }
        turnInProgress = true;
        playerPanel.setControlsEnabled(false);
        opponentPanel.setControlsEnabled(false);

        if (who.isFrozen()) {
            log(who.getName() + " is FROZEN — skipping turn.");
            who.setFrozen(false);
            advanceTurn();
            return;
        }

        diceView.roll(roll -> processMove(who, roll));
    }

    private void processMove(Monster who, int roll) {
        log(who.getName() + " rolled " + roll);
        int prePos = who.getPosition();
        int intermediate = Math.min(Constants.BOARD_SIZE - 1, prePos + roll);

        Monster opp = otherOf(who);
        int oppPrePos = opp.getPosition();
        int oppPreEnergy = opp.getEnergy();
        int selfPreEnergy = who.getEnergy();
        int deckPreSize = Board.getCards().size();
        List<Card> deckPre = new ArrayList<>(Board.getCards());

        boolean[] doorPreActivated = snapshotDoors();
        Role selfPreRole = who.getRole();

        try {
            game.getBoard().moveMonster(who, roll, opp);
        } catch (InvalidMoveException ex) {
            popError("Invalid Move", ex.getMessage());
            boardView.teleportPiece(who, prePos);
            turnInProgress = false;
            refreshTurnUi();
            return;
        } catch (Exception ex) {
            popError("Game Error", ex.getMessage() == null ? ex.toString() : ex.getMessage());
            boardView.teleportPiece(who, prePos);
            turnInProgress = false;
            refreshTurnUi();
            return;
        }

        int postPos = who.getPosition();
        Card drawnCard = detectDrawnCard(deckPre);
        int selfDelta = who.getEnergy() - selfPreEnergy;
        int oppDelta = opp.getEnergy() - oppPreEnergy;
        int oppPosDelta = opp.getPosition() - oppPrePos;

        Cell landed = cellAt(intermediate);

        boardView.animateMove(who, prePos, intermediate, () -> {
            Runnable afterLanding = () -> {
                if (postPos != intermediate) {
                    boardView.animateMove(who, intermediate, postPos, () -> {
                        finalizeMove(who, opp, oppPrePos, oppPosDelta,
                                selfDelta, oppDelta);
                    });
                } else {
                    finalizeMove(who, opp, oppPrePos, oppPosDelta,
                            selfDelta, oppDelta);
                }
            };

            if (landed instanceof CardCell && drawnCard != null) {
                deckView.animateDrawAndShow(drawnCard, afterLanding);
            } else if (landed instanceof DoorCell) {
                boardView.flashCell(intermediate,
                        ((DoorCell) landed).getRole() == who.getRole()
                                ? Color.web("#7aff7a") : Color.web("#ff5a5a"));
                afterLanding.run();
            } else if (landed instanceof ConveyorBelt) {
                boardView.flashCell(intermediate, Color.web("#5ad6ff"));
                SoundManager.get().playSfx(SoundManager.Sfx.WIN_SOMETHING);
                afterLanding.run();
            } else if (landed instanceof ContaminationSock) {
                boardView.flashCell(intermediate, Color.web("#8aff5a"));
                SoundManager.get().playSfx(SoundManager.Sfx.LOSE_SOMETHING);
                afterLanding.run();
            } else if (landed instanceof MonsterCell) {
                boardView.flashCell(intermediate, Color.web("#c080ff"));
                afterLanding.run();
            } else {
                afterLanding.run();
            }
        });
    }

    private void finalizeMove(Monster who, Monster opp, int oppPrePos, int oppPosDelta,
                              int selfDelta, int oppDelta) {
        if (oppPosDelta != 0) {
            boardView.animateMove(opp, oppPrePos, opp.getPosition(), () ->
                    completeAfterAnimations(who, selfDelta, oppDelta));
        } else {
            completeAfterAnimations(who, selfDelta, oppDelta);
        }
    }

    private void completeAfterAnimations(Monster who, int selfDelta, int oppDelta) {
        playEnergyDeltaSfx(selfDelta);
        playEnergyDeltaSfx(oppDelta);
        log(who.getName() + " self " + signed(selfDelta) + ", opp " + signed(oppDelta));
        refreshTurnUi();
        if (!checkWinner()) {
            advanceTurn();
        }
    }

    private void advanceTurn() {
        game.setCurrent(otherOf(game.getCurrent()));
        turnInProgress = false;
        refreshTurnUi();

        if (game.getCurrent() == game.getOpponent() && mode == Mode.SOLO && !gameEnded) {
            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(e -> botPlay());
            delay.play();
        }
    }

    private void botPlay() {
        if (gameEnded) return;
        Monster bot = game.getOpponent();
        if (bot.getEnergy() >= Constants.POWERUP_COST + 200 && new Random().nextDouble() < 0.3) {
            try {
                int selfPre = bot.getEnergy();
                int oppPre = game.getPlayer().getEnergy();
                game.usePowerup();
                playEnergyDeltaSfx(bot.getEnergy() - selfPre);
                playEnergyDeltaSfx(game.getPlayer().getEnergy() - oppPre);
                log(bot.getName() + " (bot) used powerup.");
                refreshTurnUi();
                if (checkWinner()) return;
            } catch (Exception ignored) { }
        }
        attemptRoll(bot);
    }

    private boolean checkWinner() {
        Monster w = game.getWinner();
        if (w == null) return false;
        gameEnded = true;
        log(w.getName() + " (" + w.getRole() + ") wins!");
        playerPanel.setControlsEnabled(false);
        opponentPanel.setControlsEnabled(false);
        PauseTransition delay = new PauseTransition(Duration.millis(600));
        delay.setOnFinished(e -> EndSequence.play(root, w.getRole(), w.getName(),
                this::returnToMenu));
        delay.play();
        return true;
    }

    private void restartGame() {
        gameEnded = false;
        turnInProgress = false;
        SoundManager.get().stopMusic();
        Scene fresh = new GameView(stage, playerRole, mode, onReturnToMenu).buildScene();
        stage.setScene(fresh);
    }

    private void returnToMenu() {
        gameEnded = true;
        SoundManager.get().stopAll();
        if (onReturnToMenu != null) onReturnToMenu.run();
    }

    private void playEnergyDeltaSfx(int delta) {
        if (delta > 0) SoundManager.get().playSfx(SoundManager.Sfx.WIN_SOMETHING);
        else if (delta < 0) SoundManager.get().playSfx(SoundManager.Sfx.LOSE_SOMETHING);
    }

    private void popError(String title, String body) {
        ThemedAlert.error(root, title, body, null);
        turnInProgress = false;
        refreshTurnUi();
    }

    private Monster otherOf(Monster m) {
        return m == game.getPlayer() ? game.getOpponent() : game.getPlayer();
    }

    private Cell cellAt(int index) {
        int cols = Constants.BOARD_COLS;
        int row = index / cols;
        int col = index % cols;
        if (row % 2 == 1) col = cols - 1 - col;
        return game.getBoard().getBoardCells()[row][col];
    }

    private boolean[] snapshotDoors() {
        boolean[] arr = new boolean[Constants.BOARD_SIZE];
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
            Cell c = cellAt(i);
            if (c instanceof DoorCell) arr[i] = ((DoorCell) c).isActivated();
        }
        return arr;
    }

    private Card detectDrawnCard(List<Card> deckPre) {
        List<Card> deckPost = Board.getCards();
        if (deckPost.size() < deckPre.size()) {
            return deckPre.get(0);
        }
        if (deckPost.size() == deckPre.size()) {
            return null;
        }
        return null;
    }

    private String signed(int v) {
        return (v >= 0 ? "+" : "") + v;
    }

    private void log(String msg) {
        Text t = new Text("• " + msg);
        t.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        t.setFill(Color.web("#cfd8ff"));
        t.setWrappingWidth(520);
        logBox.getChildren().add(t);
        while (logBox.getChildren().size() > 4) {
            logBox.getChildren().remove(0);
        }
    }
}