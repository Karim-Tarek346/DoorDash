package game.gui;

import game.engine.Role;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;

public class RoleSelectionView {

    private static final double SCENE_W = 1280;
    private static final double SCENE_H = 800;
    private static final double DOOR_W = 220;
    private static final double DOOR_H = 380;
    private static final double FRAME_PAD = 28;
    private static final double BULB_HOUSING_H = 110;

    private final Consumer<Role> onChosen;

    private ImageView menuBg;
    private ImageView scarerBg;
    private ImageView laugherBg;

    public RoleSelectionView(Consumer<Role> onChosen) {
        this.onChosen = onChosen;
    }

    public Scene buildScene() {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#05060f"), CornerRadii.EMPTY, Insets.EMPTY)));

        menuBg = backgroundImage("menu.png", 1.0);
        scarerBg = backgroundImage("scarer door.png", 0.0);
        laugherBg = backgroundImage("laugher door.png", 0.0);

        StackPane veil = new StackPane();
        veil.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 15, 0.45), CornerRadii.EMPTY, Insets.EMPTY)));

        Text title = new Text("Choose Your Side");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 72));
        title.setFill(Color.web("#ffd84d"));
        DropShadow titleShadow = new DropShadow(20, Color.web("#ff5e1a"));
        titleShadow.setSpread(0.3);
        title.setEffect(titleShadow);

        Text subtitle = new Text("Step through the door that fits your monster");
        subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        subtitle.setFill(Color.web("#e8f1ff"));
        subtitle.setEffect(new DropShadow(8, Color.web("#001428")));

        MonsterDoor scarerDoor = new MonsterDoor(Role.SCARER, HingeSide.RIGHT);
        MonsterDoor laugherDoor = new MonsterDoor(Role.LAUGHER, HingeSide.LEFT);

        SubScene scarerScene = wrap3D(scarerDoor);
        SubScene laugherScene = wrap3D(laugherDoor);

        bindDoorBehavior(scarerDoor, scarerScene, scarerBg, Role.SCARER);
        bindDoorBehavior(laugherDoor, laugherScene, laugherBg, Role.LAUGHER);

        Text scarerLabel = roleLabel("SCARER", Color.web("#ff6b6b"));
        Text laugherLabel = roleLabel("LAUGHER", Color.web("#ffd84d"));

        VBox scarerCol = new VBox(8, scarerScene, scarerLabel);
        scarerCol.setAlignment(Pos.CENTER);
        VBox laugherCol = new VBox(8, laugherScene, laugherLabel);
        laugherCol.setAlignment(Pos.CENTER);

        HBox doors = new HBox(120, scarerCol, laugherCol);
        doors.setAlignment(Pos.CENTER);

        VBox content = new VBox(20, title, subtitle, doors);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 0, 60, 0));

        root.getChildren().addAll(menuBg, scarerBg, laugherBg, veil, content);
        sceneRoot = root;

        Scene scene = game.gui.util.ResolutionScaler.createScalableScene(root, SCENE_W, SCENE_H);
        scene.setFill(Color.BLACK);
        
        return scene;
    }
    

    public static void launchStandalone(Stage stage, Consumer<Role> onChosen) {
        RoleSelectionView view = new RoleSelectionView(onChosen);
        stage.setScene(view.buildScene());
        stage.setTitle("DoorDasH — Choose Your Side");
        stage.setResizable(true);
        stage.show();
    }

