package game.logic;

import game.models.Item;
import game.models.Player;
import LinkedList.LinkedList;
import java.util.Scanner;
import game.enums.ItemType;

/**
 * Gere a interface e a lógica do menu da mochila do jogador.
 * Permite visualizar, usar e descartar itens.
 */
public class BackpackMenu {

    private Scanner scanner;
    private GameController controller;

    public BackpackMenu(Scanner scanner, GameController controller) {
        this.scanner = scanner;
        this.controller = controller;
    }

    /**
     * Abre o menu da mochila.
     * @param player O jogador.
     * @return true se usou item.
     */
    public boolean open(Player player) { 
        boolean inMenu = true;

        while (inMenu) {
            LinkedList<Item> bag = player.getBackpack();

            System.out.println("\n🎒 === MOCHILA ===");
            if (bag.isEmpty()) {
                System.out.println("   (Vazia)");
                return false; 
            }

            for (int i = 0; i < bag.size(); i++) {
                Item it = bag.get(i);
                String icon = getIconForItem(it.getType());
                System.out.println("   [" + (i + 1) + "] " + icon + " " + it.getName() + " (" + it.getType() + ")");
            }

            System.out.println("\nEscolhe o NÚMERO para usar ou [0] para voltar.");
            System.out.print("> ");
            
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return false; 
            } else {
                try {
                    int index = Integer.parseInt(input) - 1; 

                    if (index >= 0 && index < bag.size()) {
                        Item item = bag.get(index);
                        useItemLogic(player, item);
                        return true; 
                    } else {
                        System.out.println(">> Número inválido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println(">> Opção inválida.");
                }
            }
        }
        return false; 
    }
    
    private String getIconForItem(ItemType type) {
        switch (type) {
            case CURA: return "💚";
            case STAMINA: return "⚡";
            case ATAQUE_GLOBAL: return "💣";
            case STUN_GLOBAL: return "❄️";
            case PROTECAO: return "🛡️";
            default: return "📦";
        }
    }

    private void useItemLogic(Player p, Item item) {
        System.out.println(">> Usaste " + item.getName());
        ItemType tipo = item.getType();

        if (tipo == ItemType.CURA) p.heal(item.getValue());
        else if (tipo == ItemType.STAMINA) p.recoverStamina(item.getValue());
        else if (tipo == ItemType.LIXO) System.out.println("   🗑️ Lixo deitado fora.");
        
        else if (tipo == ItemType.ATAQUE_GLOBAL) {
            controller.triggerGlobalDamage(p, "Dano", item.getValue());
        }
        else if (tipo == ItemType.STUN_GLOBAL) {
            controller.triggerGlobalDamage(p, "Stun", 0);
        }

        p.getBackpack().remove(item);
    }
}
