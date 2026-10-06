# 🏰 Labirinto da Glória

Um jogo de exploração de masmorras (Dungeon Crawler) por turnos, desenvolvido em Java. O projeto utiliza uma arquitetura **MVC** (Model-View-Controller) e não depende de bibliotecas externas para processamento de JSON.

## 📋 Sobre o Projeto

O objetivo do jogo é encontrar a **Sala do Tesouro**. Os jogadores (Humanos ou Bots) devem navegar por um labirinto gerado aleatoriamente ou carregado de um ficheiro, gerindo a sua **Vida (HP)** e **Energia (Stamina)**.

O jogo termina quando um jogador encontra o tesouro (e ativa as alavancas necessárias) ou quando todos os jogadores morrem.

### ✨ Funcionalidades Principais

* **Geração Procedural:** Criação de mapas aleatórios com garantia de caminhos válidos.
* **Inteligência Artificial:** Bots com 3 níveis de dificuldade (Fácil, Normal, Difícil).
    * *Nota:* O nível Difícil utiliza o algoritmo **BFS (Breadth-First Search)** para encontrar o caminho mais curto para objetivos.
* **Sistema de Eventos:** Armadilhas, curas, teletransportes e o evento "Caos Total".
* **Mecânicas de Jogo:**
    * Enigmas para desbloquear salas.
    * Alavancas para destrancar o Tesouro.
    * Sistema de Inventário limitado (Mochila).
    * Habilidades únicas por Classe (Roubo, Magia, etc.).
* **JSON Parser Personalizado:** Leitura e escrita de ficheiros JSON implementada manualmente (`JsonLoader` e `JsonSaver`).
* **Relatórios:** Geração de um ficheiro JSON no final da partida com o histórico de todas as jogadas.

## 🚀 Como Executar

### Pré-requisitos
* Java JDK 8 ou superior.

### Compilar e Correr (Terminal)

1.  Navegue até à pasta raiz do projeto.
2.  Compile todos os ficheiros Java:
    ```bash
    javac -d bin src/**/*.java
    ```
3.  Execute o jogo:
    ```bash
    java -cp bin Main
    ```
    *(Nota: Se a classe Main estiver dentro de um package, use `java -cp bin game.Main`)*

## 🎮 Como Jogar

1.  **Menu Principal:** Escolha entre carregar um mapa existente (`maps/`) ou gerar um novo mundo.
2.  **Setup:** Defina a dificuldade dos Bots, o número de jogadores e a sua classe.
3.  **Classes Disponíveis:**
    * 🛡️ **Herói:** Mais Vida e Stamina.
    * 🎒 **Aventureiro:** Começa com itens aleatórios.
    * 🗡️ **Bandido:** Pode roubar itens de outros jogadores.
    * 💨 **Ninja:** Tem 50% de chance de evitar armadilhas/dano.
    * 🔮 **Mago:** Pode lançar um feitiço de dano global.
4.  **Durante o Turno:**
    * Gaste **5 Stamina** para se mover.
    * Resolva Enigmas para entrar em salas especiais.
    * Use itens (Poções, Comida) para recuperar.
    * Procure **Alavancas** se a porta do Tesouro estiver trancada.

## 🛠️ Arquitetura do Projeto

O código está organizado seguindo o padrão MVC:

* **`src/game/models`**: Dados puros (Player, Location, Item, Lever, Enigma).
* **`src/game/view`**: Interface de consola (GameView).
* **`src/game/logic`**: Regras de negócio (GameController, TurnManager, EventHandler).
* **`src/game/ai`**: Estratégia dos Bots (BotBrain, BotHard, BotEasy...).
* **`src/game/utils`**: Ferramentas auxiliares (Pathfinder, JsonLoader, MapGenerator).
* **`LinkedList / Stack`**: Estruturas de dados personalizadas.

## 🧠 Detalhes da IA (Bot Hard)

O Bot de dificuldade "Difícil" foi desenhado para ser competitivo:
1.  **Analisa o Estado:** Verifica se precisa de cura ou stamina antes de agir.
2.  **Decide o Objetivo:**
    * Se o Tesouro está *Trancado* -> Procura a **Alavanca** mais próxima.
    * Se o Tesouro está *Aberto* -> Procura o **Tesouro**.
3.  **Pathfinding:** Usa um algoritmo de busca em largura (BFS) na classe `Pathfinder` para calcular a rota ótima.
4.  **Memória:** Guarda os IDs das salas visitadas para evitar andar em círculos.

---
### 💡 Dica para Windows (Emojis)
Para garantir que os emojis e caracteres especiais aparecem corretamente no terminal do Windows (PowerShell), execute este comando antes de iniciar o jogo:

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8