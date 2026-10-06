package game.models;

import game.enums.ItemType;

/**
 * Representa um objeto tangível que pode ser encontrado e recolhido durante o jogo.
 * <p>
 * Os itens são armazenados na mochila do {@link Player} e podem ser usados através
 * do {@link game.logic.BackpackMenu}.
 * </p>
 * <p>
 * O comportamento do item ao ser usado depende estritamente do seu {@link ItemType}
 * (ex: CURA regenera vida, ATAQUE_GLOBAL causa dano aos inimigos).
 * </p>
 */
public class Item {

    private String name;
    private ItemType type; 
    private int value;

    /**
     * Constrói um novo item.
     * * @param name  O nome visível do item (ex: "Poção de Vida", "Bomba de Fumo").
     * @param type  A categoria do item, que define o seu efeito mecânico.
     * @param value O valor numérico do efeito (ex: 50 de cura, 20 de dano).
     */
    public Item(String name, ItemType type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    /**
     * Obtém o nome do item.
     * * @return O nome formatado para exibição.
     */
    public String getName() { 
        return name; 
    }

    /**
     * Obtém o tipo do item.
     * Este Enum é usado pela lógica do jogo para decidir o que fazer quando o item é ativado.
     * * @return O {@link ItemType} do item.
     */
    public ItemType getType() { 
        return type; 
    }

    /**
     * Obtém o poder ou valor do item.
     * * @return Um inteiro representando a magnitude do efeito.
     */
    public int getValue() { 
        return value; 
    }

    /**
     * Retorna uma representação textual do item.
     * Útil para listagens na consola.
     */
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}