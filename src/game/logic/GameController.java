package game.logic;

import game.models.Location;
import game.models.Player;
import game.models.GameEvent;
import game.view.GameView;
import game.utils.MapGenerator;
import game.utils.ReportManager; 
import game.utils.JsonLoader;
import LinkedList.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.util.Random;

/**
 * Controlador principal do jogo (Padrão MVC - Controller).
 * <p>
 * Responsável por inicializar os componentes do jogo, gerir o loop principal,
 * verificar condições de vitória e derrota, e coordenar a interação entre o utilizador,
 * os turnos e os eventos globais.
 * </p>
 */
public class GameController {

    private Scanner scanner;
    private GameMenu menu;
    private GameSetup setup;
    private GameView view;
    
    private TurnManager turnManager;
    private AbilityHandler abilities;
    private BackpackMenu backpackMenu;
    private EventHandler eventHandler;

    private LinkedList<Player> players;
    private LinkedList<GameEvent> randomEvents; 

    private ReportManager reportManager; 
    private String currentMapName;        
    private Random rand;
    
    private Location globalTreasureLocation; 

    /**
     * Construtor do GameController. Inicializa todos os sub-sistemas.
     */
    public GameController() {
        this.scanner = new Scanner(System.in);
        this.view = new GameView();
        this.menu = new GameMenu(scanner);
        this.setup = new GameSetup(scanner, menu);
        
        this.abilities = new AbilityHandler();
        this.backpackMenu = new BackpackMenu(scanner, this);
        
        this.reportManager = new ReportManager(); 
        this.eventHandler = new EventHandler(reportManager);
        this.rand = new Random();
    }

    /**
     * Inicia a aplicação, apresentando o menu principal.
     * Gere o ciclo de vida da escolha do mapa e início do jogo.
     */
    public void start() {
        new File("maps").mkdir(); 

        while (true) {
            int choice = menu.showMainMenu();
            
            if (choice == 0) {
                System.out.println("A sair...");
                break;
            }
            else if (choice == 1) { 
                String selectedMap = menu.askForMapFile();
                if (selectedMap != null) {
                    this.currentMapName = selectedMap; 
                    setupAndPlay(selectedMap);
                }
            }
            else if (choice == 2) { 
                System.out.println("\n--- CRIAR NOVO MUNDO ---");
                System.out.print("Nome para o ficheiro (ex: masmorra1): ");
                String inputName = scanner.nextLine().trim();
                
                if (inputName.isEmpty()) inputName = "novo_mapa"; 
                if (!inputName.endsWith(".json")) inputName += ".json"; 
                
                String finalPath = "maps/" + inputName; 
                
                System.out.println(">> A gerar mundo em '" + finalPath + "'...");
                MapGenerator.generateRandomMap("locais_pool.json", finalPath);
                
                this.currentMapName = finalPath; 
                setupAndPlay(finalPath);
            }
            else if (choice == 3) {
                menu.showRules();
            }
        }
        scanner.close();
    }

    /**
     * Configura o jogo com base no mapa escolhido.
     *
     * @param mapFile O caminho para o ficheiro JSON do mapa.
     */
    private void setupAndPlay(String mapFile) {
        Location startLoc = setup.loadMap(mapFile);
        if (startLoc == null) return;
        
        this.players = setup.createPlayers(startLoc);
        this.randomEvents = JsonLoader.loadEventsList("events.json");
        
        this.globalTreasureLocation = findTreasure(startLoc);
        
        this.turnManager = new TurnManager(scanner, view, backpackMenu, abilities, players, randomEvents, reportManager, globalTreasureLocation);
        this.eventHandler.setTurnManager(turnManager);
        
        playGame();
    }
    
    /**
     * Procura no grafo do mapa a localização do Tesouro (BFS).
     *
     * @param startNode O nó de partida.
     * @return A {@link Location} do tesouro, ou {@code null} se não encontrada.
     */
    private Location findTreasure(Location startNode) {
        LinkedList<Location> visited = new LinkedList<>();
        LinkedList<Location> queue = new LinkedList<>();
        queue.add(startNode);
        
        while(!queue.isEmpty()) {
            Location curr = queue.get(0);
            queue.remove(curr);
            
            boolean seen = false;
            for(int i=0; i<visited.size(); i++) if(visited.get(i).getId() == curr.getId()) seen = true;
            if(seen) continue;
            
            visited.add(curr);
            if(curr.getType().toString().toUpperCase().contains("TESOURO")) return curr;
            
            for(int i=0; i<curr.getNeighbors().size(); i++) queue.add(curr.getNeighbors().get(i));
        }
        return null;
    }

