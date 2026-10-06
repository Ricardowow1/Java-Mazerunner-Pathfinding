package game.ai;

import game.models.Player;
import game.models.Location;
import game.models.Item; 
import game.models.GameEvent;
import game.models.Lever;
import game.enums.LocationType; 
import game.enums.ItemType;
import game.utils.ReportManager;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Estratégia para o Bot de dificuldade DIFÍCIL.
 * <p>
 * Este bot utiliza algoritmos avançados para simular um jogador experiente.
 * <ul>
 * <li><b>BFS (Breadth-First Search):</b> Calcula o caminho mais curto para objetivos (Tesouro ou Alavancas).</li>
 * <li><b>Objetivos Dinâmicos:</b> Se o tesouro estiver trancado, procura ativamente alavancas.</li>
 * <li><b>Alta Inteligência:</b> Tem 85% de probabilidade de resolver enigmas corretamente.</li>
 * <li><b>Alavancas:</b> Usa um método de força bruta simulada para abrir alavancas quase sempre.</li>
 * <li><b>Gestão de Recursos:</b> Mantém HP e Stamina altos, usando itens estrategicamente e reciclando o inventário.</li>
 * </ul>
 * </p>
 */
public class BotHard extends BotStrategyBase {

    private Random random = new Random();

    /**
     * Classe interna para representar um nó no grafo de navegação.
     * Guarda a referência para a localização e para o nó "pai", permitindo
     * reconstruir o caminho de volta até à origem.
     */
    private class PathNode {
        Location location;
        PathNode parent;
        public PathNode(Location loc, PathNode parent) {
            this.location = loc;
            this.parent = parent;
        }
    }

    /**
     * Executa um turno completo com a lógica "Difícil" (Avançada).
     * * @param bot          O objeto {@link Player}.
     * @param events       Lista de eventos.
     * @param treasureLoc  Localização do tesouro (crucial para o BFS decidir o destino).
     * @param report       Gestor de relatórios.
     * @return {@code true} se o turno foi realizado com sucesso.
     */
    @Override
    public boolean play(Player bot, LinkedList<GameEvent> events, Location treasureLoc, ReportManager report) {
        if (checkStaminaAndRest(bot, report)) return true;

        Location current = bot.getCurrentLocation();

        // 1. AUTO-PRESERVAÇÃO TÁTICA
        if (bot.getHealth() < (bot.getMaxHealth() * 0.6) || bot.getStamina() < (bot.getMaxStamina() * 0.3)) {
            if (!bot.getBackpack().isEmpty()) {
                LinkedList<Item> backpack = bot.getBackpack();
                for(int i=0; i<backpack.size(); i++) {
                    Item it = backpack.get(i);
                    if ((bot.getHealth() < 60 && it.getType() == ItemType.CURA) || 
                        (bot.getStamina() < 20 && it.getType() == ItemType.STAMINA)) {
                        
                        System.out.println("🤖 [HARD] Usou taticamente " + it.getName() + ".");
                        useItem(bot, it); 
                        report.log(bot, "BOT_ITEM_USE", "Usou " + it.getName());
                        pausa(1000);
                        return true;
                    }
                }
            }
        }

        // 2. ENIGMAS (85% Chance)
        if (current.hasEnigma()) {
            System.out.println("🤖 [HARD] Bot analisou o enigma...");
            pausa(1000);
            if (random.nextInt(100) < 85) { 
                String answer = current.getEnigma().getAnswer();
                System.out.println("🤖 [HARD] Resposta correta: '" + answer + "'");
                current.solveEnigma(answer);
                report.log(bot, "ENIGMA", "Bot Hard acertou.");
            } else {
                System.out.println("🤖 [HARD] Errou (Raro).");
                current.solveEnigma("ERRADO");
                bot.takeDamage(15);
                return true; 
            }
        }

        // 3. ALAVANCAS
        if (current.hasLever() && current.getLever().isLocked()) {
             if (treasureLoc != null && treasureLoc.isLocked()) {
                 System.out.println("🤖 [HARD] Bot a desbloquear mecanismo...");
                 boolean success = false;
                 if (random.nextInt(100) < 80) {
                     for (int i=1; i<=3; i++) {
                         if (current.getLever().tryUnlock(i)) {
                             success = true; break;
                         }
                     }
                 } else {
                     success = current.getLever().tryUnlock(random.nextInt(3)+1);
                 }

                 if (success) {
                     System.out.println("🤖 [HARD] Alavanca ativada!");
                     current.pullLever();
                     report.log(bot, "ALAVANCA", "Bot ativou alavanca.");
                 } else {
                     System.out.println("🤖 [HARD] Falhou.");
                 }
                 pausa(1000);
             }
        }

        // 4. ITENS (Reciclagem Inteligente)
        if (current.hasItems()) {
            try {
                Item itemNoChao = current.getItems().get(0);
                
                if (bot.isBackpackFull()) {
                    if (!bot.getBackpack().isEmpty()) {
                        int idx = random.nextInt(bot.getBackpack().size());
                        Item itemUsar = bot.getBackpack().get(idx);
                        
                        System.out.println("🤖 [HARD] Inventário Otimizado: Usou " + itemUsar.getName());
                        useItem(bot, itemUsar);
                        
                        System.out.println("🤖 [HARD] Adquiriu " + itemNoChao.getName());
                        bot.pickUpItem(itemNoChao);
                        report.log(bot, "BOT_SWAP", "Trocou item.");
                        pausa(1500);
                        return true;
                    }
                } else {
                    bot.pickUpItem(itemNoChao);
                    System.out.println("🤖 [HARD] Apanhou: " + itemNoChao.getName());
                    report.log(bot, "BOT_ITEM", "Apanhou item.");
                    pausa(1000);
                    return true;
                }
            } catch(Exception e){}
        }

        // 5. MOVIMENTO OTIMIZADO (BFS)
        System.out.println("🤖 [HARD] A calcular rota...");
        
        Location target = null;
        String goalName = "";
        
        if (treasureLoc != null && treasureLoc.isLocked()) {
            goalName = "Alavanca";
            target = bfsFindTarget(current, "LEVER"); 
        } else {
            goalName = "Tesouro";
            target = bfsFindTarget(current, "TESOURO");
        }

        if (target != null) {
            System.out.println("🤖 [HARD] Rota para " + goalName + ": Mover para " + target.getName());
            if (target.isLocked()) System.out.println("🤖 [HARD] Porta trancada! A tentar abrir...");
            
            bot.move(target);
            report.log(bot, "BOT_MOVE", "Moveu para " + target.getName());
            pausa(1500);
            return true;
        }

        LinkedList<Location> neighbors = current.getNeighbors();
        if (!neighbors.isEmpty()) {
            int r = random.nextInt(neighbors.size());
            bot.move(neighbors.get(r));
            pausa(1500);
        } else {
            bot.rest();
        }
        return true;
    }

