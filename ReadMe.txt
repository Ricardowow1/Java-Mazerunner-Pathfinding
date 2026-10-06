# 🏰 Mazerunner: The Labyrinth of Glory

A turn-based Dungeon Crawler developed entirely in Java. This project strictly follows the **MVC** (Model-View-Controller) architecture and relies exclusively on custom data structures and a proprietary JSON parser, avoiding any external libraries.

## 📋 About the Project

The ultimate goal is to find and secure the **Treasure Room**. Players (Human or Autonomous Bots) must navigate a dynamically generated or pre-loaded maze, carefully managing their **Health (HP)** and **Stamina**.

The game ends when a player successfully claims the treasure (after activating any required levers) or when all players perish in the dungeon.

### ✨ Key Features

* **Procedural Generation:** Dynamic map creation algorithm that guarantees valid, traversable paths.
* **Artificial Intelligence:** Bots programmed with 3 difficulty tiers (Easy, Normal, Hard).
    * *Note:* The Hard difficulty implements the **BFS (Breadth-First Search)** algorithm to calculate the shortest optimal path to its objectives.
* **Dynamic Event System:** Includes hidden traps, healing zones, teleports, and a global "Total Chaos" event.
* **Core Mechanics:**
    * Riddles and Enigmas to unlock special rooms.
    * Levers scattered across the map to unseal the Treasure.
    * Limited inventory management (Backpack system).
    * Unique Class-based abilities (e.g., Stealing, Global Magic).
* **Custom JSON Parser:** Manual implementation of JSON reading/writing mechanics (`JsonLoader` and `JsonSaver`) without relying on Gson or Jackson.
* **Match Reports:** Automatically exports a detailed JSON log containing the history of all actions at the end of each session.

## 🚀 How to Run

### Prerequisites
* Java JDK 8 or higher.

### Compile and Run (Terminal)

1. Clone the repository:
   ```bash
   git clone [https://github.com/Ricardowow1/Java-Mazerunner-Pathfinding.git](https://github.com/Ricardowow1/Java-Mazerunner-Pathfinding.git)
   ```
2. Navigate to the root folder and compile all Java files:
   ```bash
   javac -d bin src/**/*.java
   ```
3. Execute the game:
   ```bash
   java -cp bin Main
   ```
   *(Note: If the Main class is inside a package, use `java -cp bin game.Main`)*

## 🎮 How to Play

1. **Main Menu:** Choose to load an existing map (`maps/`) or generate a new random world.
2. **Setup:** Define bot difficulty, number of players, and select your class.
3. **Available Classes:**
    * 🛡️️ **Hero:** Higher base HP and Stamina.
    * 🎒 **Adventurer:** Spawns with random starting items.
    * 🗡️ **Bandit:** Ability to steal items from other players in the same room.
    * 💨 **Ninja:** 50% evasion chance against traps and incoming damage.
    * 🔮 **Mage:** Can cast a global area-of-effect damage spell.
4. **During your Turn:**
    * Spend **5 Stamina** to move to an adjacent room.
    * Solve Enigmas to bypass locked doors.
    * Consume items (Potions, Food) to restore stats.
    * Locate and pull **Levers** if the Treasure Room is sealed.

## 🛠️ Project Architecture

The codebase is strictly organized following the MVC pattern:

* **`src/game/models`**: Pure data entities (Player, Location, Item, Lever, Enigma).
* **`src/game/view`**: Console interface and UI rendering (GameView).
* **`src/game/logic`**: Core business rules (GameController, TurnManager, EventHandler).
* **`src/game/ai`**: Bot strategies and decision trees (BotBrain, BotHard, BotEasy).
* **`src/game/utils`**: Auxiliary tools (Pathfinder, JsonLoader, MapGenerator).
* **`LinkedList / Stack`**: Custom-built data structures replacing standard Java Collections.

## 🧠 AI Deep Dive (Hard Bot)

The "Hard" difficulty Bot was designed to be highly competitive and efficient:
1. **State Analysis:** Evaluates its own HP and Stamina to decide if it needs to rest or heal before acting.
2. **Goal Processing:**
    * If the Treasure is *Locked* -> Locates the nearest **Lever**.
    * If the Treasure is *Unlocked* -> Rushes to the **Treasure Room**.
3. **Pathfinding:** Leverages a **Breadth-First Search (BFS)** algorithm inside the `Pathfinder` class to compute the optimal route.
4. **Memory:** Stores the IDs of previously visited rooms to prevent looping and backtracking.

---
### 💡 Windows Terminal Tip (UTF-8 & Emojis)
To ensure emojis and special characters render correctly in the Windows terminal (PowerShell), run this command before starting the game:

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```