    /**
     * O loop principal do jogo (Game Loop).
     */
    private void playGame() {
        boolean gameRunning = true;
        int currentPlayerIndex = 0;
        view.showMessage("\n=== A AVENTURA COMEÇOU ===");

        while (gameRunning) {
            if (allPlayersDead()) {
                System.out.println("\n💀 GAME OVER: Todos pereceram na masmorra.");
                break;
            }

            if (currentPlayerIndex >= players.size()) currentPlayerIndex = 0;
            Player p = players.get(currentPlayerIndex);
            
            if (!p.isAlive()) {
                System.out.println("✝️ " + p.getName() + " foi eliminado.");
                players.remove(p);
                
                if (players.isEmpty()) gameRunning = false;
                else if (currentPlayerIndex >= players.size()) currentPlayerIndex = 0;
                
                checkEndOfTurn(currentPlayerIndex);
                continue; 
            }
            
            if (p.shouldSkipTurn()) {
                System.out.println("\n🚫 " + p.getName() + " está atordoado/impedido e perde a vez.");
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                checkEndOfTurn(currentPlayerIndex); 
                continue;
            }

            if (!p.isBot()) p.onTurnStart();
            
            Location loc = p.getCurrentLocation();
            boolean isCorridor = loc.getType().toString().toUpperCase().contains("CORREDOR");
            boolean isSurprise = loc.hasEvent() && loc.getEventType().equalsIgnoreCase("Surpresa");
            
            if ((isCorridor || isSurprise) && randomEvents != null && !randomEvents.isEmpty()) {
                if (isSurprise || rand.nextInt(100) < 30) {
                    eventHandler.triggerRandomEvent(p, randomEvents, players);
                    if (isSurprise) loc.setEvent(null);
                    
                    if (!p.isAlive()) { 
                         currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                         continue;
                    }
                }
            }

            boolean keepPlaying = turnManager.playTurn(p);
            
            if (!keepPlaying) {
                gameRunning = false;
                break;
            }

            checkGlobalLevers(p.getCurrentLocation());

            if (p.isAlive() && p.getCurrentLocation().getType().toString().toUpperCase().contains("TESOURO")) {
                if (p.getCurrentLocation().isLocked()) {
                    System.out.println(">> A porta do tesouro está trancada! Precisas de ativar uma alavanca.");
                } else {
                    System.out.println("\n**************************************");
                    System.out.println("🎉 PARABÉNS! " + p.getName() + " ENCONTROU O TESOURO!");
                    System.out.println("**************************************");
                    reportManager.log(p, "VITORIA", "Encontrou o tesouro e venceu o jogo.");
                    gameRunning = false;
                    break;
                }
            }

            if (turnManager.hasExtraTurn()) {
                System.out.println("\n✨ " + p.getName() + " tem uma JOGADA EXTRA!");
            } else {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
            
            checkEndOfTurn(currentPlayerIndex);
        }

        if (currentMapName != null) {
            reportManager.generateReport(currentMapName, players);
        }
    }
    
    /**
     * Verifica o estado de todas as alavancas e destranca o tesouro se necessário.
     * @param startNode Nó inicial para a pesquisa.
     */
    private void checkGlobalLevers(Location startNode) {
        if (globalTreasureLocation != null && !globalTreasureLocation.isLocked()) return;

        LinkedList<Location> visited = new LinkedList<>();
        LinkedList<Location> queue = new LinkedList<>();
        queue.add(startNode);
        
        int activeLevers = 0;

        while (!queue.isEmpty()) {
            Location current = queue.get(0); 
            queue.remove(current); 

            boolean jaVisto = false;
            for(int k=0; k<visited.size(); k++) if (visited.get(k).getId() == current.getId()) jaVisto = true;
            if(jaVisto) continue;
            visited.add(current);

            if (current.hasJsonLever() && current.isLeverActivated()) {
                activeLevers++;
            }
            
            LinkedList<Location> neighbors = current.getNeighbors();
            for (int i = 0; i < neighbors.size(); i++) queue.add(neighbors.get(i));
        }

        if (activeLevers >= 1 && globalTreasureLocation != null) {
            if (globalTreasureLocation.isLocked()) {
                globalTreasureLocation.setLocked(false); 
                System.out.println(">> ------------------------------------------");
                System.out.println(">> ESTRONDO! O TESOURO ABRIU-SE AO LONGE!");
                System.out.println(">> (Bastava uma alavanca e tu conseguiste!)");
                System.out.println(">> ------------------------------------------");
                if (!players.isEmpty()) {
                    reportManager.log(players.get(0), "EVENTO_GLOBAL", "A sala do tesouro foi destrancada!");
                }
            }
        }
    }
    
    /**
     * Verifica se todos os jogadores estão mortos.
     * @return true se não sobrar ninguém.
     */
    private boolean allPlayersDead() {
        if (players.isEmpty()) return true;
        for(int i=0; i<players.size(); i++) {
            if(players.get(i).isAlive()) return false;
        }
        return true;
    }
    
    /**
     * Verifica se o índice voltou a 0, indicando o fim de uma ronda.
     * Se sim, notifica o ReportManager para avançar o turno.
     *
     * @param nextIndex O índice do próximo jogador a jogar.
     */
    private void checkEndOfTurn(int nextIndex) {
        if (nextIndex == 0) {
            reportManager.nextTurn();
        }
    }
    
    /**
     * Ativa um efeito global.
     * @param source Origem do efeito.
     * @param type Tipo de efeito.
     * @param value Valor.
     */
    public void triggerGlobalDamage(Player source, String type, int value) {
        abilities.affectOthers(source, players, type, value);
    }
}