package game.logic;

import game.models.Player;
import game.enums.PlayerClass;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Gere as habilidades especiais das classes de jogadores e efeitos globais de itens.
 * <p>
 * Esta classe centraliza a lógica de combate (Dano, Stun) e as mecânicas únicas
 * de classes como o Bandido (Roubo) e o Mago (Dano em Área).
 * </p>
 */
public class AbilityHandler {

    private Random random;

    /**
     * Construtor da classe AbilityHandler.
     * Inicializa o gerador de números aleatórios.
     */
    public AbilityHandler() {
        this.random = new Random();
    }

    // ==========================================
    //       MÉTODOS GENÉRICOS (Para Itens)
    // ==========================================

    /**
     * Aplica um efeito a todos os outros jogadores no jogo (exceto a fonte).
     * <p>
     * Usado por itens globais (ex: Bomba, Flashbang) ou habilidades de área.
     * </p>
     *
     * @param source     O jogador que desencadeou o efeito (não será afetado).
     * @param allPlayers A lista completa de jogadores na partida.
     * @param effect     O nome do efeito a aplicar (ex: "Dano", "Stun").
     * @param value      O valor numérico do efeito (ex: quantidade de dano).
     */
    public void affectOthers(Player source, LinkedList<Player> allPlayers, String effect, int value) {
        System.out.println("[COMBATE] " + source.getName() + " usou um efeito global!");
        
        for(int i = 0; i < allPlayers.size(); i++) {
            Player target = allPlayers.get(i);
            
            // Ignora o próprio jogador e jogadores já eliminados
            if (!target.equals(source) && target.isAlive()) {
                applyEffect(target, effect, value);
            }
        }
    }

    /**
     * Aplica um efeito específico num único alvo.
     *
     * @param target O jogador que irá sofrer o efeito.
     * @param effect O tipo de efeito ("Dano" ou "Stun").
     * @param value  A intensidade do efeito.
     */
    private void applyEffect(Player target, String effect, int value) {
        if (effect.equalsIgnoreCase("Dano")) {
            target.takeDamage(value);
        }
        else if (effect.equalsIgnoreCase("Stun")) {
            target.addSkipTurn(1); 
            System.out.println("   -> " + target.getName() + " ficou atordoado!");
        }
    }

    // ==========================================
    //       MÉTODOS DE CLASSE (Skills)
    // ==========================================

    /**
     * Verifica a classe do jogador e tenta ativar a sua habilidade especial.
     *
     * @param p          O jogador que está a tentar usar a habilidade.
     * @param allPlayers A lista de todos os jogadores (necessária para habilidades que afetam outros).
     */
    public void useAbility(Player p, LinkedList<Player> allPlayers) {
        PlayerClass pc = p.getPlayerClass();

        if (pc == PlayerClass.BANDIDO) {
            handleBandit(p, allPlayers);
        } else if (pc == PlayerClass.MAGO) {
            handleMage(p, allPlayers);
        } else {
            System.out.println(">> A tua classe não tem habilidade ativa.");
        }
    }

    /**
     * Executa a lógica da habilidade do Bandido: Roubar item.
     *
     * @param thief   O jogador da classe Bandido.
     * @param players A lista de potenciais vítimas.
     */
    private void handleBandit(Player thief, LinkedList<Player> players) {
        if (!thief.canUseBanditSteal()) {
            System.out.println(">> Já usaste o roubo nesta partida!");
            return;
        }

        LinkedList<Player> targets = new LinkedList<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            // Só pode roubar quem estiver vivo, não for ele próprio e tiver itens
            if (!p.equals(thief) && p.isAlive() && !p.getBackpack().isEmpty()) {
                targets.add(p);
            }
        }

        if (targets.isEmpty()) {
            System.out.println(">> Não há ninguém válido para roubar.");
            return;
        }

        int index = random.nextInt(targets.size());
        Player victim = targets.get(index);

        System.out.println(">> O BANDIDO ataca nas sombras... Alvo: " + victim.getName());

        // Lógica simplificada de transferência de todos os itens (assalto)
        // ou poderia ser apenas 1 item aleatório.
        LinkedList<game.models.Item> loot = victim.getBackpack();
        if (loot.isEmpty()) {
            System.out.println(">> Azar! A mochila da vítima estava vazia.");
        } else {
            System.out.println(">> ROUBO BEM SUCEDIDO! Apanhaste itens.");
            while (!loot.isEmpty()) {
                game.models.Item it = loot.get(0);
                thief.pickUpItem(it); 
                loot.remove(it);      
            }
        }
        thief.setBanditStealUsed();
    }

    /**
     * Executa a lógica da habilidade do Mago: Explosão Arcana.
     *
     * @param mage    O jogador da classe Mago.
     * @param players A lista de alvos.
     */
    private void handleMage(Player mage, LinkedList<Player> players) {
        if (!mage.canUseMageSpell()) {
            System.out.println(">> Magia em cooldown (" + mage.getMageCooldown() + " turnos).");
            return;
        }

        System.out.println(">> EXPLOSÃO ARCANA! Todos sofrem dano.");
        
        affectOthers(mage, players, "Dano", 10);
        
        mage.triggerMageCooldown();
    }
}