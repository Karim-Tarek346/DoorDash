DoorDasH - Tester Guide
=======================

A JavaFX board game (Scarer vs Laugher). This document is for testers and
covers how to launch, exercise, and reset the game. It does NOT contain
gameplay rules.


LAUNCHING
---------

Required runtime: Java JDK 17.

1. Open the project in Eclipse (any recent version).
2. Make sure the build path uses JDK 17.
3. Run the class:  game.gui.MainMenu


!!! IMPORTANT - WINDOW FIT WORKAROUND !!!
-----------------------------------------

If the game does NOT fit the window on first launch, exit Fullscreen and
enter Fullscreen again. After that, the layout will scale to any window
size from 900x650 up to your full screen.

This applies to maximized and Fullscreen windows alike: toggle once and
the fit binding takes effect.

!!! ------------------------------------ !!!


MODES
-----

Solo            Player vs bot opponent.
Multiplayer     Two players share the same keyboard.

Both are selected from the main menu before role selection.


GAMEPLAY CONTROLS
-----------------

These are the input keys you need to drive the game during testing.

  SPACE         Player rolls the dice
  P             Player uses powerup
  ENTER         Opponent rolls the dice          (multiplayer only)
  O             Opponent uses powerup            (multiplayer only)
  Gear icon     Settings popup (top-left of game window)


SETTINGS POPUP
--------------

Click the gear icon at the top-left of the game scene to open the settings
overlay. It contains:

  - Background music volume slider
  - Game sounds volume slider
  - Restart Game button (restarts the current match)
  - Exit to Main Menu button


CARD DRAWS
----------

Landing on a Card cell pops a themed dialog showing the drawn card's
name, description, type icon, and an OK button. Gameplay pauses until
OK is clicked. This dialog appears for both the player and the opponent
(including bot draws in Solo mode).


CHEAT CODES (for testing)
-------------------------

Cheat keys are always live during gameplay. A bare key targets the
CURRENT player (whoever's turn it is). Holding Shift targets the
OPPONENT for cheats that apply to a player.

Cheats 1-9 are blocked while a turn animation is running or after the
game ends. H (help) and Q (reset) always work.

  Key   Cheat
  ---   --------------------------------------------------------------
  W     Win                              (Shift = opponent wins)
  E     Set energy -> prompt int         (Shift = opponent)
  T     Teleport -> prompt cell 0-99     (Shift = opponent)
  R     Force next roll -> prompt 1-6
  C     Force next card -> prompt name (shield / swapper /
                                        energysteal / confusion /
                                        startover; case-insensitive
                                        prefix)
  X     Trigger exception -> prompt:
          out    = zero current energy (next powerup throws
                   OutOfEnergyException)
          move   = place opponent on next-landing cell (next roll
                   throws InvalidMoveException)
          turn   = pop a fake InvalidTurn dialog
        Shift swaps which player is the "current" for the setup.
  F     Toggle frozen                    (Shift = opponent)
  S     Toggle shielded                  (Shift = opponent)
  N     Toggle confused (2 turns)        (Shift = opponent)
  Q     Reset all cheat state, unfreeze / unshield / unconfuse both
  H     Toggle the on-screen cheat help overlay  (backtick ` also works)

The audit trail of every cheat is printed in the in-game log box.


WHAT NOT TO EXPECT
------------------

- No save state. Each launch / restart starts a fresh match.
- No mid-game resume after closing the window.
- No leaderboard or persistent profile.


FILES TO BE AWARE OF
--------------------

  cards.csv       Card definitions (type, name, description, rarity).
  cells.csv       Board cell definitions.
  monsters.csv    Monster definitions.

These are read from the working directory at launch. If a CSV is malformed
an InvalidCSVFormat exception is thrown at startup.
