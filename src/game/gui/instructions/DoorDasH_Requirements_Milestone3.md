# DoorDasH: Scare vs Laugh Touchdown
## Milestone 3 - GUI Implementation

**Course:** CSEN 401 - Computer Programming Lab, Spring 2024  
**University:** German University in Cairo  
**Media Engineering and Technology**

**Instructors:**
- Prof. Dr. Slim Abdennadher
- Dr. Nourhan Ehab
- Dr. Ahmed Abdelfattah

---

## Overview

In this milestone, you are required to implement the GUI to be able to play the game.

**Deadline:** 18.5.2026 @ 11:59 PM

---

## 1. General Guidelines

### Core Principles

- **Engine-GUI Synchronization**: The effects of any action performed in the GUI should be reflected in the engine and vice versa.

- **Full Visibility**: The player should be able to view all content at all times without the need to resize/minimize/maximize the window during runtime.

- **Current Action Indication**: The action that is currently happening in the game should always be clearly indicated in the GUI.

- **Exception Handling**: Make sure to handle all exceptions and validations for any input or action performed. In case any exception implemented in the second milestone arises the player should be notified and the action should be prohibited and another action should be chosen by the player.

- **Graceful Error Handling**: The game should not be stopped/terminated for any exception thrown. However, clicking the 'X' button on the window must be able to terminate the game at any instant.

### Architectural Pattern

Try to adhere to the **MVC (Model-View-Controller)** architectural pattern to organize the codebase, enhancing maintainability and scalability:

- **Model**: Manages the data, logic, and rules of the application independently of the user interface.

- **View**: Represents the GUI which displays game information to the player. The view should be dynamic and reflect changes made in the model.

- **Controller**: Accepts input, converting it into commands for the model or view. This ensures that the UI is separated from the data processing.

### Technology Constraint

- You are **not allowed to use Swing** for the GUI, only **JavaFX** is allowed.
- You're **free to use SceneBuilder** when building the GUI.

---

## 2. GUI Requirements Checklist

### Start Window

**Must be displayed whenever the player starts the game:**

- [ ] A way to select a side (either SCARER or LAUGHER)
- [ ] A way to start and play the game according to the chosen side
- [ ] Game instructions

### Game Board

**Must display the following:**

- [ ] Game board with all 100 cells in their correct positions and types
- [ ] Cell index number visible on each cell
- [ ] Visible difference in cell types:
  - SCARER doors
  - LAUGHER doors
  - Monster Cells
  - Card Cells
  - Conveyor Belts
  - Contamination Socks
  - Normal Cells
- [ ] Doors energy value must be visible
- [ ] Both monsters initialized at cell 0 with their correct starting energy

### Cards

- [ ] 25 shuffled cards for card cells

### Player Actions

**Player must be able to perform the following actions:**

- [ ] Choose whether to activate their powerup before rolling
- [ ] Roll the dice to move their monster

### Player Tracking

**Player should keep track of the following throughout the game:**

- [ ] Current turn
- [ ] Current player and opponent
- [ ] Result of each dice roll
- [ ] Card drawn whenever a Card Cell is landed on, including its name and effect
- [ ] Player skipping their turn (Freeze effect) must be clearly indicated

### Monster Information Display

**Must be displayed for each monster:**

- [ ] Name
- [ ] Original Role (SCARER / LAUGHER)
- [ ] Current Role to indicate confusion
- [ ] Type (Dasher / Dynamo / Multitasker / Schemer)
- [ ] Current energy
- [ ] Current position on the board
- [ ] Active status effects (shield, confusion turns remaining, etc)

### Board Updates

**Must be shown and updated on the board:**

- [ ] Cell effects must be indicated (changed position, energy, etc)
- [ ] Activated / exhausted door cells must be distinguishable from non-activated door cells
- [ ] Monster Cells must display the stationed monster's identity
- [ ] Any energy change to any monster must be indicated
- [ ] Shield blocking an energy loss must be indicated

### Card Display Updates

**Must be shown and updated for each card:**

- [ ] Card name and effect must be displayed when drawn
- [ ] Card effect must be indicated (Swap, start over, etc)

### Turn End Updates

**Must be shown and updated whenever a player ends a turn:**

- [ ] Both monsters' positions updated on the board
- [ ] Energy values updated for all affected monsters
- [ ] Status effect durations (Confusion, Momentum Rush, Focus Mode, Freeze, etc)
- [ ] Role confusion must be visually indicated when a monster's role is temporarily swapped

### Error Handling Display

**Must be shown for any invalid action:**

- [ ] Indicator to the player and reasons why the action could not be performed
- [ ] The game should not be stopped/terminated for any invalid action
- [ ] Closing the popup should NOT terminate the game

### Game End Screen

**Must be shown whenever a player wins:**

- [ ] Game Won / Game Over Screen
- [ ] The winning monster's name and role must be announced
- [ ] Final energy of both monsters must be displayed
- [ ] Player must be able to return to the start window

### Optional Enhancements

**Any specific one of the following can be done additionally:**

- [ ] Fantastic GUI + Animations

---

## Summary of Monster Types and Roles

### Monster Roles
- **SCARER**: Original role, may be confused temporarily
- **LAUGHER**: Original role, may be confused temporarily

### Monster Types
- **Dasher**: Fast movement capability
- **Dynamo**: High energy/power
- **Multitasker**: Multiple action capability
- **Schemer**: Strategic ability

### Special Cells and Effects

#### Cell Types
- **SCARER Doors**: Energy-related effects for SCARER role
- **LAUGHER Doors**: Energy-related effects for LAUGHER role
- **Monster Cells**: Stationary monsters that interact with moving monsters
- **Card Cells**: Draw a card with special effects
- **Conveyor Belts**: Automatic movement
- **Contamination Socks**: Negative effects
- **Normal Cells**: No special effect

#### Status Effects
- **Shield**: Blocks one energy loss
- **Confusion**: Monster temporarily swaps roles
- **Momentum Rush**: Increased movement speed
- **Focus Mode**: Enhanced abilities
- **Freeze**: Skip next turn

---

## Notes for Implementation

1. Ensure proper separation of concerns using MVC pattern
2. Keep the model independent from the view
3. Use the controller to handle all user interactions
4. Provide immediate visual feedback for all actions
5. Test exception handling thoroughly
6. Ensure the GUI is responsive and doesn't freeze during gameplay
7. Make sure all game state changes are reflected in the GUI in real-time
8. Consider using JavaFX's binding mechanisms for automatic GUI updates

---

## Resources

- JavaFX Documentation: Official Oracle JavaFX documentation
- SceneBuilder: GUI builder tool for JavaFX applications
- MVC Architecture: Design pattern for separating concerns in application development
