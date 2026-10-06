package game.utils;

import game.models.ActionLog;
import game.models.Player;
import LinkedList.LinkedList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestor de relatórios e logs do jogo.
 * <p>
 * Esta classe é responsável por armazenar o histórico de ações turno a turno
 * e, no final da partida, gerar um ficheiro JSON detalhado com o resumo do jogo,
 * incluindo o caminho percorrido por cada jogador.
 * </p>
 */
public class ReportManager {

    private LinkedList<ActionLog> logs;
    private int currentTurn;

    /**
     * Construtor do ReportManager.
     * Inicializa a lista de logs e define o turno inicial como 1.
     */
    public ReportManager() {
        this.logs = new LinkedList<>();
        this.currentTurn = 1;
    }
    
    /**
     * Obtém o número do turno atual.
     * @return O número inteiro do turno.
     */
    public Integer getCurrentTurn() { return currentTurn; }

    /**
     * Incrementa o contador de turnos global.
     * Deve ser chamado pelo controlador quando todos os jogadores terminam a sua jogada.
     */
    public void nextTurn() { this.currentTurn++; }

    /**
     * Regista uma ação específica de um jogador no histórico.
     *
     * @param p           O jogador que realizou a ação.
     * @param type        O tipo de ação (ex: "MOVIMENTO", "ATAQUE", "ITEM").
     * @param description Uma descrição detalhada do que aconteceu.
     */
    public void log(Player p, String type, String description) {
        String locName = (p.getCurrentLocation() != null) ? p.getCurrentLocation().getName() : "Desconhecido";
        // Cria um registo imutável (ActionLog)
        ActionLog entry = new ActionLog(currentTurn, p.getName(), p.getPlayerClass().toString(), locName, p.getHealth(), p.getStamina(), type, description);
        logs.add(entry);
    }

    /**
     * Gera o ficheiro JSON final com todo o histórico da partida e percursos.
     * <p>
     * O ficheiro é guardado na pasta "relatorios/" com o nome do mapa original.
     * Inclui duas secções: "game_logs" (ação a ação) e "player_paths" (resumo do caminho).
     * </p>
     *
     * @param mapPath O caminho do ficheiro de mapa original (para dar nome ao relatório).
     * @param players A lista final de jogadores (para extrair o histórico de movimentos de cada um).
     */
    public void generateReport(String mapPath, LinkedList<Player> players) {
        new File("relatorios").mkdir();

        File f = new File(mapPath);
        String mapName = f.getName().replace(".json", "");
        String filename = "relatorios/" + mapName + "Report.json";

        StringBuilder json = new StringBuilder();
        json.append("{\n"); // Abre objeto principal
        
        // 1. SECÇÃO DE LOGS (Turno a Turno)
        json.append("  \"game_logs\": [\n");
        for (int i = 0; i < logs.size(); i++) {
            json.append(logs.get(i).toJson());
            if (i < logs.size() - 1) json.append(",\n");
        }
        json.append("\n  ],\n");

        // 2. SECÇÃO DE MAPA/PERCURSO (Resumo Final)
        json.append("  \"player_paths\": {\n");
        
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            json.append("    \"").append(p.getName()).append("\": \"");
            
            // Constrói a string do caminho: "Entrada -> Corredor -> Sala"
            LinkedList<String> path = p.getPathHistory();
            for (int j = 0; j < path.size(); j++) {
                json.append(path.get(j));
                if (j < path.size() - 1) json.append(" -> ");
            }
            
            json.append("\"");
            if (i < players.size() - 1) json.append(",\n");
        }
        
        json.append("\n  }\n");
        json.append("}"); // Fecha objeto principal

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(json.toString());
            System.out.println("\n📄 RELATÓRIO GUARDADO COM MAPAS EM: " + filename);
        } catch (IOException e) {
            System.out.println("Erro ao gravar relatório: " + e.getMessage());
        }
    }
}