    /**
     * Algoritmo BFS (Busca em Largura) para encontrar o caminho mais curto até um objetivo.
     * * @param startLocation A sala onde o bot está atualmente.
     * @param targetType    O tipo de alvo: "TESOURO" ou "LEVER" (Alavanca não ativada).
     * @return A próxima {@link Location} para onde o bot se deve mover para seguir o caminho ótimo,
     * ou {@code null} se não houver caminho possível.
     */
    private Location bfsFindTarget(Location startLocation, String targetType) {
        LinkedList<PathNode> queue = new LinkedList<>();
        LinkedList<Integer> visitedIds = new LinkedList<>();

        queue.add(new PathNode(startLocation, null));
        visitedIds.add(startLocation.getId());

        PathNode targetNode = null;

        while (!queue.isEmpty()) {
            PathNode curr = queue.get(0);
            queue.remove(curr);

            boolean found = false;
            if (targetType.equals("TESOURO")) {
                if (curr.location.getType() == LocationType.TESOURO) found = true;
            } else if (targetType.equals("LEVER")) {
                if (curr.location.hasJsonLever() && !curr.location.isLeverActivated()) found = true;
            }

            if (found) {
                targetNode = curr;
                break; 
            }

            LinkedList<Location> neighbors = curr.location.getNeighbors();
            for (int i = 0; i < neighbors.size(); i++) {
                Location neighbor = neighbors.get(i);
                if (!containsId(visitedIds, neighbor.getId())) {
                    visitedIds.add(neighbor.getId());
                    queue.add(new PathNode(neighbor, curr));
                }
            }
        }

        if (targetNode == null) return null;

        PathNode step = targetNode;
        while (step.parent != null && step.parent.location != startLocation) {
            step = step.parent; 
        }

        return step.location;
    }

    /**
     * Verifica se um ID de sala já está na lista de visitados.
     * @param list A lista de IDs visitados.
     * @param id   O ID a verificar.
     * @return true se já foi visitado.
     */
    private boolean containsId(LinkedList<Integer> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == id) return true;
        }
        return false;
    }
}