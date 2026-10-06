package game.ai;

import game.models.Player;
import game.models.GameEvent;
import game.models.Location;
import game.enums.Difficulty;
import game.enums.EventType; 
import game.utils.ReportManager;
import game.utils.GameUtils;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Cérebro central dos Bots (Inteligência Artificial).
 * <p>
 * Esta classe é responsável por:
 * <ul>
 * <li>Decidir qual estratégia (Easy, Normal, Hard) aplicar com base na dificuldade do bot.</li>
 * <li>Gerir a apresentação visual do turno da IA, atribuindo cores distintas a cada nível de dificuldade.</li>
 * <li>Processar eventos aleatórios específicos para bots.</li>
 * </ul>
 * </p>
 */
public class BotBrain {

    private Random random;
    
    private IBotStrategy easyBot;
    private IBotStrategy normalBot;
    private IBotStrategy hardBot;
    
    // Cores ANSI para a consola
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";      // Para HP
    private static final String YELLOW = "\u001B[33m";   // Para Stamina
    
    // Cores de Dificuldade
    private static final String GREEN = "\u001B[32m";    // Fácil
    private static final String BLUE = "\u001B[34m";     // Normal
    private static final String PURPLE = "\u001B[35m";   // Difícil

    /**
     * Construtor do BotBrain.
     * Inicializa as estratégias e o gerador de números aleatórios.
     */
    public BotBrain() {
        this.random = new Random();
        this.easyBot = new BotEasy();
        this.normalBot = new BotNormal();
        this.hardBot = new BotHard();
    }

    /**
     * Executa o turno completo de um bot.
     * <p>
     * 1. Apresenta o Banner e HUD com a cor correspondente à dificuldade.<br>
     * 2. Verifica eventos aleatórios.<br>
     * 3. Delega a lógica de jogo para a estratégia específica (Easy/Normal/Hard).
     * </p>
     *
     * @param bot          O bot que vai jogar.
     * @param events       A lista de eventos do jogo.
     * @param treasureLoc  A localização do tesouro.
     * @param report       O gestor de logs.
     * @return {@code true} se o turno terminou com sucesso.
     */
    public boolean executeTurn(Player bot, LinkedList<GameEvent> events, Location treasureLoc, ReportManager report) {
        
        // Determina a cor baseada na dificuldade
        String themeColor = getBotColor(bot.getDifficulty());
        String diffName = bot.getDifficulty().toString();
        
        // Formata: "Bot_1 [HEROI]"
        String nameWithClass = bot.getName() + " [" + bot.getPlayerClass() + "]";

        // Banner Visual do Bot
        System.out.println("\n" + themeColor + "╔══════════════════════════════════════════════════════════╗");
        System.out.println("║ 🤖 VEZ DE: " + String.format("%-35s", nameWithClass) + "(" + diffName + ") ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝" + RESET);
        
        // HUD Simplificado
        System.out.println("📍 LOCAL: " + bot.getCurrentLocation().getName());
        
        String hpBar = drawMiniBar(bot.getHealth(), bot.getMaxHealth(), RED);
        String stmBar = drawMiniBar(bot.getStamina(), bot.getMaxStamina(), YELLOW);
        
        // Mostra stats (mantendo vermelho para HP e amarelo para Stamina)
        System.out.println("❤ " + hpBar + " " + bot.getHealth() + " | ⚡ " + stmBar + " " + bot.getStamina() + " | 🎒 " + bot.getBackpack().size() + "/3");
        System.out.println("----------------------------------------");
        
        GameUtils.sleep(1000); // Pausa dramática para leitura

        // 1. Eventos Aleatórios (30% de chance antes de agir)
        if (events != null && !events.isEmpty() && random.nextInt(100) < 30) {
            triggerBotEvent(bot, events);
            GameUtils.sleep(1500);
            if (!bot.isAlive()) {
                System.out.println("🤖 " + bot.getName() + " morreu devido ao evento!");
                return true;
            }
        }

        // 2. Executar Estratégia Polimórfica
        Difficulty diff = bot.getDifficulty();
        if (diff == Difficulty.FACIL) return easyBot.play(bot, events, treasureLoc, report);
        else if (diff == Difficulty.NORMAL) return normalBot.play(bot, events, treasureLoc, report);
        else return hardBot.play(bot, events, treasureLoc, report);
    }
    
    /**
     * Retorna a cor ANSI associada à dificuldade do bot.
     * @param diff A dificuldade do bot.
     * @return O código de cor (String).
     */
    private String getBotColor(Difficulty diff) {
        switch (diff) {
            case FACIL: return GREEN;
            case NORMAL: return BLUE;
            case DIFICIL: return PURPLE;
            default: return PURPLE;
        }
    }
    
    /**
     * Desenha uma pequena barra de progresso para o HUD do bot.
     * @param cur Valor atual.
     * @param max Valor máximo.
     * @param color Cor da barra.
     * @return String formatada visualmente.
     */
    private String drawMiniBar(int cur, int max, String color) {
        int size = 5;
        double percent = (double)cur/max;
        int filled = (int) (percent * size);
        if (filled > size) filled = size;
        if (filled < 0) filled = 0;

        StringBuilder s = new StringBuilder(color + "[");
        for(int i=0; i<size; i++) s.append(i < filled ? "█" : "-");
        return s.append("]").append(RESET).toString();
    }

    /**
     * Aplica um evento aleatório ao bot e exibe o resultado.
     * @param p O bot.
     * @param events Lista de eventos possíveis.
     */
    private void triggerBotEvent(Player p, LinkedList<GameEvent> events) {
        int index = random.nextInt(events.size());
        GameEvent event = events.get(index);
        System.out.println("\n>>> 🎲 EVENTO (BOT): " + event.getDescription());
        
        EventType type = event.getType();
        int value = event.getValue();

        switch (type) {
            case DANO: case TRAP: p.takeDamage(value); break;
            case CURA: p.heal(value); break;
            case STAMINA: p.recoverStamina(value); break;
            default: break;
        }
    }
}