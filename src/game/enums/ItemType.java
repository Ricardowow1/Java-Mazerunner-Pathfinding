package game.enums;

/**
 * Categoriza os itens disponíveis no jogo.
 * <p>
 * O tipo do item determina como ele pode ser usado (se é consumível,
 * se é uma arma ou se é apenas lixo) e qual o seu efeito no jogador.
 * </p>
 */
public enum ItemType {
    
    /** Restaura pontos de vida (HP). */
    CURA,

    /** Restaura pontos de energia (Stamina). */
    STAMINA,

    /** Item especial que causa dano a todos os outros jogadores no mapa. */
    ATAQUE_GLOBAL,

    /** Item especial que faz todos os outros jogadores perderem o turno. */
    STUN_GLOBAL,

    /** Item sem utilidade prática, ocupa espaço no inventário (Armadilha de inventário). */
    LIXO,

    /** Item passivo que reduz o dano recebido (Ex: Escudo). */
    PROTECAO,

    /** Item que confere pontos ou vantagens estatísticas. */
    BONUS
}