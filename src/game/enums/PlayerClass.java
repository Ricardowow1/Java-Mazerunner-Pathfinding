package game.enums;

/**
 * Define as classes (arquétipos) de personagens que os jogadores podem escolher.
 * <p>
 * Cada classe possui atributos iniciais diferentes (HP, Stamina) e, em alguns casos,
 * mecânicas passivas ou habilidades especiais únicas.
 * </p>
 */
public enum PlayerClass {
    
    /**
     * Classe equilibrada e resistente.
     * <br>Vantagem: Começa com mais HP e Stamina que as outras classes.
     */
    HEROI,

    /**
     * Classe utilitária.
     * <br>Vantagem: Ideal para exploração (pode ter itens iniciais extras).
     */
    AVENTUREIRO,

    /**
     * Classe ofensiva/oportunista.
     * <br>Habilidade: Pode ter mecânicas de roubo ou vantagem em combate.
     */
    BANDIDO,

    /**
     * Classe ágil e furtiva.
     * <br>Habilidade Passiva: Tem uma chance percentual de evitar armadilhas (TRAP) e eventos negativos.
     */
    NINJA,

    /**
     * Classe mágica poderosa mas frágil.
     * <br>Habilidade Ativa: Pode lançar feitiços de dano em área (com Cooldown).
     */
    MAGO
}