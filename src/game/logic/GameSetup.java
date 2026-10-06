package game.logic;

import game.models.Location;
import game.models.Player;
import game.models.GameEvent;
import game.utils.JsonLoader;
import game.enums.Difficulty;
import game.enums.PlayerClass;
import LinkedList.LinkedList;

import java.util.Random;
import java.util.Scanner;

/**
 * Responsável pela configuração inicial de uma nova partida.
 * <p>
 * Esta classe encarrega-se de carregar os dados do mapa e dos eventos,
 * criar a lista de jogadores (humanos e bots) e definir as suas posições iniciais.
 * </p>
 */
public class GameSetup {

    private GameMenu menu;
    private Scanner scanner;
    private LinkedList<GameEvent> loadedEvents;

    public GameSetup(Scanner scanner, GameMenu menu) {
        this.scanner = scanner;
        this.menu = menu;
        this.loadedEvents = new LinkedList<>(); 
    }

    /**
     * Carrega o grafo do mapa a partir de um ficheiro JSON e carrega também os eventos.
     *
     * @param filename Caminho do ficheiro do mapa.
     * @return O nó inicial (Raiz) do mapa carregado, ou {@code null} em caso de erro.
     */
    public Location loadMap(String filename) {
        System.out.println("--- A CARREGAR MUNDO (" + filename + ")... ---");
        Location startNode = JsonLoader.loadLocations(filename); 
        
        if (startNode == null) {
            System.out.println("[ERRO] Não foi possível carregar " + filename);
            return null;
        }

        this.loadedEvents = JsonLoader.loadEvents("events.json");
        return startNode;
    }

    /**
     * Cria a lista de jogadores e bots, interagindo com o utilizador para obter nomes e classes.
     * Distribui aleatoriamente os bots e permite aos humanos escolher a entrada.
     *
     * @param mapRoot A localização raiz do mapa para procurar pontos de entrada.
     * @return A lista ligada contendo todos os objetos {@link Player} criados.
     */
    public LinkedList<Player> createPlayers(Location mapRoot) {
        LinkedList<Player> players = new LinkedList<>();
        Random random = new Random();
        
        Difficulty diff = menu.askDifficulty();

        System.out.println("\n=== MODO DE JOGO ===");
        System.out.println("1. Manual (Apenas Humanos)");
        System.out.println("2. Automático (Humanos vs Bots)");
        System.out.print("> ");
        int mode = 1;
        try { 
            if (scanner.hasNextInt()) mode = scanner.nextInt(); 
            scanner.nextLine(); 
        } catch(Exception e) { scanner.nextLine(); }

        // Encontrar todas as entradas possíveis
        LinkedList<Location> entradasDisponiveis = findAllEntrances(mapRoot);
        if (entradasDisponiveis.isEmpty()) entradasDisponiveis.add(mapRoot);
        
        System.out.println("Entradas encontradas: " + entradasDisponiveis.size());

        int totalPlayers = menu.askNumberOfPlayers(); 

        int numHumans = totalPlayers;
        if (mode == 2) {
            System.out.print("Desses " + totalPlayers + ", quantos são HUMANOS? ");
            try { 
                if (scanner.hasNextInt()) numHumans = scanner.nextInt();
                scanner.nextLine();
                if (numHumans > totalPlayers) numHumans = totalPlayers;
                if (numHumans < 0) numHumans = 0;
            } catch(Exception e){ scanner.nextLine(); numHumans = 1; }
        }

        for (int i = 1; i <= totalPlayers; i++) {
            boolean isBot = (i > numHumans); 
            String name = isBot ? "Bot_" + i : "Jogador_" + i;

            if (!isBot) {
                System.out.println("\n--- Jogador " + i + " ---");
                System.out.print("Nome: ");
                String in = scanner.nextLine().trim();
                if(!in.isEmpty()) name = in;
            }

            PlayerClass pc;
            // Bots são sempre Heróis por defeito para simplificar
            if (isBot) pc = PlayerClass.HEROI; 
            else pc = menu.askPlayerClass(name);

            Location startLoc = null;

            if (isBot) {
                int r = random.nextInt(entradasDisponiveis.size());
                startLoc = entradasDisponiveis.get(r);
                System.out.println("🤖 " + name + " entrou em: " + startLoc.getName());
            } else {
                System.out.println("Escolha a entrada:");
                for(int j=0; j<entradasDisponiveis.size(); j++) {
                    System.out.println("[" + j + "] " + entradasDisponiveis.get(j).getName());
                }
                
                int choice = 0;
                try { 
                    System.out.print("> ");
                    if (scanner.hasNextInt()) choice = scanner.nextInt();
                    scanner.nextLine();
                } catch(Exception e){ scanner.nextLine(); }
                
                if (choice < 0 || choice >= entradasDisponiveis.size()) choice = 0;
                startLoc = entradasDisponiveis.get(choice);
            }

            players.add(new Player(name, startLoc, diff, pc, isBot));
        }
        
        System.out.println("\n[OK] Jogadores criados!");
        return players;
    }

    /**
     * Pesquisa no grafo todas as localizações marcadas como "ENTRADA".
     *
     * @param root O nó inicial da pesquisa.
     * @return Uma lista de localizações onde é possível iniciar o jogo.
     */
    private LinkedList<Location> findAllEntrances(Location root) {
        LinkedList<Location> entrances = new LinkedList<>();
        LinkedList<Location> toVisit = new LinkedList<>();
        LinkedList<Location> visited = new LinkedList<>();
        
        if(root != null) toVisit.add(root);
        
        while(!toVisit.isEmpty()) {
            Location current = toVisit.get(0);
            toVisit.remove(current);
            visited.add(current);
            
            String type = current.getType().toString().toUpperCase();
            if (type.equals("ENTRADA")) {
                entrances.add(current);
            }
            
            LinkedList<Location> neighbors = current.getNeighbors();
            for(int i=0; i<neighbors.size(); i++) {
                Location viz = neighbors.get(i);
                boolean seen = false;
                for(int k=0; k<visited.size(); k++) if(visited.get(k) == viz) seen = true;
                for(int k=0; k<toVisit.size(); k++) if(toVisit.get(k) == viz) seen = true;
                if (!seen) toVisit.add(viz);
            }
        }
        return entrances;
    }
}