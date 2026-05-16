package game.gui;

import game.engine.Game;
import game.engine.Role;
import game.engine.cells.*;
import game.engine.cells.Cell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;

public class Controller {

    // --- Start Screen ---
    @FXML private VBox startScreen;
    @FXML private ComboBox<Role> roleSelectionBox;
    @FXML private Button startGameBtn;

    // --- Game Screen ---
    @FXML private BorderPane gameScreen;
    @FXML private Label currentTurnLabel;
    @FXML private Label notificationLabel;
    @FXML private GridPane boardGrid;
    @FXML private Button rollDiceBtn;
    @FXML private Button usePowerupBtn;

    // --- Player 1 Stats ---
    @FXML private Label p1NameLabel;
    @FXML private Label p1TypeLabel;
    @FXML private Label p1OriginalRoleLabel;
    @FXML private Label p1CurrentRoleLabel;
    @FXML private Label p1EnergyLabel;
    @FXML private Label p1PositionLabel;
    @FXML private Label p1StatusLabel;

    // --- Player 2 Stats ---
    @FXML private Label p2NameLabel;
    @FXML private Label p2TypeLabel;
    @FXML private Label p2OriginalRoleLabel;
    @FXML private Label p2CurrentRoleLabel;
    @FXML private Label p2EnergyLabel;
    @FXML private Label p2PositionLabel;
    @FXML private Label p2StatusLabel;

    // --- Game Over Screen ---
    @FXML private VBox gameOverScreen;
    @FXML private Label winnerTextLabel;
    @FXML private Label finalPlayer1Stats;
    @FXML private Label finalPlayer2Stats;
    @FXML private Button returnToStartBtn;

    // --- Game Logic Variables ---
    private Game game;
    private StackPane[] cellPanes = new StackPane[100];
    private Circle p1Token;
    private Circle p2Token;

    @FXML
    public void initialize() {
        // Setup Start Screen
        roleSelectionBox.setItems(FXCollections.observableArrayList(Role.SCARER, Role.LAUGHER));

        startGameBtn.setOnAction(e -> handleStartGame());
        rollDiceBtn.setOnAction(e -> handleRollDice());
        usePowerupBtn.setOnAction(e -> handleUsePowerup());
        returnToStartBtn.setOnAction(e -> resetToStartScreen());

        // Create player tokens
        p1Token = new Circle(10, Color.BLUE);
        p1Token.setStroke(Color.WHITE);
        p1Token.setStrokeWidth(2);

        p2Token = new Circle(10, Color.RED);
        p2Token.setStroke(Color.WHITE);
        p2Token.setStrokeWidth(2);
    }

    private void handleStartGame() {
        Role selectedRole = roleSelectionBox.getValue();
        if (selectedRole == null) {
            showAlert("Selection Error", "Please select a side (SCARER or LAUGHER) to start.");
            return;
        }

        try {
            game = new Game(selectedRole);
            startScreen.setVisible(false);
            gameScreen.setVisible(true);
            gameOverScreen.setVisible(false);

            initializeBoardUI();
            updateUI("Game Started! Good luck.");
        } catch (IOException e) {
            showAlert("Loading Error", "Failed to load game data (CSV files). Check your file paths.");
            e.printStackTrace();
        }
    }

