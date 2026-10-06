package game.ai;

import game.models.Player;
import game.models.GameEvent;
import game.models.Location;
import game.utils.ReportManager;
import LinkedList.LinkedList;

/**
 * Interface que define o comportamento de uma estratégia de Inteligência Artificial.
 * <p>
 * Qualquer classe que implemente esta interface (ex: BotEasy, BotHard) deve fornecer
 * a lógica concreta para o método {@code play}. Isto permite utilizar o padrão Strategy
 * para trocar a dificuldade dos bots dinamicamente.
 * </p>
 */
public interface IBotStrategy {

    /**
     * Executa um turno completo do Bot com base na sua estratégia específica.
     * * @param bot          O objeto {@link Player} que representa o bot atual.
     * @param events       A lista de eventos aleatórios possíveis no jogo.
     * @param treasureLoc  A referência para a localização da Sala do Tesouro (para verificar se está trancada).
     * @param report       O gestor de relatórios para registar as ações do bot no log final.
     * @return {@code true} se o turno do bot terminou (gastou a ação), {@code false} caso contrário.
     */
    boolean play(Player bot, LinkedList<GameEvent> events, Location treasureLoc, ReportManager report);
}