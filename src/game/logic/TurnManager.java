package game.logic;

import game.models.Location;
import game.models.Player;
import game.models.Item;
import game.models.GameEvent;
import game.models.Lever;
import game.view.GameView;
import game.enums.PlayerClass;
import game.enums.LocationType;
import game.enums.ItemType;
import game.enums.EventType;
import game.ai.BotBrain;
import game.utils.ReportManager;
import game.utils.GameUtils; 
import LinkedList.LinkedList;
import java.util.Scanner;
import java.util.Random;

/**
 * Gere o ciclo de vida de um turno para um jogador (Humano ou Bot).
 * <p>
 * Esta classe controla o fluxo de interações:
 * <ul>
 * <li>Apresenta o estado visual através do {@link GameView}.</li>
 * <li>Processa eventos de sala, enigmas e alavancas.</li>
 * <li>Delega a lógica de Bots para o {@link BotBrain}.</li>
 * </ul>
 * </p>
 */
public class TurnManager {

    private Scanner scanner;
    private GameView view;
    private BackpackMenu backpack;
    private AbilityHandler abilities;
    private LinkedList<Player> allPlayers;
    private LinkedList<GameEvent> randomEvents;
    private Random random;
    private BotBrain botBrain;
    private ReportManager report;
    
    private Location treasureLocation;

    private boolean extraTurnActive = false;
    private final int ENIGMA_FAIL_DAMAGE = 15;

    /**
     * Construtor do TurnManager.
     *
     * @param scanner      O scanner para input.
     * @param view         A vista melhorada (GameView) para output.
     * @param backpack     O menu da mochila.
     * @param abilities    O gestor de habilidades.
     * @param players      A lista de jogadores.
     * @param events       A pool de eventos.
     * @param report       O gestor de logs.
     * @param treasureLoc  A localização do tesouro.
     */
    public TurnManager(Scanner scanner, GameView view, BackpackMenu backpack, AbilityHandler abilities,
                       LinkedList<Player> players, LinkedList<GameEvent> events, ReportManager report, Location treasureLoc) {
        this.scanner = scanner;
        this.view = view;
        this.backpack = backpack;
        this.abilities = abilities;
        this.allPlayers = players;
        this.randomEvents = events;
        this.random = new Random();
        this.botBrain = new BotBrain(); 
        this.report = report;
        this.treasureLocation = treasureLoc;
    }

    public LinkedList<Player> getPlayers() { return allPlayers; }
    public void grantExtraTurn() { this.extraTurnActive = true; }
    public boolean hasExtraTurn() {
        if (extraTurnActive) { extraTurnActive = false; return true; }
        return false;
    }

    /**
     * Executa a lógica principal de um turno.
     *
     * @param p O jogador que vai jogar.
     * @return {@code true} se o jogo deve continuar, {@code false} se o jogador saiu.
     */
    public boolean playTurn(Player p) {
        // --- 1. LÓGICA DE BOT ---
        if (p.isBot()) {
            // O BotBrain trata de tudo, agora com visualização melhorada
            return botBrain.executeTurn(p, randomEvents, treasureLocation, report);
        }

        // --- 2. LÓGICA HUMANA ---
        if (randomEvents != null && !randomEvents.isEmpty() && random.nextInt(100) < 30) {
             // Eventos aleatórios
        }

        boolean turnEnded = false;

        while (!turnEnded) {
            Location loc = p.getCurrentLocation();
            
            // --- UI PREMIUM ---
            view.showTurnStart(p);
            view.showPlayerStatus(p); // HUD
            view.showLocationInfo(loc);

            // A. EVENTOS DE SALA
            if (loc.hasEvent()) {
                String eventTypeStr = loc.getEventType();
                if (eventTypeStr.equalsIgnoreCase("Surpresa")) {
                    System.out.println(">> ❗ Sentes que algo vai acontecer aqui...");
                } else {
                    p.applyEventEffect(eventTypeStr);
                    report.log(p, "EVENTO_SALA", "Sofreu efeito de sala: " + eventTypeStr);
                }
                if (!p.isAlive() || p.getCurrentLocation() != loc) return true;
            }

            // B. ENIGMAS
            if (loc.hasEnigma()) {
                System.out.println("\n🔒 CAMINHO BLOQUEADO POR UM ENIGMA!");
                System.out.println("❓ " + loc.getEnigmaQuestion());
                System.out.print("   Resposta: ");
                try {
                    if (scanner.hasNextLine()) {
                        String ans = scanner.nextLine().trim();
                        if (loc.solveEnigma(ans)) {
                            System.out.println(">> ✨ Correto! A sala desbloqueou.");
                            report.log(p, "ENIGMA", "Resolveu o enigma.");
                        } else {
                            System.out.println(">> ❌ Errado! Uma armadilha dispara contra ti!");
                            p.takeDamage(ENIGMA_FAIL_DAMAGE);
                            report.log(p, "ENIGMA", "Falhou e sofreu dano.");
                            if (!p.isAlive()) return true;
                            System.out.println(">> A dor impede-te de continuar. Perdes a vez.");
                            return true; 
                        }
                    }
                } catch (Exception e) { return false; }
            }

            // C. ALAVANCAS
            handleLeverInteractions(loc, p);

            // D. AÇÕES (Usar o novo Menu do GameView)
            boolean canUseAbility = (p.getPlayerClass() != PlayerClass.AVENTUREIRO 
                                  && p.getPlayerClass() != PlayerClass.HEROI 
                                  && p.getPlayerClass() != PlayerClass.NINJA); 
            
            view.showActions(canUseAbility);
            
            String input = "";
            try {
                if (scanner.hasNextLine()) input = scanner.nextLine().trim();
                else return false;
            } catch (Exception e) { return false; }

            if (input.equals("0")) {
                System.out.print("⚠️ Desistir e morrer? (s/n): ");
                try {
                    if (scanner.hasNextLine() && scanner.nextLine().trim().equalsIgnoreCase("s")) {
                        p.takeDamage(9999);
                        report.log(p, "DESISTENCIA", "O jogador desistiu da partida.");
                        return true;
                    }
                } catch (Exception e) { return false; }
                continue;
            }

            switch (input) {
                case "1": // Mover
                    Location startLoc = p.getCurrentLocation();
                    if (loc.hasEnigma()) {
                        System.out.println(">> 🔒 Tens de resolver o enigma primeiro!");
                    } else if (handleMovement(p, loc)) {
                        report.log(p, "MOVIMENTO", "Moveu-se para " + p.getCurrentLocation().getName());
                        turnEnded = true;
                    }
                    break;
                case "2": // Apanhar
                    if (handlePickUp(p, loc)) {
                        report.log(p, "ITEM_APANHAR", "Apanhou item.");
                    } else {
                        if (!loc.hasEnigma() && !loc.hasItems()) {
                            report.log(p, "ITEM_FALHA", "Tentou apanhar item (Vazio).");
                        }
                    }
                    turnEnded = true;
                    break;
                case "3": // Mochila
                    if (backpack.open(p)) {
                        report.log(p, "ITEM_USAR", "Usou item.");
                        turnEnded = true;
                    }
                    break;
                case "4": // Habilidade
                    if (canUseAbility) {
                        abilities.useAbility(p, allPlayers);
                        report.log(p, "HABILIDADE", "Usou habilidade.");
                        turnEnded = true;
                    } else {
                        System.out.println(">> Opção inválida.");
                    }
                    break;
                case "5": // Descansar
                    p.rest();
                    report.log(p, "DESCANSO", "Descansou.");
                    turnEnded = true;
                    break;
                default:
                    System.out.println(">> Opção inválida.");
            }
        }
        return true;
    }

