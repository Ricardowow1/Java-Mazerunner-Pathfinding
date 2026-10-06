package game.enums;

/**
 * Define os tipos de eventos aleatórios que podem ocorrer no jogo.
 * <p>
 * Estes eventos são geralmente despoletados quando um jogador (Humano ou Bot)
 * atravessa um corredor ou entra numa sala com a propriedade "Surpresa".
 * </p>
 */
public enum EventType {
    
    // --- EFEITOS BÁSICOS ---
    
    /** Evento narrativo, sem efeito mecânico (apenas texto de ambiente). */
    FLAVOR,

    /** Causa dano direto aos pontos de vida (HP) do jogador. */
    DANO,

    /** Restaura pontos de vida (HP) do jogador. */
    CURA,

    /** Recupera ou consome pontos de energia (Stamina). */
    STAMINA,

    /** Armadilha física que causa dano (pode ser evitada por certas classes, ex: Ninja). */
    TRAP,

    // --- MECÂNICAS DE JOGO AVANÇADAS ---

    /** O jogador perde a sua próxima jogada (fica atordoado ou preso). */
    SKIP_TURN,

    /** O jogador ganha imediatamente uma nova ação neste turno. */
    EXTRA_TURN,

    /** O jogador troca de posição (Localização) com outro jogador aleatório. */
    SWAP_POSITION,

    /** Caos total: Todos os jogadores do jogo trocam de posições aleatoriamente. */
    SWAP_ALL,

    /** Força o jogador a recuar para a sala de onde veio. */
    MOVE_BACK
}