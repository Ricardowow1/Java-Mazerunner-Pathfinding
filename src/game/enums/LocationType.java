package game.enums;

/**
 * Define os tipos fixos de localizações possíveis no mapa.
 * <p>
 * Utilizado para categorizar as salas e determinar que tipo de interações
 * ou eventos são mais prováveis de acontecer nesse local.
 * </p>
 */
public enum LocationType {
    
    /** O ponto de partida de todos os jogadores. */
    ENTRADA,

    /**
     * Locais de passagem. Têm maior probabilidade de gerar eventos aleatórios
     * e geralmente não contêm tesouros importantes.
     */
    CORREDOR,

    /**
     * Salas principais. É aqui que normalmente se encontram Enigmas,
     * Alavancas e Itens valiosos.
     */
    DIVISAO,

    /**
     * O objetivo final do jogo. Contém a condição de vitória.
     * Geralmente começa trancado até as alavancas serem ativadas.
     */
    TESOURO
}