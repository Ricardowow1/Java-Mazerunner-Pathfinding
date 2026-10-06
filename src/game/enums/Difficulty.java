package game.enums;

/**
 * Define os níveis de dificuldade para os Bots (Inteligência Artificial).
 * <p>
 * Esta enumeração é utilizada pela classe {@code BotBrain} e pelas estratégias
 * (Easy, Normal, Hard) para determinar a probabilidade de sucesso em enigmas,
 * alavancas e a inteligência de movimento.
 * </p>
 */
public enum Difficulty {
    
    /**
     * Nível Fácil:
     * <ul>
     * <li>Baixa probabilidade de resolver enigmas (20%).</li>
     * <li>Movimento aleatório.</li>
     * <li>Esquece-se de apanhar itens frequentemente.</li>
     * </ul>
     */
    FACIL,

    /**
     * Nível Normal:
     * <ul>
     * <li>Probabilidade média de resolver enigmas (50%).</li>
     * <li>Usa itens de cura quando necessário.</li>
     * <li>Movimento semi-inteligente (evita portas trancadas).</li>
     * </ul>
     */
    NORMAL,

    /**
     * Nível Difícil:
     * <ul>
     * <li>Alta probabilidade de resolver enigmas e alavancas (+80%).</li>
     * <li>Utiliza algoritmo BFS para encontrar o caminho mais curto.</li>
     * <li>Gere o inventário de forma tática.</li>
     * </ul>
     */
    DIFICIL
}