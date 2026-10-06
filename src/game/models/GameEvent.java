package game.models;

import game.enums.EventType;

/**
 * Representa um evento aleatório (sorte ou azar) que pode ocorrer durante o jogo.
 * <p>
 * Os objetos desta classe são geralmente carregados a partir de um ficheiro JSON (ex: events.json).
 * Eles servem como "instruções" para o {@link game.logic.EventHandler} saber o que fazer
 * ao jogador (ex: causar dano, curar, mover, etc.).
 * </p>
 */
public class GameEvent {

    private String description;
    private EventType type; 
    private int value;

    /**
     * Constrói um novo evento de jogo.
     * * @param description O texto narrativo que aparece na consola (ex: "Pisaste um prego!").
     * @param type        O tipo de efeito mecânico (definido no Enum {@link EventType}).
     * @param value       A intensidade do efeito (ex: 10 de dano, 2 de cura, 1 turno sem jogar).
     */
    public GameEvent(String description, EventType type, int value) {
        this.description = description;
        this.type = type;
        this.value = value;
    }

    /**
     * Obtém a descrição textual do evento.
     * * @return A frase de "flavor text" para o jogador.
     */
    public String getDescription() { 
        return description; 
    }

    /**
     * Obtém o tipo de evento.
     * <p>
     * É este valor que permite ao sistema (Switch-Case) decidir qual a lógica a aplicar
     * (se é para tirar vida, dar stamina, trocar de lugar, etc.).
     * </p>
     * * @return O Enum {@link EventType}.
     */
    public EventType getType() { 
        return type; 
    } 

    /**
     * Obtém o valor numérico associado ao evento.
     * * @return O valor do impacto (ex: quantidade de HP, número de turnos, etc.).
     */
    public int getValue() { 
        return value; 
    }
}