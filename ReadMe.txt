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
