package game.gui.view;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.monsters.Monster;
import game.gui.util.ResourceLocator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardView extends StackPane {

    public static final int GRID_SIZE = 10;
    public static final double CELL_SIZE = 56;
    public static final double GAP = 2;
    public static final double BOARD_PX = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP;

    private final Game game;
    private final GridPane grid = new GridPane();
    private final Pane overlay = new Pane();
    private final Pane animationLayer = new Pane();
    private final CellView[] cellViews = new CellView[Constants.BOARD_SIZE];

    private MonsterPiece playerPiece;
    private MonsterPiece opponentPiece;

    private Consumer<String> logger = s -> { };

    public BoardView(Game game) {
        this.game = game;
        setPrefSize(BOARD_PX + 24, BOARD_PX + 24);
        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0e1830")),
                        new Stop(1, Color.web("#06091a"))),
                new CornerRadii(14), Insets.EMPTY)));
        setStyle("-fx-border-color: #4a6aff; -fx-border-width: 2.5; -fx-border-radius: 14;"
                + "-fx-background-radius: 14;");
        setEffect(new DropShadow(20, Color.web("#000")));
        setPadding(new Insets(12));

        grid.setHgap(GAP);
        grid.setVgap(GAP);
        grid.setAlignment(Pos.CENTER);

        overlay.setMouseTransparent(true);
        overlay.setPrefSize(BOARD_PX, BOARD_PX);
        animationLayer.setMouseTransparent(true);
        animationLayer.setPrefSize(BOARD_PX, BOARD_PX);

        Group boardStack = new Group(grid, overlay, animationLayer);

        StackPane wrapper = new StackPane(boardStack);
        wrapper.setAlignment(Pos.CENTER);
        getChildren().add(wrapper);

        buildCells();
        buildLinks();
        buildMonsterPieces();
        refreshDoorStates();
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    private void buildCells() {
        Cell[][] cells = game.getBoard().getBoardCells();
        for (int idx = 0; idx < Constants.BOARD_SIZE; idx++) {
            int[] rc = indexToRowCol(idx);
            int engineRow = rc[0];
            int engineCol = rc[1];
            int displayRow = (GRID_SIZE - 1) - engineRow;
            int displayCol = engineCol;
            Cell cell = cells[engineRow][engineCol];
            CellView cv = new CellView(idx, cell);
            cellViews[idx] = cv;
            grid.add(cv, displayCol, displayRow);
        }
    }

    private static int[] indexToRowCol(int index) {
        int cols = Constants.BOARD_COLS;
        int row = index / cols;
        int col = index % cols;
        if (row % 2 == 1) col = cols - 1 - col;
        return new int[]{row, col};
    }

    private double[] centerOf(int index) {
        int[] rc = indexToRowCol(index);
        int displayRow = (GRID_SIZE - 1) - rc[0];
        int displayCol = rc[1];
        double cx = displayCol * (CELL_SIZE + GAP) + CELL_SIZE / 2.0;
        double cy = displayRow * (CELL_SIZE + GAP) + CELL_SIZE / 2.0;
        return new double[]{cx, cy};
    }

    private void buildLinks() {
        for (int idx : Constants.CONVEYOR_CELL_INDICES) {
            Cell c = cellAt(idx);
            if (c instanceof ConveyorBelt) {
                int target = clampIndex(idx + ((ConveyorBelt) c).getEffect());
                drawConveyor(idx, target);
            }
        }
        for (int idx : Constants.SOCK_CELL_INDICES) {
            Cell c = cellAt(idx);
            if (c instanceof ContaminationSock) {
                int target = clampIndex(idx + ((ContaminationSock) c).getEffect());
                drawSock(idx, target);
            }
        }
    }

    private int clampIndex(int i) {
        return Math.max(0, Math.min(Constants.BOARD_SIZE - 1, i));
    }

    private Cell cellAt(int index) {
        int[] rc = indexToRowCol(index);
        return game.getBoard().getBoardCells()[rc[0]][rc[1]];
    }

    private void drawConveyor(int from, int to) {
        double[] a = centerOf(from);
        double[] b = centerOf(to);
        Line belt = new Line(a[0], a[1], b[0], b[1]);
        belt.setStroke(Color.web("#5ad6ff"));
        belt.setStrokeWidth(5);
        belt.setOpacity(0.55);
        belt.getStrokeDashArray().addAll(8.0, 6.0);
        belt.setEffect(new Glow(0.5));
        overlay.getChildren().add(belt);

        Timeline marquee = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(belt.strokeDashOffsetProperty(), 0)),
                new KeyFrame(Duration.seconds(1.4), new KeyValue(belt.strokeDashOffsetProperty(), -28)));
        marquee.setCycleCount(Animation.INDEFINITE);
        marquee.play();

        Image scareImg = ResourceLocator.png("Scare.png");
        Image laughImg = ResourceLocator.png("Laugh.png");
        Image carry = (from + to) % 2 == 0 ? scareImg : laughImg;
        if (carry != null) {
            ImageView mover = new ImageView(carry);
            mover.setFitWidth(22);
            mover.setFitHeight(22);
            mover.setPreserveRatio(true);
            mover.setEffect(new DropShadow(6, Color.web("#000")));
            Path p = new Path(new MoveTo(a[0], a[1]), new LineTo(b[0], b[1]));
            PathTransition pt = new PathTransition(Duration.seconds(2.2), p, mover);
            pt.setCycleCount(Animation.INDEFINITE);
            overlay.getChildren().add(mover);
            pt.play();
        }
    }

    private void drawSock(int from, int to) {
        double[] a = centerOf(from);
        double[] b = centerOf(to);
        double mx = (a[0] + b[0]) / 2;
        double my = (a[1] + b[1]) / 2;
        double dx = b[1] - a[1];
        double dy = -(b[0] - a[0]);
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) len = 1;
        double bulge = 40;
        double cx1 = a[0] + (mx - a[0]) * 0.4 + dx / len * bulge;
        double cy1 = a[1] + (my - a[1]) * 0.4 + dy / len * bulge;
        double cx2 = b[0] + (mx - b[0]) * 0.4 + dx / len * bulge;
        double cy2 = b[1] + (my - b[1]) * 0.4 + dy / len * bulge;

        CubicCurve curve = new CubicCurve(a[0], a[1], cx1, cy1, cx2, cy2, b[0], b[1]);
        curve.setStroke(Color.web("#8aff5a"));
        curve.setStrokeWidth(3);
        curve.setFill(null);
        curve.setOpacity(0.7);
        curve.getStrokeDashArray().addAll(4.0, 4.0);
        curve.setEffect(new Glow(0.4));
        overlay.getChildren().add(curve);

        Timeline marquee = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(curve.strokeDashOffsetProperty(), 0)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(curve.strokeDashOffsetProperty(), -16)));
        marquee.setCycleCount(Animation.INDEFINITE);
        marquee.play();
    }

    private void buildMonsterPieces() {
        playerPiece = new MonsterPiece(game.getPlayer());
        opponentPiece = new MonsterPiece(game.getOpponent());
        animationLayer.getChildren().addAll(playerPiece, opponentPiece);
        placePiece(playerPiece, game.getPlayer().getPosition(), -10);
        placePiece(opponentPiece, game.getOpponent().getPosition(), 10);
    }

    private void placePiece(MonsterPiece piece, int index, double offsetX) {
        double[] c = centerOf(index);
        piece.setLayoutX(c[0] + offsetX - piece.radius);
        piece.setLayoutY(c[1] - piece.radius);
    }

    public void animateMove(Monster monster, int from, int to, Runnable onDone) {
        MonsterPiece piece = (monster == game.getPlayer()) ? playerPiece : opponentPiece;
        double offsetX = (monster == game.getPlayer()) ? -10 : 10;

        List<KeyFrame> frames = new ArrayList<>();
        int step = (to >= from) ? 1 : -1;
        double duration = 0;
        double stepMs = 110;

        for (int i = from + step; (step > 0 ? i <= to : i >= to); i += step) {
            double[] c = centerOf(i);
            duration += stepMs;
            frames.add(new KeyFrame(Duration.millis(duration),
                    new KeyValue(piece.layoutXProperty(), c[0] + offsetX - piece.radius),
                    new KeyValue(piece.layoutYProperty(), c[1] - piece.radius)));
        }
        if (frames.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }
        Timeline tl = new Timeline(frames.toArray(new KeyFrame[0]));
        tl.setOnFinished(e -> {
            if (onDone != null) onDone.run();
        });
        tl.play();
    }

    public void teleportPiece(Monster m, int index) {
        MonsterPiece piece = (m == game.getPlayer()) ? playerPiece : opponentPiece;
        double offsetX = (m == game.getPlayer()) ? -10 : 10;
        double[] c = centerOf(index);
        piece.setLayoutX(c[0] + offsetX - piece.radius);
        piece.setLayoutY(c[1] - piece.radius);
    }

    public void flashCell(int index, Color color) {
        if (index < 0 || index >= cellViews.length) return;
        CellView cv = cellViews[index];
        cv.flash(color);
    }

    public void refreshDoorStates() {
        Cell[][] cells = game.getBoard().getBoardCells();
        for (int idx = 0; idx < Constants.BOARD_SIZE; idx++) {
            int[] rc = indexToRowCol(idx);
            Cell c = cells[rc[0]][rc[1]];
            if (c instanceof DoorCell) {
                cellViews[idx].setActivated(((DoorCell) c).isActivated());
            }
        }
    }

    public void refreshMonsterCellOccupancy() {
        for (int idx : Constants.MONSTER_CELL_INDICES) {
            cellViews[idx].refreshMonsterPresence();
        }
    }

    private static final class CellView extends StackPane {
        private final int index;
        private final Cell cell;
        private final Rectangle base;
        private final Rectangle activatedTint;
        private ImageView doorImageView;
        private Image doorClosedImage;
        private Image doorOpenImage;
        private Text exhaustedX;

        CellView(int index, Cell cell) {
            this.index = index;
            this.cell = cell;
            setPrefSize(CELL_SIZE, CELL_SIZE);
            setMinSize(CELL_SIZE, CELL_SIZE);
            setMaxSize(CELL_SIZE, CELL_SIZE);

            base = new Rectangle(CELL_SIZE, CELL_SIZE);
            base.setArcWidth(8);
            base.setArcHeight(8);
            base.setFill(paintForCell(cell, index));
            base.setStroke(Color.web("#000", 0.6));
            base.setStrokeWidth(1);

            activatedTint = new Rectangle(CELL_SIZE, CELL_SIZE);
            activatedTint.setArcWidth(8);
            activatedTint.setArcHeight(8);
            activatedTint.setFill(Color.web("#00000099"));
            activatedTint.setOpacity(0);

            Text idx = new Text(String.valueOf(index));
            idx.setFont(Font.font("Verdana", FontWeight.BOLD, 9));
            idx.setFill(Color.web("#ffffff"));
            idx.setEffect(new DropShadow(2, Color.BLACK));
            StackPane.setAlignment(idx, Pos.TOP_LEFT);
            StackPane.setMargin(idx, new Insets(2, 0, 0, 3));

            getChildren().addAll(base, activatedTint);
            addCellContent();
            getChildren().add(idx);
        }

        private Paint paintForCell(Cell cell, int index) {
            if (cell instanceof DoorCell) {
                DoorCell d = (DoorCell) cell;
                Color top, bot;
                if (index == Constants.WINNING_POSITION) {
                    top = Color.web("#ff8fb0"); bot = Color.web("#a02a55");
                } else if (d.getRole() == Role.SCARER) {
                    top = Color.web("#a02a26"); bot = Color.web("#4a0e0c");
                } else {
                    top = Color.web("#e89020"); bot = Color.web("#8a4810");
                }
                return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, top), new Stop(1, bot));
            }
            if (cell instanceof CardCell) {
                return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#2a6a98")),
                        new Stop(1, Color.web("#0e2848")));
            }
            if (cell instanceof ConveyorBelt) {
                return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#3a8ad4")),
                        new Stop(1, Color.web("#152c52")));
            }
            if (cell instanceof ContaminationSock) {
                return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#6aa030")),
                        new Stop(1, Color.web("#283e10")));
            }
            if (cell instanceof MonsterCell) {
                return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#5a3a80")),
                        new Stop(1, Color.web("#1e1238")));
            }
            return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#d4d2c4")),
                    new Stop(1, Color.web("#7a7868")));
        }

        private void addCellContent() {
            if (cell instanceof DoorCell) {
                DoorCell d = (DoorCell) cell;
                if (index == Constants.WINNING_POSITION) {
                    doorClosedImage = ResourceLocator.png("Boo's door.png");
                    doorOpenImage = doorClosedImage;
                } else if (d.getRole() == Role.SCARER) {
                    doorClosedImage = ResourceLocator.png("Scare.png");
                    doorOpenImage = ResourceLocator.png("Scare Open.png");
                } else {
                    doorClosedImage = ResourceLocator.png("Laugh.png");
                    doorOpenImage = ResourceLocator.png("Laugh Open.png");
                }
                if (doorClosedImage != null) {
                    doorImageView = new ImageView(doorClosedImage);
                    doorImageView.setFitWidth(CELL_SIZE - 14);
                    doorImageView.setFitHeight(CELL_SIZE - 18);
                    doorImageView.setPreserveRatio(true);
                    StackPane.setAlignment(doorImageView, Pos.CENTER);
                    getChildren().add(doorImageView);
                }
                if (index != Constants.WINNING_POSITION) {
                    exhaustedX = new Text("✕");
                    exhaustedX.setFont(Font.font("Verdana", FontWeight.BOLD, 32));
                    exhaustedX.setFill(Color.web("#ff2a2a"));
                    exhaustedX.setStroke(Color.web("#000"));
                    exhaustedX.setStrokeWidth(1.2);
                    exhaustedX.setEffect(new DropShadow(6, Color.web("#000c")));
                    exhaustedX.setVisible(false);
                    StackPane.setAlignment(exhaustedX, Pos.CENTER);
                    getChildren().add(exhaustedX);
                }
                Text energy = new Text(String.valueOf(d.getEnergy()));
                energy.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
                energy.setFill(Color.web("#fff7c8"));
                energy.setEffect(new DropShadow(2, Color.BLACK));
                StackPane.setAlignment(energy, Pos.BOTTOM_CENTER);
                StackPane.setMargin(energy, new Insets(0, 0, 2, 0));
                getChildren().add(energy);
            } else if (cell instanceof MonsterCell) {
                MonsterCell mc = (MonsterCell) cell;
                Monster m = mc.getCellMonster();
                Image img = ResourceLocator.monsterImage(m.getName() + ".png");
                if (img != null) {
                    Circle c = new Circle((CELL_SIZE - 16) / 2.0);
                    c.setFill(new ImagePattern(img, 0, 0, 1, 1, true));
                    c.setStroke(m.getRole() == Role.SCARER ? Color.web("#ff5a5a") : Color.web("#7aff7a"));
                    c.setStrokeWidth(2);
                    c.setEffect(new DropShadow(4, Color.BLACK));
                    StackPane.setAlignment(c, Pos.CENTER);
                    getChildren().add(c);
                }
            } else if (cell instanceof CardCell) {
                Rectangle card = new Rectangle(20, 28);
                card.setArcWidth(4);
                card.setArcHeight(4);
                card.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#ffd84d")),
                        new Stop(1, Color.web("#a05010"))));
                card.setStroke(Color.web("#2a1808"));
                card.setStrokeWidth(1);
                card.setRotate(-12);
                card.setEffect(new DropShadow(3, Color.BLACK));
                Text q = new Text("?");
                q.setFont(Font.font("Impact", FontWeight.BOLD, 18));
                q.setFill(Color.web("#2a1808"));
                q.setRotate(-12);
                getChildren().addAll(card, q);
            } else if (cell instanceof ConveyorBelt) {
                Text arrow = new Text("➤");
                arrow.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
                arrow.setFill(Color.web("#e0f8ff"));
                arrow.setEffect(new Glow(0.5));
                int eff = ((ConveyorBelt) cell).getEffect();
                arrow.setRotate(eff > 0 ? 0 : 180);
                getChildren().add(arrow);
            } else if (cell instanceof ContaminationSock) {
                Text bio = new Text("☠");
                bio.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
                bio.setFill(Color.web("#f8ffd0"));
                bio.setEffect(new Glow(0.5));
                getChildren().add(bio);
            }
        }

        void setActivated(boolean activated) {
            activatedTint.setOpacity(activated ? 0.55 : 0);
            if (doorImageView != null && doorOpenImage != null && doorClosedImage != null) {
                doorImageView.setImage(activated ? doorOpenImage : doorClosedImage);
            }
            if (exhaustedX != null) exhaustedX.setVisible(activated);
        }

        void flash(Color c) {
            Rectangle flash = new Rectangle(CELL_SIZE, CELL_SIZE);
            flash.setArcWidth(8);
            flash.setArcHeight(8);
            flash.setFill(c);
            flash.setOpacity(0.9);
            getChildren().add(flash);
            Timeline t = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(flash.opacityProperty(), 0.9)),
                    new KeyFrame(Duration.millis(700), new KeyValue(flash.opacityProperty(), 0)));
            t.setOnFinished(e -> getChildren().remove(flash));
            t.play();
        }

        void refreshMonsterPresence() {
            // Stationed monster image already drawn; nothing dynamic needed,
            // but we can dim if monster has left (engine doesn't remove monster cells)
        }
    }

    private static final class MonsterPiece extends Group {
        final double radius = 18;
        final Monster monster;

        MonsterPiece(Monster m) {
            this.monster = m;
            Image img = ResourceLocator.monsterImage(m.getName() + ".png");
            Circle ring = new Circle(radius + 2);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(m.getRole() == Role.SCARER ? Color.web("#ff4a4a") : Color.web("#5aff7a"));
            ring.setStrokeWidth(3);
            ring.setEffect(new Glow(0.7));

            Circle body = new Circle(radius);
            if (img != null) {
                body.setFill(new ImagePattern(img, 0, 0, 1, 1, true));
            } else {
                body.setFill(m.getRole() == Role.SCARER ? Color.web("#a02020") : Color.web("#20a020"));
            }
            body.setStroke(Color.web("#0a0a0e"));
            body.setStrokeWidth(1.5);
            body.setEffect(new DropShadow(8, Color.BLACK));

            getChildren().addAll(body, ring);
            setTranslateX(radius);
            setTranslateY(radius);
        }
    }
}
