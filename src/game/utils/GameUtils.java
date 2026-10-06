package game.utils;

/**
 * Classe utilitária que fornece métodos estáticos auxiliares para todo o jogo.
 * <p>
 * O objetivo desta classe é centralizar lógicas genéricas, como pausas na execução
 * ou formatação de texto, evitando duplicação de código.
 * </p>
 */
public class GameUtils {

    /**
     * Pausa a execução da thread atual (o jogo) por um determinado período de tempo.
     * <p>
     * Este método é utilizado principalmente para criar um "delay" visual nas ações
     * dos Bots, permitindo ao jogador humano acompanhar o que está a acontecer
     * no ecrã sem que o texto apareça instantaneamente.
     * </p>
     * * @param ms O tempo de pausa em milissegundos (ex: 1000 = 1 segundo).
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // Restaura o estado de interrupção da thread
            Thread.currentThread().interrupt();
            System.err.println("Erro na pausa (Sleep interrompido): " + e.getMessage());
        }
    }
}