    private void initializeBoardUI() {
        boardGrid.getChildren().clear();
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int i = 0; i < 100; i++) {
            StackPane cellPane = new StackPane();
            cellPane.setPrefSize(60, 60);
            cellPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");

            int row = i / 10;
            int col = i % 10;
            if (row % 2 == 1) col = 9 - col; // Snake pattern matching engine

            Cell cell = cells[row][col];

            // Background colors based on cell type
            if (cell instanceof DoorCell) {
                DoorCell door = (DoorCell) cell;
                cellPane.setStyle("-fx-background-color: " + (door.getRole() == Role.SCARER ? "#e6b3ff" : "#b3e6ff") + "; -fx-border-color: #aaaaaa;");
                Label energyLbl = new Label("E: " + door.getEnergy());
                energyLbl.setFont(Font.font("System", 10));
                StackPane.setAlignment(energyLbl, Pos.BOTTOM_CENTER);
                cellPane.getChildren().add(energyLbl);
            } else if (cell instanceof ConveyorBelt) {
                cellPane.setStyle("-fx-background-color: #ffff99; -fx-border-color: #aaaaaa;");
            } else if (cell instanceof ContaminationSock) {
                cellPane.setStyle("-fx-background-color: #ff9999; -fx-border-color: #aaaaaa;");
            } else if (cell instanceof CardCell) {
                cellPane.setStyle("-fx-background-color: #ffd480; -fx-border-color: #aaaaaa;");
            } else if (cell instanceof MonsterCell) {
                cellPane.setStyle("-fx-background-color: #d9d9d9; -fx-border-color: #aaaaaa;");
            } else {
                cellPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #aaaaaa;");
            }

            // Index Label
            Label indexLbl = new Label(String.valueOf(i));
            indexLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
            StackPane.setAlignment(indexLbl, Pos.TOP_LEFT);
            StackPane.setMargin(indexLbl, new Insets(2, 0, 0, 2));

            cellPane.getChildren().add(indexLbl);
            boardGrid.add(cellPane, col, row);
            cellPanes[i] = cellPane; // Store reference for updating tokens
        }
    }

    private void handleRollDice() {
        try {
            game.playTurn();
            updateUI("Dice rolled! " + (game.getCurrent() == game.getPlayer() ? "Opponent's" : "Your") + " turn.");
            checkWinCondition();
        } catch (InvalidMoveException e) {
            updateUI("Action Failed: " + e.getMessage());
            showAlert("Invalid Move", e.getMessage());
        }
    }

    private void handleUsePowerup() {
        try {
            game.usePowerup();
            updateUI(game.getCurrent().getName() + " used a powerup!");
        } catch (OutOfEnergyException e) {
            updateUI("Action Failed: Not enough energy!");
            showAlert("Out of Energy", e.getMessage());
        }
    }

    private void updateUI(String message) {
        notificationLabel.setText(message);
        currentTurnLabel.setText(game.getCurrent() == game.getPlayer() ? "Current Turn: YOU" : "Current Turn: OPPONENT");

        updateMonsterStats(game.getPlayer(), p1NameLabel, p1TypeLabel, p1OriginalRoleLabel, p1CurrentRoleLabel, p1EnergyLabel, p1PositionLabel, p1StatusLabel);
        updateMonsterStats(game.getOpponent(), p2NameLabel, p2TypeLabel, p2OriginalRoleLabel, p2CurrentRoleLabel, p2EnergyLabel, p2PositionLabel, p2StatusLabel);

        updateBoardTokens();
    }

    private void updateMonsterStats(Monster m, Label name, Label type, Label origRole, Label curRole, Label energy, Label pos, Label status) {
        name.setText("Name: " + m.getName());
        type.setText("Type: " + m.getClass().getSimpleName());
        origRole.setText("Original Role: " + m.getOriginalRole());
        curRole.setText("Current Role: " + m.getRole());

        if (m.getRole() != m.getOriginalRole()) {
            curRole.setTextFill(Color.RED);
        } else {
            curRole.setTextFill(Color.BLACK);
        }

        energy.setText("Energy: " + m.getEnergy());
        pos.setText("Position: " + m.getPosition());

        // Build status string
        StringBuilder sb = new StringBuilder();
        if (m.isFrozen()) sb.append("Frozen! ");
        if (m.isShielded()) sb.append("Shielded! ");
        if (m.getConfusionTurns() > 0) sb.append("Confused (").append(m.getConfusionTurns()).append(" turns left) ");
        if (m instanceof Dasher && ((Dasher) m).getMomentumTurns() > 0) {
            sb.append("Momentum (").append(((Dasher) m).getMomentumTurns()).append(" left) ");
        }
        if (m instanceof MultiTasker && ((MultiTasker) m).getNormalSpeedTurns() > 0) {
            sb.append("Focus (").append(((MultiTasker) m).getNormalSpeedTurns()).append(" left) ");
        }

        String finalStatus = sb.toString().trim();
        status.setText(finalStatus.isEmpty() ? "None" : finalStatus);
    }

    private void updateBoardTokens() {
        // Remove tokens from old panes
        for (StackPane pane : cellPanes) {
            pane.getChildren().remove(p1Token);
            pane.getChildren().remove(p2Token);
        }

        // Add tokens to new positions
        HBox tokenBox = new HBox(2);
        tokenBox.setAlignment(Pos.CENTER);

        if (game.getPlayer().getPosition() == game.getOpponent().getPosition()) {
            tokenBox.getChildren().addAll(p1Token, p2Token);
            cellPanes[game.getPlayer().getPosition()].getChildren().add(tokenBox);
        } else {
            cellPanes[game.getPlayer().getPosition()].getChildren().add(p1Token);
            cellPanes[game.getOpponent().getPosition()].getChildren().add(p2Token);
        }
    }

    private void checkWinCondition() {
        Monster winner = game.getWinner();
        if (winner != null) {
            gameScreen.setVisible(false);
            gameOverScreen.setVisible(true);

            winnerTextLabel.setText("Winner: " + winner.getName() + " (" + winner.getOriginalRole() + ")");
            finalPlayer1Stats.setText("Player 1 Final Energy: " + game.getPlayer().getEnergy());
            finalPlayer2Stats.setText("Player 2 Final Energy: " + game.getOpponent().getEnergy());
        }
    }

    private void resetToStartScreen() {
        gameOverScreen.setVisible(false);
        gameScreen.setVisible(false);
        startScreen.setVisible(true);
        roleSelectionBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}