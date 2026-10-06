package game.models;

/**
 * Representa uma entrada individual de registo (Log) no relatório final do jogo.
 * <p>
 * Cada objeto {@code ActionLog} captura um "snapshot" do estado do jogador num
 * determinado momento (Turno), incluindo a sua localização, vida, energia e a
 * ação que acabou de realizar.
 * </p>
 * <p>
 * Esta classe é essencial para a geração do ficheiro JSON final, permitindo
 * reconstruir a narrativa da partida passo a passo.
 * </p>
 */
public class ActionLog {

    private int turn;
    private String playerName;
    private String playerClass;
    private String location;
    private int hp;
    private int stamina;
    private String actionType;
    private String description;

    /**
     * Construtor completo para criar um registo de ação.
     * * @param turn        O número do turno em que a ação ocorreu.
     * @param pName       O nome do jogador ou bot.
     * @param pClass      A classe da personagem (ex: "HEROI", "MAGO").
     * @param loc         O nome da localização onde o jogador se encontra.
     * @param hp          Os pontos de vida atuais do jogador no momento do registo.
     * @param stamina     Os pontos de energia atuais do jogador no momento do registo.
     * @param type        O tipo de ação realizada (ex: "MOVIMENTO", "COMBATE", "ITEM").
     * @param desc        Uma descrição textual detalhada do que aconteceu.
     */
    public ActionLog(int turn, String pName, String pClass, String loc, int hp, int stamina, String type, String desc) {
        this.turn = turn;
        this.playerName = pName;
        this.playerClass = pClass;
        this.location = loc;
        this.hp = hp;
        this.stamina = stamina;
        this.actionType = type;
        this.description = desc;
    }

    /**
     * Converte os dados deste registo para uma String formatada em JSON.
     * <p>
     * Este método constrói manualmente a estrutura JSON para evitar dependências
     * de bibliotecas externas (como Gson ou Jackson).
     * </p>
     * * @return Uma String contendo o objeto JSON formatado e indentado.
     */
    public String toJson() {
        return String.format(
            "  {\n" +
            "    \"turn\": %d,\n" +
            "    \"player\": \"%s\",\n" +
            "    \"class\": \"%s\",\n" +
            "    \"location\": \"%s\",\n" +
            "    \"hp\": %d,\n" +
            "    \"stamina\": %d,\n" +
            "    \"action\": \"%s\",\n" +
            "    \"details\": \"%s\"\n" +
            "  }",
            turn, playerName, playerClass, location, hp, stamina, actionType, description
        );
    }
}