//    private void bindBackgrounds(Scene scene) {
//        for (ImageView iv : new ImageView[]{menuBg, scarerBg, laugherBg}) {
//            if (iv.getImage() != null) {
//                iv.fitWidthProperty().bind(scene.widthProperty());
//                iv.fitHeightProperty().bind(scene.heightProperty());
//            }
//        }
//    }

    private ImageView backgroundImage(String filename, double initialOpacity) {
        ImageView iv = new ImageView();
        File f = resolve("png files", filename);
        if (f != null && f.exists()) {
            iv.setImage(new Image(f.toURI().toString()));
        }
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        iv.setOpacity(initialOpacity);
        return iv;
    }

    private Text roleLabel(String s, Color c) {
        Text t = new Text(s);
        t.setFont(Font.font("Impact", FontWeight.BOLD, 36));
        t.setFill(c);
        t.setEffect(new DropShadow(10, Color.web("#000")));
        return t;
    }

    private SubScene wrap3D(MonsterDoor door) {
        Group g = new Group(door);
        double w = DOOR_W + FRAME_PAD * 2 + 60;
        double h = DOOR_H + FRAME_PAD * 2 + BULB_HOUSING_H + 30;
        door.setTranslateX(30);
        door.setTranslateY(BULB_HOUSING_H + 10);
        SubScene sub = new SubScene(g, w, h, true, SceneAntialiasing.BALANCED);
        sub.setFill(Color.TRANSPARENT);
        PerspectiveCamera cam = new PerspectiveCamera(false);
        cam.setFieldOfView(28);
        sub.setCamera(cam);
        return sub;
    }

    private boolean choosing = false;

    private void bindDoorBehavior(MonsterDoor door, SubScene host, ImageView bgFor, Role role) {
        host.setOnMouseEntered(e -> {
            if (choosing) return;
            door.openDoor();
            door.lightBulb();
            fadeBackground(bgFor, 1.0);
            if (role == Role.SCARER) fadeBackground(laugherBg, 0.0);
            else fadeBackground(scarerBg, 0.0);
            fadeBackground(menuBg, 0.0);
        });
        host.setOnMouseExited(e -> {
            if (choosing) return;
            door.closeDoor();
            door.dimBulb();
            fadeBackground(bgFor, 0.0);
            fadeBackground(menuBg, 1.0);
        });
        host.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (choosing) return;
            choosing = true;
            door.openDoor();
            door.lightBulb();
            playDollyTransition(host, role);
        });
    }

    private StackPane sceneRoot;

    private void playDollyTransition(SubScene chosen, Role role) {
        if (sceneRoot == null) return;

        // Fade other content away
        for (javafx.scene.Node node : sceneRoot.getChildren()) {
            if (node == chosen || node.getParent() == chosen.getParent()) continue;
        }

        // Find the VBox column that holds the chosen door so we dolly the column
        javafx.scene.Node target = chosen;
        javafx.scene.Node parent = chosen.getParent();
        while (parent != null && !(parent instanceof StackPane)) {
            target = parent;
            parent = parent.getParent();
        }

        // Animate scale up + center
        javafx.scene.Node dolly = target;
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                Duration.millis(1100), dolly);
        scale.setToX(4.5);
        scale.setToY(4.5);

        FadeTransition blackOut = new FadeTransition(Duration.millis(1100), sceneRoot);
        blackOut.setFromValue(1.0);
        blackOut.setToValue(0.0);
        blackOut.setDelay(Duration.millis(500));

        scale.play();
        blackOut.play();
        blackOut.setOnFinished(e -> {
            if (onChosen != null) onChosen.accept(role);
        });
    }

    private void fadeBackground(ImageView iv, double to) {
        if (iv.getImage() == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(380), iv);
        ft.setFromValue(iv.getOpacity());
        ft.setToValue(to);
        ft.play();
    }

    private File resolve(String subdir, String name) {
        String[] roots = {
                "src/game/gui/resources/",
                "game/gui/resources/",
                "../src/game/gui/resources/",
                System.getProperty("user.dir") + "/src/game/gui/resources/"
        };
        for (String root : roots) {
            File c = new File(root + subdir + "/" + name);
            if (c.exists()) return c;
        }
        return new File("src/game/gui/resources/" + subdir + "/" + name);
    }

    private enum HingeSide { LEFT, RIGHT }

    private static final class MonsterDoor extends Group {
        private final Role role;
        private final HingeSide hinge;
        private final Rotate doorHinge;
        private final Group doorPanel;
        private final Circle bulbCore;
        private final Circle bulbGlow;
        private final DoubleProperty bulbIntensity = new SimpleDoubleProperty(0);
        private final Timeline openTl;
        private final Timeline closeTl;
        private final Timeline lightTl;
        private final Timeline dimTl;

        MonsterDoor(Role role, HingeSide hinge) {
            this.role = role;
            this.hinge = hinge;
            this.doorHinge = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);

            Group bulbHousing = buildBulbHousing();
            Group frame = buildFrame();
            Group interior = buildInterior();
            this.doorPanel = buildDoorPanel();

            doorPanel.getTransforms().add(doorHinge);
            if (hinge == HingeSide.RIGHT) {
                doorHinge.setPivotX(DOOR_W);
                doorHinge.setPivotY(DOOR_H / 2);
            } else {
                doorHinge.setPivotX(0);
                doorHinge.setPivotY(DOOR_H / 2);
            }
            doorPanel.setTranslateX(FRAME_PAD);
            doorPanel.setTranslateY(FRAME_PAD);

            interior.setTranslateX(FRAME_PAD);
            interior.setTranslateY(FRAME_PAD);

            getChildren().addAll(frame, bulbHousing, interior, doorPanel);

            this.bulbCore = (Circle) bulbHousing.getProperties().get("core");
            this.bulbGlow = (Circle) bulbHousing.getProperties().get("glow");

            this.openTl = new Timeline(new KeyFrame(Duration.millis(420),
                    new KeyValue(doorHinge.angleProperty(),
                            hinge == HingeSide.RIGHT ? 72 : -72)));
            this.closeTl = new Timeline(new KeyFrame(Duration.millis(380),
                    new KeyValue(doorHinge.angleProperty(), 0)));

            this.lightTl = new Timeline(new KeyFrame(Duration.millis(280),
                    new KeyValue(bulbIntensity, 1.0)));
            this.dimTl = new Timeline(new KeyFrame(Duration.millis(420),
                    new KeyValue(bulbIntensity, 0.0)));

            bulbIntensity.addListener((o, ov, nv) -> applyBulb(nv.doubleValue()));
            applyBulb(0);
        }

        void openDoor() {
            closeTl.stop();
            openTl.playFromStart();
        }
        void closeDoor() {
            openTl.stop();
            closeTl.playFromStart();
        }
        void lightBulb() {
            dimTl.stop();
            lightTl.playFromStart();
        }
        void dimBulb() {
            lightTl.stop();
            dimTl.playFromStart();
        }

        private void applyBulb(double t) {
            Color off = Color.web("#3a2a18");
            Color onColor = role == Role.SCARER
                    ? Color.web("#ff5028")
                    : Color.web("#fff0a8");
            bulbCore.setFill(off.interpolate(onColor, t));
            bulbGlow.setOpacity(t);
            Glow g = new Glow(0.85 * t);
            bulbCore.setEffect(g);
        }

        private Group buildBulbHousing() {
            Group g = new Group();
            double centerX = FRAME_PAD + DOOR_W / 2.0;
            double housingBaseY = FRAME_PAD - 4;

            Rectangle mount = new Rectangle(centerX - 38, housingBaseY - 18, 76, 22);
            mount.setFill(metalGradient());
            mount.setArcWidth(8);
            mount.setArcHeight(8);
            mount.setStroke(Color.web("#1a1a1e"));
            mount.setStrokeWidth(1);

            for (int i = 0; i < 4; i++) {
                Circle rivet = new Circle(centerX - 28 + i * 18, housingBaseY - 7, 1.8, Color.web("#aab0b8"));
                rivet.setEffect(new InnerShadow(3, Color.web("#222")));
                g.getChildren().add(rivet);
            }

            Polyline armLeft = new Polyline(
                    centerX - 30, housingBaseY - 18,
                    centerX - 46, housingBaseY - 50,
                    centerX - 26, housingBaseY - 80);
            Polyline armRight = new Polyline(
                    centerX + 30, housingBaseY - 18,
                    centerX + 46, housingBaseY - 50,
                    centerX + 26, housingBaseY - 80);
            Polyline armCenter = new Polyline(
                    centerX, housingBaseY - 18,
                    centerX, housingBaseY - 50,
                    centerX, housingBaseY - 86);

            for (Polyline p : new Polyline[]{armLeft, armRight, armCenter}) {
                p.setStroke(Color.web("#2c2c32"));
                p.setStrokeWidth(4);
                p.setFill(null);
                g.getChildren().add(p);
            }
            for (Polyline p : new Polyline[]{armLeft, armRight, armCenter}) {
                Polyline shine = new Polyline(p.getPoints().stream().mapToDouble(Double::doubleValue).toArray());
                shine.setStroke(Color.web("#7a7d85"));
                shine.setStrokeWidth(1);
                shine.setFill(null);
                shine.setTranslateY(-1);
                g.getChildren().add(shine);
            }

            double bulbCY = housingBaseY - 86;
            Circle socket = new Circle(centerX, bulbCY + 8, 9);
            socket.setFill(metalGradient());
            socket.setStroke(Color.web("#15151a"));

            Circle glow = new Circle(centerX, bulbCY, 36);
            glow.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#fff7c8", 0.95)),
                    new Stop(0.45, Color.web("#ffd84d", 0.55)),
                    new Stop(1, Color.web("#ffd84d", 0))));
            glow.setEffect(new BoxBlur(8, 8, 2));
            glow.setOpacity(0);

            Circle core = new Circle(centerX, bulbCY, 11);
            core.setFill(Color.web("#3a2a18"));
            core.setStroke(Color.web("#1a120a"));
            core.setStrokeWidth(1.2);

            Circle highlight = new Circle(centerX - 3, bulbCY - 4, 3, Color.web("#ffffff", 0.55));

            g.getChildren().addAll(mount, glow, socket, core, highlight);
            g.getProperties().put("core", core);
            g.getProperties().put("glow", glow);
            return g;
        }

        private Group buildFrame() {
            Group g = new Group();
            double w = DOOR_W + FRAME_PAD * 2;
            double h = DOOR_H + FRAME_PAD * 2;

            Rectangle outer = new Rectangle(0, 0, w, h);
            outer.setFill(metalGradient());
            outer.setArcWidth(14);
            outer.setArcHeight(14);
            outer.setStroke(Color.web("#0c0c10"));
            outer.setStrokeWidth(2);
            outer.setEffect(new DropShadow(22, Color.web("#000", 0.7)));

            Rectangle inner = new Rectangle(FRAME_PAD - 6, FRAME_PAD - 6,
                    DOOR_W + 12, DOOR_H + 12);
            inner.setFill(Color.web("#0a0a0e"));
            inner.setArcWidth(6);
            inner.setArcHeight(6);
            inner.setEffect(new InnerShadow(10, Color.web("#000")));

            g.getChildren().addAll(outer, inner);

            double[][] rivetSpots = {
                    {10, 10}, {w - 10, 10}, {10, h - 10}, {w - 10, h - 10},
                    {w / 2, 10}, {w / 2, h - 10}, {10, h / 2}, {w - 10, h / 2}
            };
            for (double[] rs : rivetSpots) {
                Circle c = new Circle(rs[0], rs[1], 3.4);
                c.setFill(new RadialGradient(0, 0, 0.4, 0.4, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#d8dce4")),
                        new Stop(1, Color.web("#3a3a44"))));
                c.setStroke(Color.web("#0c0c10"));
                c.setStrokeWidth(0.6);
                g.getChildren().add(c);
            }
            return g;
        }

        private Group buildInterior() {
            Group g = new Group();
            Rectangle dark = new Rectangle(0, 0, DOOR_W, DOOR_H);
            LinearGradient lg = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#000")),
                    new Stop(0.6, Color.web("#0a0612")),
                    new Stop(1, Color.web("#1a0a22")));
            dark.setFill(lg);
            dark.setEffect(new InnerShadow(30, Color.web("#000")));
            g.getChildren().add(dark);

            if (role == Role.SCARER) {
                for (int i = 0; i < 5; i++) {
                    double y = 60 + i * 50;
                    Circle eye = new Circle(50 + (i % 2) * 110, y, 2.4, Color.web("#ff2a2a", 0.7));
                    g.getChildren().add(eye);
                }
            } else {
                for (int i = 0; i < 12; i++) {
                    double x = 20 + (i * 37) % (DOOR_W - 40);
                    double y = 30 + (i * 53) % (DOOR_H - 40);
                    Circle spark = new Circle(x, y, 1.6, Color.web("#fff0a8", 0.8));
                    g.getChildren().add(spark);
                }
            }
            return g;
        }

        private Group buildDoorPanel() {
            Group g = new Group();

            Color baseTop, baseBot, accent, trim;
            if (role == Role.SCARER) {
                baseTop = Color.web("#7a1410");
                baseBot = Color.web("#2a0606");
                accent = Color.web("#ff3a28");
                trim = Color.web("#3a0a08");
            } else {
                baseTop = Color.web("#ffb627");
                baseBot = Color.web("#b86508");
                accent = Color.web("#fff0a8");
                trim = Color.web("#5a2c04");
            }

            Rectangle panel = new Rectangle(0, 0, DOOR_W, DOOR_H);
            panel.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, baseTop), new Stop(1, baseBot)));
            panel.setStroke(trim);
            panel.setStrokeWidth(3);
            panel.setEffect(new DropShadow(14, Color.web("#000", 0.55)));
            g.getChildren().add(panel);

            Rectangle bevel = new Rectangle(8, 8, DOOR_W - 16, DOOR_H - 16);
            bevel.setFill(Color.TRANSPARENT);
            bevel.setStroke(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, baseTop.brighter()), new Stop(1, baseBot.darker())));
            bevel.setStrokeWidth(2);
            g.getChildren().add(bevel);

            Rectangle topPanel = new Rectangle(20, 24, DOOR_W - 40, 110);
            topPanel.setArcWidth(8);
            topPanel.setArcHeight(8);
            topPanel.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, baseBot), new Stop(1, baseTop.darker())));
            topPanel.setStroke(trim);
            topPanel.setStrokeWidth(2);
            topPanel.setEffect(new InnerShadow(8, Color.web("#000", 0.6)));
            g.getChildren().add(topPanel);

            Rectangle botPanel = new Rectangle(20, 160, DOOR_W - 40, DOOR_H - 200);
            botPanel.setArcWidth(8);
            botPanel.setArcHeight(8);
            botPanel.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, baseBot), new Stop(1, baseTop.darker())));
            botPanel.setStroke(trim);
            botPanel.setStrokeWidth(2);
            botPanel.setEffect(new InnerShadow(8, Color.web("#000", 0.6)));
            g.getChildren().add(botPanel);

            if (role == Role.SCARER) {
                addClawMarks(g, accent);
                addSkullPlate(g);
            } else {
                addSmileyPlate(g);
                addStars(g, accent);
            }

            double knobX = hinge == HingeSide.RIGHT ? 22 : DOOR_W - 22;
            double knobY = DOOR_H / 2 + 20;
            Circle knobBase = new Circle(knobX, knobY, 10);
            knobBase.setFill(metalGradient());
            knobBase.setStroke(Color.web("#0c0c10"));
            knobBase.setStrokeWidth(1.2);
            Circle knob = new Circle(knobX, knobY, 6);
            knob.setFill(new RadialGradient(0, 0, 0.35, 0.35, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#e2e6ee")),
                    new Stop(1, Color.web("#46464e"))));
            knob.setStroke(Color.web("#101014"));
            knob.setStrokeWidth(0.6);
            g.getChildren().addAll(knobBase, knob);

            Rectangle hingeStrip = new Rectangle(
                    hinge == HingeSide.RIGHT ? DOOR_W - 8 : 0,
                    24, 8, DOOR_H - 48);
            hingeStrip.setFill(metalGradient());
            g.getChildren().add(hingeStrip);
            for (int i = 0; i < 3; i++) {
                double hy = 60 + i * (DOOR_H - 120) / 2.0;
                Circle hr = new Circle(hinge == HingeSide.RIGHT ? DOOR_W - 4 : 4, hy, 2.2,
                        Color.web("#c0c4cc"));
                hr.setStroke(Color.web("#0a0a0e"));
                hr.setStrokeWidth(0.6);
                g.getChildren().add(hr);
            }

            Rectangle sheen = new Rectangle(0, 0, DOOR_W, DOOR_H);
            sheen.setFill(new LinearGradient(0, 0, 1, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#ffffff", 0.15)),
                    new Stop(0.5, Color.web("#ffffff", 0.0)),
                    new Stop(1, Color.web("#000000", 0.18))));
            sheen.setMouseTransparent(true);
            g.getChildren().add(sheen);

            return g;
        }

        private void addClawMarks(Group g, Color accent) {
            for (int i = 0; i < 3; i++) {
                double x = 50 + i * 18;
                Line claw = new Line(x, 200, x + 20, 320);
                claw.setStroke(accent);
                claw.setStrokeWidth(3);
                claw.setEffect(new Glow(0.4));
                g.getChildren().add(claw);
            }
        }

        private void addSkullPlate(Group g) {
            double cx = DOOR_W / 2.0, cy = 78;
            Circle plate = new Circle(cx, cy, 30);
            plate.setFill(new RadialGradient(0, 0, 0.4, 0.4, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#e8d8b0")),
                    new Stop(1, Color.web("#6a4a20"))));
            plate.setStroke(Color.web("#2a1808"));
            plate.setStrokeWidth(2);

            Circle eyeL = new Circle(cx - 9, cy - 4, 4, Color.web("#0a0a0e"));
            Circle eyeR = new Circle(cx + 9, cy - 4, 4, Color.web("#0a0a0e"));
            Polygon nose = new Polygon(cx, cy + 4, cx - 3, cy + 10, cx + 3, cy + 10);
            nose.setFill(Color.web("#0a0a0e"));
            Polyline teeth = new Polyline(
                    cx - 8, cy + 14, cx - 5, cy + 19,
                    cx - 2, cy + 14, cx + 1, cy + 19,
                    cx + 4, cy + 14, cx + 7, cy + 19);
            teeth.setStroke(Color.web("#0a0a0e"));
            teeth.setStrokeWidth(1.6);
            teeth.setFill(null);

            g.getChildren().addAll(plate, eyeL, eyeR, nose, teeth);
        }

        private void addSmileyPlate(Group g) {
            double cx = DOOR_W / 2.0, cy = 78;
            Circle plate = new Circle(cx, cy, 30);
            plate.setFill(new RadialGradient(0, 0, 0.35, 0.35, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#fff7c8")),
                    new Stop(1, Color.web("#d97706"))));
            plate.setStroke(Color.web("#5a2c04"));
            plate.setStrokeWidth(2);

            Circle eyeL = new Circle(cx - 9, cy - 6, 3, Color.web("#3a1a02"));
            Circle eyeR = new Circle(cx + 9, cy - 6, 3, Color.web("#3a1a02"));
            Arc mouth = new Arc(cx, cy + 2, 12, 8, 200, 140);
            mouth.setType(ArcType.OPEN);
            mouth.setStroke(Color.web("#3a1a02"));
            mouth.setStrokeWidth(2.2);
            mouth.setFill(null);

            g.getChildren().addAll(plate, eyeL, eyeR, mouth);
        }

        private void addStars(Group g, Color accent) {
            double[][] spots = {{40, 220}, {80, 260}, {130, 230}, {170, 270}, {60, 310}, {150, 320}};
            for (double[] p : spots) {
                Polygon star = star5(p[0], p[1], 7, 3);
                star.setFill(accent);
                star.setStroke(Color.web("#7a3a02"));
                star.setStrokeWidth(0.8);
                star.setEffect(new Glow(0.3));
                g.getChildren().add(star);
            }
        }

        private Polygon star5(double cx, double cy, double rOut, double rIn) {
            Polygon p = new Polygon();
            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + i * Math.PI / 5;
                double r = (i % 2 == 0) ? rOut : rIn;
                p.getPoints().add(cx + Math.cos(a) * r);
                p.getPoints().add(cy + Math.sin(a) * r);
            }
            return p;
        }

        private LinearGradient metalGradient() {
            return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#6a6d75")),
                    new Stop(0.45, Color.web("#3c3e44")),
                    new Stop(0.55, Color.web("#2a2c32")),
                    new Stop(1, Color.web("#16171b")));
        }
    }

    public static class Standalone extends Application {
        @Override
        public void start(Stage stage) {
            launchStandalone(stage, role ->
                    System.out.println("Selected: " + role));
        }
        public static void main(String[] args) {
            launch(args);
        }
    }
}
