package game.models;

/**
 * Representa um desafio intelectual (puzzle/charada) presente numa localização.
 * <p>
 * O Enigma serve como um "guardião" de uma sala: enquanto não for resolvido,
 * o jogador não pode avançar ou recolher itens dessa sala.
 * </p>
 * <p>
 * A resposta é validada de forma flexível (case-insensitive e ignora espaços nas pontas).
 * </p>
 */
public class Enigma {

    private String question;
    private String answer;
    private boolean isSolved;

    /**
     * Construtor para criar um novo enigma.
     * * @param question A pergunta ou charada a ser apresentada ao jogador.
     * @param answer   A resposta correta (chave da solução).
     */
    public Enigma(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.isSolved = false;
    }

    /**
     * Tenta resolver o enigma comparando a tentativa do jogador com a resposta correta.
     * <p>
     * A comparação é feita ignorando maiúsculas/minúsculas e espaços em branco no início ou fim.
     * Se o enigma já tiver sido resolvido anteriormente, retorna {@code true} imediatamente.
     * </p>
     * * @param attempt A string com a resposta fornecida pelo jogador ou bot.
     * @return {@code true} se a resposta estiver correta (ou já resolvido), {@code false} caso contrário.
     */
    public boolean solve(String attempt) {
        if (isSolved) return true; // Já estava resolvido
        if (attempt == null) return false;
        
        // Compara ignorando maiúsculas e espaços extras
        if (attempt.trim().equalsIgnoreCase(answer.trim())) {
            isSolved = true;
            return true;
        }
        return false;
    }

    /**
     * Obtém o texto da pergunta.
     * * @return A pergunta do enigma.
     */
    public String getQuestion() { 
        return question; 
    }
    
    /**
     * Obtém a resposta correta do enigma.
     * <p>
     * <b>Nota:</b> Este método é utilizado principalmente pelos Bots de dificuldade elevada
     * (Hard) para "simular" inteligência ao saberem a resposta correta.
     * </p>
     * * @return A string com a resposta correta.
     */
    public String getAnswer() { 
        return answer; 
    }
    
    /**
     * Verifica se o enigma já foi resolvido.
     * * @return {@code true} se já foi solucionado, {@code false} caso contrário.
     */
    public boolean isSolved() { 
        return isSolved; 
    }
}