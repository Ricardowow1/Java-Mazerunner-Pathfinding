package game.ai;

import game.models.Player;
import game.models.Item;
import game.enums.ItemType;
import game.utils.GameUtils;
import game.utils.ReportManager;
import LinkedList.LinkedList;

/**
 * Classe abstrata que serve de base para todas as estratégias de IA.
 * <p>
 * Esta classe implementa a interface {@link IBotStrategy} e fornece métodos
 * auxiliares comuns a todos os níveis de dificuldade, como a gestão de Stamina,
 * o uso de itens e as pausas visuais, promovendo a reutilização de código (DRY).
 * </p>
 */
public abstract class BotStrategyBase implements IBotStrategy {

    /**
     * Verifica se o nível de Stamina do bot é crítico (inferior a 5).
     * Se for, força o bot a descansar para recuperar energia.
     * * @param bot    O bot a ser verificado.
     * @param report O gestor de relatórios para registar a ação.
     * @return {@code true} se o bot foi forçado a descansar (o turno acaba), {@code false} se pode continuar.
     */
    protected boolean checkStaminaAndRest(Player bot, ReportManager report) {
        if (bot.getStamina() < 5) {
            System.out.println("🤖 [" + bot.getDifficulty() + "] Exausto. A descansar.");
            bot.rest();
            report.log(bot, "BOT_REST", "Descansou forçadamente.");
            GameUtils.sleep(1000);
            return true;
        }
        return false;
    }

    /**
     * Usa um item da mochila do bot para recuperar atributos.
     * <p>
     * Se o item for do tipo {@code CURA}, recupera vida.
     * Se for {@code STAMINA}, recupera energia.
     * O item é removido da mochila após o uso.
     * </p>
     * * @param bot  O bot que vai usar o item.
     * @param item O item a ser consumido.
     */
    protected void useItem(Player bot, Item item) {
        if (item.getType() == ItemType.CURA) {
            bot.heal(item.getValue());
        } else if (item.getType() == ItemType.STAMINA) {
            bot.recoverStamina(item.getValue());
        }
        bot.getBackpack().remove(item);
    }
    
    /**
     * Método auxiliar para realizar uma pausa visual na execução.
     * Encapsula a chamada ao {@link GameUtils#sleep(int)}.
     * * @param ms Tempo de pausa em milissegundos.
     */
    protected void pausa(int ms) {
        GameUtils.sleep(ms);
    }
}