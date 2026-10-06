package game.utils;

import game.models.Location;
import game.models.Item;
import game.models.Enigma;
import LinkedList.LinkedList;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utilitário responsável por serializar (guardar) o estado do mapa num ficheiro JSON.
 * <p>
 * Esta classe é utilizada principalmente pelo Gerador de Mapas Aleatórios para
 * persistir o mundo criado no disco, permitindo que seja carregado posteriormente.
 * Constrói a String JSON manualmente para evitar dependências externas.
 * </p>
 */
public class JsonSaver {

    /**
     * Recebe uma lista de locais (o mapa completo) e grava-a num ficheiro JSON.
     * <p>
     * O método percorre todas as localizações e converte os seus atributos
     * (ID, nome, vizinhos, itens, enigmas, etc.) para o formato JSON padrão.
     * </p>
     * @param map      A lista encadeada contendo todas as {@link Location} do mapa.
     * @param filePath O caminho onde o ficheiro será criado (ex: "maps/novo_mundo.json").
     */
    public static void saveMap(LinkedList<Location> map, String filePath) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < map.size(); i++) {
            Location loc = map.get(i);
            json.append("  {\n");
            
            // 1. Dados básicos
            json.append("    \"id\": ").append(loc.getId()).append(",\n");
            json.append("    \"name\": \"").append(escape(loc.getName())).append("\",\n");
            json.append("    \"type\": \"").append(loc.getType()).append("\",\n");
            json.append("    \"description\": \"").append(escape(loc.getDescription())).append("\",\n");
            
            // 2. Eventos
            if (loc.hasEvent()) {
                json.append("    \"eventType\": \"").append(loc.getEventType()).append("\",\n");
            }

            // 3. Propriedades Especiais (Trancado / Alavanca)
            if (loc.isLocked()) {
                json.append("    \"locked\": \"true\",\n");
            }
            if (loc.hasJsonLever()) {
                json.append("    \"lever\": \"true\",\n");
            }

            // 4. Enigmas
            if (loc.hasEnigma()) {
                Enigma e = loc.getEnigma();
                json.append("    \"enigma\": {\n");
                json.append("      \"question\": \"").append(escape(e.getQuestion())).append("\",\n");
                json.append("      \"answer\": \"").append(escape(e.getAnswer())).append("\"\n");
                json.append("    },\n");
            }

            // 5. Itens
            if (loc.hasItems()) {
                json.append("    \"items\": [");
                LinkedList<Item> items = loc.getItems();
                for (int j = 0; j < items.size(); j++) {
                    json.append("\"").append(escape(items.get(j).getName())).append("\"");
                    if (j < items.size() - 1) json.append(", ");
                }
                json.append("],\n");
            }

            // 6. Vizinhos (IDs)
            json.append("    \"neighborIds\": [");
            LinkedList<Location> neighbors = loc.getNeighbors();
            for (int k = 0; k < neighbors.size(); k++) {
                json.append(neighbors.get(k).getId());
                if (k < neighbors.size() - 1) json.append(", ");
            }
            json.append("]\n");

            // Fecha objeto
            json.append("  }");
            if (i < map.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("]");

        // Escrever no disco
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json.toString());
            System.out.println(">> Mapa gerado e guardado com sucesso em: " + filePath);
        } catch (IOException e) {
            System.out.println("ERRO ao guardar mapa: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para escapar caracteres especiais (como aspas) dentro de strings JSON.
     * Evita que o JSON fique inválido se um nome contiver aspas.
     * @param text O texto original.
     * @return O texto seguro para JSON.
     */
    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}