package game.gui.view;

import game.engine.Constants;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.gui.util.ResourceLocator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PlayerPanel extends VBox {

    private final Monster monster;
    private final boolean isPlayerSide;
    private final IntegerProperty energy = new SimpleIntegerProperty();
    private final DoubleProperty energyRatio = new SimpleDoubleProperty();

    private final Text nameText;
    private final Text typeText;
    private final Text roleText;
    private final Text energyLabel;
    private final Rectangle energyFill;
    private final Rectangle energyTrack;
    private final Text statusText;
    private final Button rollBtn;
    private final Button powerupBtn;
    private final Text turnIndicator;

    public PlayerPanel(Monster monster, boolean isPlayerSide,
                       String rollKeyLabel, String powerupKeyLabel) {
        this.monster = monster;
        this.isPlayerSide = isPlayerSide;
        setAlignment(Pos.TOP_CENTER);
        setSpacing(10);
        setPadding(new Insets(14, 12, 14, 12));
        setMinWidth(220);
        setPrefWidth(240);
        setMaxWidth(260);

        Color edgeColor = monster.getRole() == Role.SCARER
                ? Color.web("#ff5a5a") : Color.web("#5aff8a");

        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#10142a", 0.92)),
                        new Stop(1, Color.web("#05071a", 0.95))),
                new CornerRadii(16), Insets.EMPTY)));
        setStyle("-fx-border-color: " + toWeb(edgeColor) + ";"
                + "-fx-border-width: 2.5;"
                + "-fx-border-radius: 16;"
                + "-fx-background-radius: 16;");
        setEffect(new DropShadow(18, Color.web("#000a")));

        turnIndicator = new Text(isPlayerSide ? "YOU" : "OPPONENT");
        turnIndicator.setFont(Font.font("Impact", FontWeight.BOLD, 16));
        turnIndicator.setFill(edgeColor);
        turnIndicator.setEffect(new DropShadow(4, Color.BLACK));

        Image monsterImg = ResourceLocator.monsterImage(monster.getName() + ".png");
        StackPane portrait = new StackPane();
        Circle frame = new Circle(48);
        frame.setStroke(edgeColor);
        frame.setStrokeWidth(3);
        frame.setEffect(new Glow(0.4));
        if (monsterImg != null) {
            frame.setFill(new ImagePattern(monsterImg, 0, 0, 1, 1, true));
        } else {
            frame.setFill(Color.web("#1a1a2a"));
        }
        portrait.getChildren().add(frame);

        nameText = new Text(monster.getName());
        nameText.setFont(Font.font("Impact", FontWeight.BOLD, 22));
        nameText.setFill(Color.web("#fff7e0"));
        nameText.setEffect(new DropShadow(4, Color.BLACK));

        typeText = new Text(monster.getClass().getSimpleName().toUpperCase());
        typeText.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        typeText.setFill(Color.web("#cfd8ff"));

        roleText = new Text(roleLine());
        roleText.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        roleText.setFill(edgeColor);

        // Canister visual
        StackPane canisterStack = new StackPane();
        canisterStack.setPrefSize(110, 150);
        canisterStack.setAlignment(Pos.CENTER);

        Image canisterBg = ResourceLocator.png(
                monster.getRole() == Role.SCARER ? "player scare.png" : "player laugh.png");
        ImageView canisterImg = new ImageView();
        if (canisterBg != null) {
            canisterImg.setImage(canisterBg);
            canisterImg.setFitHeight(150);
            canisterImg.setPreserveRatio(true);
        }

        Pane fillContainer = new Pane();
        fillContainer.setPrefSize(36, 110);
        fillContainer.setMaxSize(36, 110);
        fillContainer.setMinSize(36, 110);

        energyTrack = new Rectangle(36, 110);
        energyTrack.setArcWidth(6);
        energyTrack.setArcHeight(6);
        energyTrack.setFill(Color.web("#0a0a14"));
        energyTrack.setStroke(Color.web("#000"));
        energyTrack.setOpacity(0.65);

        energyFill = new Rectangle(36, 0);
        energyFill.setArcWidth(6);
        energyFill.setArcHeight(6);
        Color fillColor = monster.getRole() == Role.SCARER
                ? Color.web("#ff3a3a") : Color.web("#3aff5a");
        Color fillDark = monster.getRole() == Role.SCARER
                ? Color.web("#6a0a0a") : Color.web("#0a6a14");
        energyFill.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, fillColor.brighter()), new Stop(1, fillDark)));
        energyFill.setEffect(new Glow(0.6));
        energyFill.setY(110);
        energyFill.setHeight(0);

        energyRatio.addListener((o, ov, nv) -> {
            double h = 110 * Math.min(1, Math.max(0, nv.doubleValue()));
            energyFill.setHeight(h);
            energyFill.setY(110 - h);
        });
        energy.addListener((o, ov, nv) ->
                energyRatio.set((double) nv.intValue() / Constants.WINNING_ENERGY));
        energy.set(monster.getEnergy());

        fillContainer.getChildren().addAll(energyTrack, energyFill);

        canisterStack.getChildren().addAll(fillContainer, canisterImg);

        energyLabel = new Text();
        energyLabel.textProperty().bind(Bindings.format("Energy: %d", energy));
        energyLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        energyLabel.setFill(Color.web("#fff7c8"));
        energyLabel.setEffect(new DropShadow(3, Color.BLACK));

        statusText = new Text("Ready");
        statusText.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
        statusText.setFill(Color.web("#a8c8ff"));
        statusText.setWrappingWidth(200);

        rollBtn = action("Roll Dice  [" + rollKeyLabel + "]", edgeColor);
        powerupBtn = action("Use Powerup  [" + powerupKeyLabel + "]", Color.web("#ffd84d"));

        VBox buttons = new VBox(8, powerupBtn, rollBtn);
        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(turnIndicator, portrait, nameText, typeText, roleText,
                canisterStack, energyLabel, statusText, buttons);
    }

    private Button action(String text, Color accent) {
        Button b = new Button(text);
        b.setPrefSize(200, 40);
        b.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        b.setFocusTraversable(false);
        String accentWeb = toWeb(accent);
        String idle = "-fx-background-color: linear-gradient(to bottom, #1a2a55, #0a1430);"
                + "-fx-text-fill: #e8f0ff;"
                + "-fx-border-color: " + accentWeb + ";"
                + "-fx-border-width: 2;"
                + "-fx-background-radius: 10;"
                + "-fx-border-radius: 10;"
                + "-fx-cursor: hand;";
        String hover = "-fx-background-color: linear-gradient(to bottom, #2a4a90, #122455);"
                + "-fx-text-fill: #ffffff;"
                + "-fx-border-color: #ffffff;"
                + "-fx-border-width: 2;"
                + "-fx-background-radius: 10;"
                + "-fx-border-radius: 10;"
                + "-fx-cursor: hand;";
        b.setStyle(idle);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(idle));
        return b;
    }

    public Button getRollButton() { return rollBtn; }
    public Button getPowerupButton() { return powerupBtn; }
    public Monster getMonster() { return monster; }

    public void setEnergy(int v) {
        energy.set(Math.max(0, v));
    }

    public void refresh() {
        energy.set(monster.getEnergy());
        roleText.setText(roleLine());
        StringBuilder st = new StringBuilder();
        if (monster.isFrozen()) st.append("Frozen ");
        if (monster.isShielded()) st.append("Shielded ");
        if (monster.isConfused()) st.append("Confused(").append(monster.getConfusionTurns()).append(") ");
        if (st.length() == 0) st.append("Ready");
        statusText.setText(st.toString().trim());
    }

    public void setActive(boolean active) {
        if (active) {
            turnIndicator.setText("YOUR TURN");
            turnIndicator.setEffect(new Glow(0.8));
            setStyle(getStyle().replaceAll("-fx-border-width: [0-9.]+;",
                    "-fx-border-width: 4;"));
        } else {
            turnIndicator.setText(isPlayerSide ? "YOU" : "OPPONENT");
            turnIndicator.setEffect(new DropShadow(4, Color.BLACK));
            setStyle(getStyle().replaceAll("-fx-border-width: [0-9.]+;",
                    "-fx-border-width: 2.5;"));
        }
    }

    public void setControlsEnabled(boolean enabled) {
        rollBtn.setDisable(!enabled);
        powerupBtn.setDisable(!enabled);
    }

    private String roleLine() {
        String role = monster.getRole().name();
        if (monster.isConfused()) {
            role += "  (orig " + monster.getOriginalRole().name() + ")";
        }
        return role;
    }

    private String toWeb(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
