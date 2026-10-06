package game.utils;

import game.models.Location;
import game.models.Item;
import game.models.Enigma;
import game.enums.LocationType;
import LinkedList.LinkedList;
import java.util.Random;

/**
 * Gerador Procedural de Mapas (Dungeon Generator).
 * <p>
 * Esta classe é responsável por criar uma estrutura de mapa aleatória mas jogável,
 * garantindo que existem entradas, um tesouro final e caminhos válidos.
 * O algoritmo segue 4 fases principais:
 * <ol>
 * <li>Carregar "Pools" de salas (templates) a partir de um JSON base.</li>
 * <li>Selecionar um subconjunto de salas (1 Tesouro, 4 Entradas, N Corredores).</li>
 * <li>Baralhar e instanciar as salas com IDs sequenciais.</li>
 * <li>Criar conexões (arestas) entre as salas para formar um grafo conexo.</li>
 * </ol>
 * </p>
 */
public class MapGenerator {

    /** Catálogo global de itens carregados para popular baús. */
    private static LinkedList<Item> gameItemsCatalog = new LinkedList<>();
    
    /** Pool global de enigmas disponíveis para distribuir pelas salas. */
    private static LinkedList<Enigma> enigmasPool = new LinkedList<>();

    /**
     * Gera um novo mapa aleatório e guarda-o num ficheiro JSON.
     * @param poolFile   Ficheiro JSON com os templates de salas (ex: "locais_pool.json").
     * @param targetFile Caminho onde o novo mapa será guardado (ex: "maps/masmorra1.json").
     */
    public static void generateRandomMap(String poolFile, String targetFile) {
        
        // 1. Carregar Dados Auxiliares
        gameItemsCatalog = JsonLoader.loadItems("items.json");
        enigmasPool = JsonLoader.loadEnigmas("enigmas.json");
        
        // Carregar todos os templates de salas
        LinkedList<Location> allPool = JsonLoader.loadAllLocationsList(poolFile);
        if (allPool == null || allPool.isEmpty()) {
            System.out.println("ERRO: Pool de locais vazia ou inválida.");
            return;
        }

        // 2. Classificar Salas por Tipo (Separar Pools)
        LinkedList<Location> poolCorridors = new LinkedList<>();
        LinkedList<Location> poolLeverRooms = new LinkedList<>();   
        LinkedList<Location> poolOtherSpecials = new LinkedList<>(); 
        LinkedList<Location> poolNormalRooms = new LinkedList<>();
        LinkedList<Location> poolEntrances = new LinkedList<>();
        Location templateTreasure = null;

        for (int i = 0; i < allPool.size(); i++) {
            Location loc = allPool.get(i);
            String typeStr = loc.getType().toString().toUpperCase();
            
            if (typeStr.contains("TESOURO")) { templateTreasure = loc; continue; } 
            if (typeStr.contains("ENTRADA")) { poolEntrances.add(loc); continue; } 
            if (typeStr.contains("CORREDOR")) { poolCorridors.add(loc); continue; }

            if (loc.hasJsonLever()) poolLeverRooms.add(loc); 
            else if (loc.hasEnigma() || loc.hasEvent()) poolOtherSpecials.add(loc);
            else poolNormalRooms.add(loc);
        }

        // Validação Mínima
        if (templateTreasure == null || poolEntrances.isEmpty()) {
            System.out.println("ERRO: O JSON de pool tem de ter pelo menos 1 Tesouro e 1 Entrada.");
            return;
        }

        Random rand = new Random();
        boolean mapaValido = false;
        int tentativas = 0;

        // Tenta gerar até conseguir um mapa válido (com alavancas suficientes)
        while (!mapaValido && tentativas < 200) {
            
            // --- FASE 1: SELEÇÃO DE TEMPLATES ---
            LinkedList<Location> templatesToUse = new LinkedList<>();
            
            // A. Adicionar obrigatoriamente 1 Tesouro
            templatesToUse.add(templateTreasure);

            // B. Adicionar 4 Entradas (Aleatórias do pool)
            LinkedList<Location> tempEnts = clonarListaRefs(poolEntrances);
            for(int i=0; i<4; i++) {
                if(tempEnts.isEmpty()) tempEnts = clonarListaRefs(poolEntrances);
                
                int idx = rand.nextInt(tempEnts.size());
                Location selected = tempEnts.get(idx);
                
                templatesToUse.add(selected);
                tempEnts.remove(selected);
            }

            // C. Preencher o resto até atingir 50 salas
            int totalRoomsTarget = 50;
            int remaining = totalRoomsTarget - 5; // -1 Tesouro -4 Entradas

            for(int i=0; i<remaining; i++) {
                // 60% chance de ser corredor (para criar "labirinto")
                if(rand.nextInt(100) < 60 && !poolCorridors.isEmpty()) {
                    templatesToUse.add(poolCorridors.get(rand.nextInt(poolCorridors.size())));
                } else {
                    // Distribuição de salas especiais
                    if(!poolLeverRooms.isEmpty() && rand.nextInt(100) < 10) {
                        templatesToUse.add(poolLeverRooms.get(rand.nextInt(poolLeverRooms.size())));
                    } else if(!poolOtherSpecials.isEmpty() && rand.nextInt(100) < 20) {
                        templatesToUse.add(poolOtherSpecials.get(rand.nextInt(poolOtherSpecials.size())));
                    } else if (!poolNormalRooms.isEmpty()) {
                        templatesToUse.add(poolNormalRooms.get(rand.nextInt(poolNormalRooms.size())));
                    } else if (!poolCorridors.isEmpty()) {
                        templatesToUse.add(poolCorridors.get(0)); // Fallback
                    }
                }
            }

            // --- FASE 2: BARALHAR (SHUFFLE) ---
            LinkedList<Location> shuffledTemplates = new LinkedList<>();
            while(!templatesToUse.isEmpty()) {
                int idx = rand.nextInt(templatesToUse.size());
                Location selected = templatesToUse.get(idx);
                shuffledTemplates.add(selected);
                templatesToUse.remove(selected);
            }

            // --- FASE 3: INSTANCIAR O MAPA FINAL ---
            LinkedList<Location> finalMap = new LinkedList<>();
            
            // Copia os enigmas disponíveis para ir gastando
            LinkedList<Enigma> currentEnigmas = new LinkedList<>();
            if (enigmasPool != null) {
                for(int i=0; i<enigmasPool.size(); i++) currentEnigmas.add(enigmasPool.get(i));
            }

            for(int i=0; i<shuffledTemplates.size(); i++) {
                Location template = shuffledTemplates.get(i);
                
                // Cria uma nova instância (ID sequencial: 1, 2, 3...)
                Location newLoc = createLocationFromTemplate(i + 1, template, currentEnigmas, rand);
                
                // Tesouro começa trancado
                if(newLoc.getType() == LocationType.TESOURO) newLoc.setLocked(true);
                
                finalMap.add(newLoc);
            }

            // --- FASE 4: CONECTAR GRAFO ---
            connectMap(finalMap, rand);

            // Validação Final: O mapa tem alavancas suficientes?
            if (validarAlavancas(finalMap)) mapaValido = true;
            else tentativas++;
            
            if(mapaValido) {
                JsonSaver.saveMap(finalMap, targetFile);
            }
        }
        
        if (!mapaValido) System.out.println("ERRO: Falha ao gerar mapa válido após várias tentativas.");
    }

