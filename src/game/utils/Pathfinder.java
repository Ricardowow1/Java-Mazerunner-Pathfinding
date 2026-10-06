package game.utils;

import game.models.Location;
import game.enums.LocationType;
import LinkedList.LinkedList;

/**
 * Classe utilitária responsável por algoritmos de navegação e pesquisa no grafo do mapa.
 * <p>
 * Implementa o algoritmo <b>Breadth-First Search (BFS)</b> (Busca em Largura) para
 * percorrer as salas de forma eficiente, garantindo que o caminho mais curto é encontrado
 * primeiro (no caso de {@code findLocationByType}) ou que todo o mapa é explorado
 * (no caso de {@code countLevers}).
 * </p>
 */
public class Pathfinder {

    /**
     * Procura a <b>primeira</b> localização de um determinado tipo no mapa.
     * <p>
     * O algoritmo para imediatamente assim que encontra a primeira ocorrência.
     * Este comportamento é ideal para encontrar objetivos únicos, como a sala do TESOURO.
     * </p>
     * * @param startNode A localização inicial para começar a busca (raiz).
     * @param type      O tipo de sala a procurar (ex: {@link LocationType#TESOURO}).
     * @return A primeira {@link Location} encontrada ou {@code null} se não existir.
     */
    public static Location findLocationByType(Location startNode, LocationType type) {
        if (startNode == null) return null;

        LinkedList<Location> visited = new LinkedList<>();
        LinkedList<Location> queue = new LinkedList<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Location current = queue.get(0);
            queue.remove(current);

            if (isVisited(visited, current)) continue;
            visited.add(current);

            // [OBJETIVO]: Retorna imediatamente assim que encontra
            if (current.getType() == type) {
                return current;
            }

            addNeighborsToQueue(queue, current);
        }
        return null;
    }

    /**
     * Encontra <b>todas</b> as localizações de um determinado tipo no mapa.
     * <p>
     * Ao contrário do método anterior, este percorre o mapa inteiro sem parar.
     * É utilizado no setup inicial para identificar todas as ENTRADAS possíveis
     * para os jogadores e bots.
     * </p>
     * * @param startNode Ponto de partida.
     * @param type      O tipo de sala a procurar (ex: {@link LocationType#ENTRADA}).
     * @return Uma {@link LinkedList} contendo todas as localizações encontradas.
     */
    public static LinkedList<Location> findAllLocationsByType(Location startNode, LocationType type) {
        LinkedList<Location> foundList = new LinkedList<>();
        if (startNode == null) return foundList;

        LinkedList<Location> visited = new LinkedList<>();
        LinkedList<Location> queue = new LinkedList<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Location current = queue.get(0);
            queue.remove(current);

            if (isVisited(visited, current)) continue;
            visited.add(current);

            // [OBJETIVO]: Adiciona à lista e CONTINUA a procurar
            if (current.getType() == type) {
                foundList.add(current);
            }

            addNeighborsToQueue(queue, current);
        }
        return foundList;
    }

    /**
     * Conta quantas alavancas existem no mapa e quantas delas já foram ativadas.
     * <p>
     * Percorre o mapa inteiro para fornecer um relatório preciso do progresso
     * global do jogo. Essencial para verificar a condição de vitória.
     * </p>
     * * @param startNode Um ponto de entrada no mapa.
     * @return Um array de int onde:
     * <ul>
     * <li>Índice 0 = Total de Alavancas no mapa.</li>
     * <li>Índice 1 = Quantidade de Alavancas já ativadas.</li>
     * </ul>
     */
    public static int[] countLevers(Location startNode) {
        int[] results = {0, 0}; // {total, ativas}
        
        if (startNode == null) return results;

        LinkedList<Location> visited = new LinkedList<>();
        LinkedList<Location> queue = new LinkedList<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Location current = queue.get(0);
            queue.remove(current);

            if (isVisited(visited, current)) continue;
            visited.add(current);

            // Lógica específica de contagem de alavancas
            if (current.hasJsonLever()) {
                results[0]++; // Incrementa total
                if (current.isLeverActivated()) {
                    results[1]++; // Incrementa ativas
                }
            }

            addNeighborsToQueue(queue, current);
        }
        return results;
    }

    // ==========================================
    //           MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Verifica se uma localização já foi visitada pelo algoritmo.
     * Evita ciclos infinitos em mapas com loops.
     */
    private static boolean isVisited(LinkedList<Location> visited, Location loc) {
        for (int i = 0; i < visited.size(); i++) {
            if (visited.get(i).getId() == loc.getId()) return true;
        }
        return false;
    }

    /**
     * Adiciona todos os vizinhos da localização atual à fila de processamento.
     */
    private static void addNeighborsToQueue(LinkedList<Location> queue, Location current) {
        LinkedList<Location> neighbors = current.getNeighbors();
        for (int i = 0; i < neighbors.size(); i++) {
            queue.add(neighbors.get(i));
        }
    }
}