package game.gui.view;

import game.gui.util.SoundManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Random;
import java.util.function.IntConsumer;

public class DiceView extends StackPane {

    private static final double SIZE = 80;
    private final Group dieGroup = new Group();
    private final Random rand = new Random();
    private final Rectangle face;
    private final Group pipsGroup = new Group();
    private int currentValue = 1;

    public DiceView() {
        setPrefSize(SIZE + 24, SIZE + 60);
        setAlignment(Pos.CENTER);

        face = new Rectangle(SIZE, SIZE);
        face.setArcWidth(14);
        face.setArcHeight(14);
        face.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fffbe8")),
                new Stop(1, Color.web("#d8c890"))));
        face.setStroke(Color.web("#3a2808"));
        face.setStrokeWidth(2);
        face.setEffect(new DropShadow(10, Color.web("#000a")));

        dieGroup.getChildren().addAll(face, pipsGroup);

        Text label = new Text("DICE");
        label.setFont(Font.font("Impact", FontWeight.BOLD, 16));
        label.setFill(Color.web("#ffd84d"));
        label.setEffect(new DropShadow(4, Color.BLACK));

        getChildren().addAll(label, dieGroup);
        StackPane.setAlignment(label, Pos.TOP_CENTER);
        StackPane.setAlignment(dieGroup, Pos.CENTER);

        drawPips(1);
    }

    private void drawPips(int value) {
        pipsGroup.getChildren().clear();
        double r = 5;
        double c = SIZE / 2;
        double e1 = SIZE * 0.25;
        double e2 = SIZE * 0.75;
        double[][][] positions = {
                {},
                {{c, c}},
                {{e1, e1}, {e2, e2}},
                {{e1, e1}, {c, c}, {e2, e2}},
                {{e1, e1}, {e2, e1}, {e1, e2}, {e2, e2}},
                {{e1, e1}, {e2, e1}, {c, c}, {e1, e2}, {e2, e2}},
                {{e1, e1}, {e2, e1}, {e1, c}, {e2, c}, {e1, e2}, {e2, e2}}
        };
        for (double[] p : positions[value]) {
            Circle pip = new Circle(p[0], p[1], r);
            pip.setFill(Color.web("#1a0e02"));
            pip.setEffect(new DropShadow(2, Color.web("#0006")));
            pipsGroup.getChildren().add(pip);
        }
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void roll(IntConsumer onResult) {
        SoundManager.get().playSfx(SoundManager.Sfx.DICE);

        Rotate rot = new Rotate(0, SIZE / 2, SIZE / 2);
        dieGroup.getTransforms().setAll(rot);

        Timeline rollTl = new Timeline();
        double duration = 900;
        int frames = 9;
        for (int i = 0; i < frames; i++) {
            int v = rand.nextInt(6) + 1;
            double t = duration * i / frames;
            rollTl.getKeyFrames().add(new KeyFrame(Duration.millis(t),
                    e -> drawPips(v),
                    new KeyValue(rot.angleProperty(), 720 * i / (double) frames),
                    new KeyValue(dieGroup.scaleXProperty(), 1.0 + 0.15 * Math.sin(i * 1.3)),
                    new KeyValue(dieGroup.scaleYProperty(), 1.0 + 0.15 * Math.cos(i * 1.3))));
        }
        int result = rand.nextInt(6) + 1;
        currentValue = result;
        rollTl.getKeyFrames().add(new KeyFrame(Duration.millis(duration),
                e -> {
                    drawPips(result);
                    dieGroup.setScaleX(1);
                    dieGroup.setScaleY(1);
                },
                new KeyValue(rot.angleProperty(), 720),
                new KeyValue(dieGroup.scaleXProperty(), 1.0),
                new KeyValue(dieGroup.scaleYProperty(), 1.0)));

        rollTl.setOnFinished(e -> {
            face.setEffect(new Glow(0.8));
            Timeline cool = new Timeline(new KeyFrame(Duration.millis(300),
                    ev -> face.setEffect(new DropShadow(10, Color.web("#000a")))));
            cool.play();
            if (onResult != null) onResult.accept(result);
        });
        rollTl.play();
    }

    public void setValueSilently(int value) {
        currentValue = value;
        drawPips(value);
    }
}
