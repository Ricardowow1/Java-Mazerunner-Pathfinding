package game.models;

import java.util.Random;

/**
 * Representa um mecanismo físico interativo (Alavanca) presente numa localização.
 * <p>
 * As alavancas são componentes cruciais para o progresso no jogo, geralmente servindo
 * para destrancar a Sala do Tesouro remotamente. Funcionam com base num mecanismo 
 * de combinação simples: o jogador deve escolher a posição correta (1, 2 ou 3).
 * </p>
 * <p>
 * Uma vez ativada com sucesso, a alavanca permanece desbloqueada permanentemente.
 * </p>
 */
public class Lever {
    
    /** Estado atual da alavanca (true = precisa de ser ativada, false = já foi ativada). */
    private boolean isLocked;
    
    /** O segredo gerado aleatoriamente que o jogador tem de adivinhar. */
    private int correctPosition; 

    /**
     * Constrói uma nova Alavanca.
     * <p>
     * O estado inicial é sempre "Trancado" e a posição correta (o segredo)
     * é gerada aleatoriamente (entre 1 e 3) no momento da criação.
     * </p>
     */
    public Lever() {
        this.isLocked = true; 
        this.correctPosition = new Random().nextInt(3) + 1;
    }

    /**
     * Tenta ativar a alavanca com uma posição fornecida pelo jogador.
     * <p>
     * Se a alavanca já estiver desbloqueada, retorna {@code true} imediatamente.
     * Caso contrário, verifica se a tentativa coincide com o segredo gerado.
     * Se acertar, o estado da alavanca muda permanentemente para desbloqueado.
     * </p>
     * @param attempt A escolha do jogador (1, 2 ou 3).
     * @return {@code true} se a alavanca foi desbloqueada com sucesso (ou já estava),
     * {@code false} se a posição escolhida estava incorreta.
     */
    public boolean tryUnlock(int attempt) {
        if (!isLocked) return true; // Já está aberta

        if (attempt == correctPosition) {
            isLocked = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifica se a alavanca ainda precisa de ser ativada.
     * @return {@code true} se está trancada, {@code false} se já foi ativada.
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Retorna uma representação textual do estado da alavanca.
     * Útil para logs e depuração.
     */
    @Override
    public String toString() {
        return "Alavanca [" + (isLocked ? "TRANCADA" : "ABERTA") + "]";
    }
}