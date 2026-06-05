package game.gui.view;

import game.engine.Constants;
import game.engine.Game;
import game.engine.cards.Card;
import game.engine.cards.ConfusionCard;
import game.engine.cards.EnergyStealCard;
import game.engine.cards.ShieldCard;
import game.engine.cards.StartOverCard;
import game.engine.cards.SwapperCard;
import game.engine.monsters.Monster;
import game.gui.util.ThemedAlert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public final class CheatCodes {

    public static Integer nextForcedRoll;
    public static Class<? extends Card> nextForcedCard;

    private static Node helpOverlay;

    private CheatCodes() { }

    public static void install(Scene scene, GameView view, Game game) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getTarget() instanceof TextField) return;

            KeyCode code = e.getCode();
            boolean shift = e.isShiftDown();

            if (code == KeyCode.H || code == KeyCode.BACK_QUOTE || code == KeyCode.DEAD_TILDE) {
                toggleHelp(view);
                e.consume();
                return;
            }
            if (code == KeyCode.Q) {
                resetAll(view, game);
                e.consume();
                return;
            }

            if (view.turnInProgress || view.gameEnded) return;

            if (code == KeyCode.W) {
                instantWin(view, game, shift);
                e.consume();
            } else if (code == KeyCode.E) {
                setEnergy(view, game, shift);
                e.consume();
            } else if (code == KeyCode.T) {
                teleport(view, game, shift);
                e.consume();
            } else if (code == KeyCode.R) {
                forceRoll(view);
                e.consume();
            } else if (code == KeyCode.C) {
                forceCard(view);
                e.consume();
            } else if (code == KeyCode.X) {
                triggerException(view, game, shift);
                e.consume();
            } else if (code == KeyCode.F) {
                toggleFrozen(view, game, shift);
                e.consume();
            } else if (code == KeyCode.S) {
                toggleShielded(view, game, shift);
                e.consume();
            } else if (code == KeyCode.N) {
                toggleConfused(view, game, shift);
                e.consume();
            }
        });
    }

    private static Monster target(Game game, boolean shift) {
        Monster cur = game.getCurrent();
        if (!shift) return cur;
        return cur == game.getPlayer() ? game.getOpponent() : game.getPlayer();
    }

    private static String label(Game game, Monster m) {
        return m.getName() + (m == game.getPlayer() ? " (player)" : " (opponent)");
    }

    private static void instantWin(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        t.setPosition(Constants.WINNING_POSITION);
        t.setEnergyExact(Constants.WINNING_ENERGY);
        view.log("CHEAT: instant win on " + label(game, t));
        view.refreshTurnUi();
        view.checkWinner();
    }

    private static void setEnergy(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        ThemedAlert.prompt(view.root, "Set Energy",
                "Set " + label(game, t) + "'s energy to:",
                "e.g. 0, 499, 1000",
                value -> {
                    try {
                        int n = Integer.parseInt(value);
                        t.setEnergyExact(n);
                        view.log("CHEAT: " + label(game, t) + " energy = " + t.getEnergy());
                        view.refreshTurnUi();
                    } catch (NumberFormatException ex) {
                        view.log("CHEAT: bad number '" + value + "'");
                    }
                });
    }

    private static void teleport(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        ThemedAlert.prompt(view.root, "Teleport",
                "Move " + label(game, t) + " to cell index (0-" + (Constants.BOARD_SIZE - 1) + "):",
                "0-99",
                value -> {
                    try {
                        int pos = Integer.parseInt(value);
                        pos = Math.max(0, Math.min(Constants.BOARD_SIZE - 1, pos));
                        t.setPosition(pos);
                        view.log("CHEAT: teleported " + label(game, t) + " to " + pos);
                        view.refreshTurnUi();
                    } catch (NumberFormatException ex) {
                        view.log("CHEAT: bad number '" + value + "'");
                    }
                });
    }

    private static void forceRoll(GameView view) {
        ThemedAlert.prompt(view.root, "Force Next Roll",
                "Next dice roll value (1-6):",
                "1-6",
                value -> {
                    try {
                        int n = Integer.parseInt(value);
                        if (n < 1 || n > 6) {
                            view.log("CHEAT: roll must be 1-6, got " + n);
                            return;
                        }
                        nextForcedRoll = n;
                        view.log("CHEAT: next roll forced to " + n);
                    } catch (NumberFormatException ex) {
                        view.log("CHEAT: bad number '" + value + "'");
                    }
                });
    }

    private static void forceCard(GameView view) {
        ThemedAlert.prompt(view.root, "Force Next Card",
                "Card type: shield / swapper / energysteal / confusion / startover",
                "shield",
                value -> {
                    Class<? extends Card> cls = matchCardType(value);
                    if (cls == null) {
                        view.log("CHEAT: unknown card '" + value + "'");
                        return;
                    }
                    nextForcedCard = cls;
                    view.log("CHEAT: next card forced to " + cls.getSimpleName());
                });
    }

    private static Class<? extends Card> matchCardType(String input) {
        String s = input.toLowerCase().replace(" ", "").replace("card", "");
        if (s.startsWith("shi")) return ShieldCard.class;
        if (s.startsWith("swa")) return SwapperCard.class;
        if (s.startsWith("ene") || s.startsWith("ste")) return EnergyStealCard.class;
        if (s.startsWith("con")) return ConfusionCard.class;
        if (s.startsWith("sta")) return StartOverCard.class;
        return null;
    }

    private static void triggerException(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        ThemedAlert.prompt(view.root, "Trigger Exception",
                "Type: out (OutOfEnergy on next powerup), move (InvalidMove on next roll), turn (InvalidTurn dialog)",
                "out / move / turn",
                value -> {
                    String s = value.toLowerCase();
                    if (s.startsWith("out")) {
                        t.setEnergyExact(0);
                        view.log("CHEAT: " + label(game, t) + " energy=0; powerup will throw OutOfEnergyException");
                        view.refreshTurnUi();
                    } else if (s.startsWith("mov")) {
                        Monster opp = (t == game.getPlayer()) ? game.getOpponent() : game.getPlayer();
                        int roll = (nextForcedRoll != null) ? nextForcedRoll : 3;
                        int landing = (t.getPosition() + roll) % Constants.BOARD_SIZE;
                        opp.setPosition(landing);
                        view.log("CHEAT: opponent placed at " + landing + "; " + label(game, t)
                                + "'s next roll will throw InvalidMoveException");
                        view.refreshTurnUi();
                    } else if (s.startsWith("tur")) {
                        view.log("CHEAT: faking InvalidTurnException dialog");
                        view.popError("Invalid Turn", "It's not your turn.");
                    } else {
                        view.log("CHEAT: unknown exception '" + value + "'");
                    }
                });
    }

    private static void toggleFrozen(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        boolean now = !t.isFrozen();
        t.setFrozen(now);
        view.log("CHEAT: " + label(game, t) + " frozen=" + now);
        view.refreshTurnUi();
    }

    private static void toggleShielded(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        boolean now = !t.isShielded();
        t.setShielded(now);
        view.log("CHEAT: " + label(game, t) + " shielded=" + now);
        view.refreshTurnUi();
    }

    private static void toggleConfused(GameView view, Game game, boolean shift) {
        Monster t = target(game, shift);
        if (t.isConfused()) {
            t.setConfusionTurns(0);
            t.setRole(t.getOriginalRole());
            view.log("CHEAT: " + label(game, t) + " confusion cleared");
        } else {
            t.setConfusionTurns(2);
            view.log("CHEAT: " + label(game, t) + " confused for 2 turns");
        }
        view.refreshTurnUi();
    }

    private static void resetAll(GameView view, Game game) {
        nextForcedRoll = null;
        nextForcedCard = null;
        for (Monster m : new Monster[] { game.getPlayer(), game.getOpponent() }) {
            m.setFrozen(false);
            m.setShielded(false);
            if (m.isConfused()) {
                m.setConfusionTurns(0);
                m.setRole(m.getOriginalRole());
            }
        }
        view.log("CHEAT: all cheat state reset");
        view.refreshTurnUi();
    }

    private static void toggleHelp(GameView view) {
        if (helpOverlay == null) {
            helpOverlay = buildHelpOverlay();
            view.root.getChildren().add(helpOverlay);
        } else if (view.root.getChildren().contains(helpOverlay)) {
            view.root.getChildren().remove(helpOverlay);
        } else {
            view.root.getChildren().add(helpOverlay);
        }
    }

    private static Node buildHelpOverlay() {
        VBox panel = new VBox(6);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(18, 22, 18, 22));
        panel.setMaxWidth(520);
        panel.setMaxHeight(420);
        panel.setBackground(new Background(new BackgroundFill(
                ThemedAlert.cardGradient(), new CornerRadii(14), Insets.EMPTY)));
        panel.setStyle(
                "-fx-border-color: #6a8aff;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 14;"
                + "-fx-background-radius: 14;");

        Text title = new Text("CHEAT CODES  (H to close)");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 22));
        title.setFill(Color.web("#ffd84d"));
        panel.getChildren().add(title);

        String[] lines = {
                "W   Win  (Shift = opponent)",
                "E   Energy  -> prompt  (Shift = opponent)",
                "T   Teleport  -> prompt 0-99  (Shift = opponent)",
                "R   Roll force  -> prompt 1-6",
                "C   Card force  -> prompt name",
                "X   eXception  -> out / move / turn  (Shift swaps target)",
                "F   Freeze toggle  (Shift = opponent)",
                "S   Shield toggle  (Shift = opponent)",
                "N   coNfusion toggle  (Shift = opponent)",
                "Q   Reset all cheats",
                "H   Toggle this help  (or `)",
        };
        for (String s : lines) {
            Text t = new Text(s);
            t.setFont(Font.font("Verdana", 13));
            t.setFill(Color.web("#cfd8ff"));
            panel.getChildren().add(t);
        }

        StackPane wrap = new StackPane(panel);
        StackPane.setAlignment(panel, Pos.CENTER);
        wrap.setPickOnBounds(true);
        wrap.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        return wrap;
    }
}
