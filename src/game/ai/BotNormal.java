package game.ai;

import game.models.Player;
import game.models.Location;
import game.models.Item;
import game.models.GameEvent;
import game.enums.ItemType;
import game.utils.ReportManager;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Implementação da estratégia para o Bot de dificuldade NORMAL.
 * <p>
 * Este bot simula um jogador médio. As suas principais características são:
 * <ul>
 * <li><b>Equilíbrio:</b> Tem 50% de chance de acertar enigmas.</li>
 * <li><b>Reativo:</b> Usa itens de cura se a vida estiver abaixo de 50%.</li>
 * <li><b>Persistência:</b> Se falhar uma alavanca, tenta uma segunda vez.</li>
 * <li><b>Navegação Básica:</b> Tenta evitar portas trancadas, mas não planeia rotas a longo prazo.</li>
 * </ul>
 * </p>
 */
public class BotNormal extends BotStrategyBase {

    private Random random = new Random();

    /**
     * Executa um turno completo com a lógica "Normal".
     * * @param bot          O objeto {@link Player} que representa este bot.
     * @param events       Lista de eventos de jogo.
     * @param treasureLoc  A localização do tesouro.
     * @param report       O gestor de relatórios.
     * @return {@code true} se o bot realizou uma ação válida, {@code false} caso contrário.
     */
    @Override
    public boolean play(Player bot, LinkedList<GameEvent> events, Location treasureLoc, ReportManager report) {
        if (checkStaminaAndRest(bot, report)) return true;

        Location current = bot.getCurrentLocation();
        
        // 1. AUTO-CURA RÁPIDA (Se tiver item e precisar)
        if (!bot.getBackpack().isEmpty()) {
            LinkedList<Item> pack = bot.getBackpack();
            for(int i=0; i<pack.size(); i++) {
                Item it = pack.get(i);
                if ((it.getType() == ItemType.CURA && bot.getHealth() < (bot.getMaxHealth()/2)) ||
                    (it.getType() == ItemType.STAMINA && bot.getStamina() < (bot.getMaxStamina()*0.3))) {
                    
                    System.out.println("🤖 [NORMAL] Usou " + it.getName() + ".");
                    useItem(bot, it);
                    pausa(1000);
                    return true;
                }
            }
        }

        // 2. ENIGMAS (50% Chance)
        if (current.hasEnigma()) {
            System.out.println("🤖 [NORMAL] Bot está a pensar...");
            pausa(1500);
            if (random.nextInt(100) < 50) { 
                String answer = current.getEnigma().getAnswer();
                System.out.println("🤖 [NORMAL] Bot respondeu: '" + answer + "'");
                current.solveEnigma(answer);
                report.log(bot, "ENIGMA", "Bot Normal acertou.");
            } else {
                System.out.println("🤖 [NORMAL] Bot respondeu errado.");
                current.solveEnigma("ERRADO");
                bot.takeDamage(15);
                return true; 
            }
        }

        // 3. ALAVANCAS (Tenta 2 vezes)
        if (current.hasLever() && current.getLever().isLocked()) {
             if (treasureLoc != null && treasureLoc.isLocked()) {
                 System.out.println("🤖 [NORMAL] Bot a testar a alavanca...");
                 int guess = random.nextInt(3) + 1;
                 boolean success = current.getLever().tryUnlock(guess);
                 
                 if (!success) {
                     System.out.println("🤖 [NORMAL] Falhou, tenta de novo...");
                     int secondGuess = (guess % 3) + 1; 
                     success = current.getLever().tryUnlock(secondGuess);
                 }
                 
                 if (success) {
                     System.out.println("🤖 [NORMAL] Sucesso!");
                     current.pullLever();
                     report.log(bot, "ALAVANCA", "Bot Normal ativou alavanca.");
                 }
                 pausa(1000);
             }
        }

        // 4. ITENS
        if (current.hasItems()) {
            try {
                Item itemNoChao = current.getItems().get(0);
                
                if (bot.isBackpackFull()) {
                    if (!bot.getBackpack().isEmpty()) {
                        int idx = random.nextInt(bot.getBackpack().size());
                        Item itemUsar = bot.getBackpack().get(idx);
                        
                        System.out.println("🤖 [NORMAL] Usou " + itemUsar.getName() + " para espaço.");
                        useItem(bot, itemUsar);
                        
                        System.out.println("🤖 [NORMAL] Apanhou " + itemNoChao.getName());
                        bot.pickUpItem(itemNoChao);
                        report.log(bot, "BOT_SWAP", "Trocou item.");
                        pausa(1500);
                        return true;
                    }
                } else {
                    bot.pickUpItem(itemNoChao);
                    System.out.println("🤖 [NORMAL] Apanhou " + itemNoChao.getName());
                    report.log(bot, "BOT_ITEM", "Apanhou item.");
                    pausa(1000);
                    return true;
                }
            } catch(Exception e){}
        }

        // 5. MOVIMENTO
        LinkedList<Location> neighbors = current.getNeighbors();
        if (neighbors.isEmpty()) {
            bot.rest();
            return true;
        }

        for(int i=0; i<10; i++) { 
            int r = random.nextInt(neighbors.size());
            Location dest = neighbors.get(r);
            if (!dest.isLocked()) {
                bot.move(dest);
                report.log(bot, "BOT_MOVE", "Moveu para " + dest.getName());
                pausa(1500);
                return true;
            }
        }
        
        bot.rest();
        return true;
    }
}