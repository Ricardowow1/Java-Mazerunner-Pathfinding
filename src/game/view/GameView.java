package game.view;

import game.models.Player;
import game.models.Location;
import LinkedList.LinkedList;

/**
 * Responsável pela camada de apresentação visual do jogo na consola (View no padrão MVC).
 * <p>
 * Esta classe centraliza a formatação de texto, utilizando códigos ANSI para cores,
 * caracteres Unicode para ícones e métodos auxiliares para desenhar interfaces
 * como barras de progresso e menus estilizados.
 * </p>
 */
public class GameView {

    // Códigos de Cores ANSI para a consola
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE_BOLD = "\u001B[1;37m";

    /**
     * Imprime uma mensagem simples na consola.
     * * @param msg A mensagem a ser exibida.
     */
    public void showMessage(String msg) {
        System.out.println(msg);
    }

    /**
     * Apresenta um cabeçalho estilizado indicando o início do turno de um jogador.
     * <p>
     * Utiliza cores diferentes para distinguir Humanos (Ciano) de Bots (Roxo)
     * e exibe o Nome e a Classe do jogador.
     * </p>
     * * @param p O objeto {@link Player} cujo turno está a começar.
     */
    public void showTurnStart(Player p) {
        String color = p.isBot() ? PURPLE : CYAN;
        String icon = p.isBot() ? "🤖" : "👤";
        String type = p.isBot() ? "(IA)" : "(TU)";
        
        // Formata o nome com a classe: "Jogador_1 [HEROI]"
        String nameWithClass = p.getName() + " [" + p.getPlayerClass() + "]";

        System.out.println("\n" + color + "╔══════════════════════════════════════════════════════════╗");
        System.out.println("║ " + icon + " VEZ DE: " + String.format("%-35s", nameWithClass) + type + " ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Exibe o HUD (Heads-Up Display) com o estado atual do jogador.
     * <p>
     * Mostra barras de progresso visuais para Vida e Stamina, bem como
     * a ocupação do inventário e a classe do jogador.
     * </p>
     * * @param p O jogador cujos atributos serão apresentados.
     */
    public void showPlayerStatus(Player p) {
        String hpBar = drawProgressBar(p.getHealth(), p.getMaxHealth(), 10, RED);
        String stmBar = drawProgressBar(p.getStamina(), p.getMaxStamina(), 10, YELLOW);
        
        // Prepara a string da classe para mostrar no HUD
        String classInfo = WHITE_BOLD + p.getPlayerClass().toString() + RESET;

        System.out.println("┌──────────────────────────────────────────────────────────┐");
        System.out.println("│ " + WHITE_BOLD + "STATUS" + RESET + " (" + classInfo + ")                                    │");
        System.out.println("│ ❤ HP:  " + hpBar + " " + String.format("%-3d", p.getHealth()) + "/" + String.format("%-3d", p.getMaxHealth()) + "                │");
        System.out.println("│ ⚡ STM: " + stmBar + " " + String.format("%-3d", p.getStamina()) + "/" + String.format("%-3d", p.getMaxStamina()) + "                 │");
        System.out.println("│ 🎒 MOCHILA: " + drawBackpackSlots(p.getBackpack().size()) + " (" + p.getBackpack().size() + "/3)                         │");
        System.out.println("└──────────────────────────────────────────────────────────┘");
    }

    /**
     * Gera uma representação textual de uma barra de progresso.
     * * @param current Valor atual do atributo.
     * @param max     Valor máximo do atributo.
     * @param size    O tamanho total da barra em caracteres.
     * @param color   A cor ANSI da barra preenchida.
     * @return Uma String contendo a barra formatada (ex: "[████----]").
     */
    private String drawProgressBar(int current, int max, int size, String color) {
        double percent = (double) current / max;
        int filled = (int) (size * percent);
        if (filled > size) filled = size;
        if (filled < 0) filled = 0;

        StringBuilder bar = new StringBuilder(color + "[");
        for (int i = 0; i < size; i++) {
            if (i < filled) bar.append("█");
            else bar.append("-");
        }
        bar.append("]" + RESET);
        return bar.toString();
    }
    
    /**
     * Gera uma representação visual dos slots da mochila.
     * * @param count Número de itens atuais na mochila.
     * @return Uma String com ícones representando os itens (ex: "📦 📦 _").
     */
    private String drawBackpackSlots(int count) {
        StringBuilder slots = new StringBuilder();
        for(int i=0; i<3; i++) {
            if(i < count) slots.append("📦 ");
            else slots.append("__ ");
        }
        return slots.toString().trim();
    }

    /**
     * Apresenta as informações detalhadas da localização atual.
     * * @param loc A {@link Location} onde o jogador se encontra.
     */
    public void showLocationInfo(Location loc) {
        System.out.println("\n📍 " + WHITE_BOLD + loc.getName().toUpperCase() + RESET + " (ID: " + loc.getId() + ")");
        System.out.println("   " + loc.getDescription());
        
        if (loc.isLocked()) {
            System.out.println(RED + "   (!) ATENÇÃO: As saídas estão trancadas!" + RESET);
        }
    }

    /**
     * Apresenta o Menu Principal de Ações disponíveis no turno.
     * * @param hasActiveAbility Define se a opção de "Habilidade de Classe" deve ser mostrada.
     */
    public void showActions(boolean hasActiveAbility) {
        System.out.println("\n" + WHITE_BOLD + "ESCOLHE UMA AÇÃO:" + RESET);
        System.out.println(" [1] 🏃 Mover");
        System.out.println(" [2] 🖐️  Apanhar Item");
        System.out.println(" [3] 🎒 Abrir Mochila");
        
        if (hasActiveAbility) {
            System.out.println(" [4] ✨ Habilidade de Classe");
        }
        
        System.out.println(" [5] 💤 Descansar");
        System.out.println(" [0] 🏳️  DESISTIR");
        System.out.print(GREEN + "> " + RESET);
    }
    
    /**
     * Sobrecarga do método showActions que assume que a habilidade está disponível por defeito.
     */
    public void showActions() {
        showActions(true); 
    }
}