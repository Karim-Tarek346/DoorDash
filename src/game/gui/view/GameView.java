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
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
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
import javafx.scene.layout.Pane;
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
import javafx.scene.transform.Scale;
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
    StackPane root;
    private StackPane centerPane;
    private VBox logBox;
    boolean turnInProgress = false;
    boolean gameEnded = false;
    private StatusSnapshot pendingSelfBefore;
    private StatusSnapshot pendingOppBefore;

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

        final double DESIGN_W = 1500;
        final double DESIGN_H = 880;
        layout.setPrefSize(DESIGN_W, DESIGN_H);
        layout.setMinSize(DESIGN_W, DESIGN_H);
        layout.setMaxSize(DESIGN_W, DESIGN_H);

        Scale fitScale = new Scale(1, 1, 0, 0);
        layout.getTransforms().add(fitScale);

        Pane fitWrapper = new Pane(layout);
        fitWrapper.setBackground(Background.EMPTY);
        fitWrapper.setPickOnBounds(false);

        root.getChildren().add(fitWrapper);

        Scene scene = new Scene(root, DESIGN_W, DESIGN_H);
        scene.setFill(Color.web("#03050e"));

        DoubleBinding factor = Bindings.createDoubleBinding(
                () -> {
                    double w = root.getWidth();
                    double h = root.getHeight();
                    if (w <= 0 || h <= 0) return 1.0;
                    return Math.min(w / DESIGN_W, h / DESIGN_H);
                },
                root.widthProperty(), root.heightProperty());
        fitScale.xProperty().bind(factor);
        fitScale.yProperty().bind(factor);

        DoubleBinding wrapW = factor.multiply(DESIGN_W);
        DoubleBinding wrapH = factor.multiply(DESIGN_H);
        fitWrapper.minWidthProperty().bind(wrapW);
        fitWrapper.prefWidthProperty().bind(wrapW);
        fitWrapper.maxWidthProperty().bind(wrapW);
        fitWrapper.minHeightProperty().bind(wrapH);
        fitWrapper.prefHeightProperty().bind(wrapH);
        fitWrapper.maxHeightProperty().bind(wrapH);

        Platform.runLater(factor::invalidate);

        wireActions(scene);
        beginGame();
        return scene;
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
            if (e.getCode() == KeyCode.ESCAPE) {
                // Pause via settings instead of hard-exit
            }
        });

        CheatCodes.install(scene, this, game);
    }

    private void beginGame() {
        SoundManager.get().stopMusic();
        SoundManager.get().playTheme();
        deckView.animateShuffle(() -> { });
        refreshTurnUi();
        log("Game started. " + game.getCurrent().getName() + " goes first.");
    }

    void refreshTurnUi() {
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
            StatusSnapshot selfBefore = StatusSnapshot.of(who);
            StatusSnapshot oppBefore = StatusSnapshot.of(otherOf(who));
            game.usePowerup();
            int selfDelta = who.getEnergy() - selfPre;
            int oppDelta = otherOf(who).getEnergy() - oppPre;
            log(who.getName() + " used powerup. (self " + signed(selfDelta)
                    + ", opp " + signed(oppDelta) + ")");
            playEnergyDeltaSfx(selfDelta);
            playEnergyDeltaSfx(oppDelta);
            refreshTurnUi();
            java.util.List<String> bodies = new java.util.ArrayList<>();
            collectEffectChanges(who, selfBefore, bodies);
            collectEffectChanges(otherOf(who), oppBefore, bodies);
            chainPopups(bodies, 0, () -> checkWinner());
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
            StatusSnapshot before = StatusSnapshot.of(who);
            who.setFrozen(false);
            java.util.List<String> bodies = new java.util.ArrayList<>();
            collectEffectChanges(who, before, bodies);
            chainPopups(bodies, 0, this::advanceTurn);
            return;
        }

        diceView.roll(roll -> processMove(who, roll));
    }

    private void processMove(Monster who, int roll) {
        log(who.getName() + " rolled " + roll);
        int prePos = who.getPosition();
        int intermediate = Math.min(Constants.BOARD_SIZE - 1, prePos + roll);

        // Snapshot
        Monster opp = otherOf(who);
        int oppPrePos = opp.getPosition();
        int oppPreEnergy = opp.getEnergy();
        int selfPreEnergy = who.getEnergy();
        int deckPreSize = Board.getCards().size();
        List<Card> deckPre = new ArrayList<>(Board.getCards());

        boolean[] doorPreActivated = snapshotDoors();
        Role selfPreRole = who.getRole();

        pendingSelfBefore = StatusSnapshot.of(who);
        pendingOppBefore = StatusSnapshot.of(opp);

        // Execute move via engine
        try {
            game.getBoard().moveMonster(who, roll, opp);
        } catch (InvalidMoveException ex) {
            handleMoveError(who, prePos, "Invalid Move", ex.getMessage());
            return;
        } catch (Exception ex) {
            handleMoveError(who, prePos, "Game Error",
                    ex.getMessage() == null ? ex.toString() : ex.getMessage());
            return;
        }

        int postPos = who.getPosition();
        Card drawnCard = detectDrawnCard(deckPre);
        int selfDelta = who.getEnergy() - selfPreEnergy;
        int oppDelta = opp.getEnergy() - oppPreEnergy;
        int oppPosDelta = opp.getPosition() - oppPrePos;

        Cell landed = cellAt(intermediate);

        // Step 1: animate move along path to intermediate
        boardView.animateMove(who, prePos, intermediate, () -> {
            // Step 2: animate landing effect
            Runnable afterLanding = () -> {
                // Step 3: any extra translocation (conveyor/sock/card teleport)
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
                ThemedAlert.card(root, drawnCard, afterLanding);
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
            // Opponent moved (e.g., 2319 Alert)
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
        java.util.List<String> bodies = new java.util.ArrayList<>();
        if (pendingSelfBefore != null) {
            collectEffectChanges(who, pendingSelfBefore, bodies);
            pendingSelfBefore = null;
        }
        if (pendingOppBefore != null) {
            collectEffectChanges(otherOf(who), pendingOppBefore, bodies);
            pendingOppBefore = null;
        }
        Runnable afterPopups = () -> {
            if (!checkWinner()) {
                advanceTurn();
            }
        };
        chainPopups(bodies, 0, afterPopups);
    }

    private void advanceTurn() {
        // Engine already advances internally via switchTurn in playTurn,
        // but we use moveMonster directly so we must set current manually.
        game.setCurrent(otherOf(game.getCurrent()));
        turnInProgress = false;
        refreshTurnUi();

        if (game.getCurrent() == game.getOpponent() && mode == Mode.SOLO && !gameEnded) {
            // Bot plays automatically
            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(e -> botPlay());
            delay.play();
        }
    }

    private void botPlay() {
        if (gameEnded) return;
        Monster bot = game.getOpponent();
        // Optionally use powerup if affordable & worthwhile
        if (bot.getEnergy() >= Constants.POWERUP_COST + 200 && new Random().nextDouble() < 0.3) {
            try {
                int selfPre = bot.getEnergy();
                int oppPre = game.getPlayer().getEnergy();
                StatusSnapshot selfBefore = StatusSnapshot.of(bot);
                StatusSnapshot oppBefore = StatusSnapshot.of(game.getPlayer());
                game.usePowerup();
                playEnergyDeltaSfx(bot.getEnergy() - selfPre);
                playEnergyDeltaSfx(game.getPlayer().getEnergy() - oppPre);
                log(bot.getName() + " (bot) used powerup.");
                refreshTurnUi();
                if (checkWinner()) return;
                java.util.List<String> bodies = new java.util.ArrayList<>();
                collectEffectChanges(bot, selfBefore, bodies);
                collectEffectChanges(game.getPlayer(), oppBefore, bodies);
                chainPopups(bodies, 0, () -> attemptRoll(bot));
                return;
            } catch (Exception ignored) { }
        }
        attemptRoll(bot);
    }

    boolean checkWinner() {
        Monster w = game.getWinner();
        if (w == null) return false;
        gameEnded = true;
        log(w.getName() + " (" + w.getRole() + ") wins!");
        playerPanel.setControlsEnabled(false);
        opponentPanel.setControlsEnabled(false);
        PauseTransition delay = new PauseTransition(Duration.millis(600));
        Monster loser = (w == game.getPlayer()) ? game.getOpponent() : game.getPlayer();
        delay.setOnFinished(e -> EndSequence.play(root, w.getRole(), w.getName(),
                w, loser, this::returnToMenu));
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

    void popError(String title, String body) {
        ThemedAlert.error(root, title, body, null);
        turnInProgress = false;
        refreshTurnUi();
    }

    private static final class StatusSnapshot {
        boolean shielded;
        boolean frozen;
        boolean confused;
        static StatusSnapshot of(Monster m) {
            StatusSnapshot s = new StatusSnapshot();
            s.shielded = m.isShielded();
            s.frozen = m.isFrozen();
            s.confused = m.isConfused();
            return s;
        }
    }

    private void collectEffectChanges(Monster m, StatusSnapshot before, java.util.List<String> bodies) {
        if (!before.shielded && m.isShielded()) {
            bodies.add(m.getName() + " is now SHIELDED — next negative effect blocked.");
        }
        if (!before.frozen && m.isFrozen()) {
            bodies.add(m.getName() + " is FROZEN — next turn skipped.");
        }
        if (!before.confused && m.isConfused()) {
            bodies.add(m.getName() + " is CONFUSED — roles flipped for "
                    + m.getConfusionTurns() + " turns.");
        }
        if (before.shielded && !m.isShielded()) {
            bodies.add(m.getName() + "'s SHIELD wore off.");
        }
        if (before.frozen && !m.isFrozen()) {
            bodies.add(m.getName() + " is no longer FROZEN.");
        }
        if (before.confused && !m.isConfused()) {
            bodies.add(m.getName() + " is no longer CONFUSED.");
        }
    }

    private void chainPopups(java.util.List<String> bodies, int idx) {
        chainPopups(bodies, idx, null);
    }

    private void chainPopups(java.util.List<String> bodies, int idx, Runnable onComplete) {
        if (idx >= bodies.size()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        ThemedAlert.info(root, "Effect Triggered", bodies.get(idx),
                () -> chainPopups(bodies, idx + 1, onComplete));
    }

    private void handleMoveError(Monster who, int prePos,
                                 String title, String message) {
        boardView.teleportPiece(who, prePos);
        turnInProgress = false;
        refreshTurnUi();
        ThemedAlert.error(root, title, message, () -> {
            if (game.getCurrent() == game.getOpponent()
                    && mode == Mode.SOLO && !gameEnded) {
                PauseTransition delay = new PauseTransition(Duration.millis(400));
                delay.setOnFinished(e -> botPlay());
                delay.play();
            }
        });
    }

    Monster otherOf(Monster m) {
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
            // One removed from front
            return deckPre.get(0);
        }
        if (deckPost.size() == deckPre.size()) {
            // Possibly reshuffle then draw — best-effort
            return null;
        }
        return null;
    }

    private String signed(int v) {
        return (v >= 0 ? "+" : "") + v;
    }

    void log(String msg) {
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
