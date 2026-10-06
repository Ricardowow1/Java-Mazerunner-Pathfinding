package game.logic;

import game.models.Player;
import game.models.Location;
import game.models.GameEvent;
import game.utils.ReportManager;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Responsável por gerir e executar os eventos aleatórios do jogo.
 * <p>
 * Esta classe isola a lógica de aplicação de efeitos (Dano, Cura, Troca de Posição)
 * das classes de controlo de fluxo, promovendo o princípio da responsabilidade única.
 * </p>
 */
public class EventHandler {

    private Random rand;
    private ReportManager reportManager;
    private TurnManager turnManager; 

    /**
     * Construtor do EventHandler.
     * @param reportManager O gestor de relatórios para registar os eventos ocorridos.
     */
    public EventHandler(ReportManager reportManager) {
        this.rand = new Random();
        this.reportManager = reportManager;
    }

    /**
     * Define o TurnManager. Necessário para eventos que alteram o fluxo de turnos (ex: Extra Turn).
     * @param tm A instância do TurnManager.
     */
    public void setTurnManager(TurnManager tm) {
        this.turnManager = tm;
    }

    /**
     * Sorteia um evento da lista e aplica-o ao jogador.
     *
     * @param p          O jogador alvo do evento.
     * @param events     A lista de todos os eventos possíveis carregados do JSON.
     * @param allPlayers A lista de todos os jogadores (necessária para eventos de troca).
     */
    public void triggerRandomEvent(Player p, LinkedList<GameEvent> events, LinkedList<Player> allPlayers) {
        if (events == null || events.isEmpty()) return;

        int idx = rand.nextInt(events.size());
        GameEvent event = events.get(idx);
        
        System.out.println("\n>>> 🎲 EVENTO! " + event.getDescription());
        if (reportManager != null) reportManager.log(p, "EVENTO", event.getDescription());

        switch (event.getType()) {
            case DANO: 
            case TRAP: 
                p.takeDamage(event.getValue()); 
                break;
            case CURA: 
                p.heal(event.getValue()); 
                break;
            case STAMINA:
                if(event.getValue() > 0) p.recoverStamina(event.getValue());
                else p.consumeStamina(Math.abs(event.getValue()));
                break;
            case SKIP_TURN: 
                p.addSkipTurn(event.getValue()); 
                break;
            case EXTRA_TURN: 
                if (turnManager != null) turnManager.grantExtraTurn(); 
                break;
            case MOVE_BACK:
                if(p.moveBack()) System.out.println(">> Recuaste uma sala!");
                else System.out.println(">> Não conseguiste recuar.");
                break;
            case SWAP_POSITION: 
                swapPlayerPosition(p, allPlayers); 
                break;
            case SWAP_ALL: 
                swapAllPlayers(allPlayers); 
                break;
            default: break;
        }
        System.out.println("-----------------------------------");
    }

    /**
     * Troca a posição do jogador atual com outro jogador aleatório.
     *
     * @param activePlayer O jogador que ativou o evento.
     * @param players      A lista de todos os jogadores para escolher um alvo.
     */
    private void swapPlayerPosition(Player activePlayer, LinkedList<Player> players) {
        if (players.size() < 2) {
            System.out.println(">> (Sem outros jogadores suficientes para trocar)");
            return;
        }
        
        Player target = activePlayer;
        // Garante que não troca consigo mesmo
        while (target == activePlayer) {
            int idx = rand.nextInt(players.size());
            target = players.get(idx);
        }
        
        Location loc1 = activePlayer.getCurrentLocation();
        Location loc2 = target.getCurrentLocation();
        
        activePlayer.forceLocation(loc2);
        target.forceLocation(loc1);
        
        System.out.println(">> 🔄 TROCA! " + activePlayer.getName() + " foi para: " + loc2.getName());
        System.out.println("   (" + target.getName() + " veio para: " + loc1.getName() + ")");
    }

    /**
     * Baralha as posições de todos os jogadores no mapa.
     * Utiliza um algoritmo de shuffle (Fisher-Yates) e garante Derangement (ninguém fica no mesmo sítio).
     *
     * @param players A lista de todos os jogadores a baralhar.
     */
    private void swapAllPlayers(LinkedList<Player> players) {
        if (players.size() < 2) {
            System.out.println(">> (Jogadores insuficientes para o Caos)");
            return;
        }
        
        System.out.println(">> 🌀 CAOS! Um feitiço baralhou a posição de TODOS os jogadores!");
        
        int count = players.size();
        Location[] originalLocs = new Location[count];
        
        // 1. Guardar localizações atuais
        for(int i=0; i<count; i++) {
            originalLocs[i] = players.get(i).getCurrentLocation();
        }
        
        // 2. Baralhar as localizações
        for (int i = count - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            Location temp = originalLocs[index];
            originalLocs[index] = originalLocs[i];
            originalLocs[i] = temp;
        }
        
        // 3. Garantir que ninguém fica na mesma sala (Derangement)
        for (int i = 0; i < count; i++) {
            Player p = players.get(i);
            if (originalLocs[i] == p.getCurrentLocation()) {
                // Se calhou na mesma, troca com o vizinho no array
                int next = (i + 1) % count;
                Location swap = originalLocs[next];
                originalLocs[next] = originalLocs[i];
                originalLocs[i] = swap;
            }
        }
        
        // 4. Aplicar as novas localizações
        for(int i=0; i<count; i++) {
            players.get(i).forceLocation(originalLocs[i]);
        }
    }
}