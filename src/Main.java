import game.logic.GameController;

/**
 * Ponto de entrada (Entry Point) da aplicação "Labirinto da Glória".
 * <p>
 * Esta classe contém o método {@code main}, responsável por instanciar
 * o controlador principal e iniciar o ciclo de vida do jogo.
 * </p>
 */
public class Main {

    /**
     * Método principal executado pela JVM.
     * @param args Argumentos de linha de comandos (não utilizados neste projeto).
     */
    public static void main(String[] args) {
        System.out.println(">> A iniciar o sistema...");

        // 1. Instancia o Controlador (O "Cérebro" do padrão MVC)
        GameController game = new GameController();
        
        // 2. Arranca o loop do menu e do jogo
        game.start();
    }
}