    /**
     * Gere a interação com alavancas, verificando se o tesouro já está aberto.
     *
     * @param loc A localização atual.
     * @param p   O jogador.
     */
    private void handleLeverInteractions(Location loc, Player p) {
        if (loc.hasLever()) {
            Lever lever = loc.getLever();
            
            if (treasureLocation != null && !treasureLocation.isLocked()) {
                System.out.println("! ALAVANCA: [DESATIVADA] (O Tesouro já foi aberto, não precisas de mexer aqui)");
                return;
            }

            if (lever.isLocked()) {
                 System.out.println("\n🛠️ ALAVANCA! [1] Cima [2] Meio [3] Baixo [0] Sair");
                 System.out.print("  Escolha: ");
                 if (scanner.hasNextLine()) {
                     try {
                        String line = scanner.nextLine().trim();
                        if(line.equals("0")) return;
                        int choice = Integer.parseInt(line);
                        if (lever.tryUnlock(choice)) {
                            System.out.println(">> ✅ CLICK! Sucesso.");
                            report.log(p, "ALAVANCA", "Ativou a alavanca.");
                            loc.pullLever();
                        } else {
                            System.out.println(">> ❌ Emperrou.");
                            report.log(p, "ALAVANCA", "Falhou.");
                        }
                     } catch (Exception e) {}
                 }
            } else {
                System.out.println("! ALAVANCA: [✅ ATIVADA]");
            }
        }
    }
    
    /**
     * Gere a tentativa de apanhar um item.
     *
     * @param p   O jogador.
     * @param loc A localização.
     * @return true se apanhou o item.
     */
    private boolean handlePickUp(Player p, Location loc) {
        if (loc.hasEnigma()) {
            System.out.println(">> 🔒 Enigma protege os itens!");
            return false;
        }
        if (loc.hasItems()) {
            try {
                Item item = loc.getItems().get(0);
                if (p.pickUpItem(item)) return true;
            } catch(Exception e){}
        } else {
            System.out.println(">> Nada para apanhar.");
        }
        return false;
    }
    
    /**
     * Menu de movimento.
     *
     * @param p       O jogador.
     * @param current A sala atual.
     * @return true se o jogador se moveu com sucesso.
     */
    private boolean handleMovement(Player p, Location current) {
        LinkedList<Location> neighbors = current.getNeighbors();
        if (neighbors.isEmpty()) {
            System.out.println(">> Sem saída.");
            return false;
        }

        System.out.println("\n🏃 PARA ONDE QUERES IR?");
        for (int i = 0; i < neighbors.size(); i++) {
            Location viz = neighbors.get(i);
            String status = viz.isLocked() ? " [🔒 TRANCADO]" : "";
            System.out.println(" [" + (i + 1) + "] " + viz.getName() + status);
        }
        System.out.println(" [0] Voltar");
        System.out.print("> ");
        
        try {
            if (!scanner.hasNextLine()) return false;
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            
            if (choice == -1) return false;
            if (choice >= 0 && choice < neighbors.size()) {
                return p.move(neighbors.get(choice));
            }
        } catch (Exception e) {}
        
        System.out.println(">> Opção inválida.");
        return false;
    }
}