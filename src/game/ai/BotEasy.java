package game.ai;

import game.models.Player;
import game.models.Location;
import game.models.Item;
import game.models.GameEvent;
import game.utils.ReportManager;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Implementação da estratégia para o Bot de dificuldade FÁCIL.
 * <p>
 * Este bot simula um jogador inexperiente ou distraído. As suas principais características são:
 * <ul>
 * <li><b>Baixa taxa de sucesso:</b> Apenas 20% de chance de resolver enigmas.</li>
 * <li><b>Movimento Aleatório:</b> Escolhe caminhos sem critério, batendo frequentemente em portas trancadas.</li>
 * <li><b>Esquecimento:</b> Tem 50% de chance de ignorar itens que estão no chão.</li>
 * <li><b>Alavancas:</b> Tenta uma posição à sorte e desiste se falhar.</li>
 * </ul>
 * </p>
 */
public class BotEasy extends BotStrategyBase {

    private Random random = new Random();

    /**
     * Executa um turno completo com a lógica "Fácil".
     * * @param bot          O objeto {@link Player} que representa este bot.
     * @param events       Lista de eventos de jogo (usada para processamento interno se necessário).
     * @param treasureLoc  A localização do tesouro (usada para decidir se vale a pena mexer em alavancas).
     * @param report       O gestor de relatórios para registar as ações (erros e sucessos).
     * @return {@code true} se o bot realizou uma ação que consome o turno (mover, usar item, falhar enigma),
     * {@code false} caso contrário.
     */
    @Override
    public boolean play(Player bot, LinkedList<GameEvent> events, Location treasureLoc, ReportManager report) {
        // Usa método herdado da base para verificar cansaço extremo
        if (checkStaminaAndRest(bot, report)) return true;

        Location current = bot.getCurrentLocation();

        // 1. ENIGMAS (20% de chance de acertar)
        if (current.hasEnigma()) {
            System.out.println("🤖 [EASY] Bot está confuso com o enigma...");
            pausa(1000);
            
            if (random.nextInt(100) < 20) { 
                String answer = current.getEnigma().getAnswer();
                System.out.println("🤖 [EASY] Bot chutou: '" + answer + "' e acertou!");
                current.solveEnigma(answer);
                report.log(bot, "ENIGMA", "Bot Easy acertou.");
            } else {
                System.out.println("🤖 [EASY] Bot respondeu: 'Batatas'. Errou!");
                current.solveEnigma("ERRADO");
                bot.takeDamage(15);
                report.log(bot, "ENIGMA", "Bot Easy errou.");
                return true; 
            }
        }

        // 2. ALAVANCAS (Aleatório)
        if (current.hasLever() && current.getLever().isLocked()) {
             if (treasureLoc != null && treasureLoc.isLocked()) {
                 System.out.println("🤖 [EASY] Bot puxou a alavanca à sorte...");
                 int guess = random.nextInt(3) + 1;
                 if (current.getLever().tryUnlock(guess)) {
                     System.out.println("🤖 [EASY] Teve sorte! Abriu.");
                     current.pullLever();
                     report.log(bot, "ALAVANCA", "Bot Easy ativou alavanca.");
                 } else {
                     System.out.println("🤖 [EASY] Emperrou.");
                 }
                 pausa(1000);
             }
        }

        // 3. ITENS (50% chance de reparar neles)
        if (current.hasItems() && random.nextBoolean()) {
            try {
                Item itemNoChao = current.getItems().get(0);
                
                if (bot.isBackpackFull()) {
                    if (!bot.getBackpack().isEmpty()) {
                        int idx = random.nextInt(bot.getBackpack().size());
                        Item itemUsar = bot.getBackpack().get(idx);
                        
                        System.out.println("🤖 [EASY] Mochila cheia! Usou " + itemUsar.getName() + " à pressa.");
                        useItem(bot, itemUsar); // Método da Base
                        
                        System.out.println("🤖 [EASY] ...e apanhou " + itemNoChao.getName());
                        bot.pickUpItem(itemNoChao);
                        report.log(bot, "BOT_SWAP", "Usou item para apanhar " + itemNoChao.getName());
                        pausa(1500);
                        return true;
                    }
                } else {
                    bot.pickUpItem(itemNoChao);
                    System.out.println("🤖 [EASY] Apanhou " + itemNoChao.getName());
                    report.log(bot, "BOT_ITEM", "Apanhou " + itemNoChao.getName());
                    pausa(1000);
                    return true;
                }
            } catch(Exception e){}
        }

        // 4. MOVIMENTO (Aleatório)
        LinkedList<Location> neighbors = current.getNeighbors();
        if (neighbors.isEmpty()) {
            bot.rest();
            return true;
        }

        int r = random.nextInt(neighbors.size());
        Location dest = neighbors.get(r);

        if (dest.isLocked()) {
            System.out.println("🤖 [EASY] Bateu numa porta trancada.");
            pausa(1000);
        } else {
            bot.move(dest);
            report.log(bot, "BOT_MOVE", "Moveu para " + dest.getName());
            pausa(1500);
        }
        return true;
    }
}