    // ==========================================
    //           MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Cria uma nova instância de Location baseada num template.
     * Atribui itens e enigmas únicos.
     */
    private static Location createLocationFromTemplate(int newId, Location template, LinkedList<Enigma> enigmasAvailable, Random rand) {
        Location newLoc = new Location(newId, template.getName(), template.getType(), template.getDescription());
        
        if (template.hasEvent()) newLoc.setEvent(template.getEventType());
        if (template.isLocked()) newLoc.setLocked(true);

        // Atribuir Enigma Único (sem repetições)
        if (template.hasEnigma() && !enigmasAvailable.isEmpty()) {
            int idx = rand.nextInt(enigmasAvailable.size());
            Enigma uniqueEnigma = enigmasAvailable.get(idx);
            enigmasAvailable.remove(uniqueEnigma); // Remove da pool temporária
            newLoc.setEnigma(uniqueEnigma);
        }

        // Copiar Itens
        LinkedList<Item> items = template.getItems();
        if (items != null) {
            for (int k = 0; k < items.size(); k++) {
                Item itemTemplate = items.get(k);
                // Tenta encontrar o item completo no catálogo
                Item realItem = findItemTemplate(itemTemplate.getName());
                if (realItem != null) newLoc.addItem(new Item(realItem.getName(), realItem.getType(), realItem.getValue()));
                else newLoc.addItem(new Item(itemTemplate.getName(), itemTemplate.getType(), 0));
            }
        }
        
        // Define a propriedade de alavanca para o JsonSaver saber guardar
        if (template.hasJsonLever()) newLoc.setLeverProperty("true");
        
        return newLoc;
    }

    private static Item findItemTemplate(String name) {
        if (gameItemsCatalog == null) return null;
        for (int i = 0; i < gameItemsCatalog.size(); i++) {
            Item it = gameItemsCatalog.get(i);
            if (it.getName().equalsIgnoreCase(name)) return it;
        }
        return null;
    }

    /** Valida se o mapa gerado tem entre 1 e 2 alavancas. */
    private static boolean validarAlavancas(LinkedList<Location> map) {
        int count = 0;
        for (int i = 0; i < map.size(); i++) if (map.get(i).hasJsonLever()) count++;
        return count >= 1 && count <= 2;
    }

    private static LinkedList<Location> clonarListaRefs(LinkedList<Location> original) {
        LinkedList<Location> clone = new LinkedList<>();
        for (int i = 0; i < original.size(); i++) clone.add(original.get(i));
        return clone;
    }

    /**
     * Liga as salas para formar um grafo conexo.
     * Estratégia: Liga sequencialmente (A-B-C-D...) e adiciona atalhos aleatórios.
     */
    private static void connectMap(LinkedList<Location> map, Random rand) {
        // 1. Caminho Principal (Espinha Dorsal)
        for (int i = 0; i < map.size() - 1; i++) {
            Location c = map.get(i);
            Location n = map.get(i + 1);
            c.addNeighbor(n);
            n.addNeighbor(c);
        }
        
        // 2. Atalhos Aleatórios (Conexões extra)
        int extras = (int)(map.size() * 0.6); // 60% de conexões extra
        for (int k = 0; k < extras; k++) {
            int idxA = rand.nextInt(map.size());
            int idxB = rand.nextInt(map.size());
            
            // Evita auto-ligações e vizinhos imediatos (já ligados)
            if (idxA != idxB && Math.abs(idxA - idxB) > 1) {
                Location a = map.get(idxA);
                Location b = map.get(idxB);
                
                boolean linked = false;
                LinkedList<Location> neighs = a.getNeighbors();
                for(int j=0; j<neighs.size(); j++) if(neighs.get(j) == b) linked = true;
                
                if(!linked) {
                    a.addNeighbor(b);
                    b.addNeighbor(a);
                }
            }
        }
    }
}