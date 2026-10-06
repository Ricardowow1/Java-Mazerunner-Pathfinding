package game.logic;

import game.enums.Difficulty;
import game.enums.PlayerClass;
import java.io.File;
import java.util.Scanner;

/**
 * Gere a interface textual dos menus principais do jogo.
 * <p>
 * Responsável por apresentar o menu inicial, seleção de mapas e configuração
 * de personagens com uma formatação visual melhorada.
 * </p>
 */
public class GameMenu {

    private Scanner scanner;
    
    // Cores (Redundância local para garantir funcionamento isolado se necessário)
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    /**
     * Construtor do GameMenu.
     * @param scanner O scanner para leitura de dados.
     */
    public GameMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Apresenta o menu principal estilizado e recolhe a escolha do utilizador.
     * * @return O número inteiro correspondente à opção escolhida.
     */
    public int showMainMenu() {
        System.out.println("\n" + CYAN + "╔══════════════════════════════════════╗");
        System.out.println("║         LABIRINTO DA GLÓRIA          ║");
        System.out.println("╚══════════════════════════════════════╝" + RESET);
        System.out.println(" [1] 🗺️  JOGAR (Escolher Mapa)");
        System.out.println(" [2] 🎲 NOVO JOGO (Mapa Aleatório)");
        System.out.println(" [3] 📜 REGRAS");
        System.out.println(" [0] ❌ SAIR");
        System.out.print(GREEN + "> " + RESET);
        return readInt();
    }

    /**
     * Lista os mapas disponíveis na pasta 'maps' com ícones.
     * * @return O caminho relativo do ficheiro escolhido (ex: "maps/level1.json"), ou {@code null} se cancelado.
     */
    public String askForMapFile() {
        File dir = new File("maps"); 
        
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println(">> A pasta 'maps' não existia. Foi criada agora.");
            return null;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            System.out.println(">> A pasta 'maps' está vazia!");
            return null;
        }

        System.out.println("\n--- MAPAS DISPONÍVEIS ---");
        for (int i = 0; i < files.length; i++) {
            System.out.println(" [" + (i + 1) + "] 📄 " + files[i].getName());
        }
        System.out.println(" [0] 🔙 Cancelar");
        System.out.print(GREEN + "> " + RESET);

        while (true) {
            int choice = readInt();
            if (choice == 0) return null; 
            
            if (choice > 0 && choice <= files.length) {
                return files[choice - 1].getPath(); 
            }
            System.out.print(">> Opção inválida. Tenta de novo: ");
        }
    }

    /**
     * Exibe as regras do jogo formatadas.
     */
    public void showRules() {
        System.out.println("\n" + YELLOW + "--- 📜 REGRAS ---" + RESET);
        System.out.println("1. O objetivo é encontrar a sala do 🏆 TESOURO.");
        System.out.println("2. Tens Stamina limitada. Mover gasta 5 ⚡.");
        System.out.println("3. Podes descansar para recuperar energia.");
        System.out.println("4. Existem eventos aleatórios e armadilhas!");
        System.out.println("5. Usa as habilidades da tua classe para sobreviver.");
    }

    /**
     * Pede ao utilizador o número de jogadores para a partida.
     * @return Um inteiro validado (entre 3 e 4).
     */
    public int askNumberOfPlayers() {
        int num = 0;
        while (num < 3 || num > 4) {
            System.out.print("\nNúmero de Jogadores? (3 ou 4): ");
            num = readInt();
            if (num < 3 || num > 4) {
                System.out.println(">> Erro: Mínimo 3, Máximo 4 jogadores.");
            }
        }
        return num;
    }

    /**
     * Pede a dificuldade dos Bots.
     * @return O Enum {@link Difficulty} selecionado.
     */
    public Difficulty askDifficulty() {
        System.out.println("\n--- DIFICULDADE (BOTS) ---");
        System.out.println("1. 👶 FÁCIL   (Erram muito)");
        System.out.println("2. 🧑 NORMAL  (Equilibrados)");
        System.out.println("3. 🧠 DIFÍCIL (Inteligentes)");
        System.out.print(GREEN + "> " + RESET);
        
        while (true) {
            int c = readInt();
            if (c == 1) return Difficulty.FACIL;
            if (c == 2) return Difficulty.NORMAL;
            if (c == 3) return Difficulty.DIFICIL;
            System.out.print(">> Opção inválida. Escolhe 1, 2 ou 3: ");
        }
    }

    /**
     * Pede ao utilizador para escolher a classe de uma personagem.
     * * @param name O nome do jogador que está a ser configurado.
     * @return A {@link PlayerClass} escolhida.
     */
    public PlayerClass askPlayerClass(String name) {
        while (true) {
            System.out.println("\nClasse para " + YELLOW + name + RESET + ":");
            System.out.println(" [1] 🛡️  HERÓI       (+HP, +Stamina)");
            System.out.println(" [2] 🤠 AVENTUREIRO (Começa com itens)");
            System.out.println(" [3] 🗡️  BANDIDO     (Rouba itens)"); 
            System.out.println(" [4] 🥷 NINJA       (Esquiva de armadilhas)");
            System.out.println(" [5] 🔮 MAGO        (Dano em área)");
            System.out.print(GREEN + "> " + RESET);
            
            int c = readInt();
            switch (c) {
                case 1: return PlayerClass.HEROI;
                case 2: return PlayerClass.AVENTUREIRO;
                case 3: return PlayerClass.BANDIDO; 
                case 4: return PlayerClass.NINJA;
                case 5: return PlayerClass.MAGO;
                default: System.out.println(">> Opção inválida. Tenta de novo.");
            }
        }
    }

    /**
     * Helper privado para ler inteiros de forma segura.
     * @return O inteiro lido ou -1 em caso de exceção.
     */
    private int readInt() {
        try { 
            return Integer.parseInt(scanner.nextLine().trim()); 
        } catch (Exception e) { 
            return -1; 
        }
    }
}