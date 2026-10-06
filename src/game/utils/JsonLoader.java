package game.utils;

import LinkedList.LinkedList;
import game.models.Item;
import game.models.Lever;
import game.models.Enigma;
import game.models.Location;
import game.models.GameEvent;
import game.enums.ItemType;
import game.enums.LocationType;
import game.enums.EventType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Utilitário responsável por ler e interpretar ficheiros de texto no formato JSON.
 * <p>
 * Como o projeto não utiliza bibliotecas externas (como Gson ou Jackson), esta classe
 * implementa um parser manual ("String Parsing") para extrair dados de:
 * <ul>
 * <li>Itens (items.json)</li>
 * <li>Eventos (events.json)</li>
 * <li>Mapas e Enigmas (ex: masmorra1.json)</li>
 * </ul>
 * </p>
 */
public class JsonLoader {

    /** Cache estática para evitar carregar o ficheiro de itens repetidamente. */
    private static LinkedList<Item> itemsCache = null;

    /**
     * Garante que a lista de itens base está carregada em memória.
     */
    private static void ensureItemsLoaded() {
        if (itemsCache == null) {
            itemsCache = loadItems("items.json"); 
        }
    }

    /**
     * Procura um item na cache pelo seu nome.
     * @param name O nome do item a procurar.
     * @return O objeto Item se encontrado, ou null.
     */
    private static Item findItemInCache(String name) {
        ensureItemsLoaded();
        for (int i = 0; i < itemsCache.size(); i++) {
            Item item = itemsCache.get(i);
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Retorna uma cópia de um item aleatório da lista de itens carregada.
     * Útil para o saque inicial do Aventureiro ou baús.
     * @return Um novo objeto Item.
     */
    public static Item getRandomItem() {
        ensureItemsLoaded();
        if (itemsCache == null || itemsCache.isEmpty()) return null;
        int idx = (int) (Math.random() * itemsCache.size());
        Item template = itemsCache.get(idx);
        return new Item(template.getName(), template.getType(), template.getValue());
    }

    /**
     * Carrega a lista de eventos (wrapper para loadEvents).
     * @param filePath Caminho do ficheiro.
     * @return Lista de GameEvent.
     */
    public static LinkedList<GameEvent> loadEventsList(String filePath) {
        return loadEvents(filePath);
    }

    /**
     * Lê um ficheiro JSON e converte-o numa lista de objetos Item.
     * @param filePath Caminho do ficheiro (ex: "items.json").
     * @return Lista encadeada de itens.
     */
    public static LinkedList<Item> loadItems(String filePath) {
        LinkedList<Item> items = new LinkedList<>();
        String content = readFile(filePath);
        if (content == null) return items;

        String[] rawObjects = content.split("\\{");
        for (int i = 1; i < rawObjects.length; i++) {
            String obj = "{" + rawObjects[i];
            obj = cleanObject(obj);
            
            String nome = extract(obj, "nome");
            String tipoStr = extract(obj, "tipo");
            String valorStr = extract(obj, "valor");

            if (nome != null && tipoStr != null) {
                ItemType type = ItemType.LIXO;
                try { type = ItemType.valueOf(tipoStr.toUpperCase()); } catch (Exception e) {}
                int valor = 0;
                try { if (valorStr != null) valor = Integer.parseInt(valorStr.trim()); } catch (Exception e) {}
                items.add(new Item(nome, type, valor));
            }
        }
        return items;
    }

    /**
     * Lê um ficheiro JSON e converte-o numa lista de Enigmas.
     * @param filePath Caminho do ficheiro.
     * @return Lista encadeada de enigmas.
     */
    public static LinkedList<Enigma> loadEnigmas(String filePath) {
        LinkedList<Enigma> enigmas = new LinkedList<>();
        String content = readFile(filePath);
        if (content == null) return enigmas;

        String[] rawObjects = content.split("\\{");
        for (int i = 1; i < rawObjects.length; i++) {
            String obj = "{" + rawObjects[i];
            obj = cleanObject(obj);

            String quest = extract(obj, "question");
            if (quest == null) quest = extract(obj, "pergunta");
            String ans = extract(obj, "answer");
            if (ans == null) ans = extract(obj, "resposta");

            if (quest != null && ans != null) {
                enigmas.add(new Enigma(quest, ans));
            }
        }
        return enigmas;
    }

    /**
     * Lê um ficheiro JSON e converte-o numa lista de GameEvent.
     * @param filePath Caminho do ficheiro.
     * @return Lista encadeada de eventos.
     */
    public static LinkedList<GameEvent> loadEvents(String filePath) {
        LinkedList<GameEvent> events = new LinkedList<>();
        String content = readFile(filePath);
        if (content == null) return events;

        String[] rawObjects = content.split("\"id\":");
        for (int i = 1; i < rawObjects.length; i++) {
            String obj = "\"id\":" + rawObjects[i];
            obj = cleanObject(obj);

            String desc = extract(obj, "description");
            String typeStr = extract(obj, "type");
            String valStr = extract(obj, "value");
            int value = 0;
            try { if (valStr != null) value = Integer.parseInt(valStr.trim()); } catch (Exception e) {}

            if (desc != null && typeStr != null) {
                EventType typeEnum = EventType.FLAVOR;
                try { typeEnum = EventType.valueOf(typeStr.trim().toUpperCase()); } catch (Exception e) {}
                events.add(new GameEvent(desc, typeEnum, value));
            }
        }
        return events;
    }

    // --- CARREGAMENTO DO MAPA ---

    /**
     * Carrega o mapa completo, criando as Localizações e ligando-as (vizinhos).
     * Faz duas passagens: 1ª cria os objetos, 2ª cria as ligações.
     * @param filePath Caminho do ficheiro do mapa.
     * @return A localização inicial (raiz).
     */
    public static Location loadLocations(String filePath) {
        LinkedList<Location> list = loadAllLocationsList(filePath);
        if (list.isEmpty()) return null;
        
        Map<Integer, Location> map = new HashMap<>();
        for(int i=0; i<list.size(); i++) {
            Location l = list.get(i);
            map.put(l.getId(), l);
        }
        
        String content = readFile(filePath);
        if (content != null) {
            String[] rawObjects = content.split("\"id\":");
            for (int i = 1; i < rawObjects.length; i++) {
                String obj = "\"id\":" + rawObjects[i];
                obj = cleanObject(obj);
                
                String idStr = extract(obj, "id");
                if(idStr == null) continue;
                int id = Integer.parseInt(idStr.trim());
                Location loc = map.get(id);
                
                if (loc != null) {
                    String neighborsRaw = extractArrayContent(obj, "neighborIds");
                    if (neighborsRaw != null && !neighborsRaw.isEmpty()) {
                        String[] ids = neighborsRaw.split(",");
                        for (String nIdStr : ids) {
                            try {
                                int nId = Integer.parseInt(nIdStr.trim());
                                Location viz = map.get(nId);
                                if (viz != null) loc.addNeighbor(viz);
                            } catch(Exception e){}
                        }
                    }
                }
            }
        }
        return list.get(0); 
    }

    /**
     * Helper que carrega todas as localizações para uma lista plana, sem ligações.
     */
    public static LinkedList<Location> loadAllLocationsList(String filePath) {
        LinkedList<Location> list = new LinkedList<>();
        String content = readFile(filePath);
        if (content == null) return list;

        String[] rawObjects = content.split("\"id\":");
        for (int i = 1; i < rawObjects.length; i++) {
            String obj = "\"id\":" + rawObjects[i];
            obj = cleanObject(obj);
            Location loc = parseLocationObject(obj);
            if (loc != null) list.add(loc);
        }
        return list;
    }

    /**
     * Faz o parsing de um bloco JSON individual para um objeto Location.
     * Trata de detalhes como Enigmas, Alavancas e Itens aninhados.
     */
    private static Location parseLocationObject(String obj) {
        String idStr = extract(obj, "id");
        if (idStr == null) return null;
        int id = Integer.parseInt(idStr.trim());

        String nome = extract(obj, "name");
        String typeStr = extract(obj, "type");
        String desc = extract(obj, "description");

        LocationType type = LocationType.CORREDOR;
        try { if(typeStr != null) type = LocationType.valueOf(typeStr.toUpperCase()); } catch (Exception e) {}

        Location loc = new Location(id, nome, type, desc);

        String event = extract(obj, "eventType");
        if (event != null) loc.setEvent(event);

        String lockedStr = extract(obj, "locked");
        if (lockedStr != null && lockedStr.contains("true")) loc.setLocked(true);
        
        // Alavancas
        String leverVal = extract(obj, "lever");
        if (leverVal != null && leverVal.contains("true")) {
            loc.setLever(new Lever());      
            loc.setLeverProperty("true");    
        } else {
             String targetStr = extract(obj, "leverTarget");
             if (targetStr != null) {
                try {
                    loc.setLeverTargetId(Integer.parseInt(targetStr.trim()));
                    loc.setLever(new Lever());
                    loc.setLeverProperty("true");
                } catch (Exception e) {}
             }
        }

        // Enigmas
        if (obj.contains("\"enigma\": {")) {
            String enigmaBlock = extractBlock(obj, "enigma");
            if (enigmaBlock != null) {
                String q = extract(enigmaBlock, "question");
                String a = extract(enigmaBlock, "answer");
                if (q != null && a != null) {
                    loc.setEnigma(new Enigma(q, a));
                }
            }
        }
        else if (obj.contains("\"enigma\":") || (typeStr != null && typeStr.contains("ENIGMA"))) {
             loc.setEnigma(new Enigma("?", "?")); 
        }

        // Items
        String itemsRaw = extractArrayContent(obj, "items");
        if (itemsRaw != null && !itemsRaw.isEmpty()) {
            String[] itemNames = itemsRaw.split(",");
            for (String itemName : itemNames) {
                itemName = itemName.replace("\"", "").trim();
                if (!itemName.isEmpty()) {
                    Item realItem = findItemInCache(itemName);
                    if (realItem != null) {
                        loc.addItem(new Item(realItem.getName(), realItem.getType(), realItem.getValue()));
                    } else {
                        loc.addItem(new Item(itemName, ItemType.LIXO, 0));
                    }
                }
            }
        }
        return loc;
    }

    private static String cleanObject(String obj) {
        int lastBrace = obj.lastIndexOf("}");
        if (lastBrace != -1) return obj.substring(0, lastBrace + 1);
        return obj;
    }

    private static String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line.trim());
        } catch (IOException e) { return null; }
        return sb.toString();
    }
    
    // --- MÉTODOS DE EXTRAÇÃO MANUAL DE STRINGS ---
    
    /**
     * Extrai o valor de uma chave JSON simples.
     * @param s O bloco JSON.
     * @param k A chave a procurar.
     * @return O valor como string, ou null.
     */
    private static String extract(String s, String k) {
        String sk = "\"" + k + "\":"; 
        int startIdx = s.indexOf(sk);
        if (startIdx == -1) return null;

        startIdx += sk.length();
        
        while (startIdx < s.length() && s.charAt(startIdx) == ' ') {
            startIdx++;
        }

        if (startIdx < s.length() && s.charAt(startIdx) == '"') {
            startIdx++; 
            int endIdx = startIdx;
            while (endIdx < s.length() && s.charAt(endIdx) != '"') {
                endIdx++;
            }
            return s.substring(startIdx, endIdx);
        } 
        else {
            int endIdx = startIdx;
            while (endIdx < s.length() && s.charAt(endIdx) != ',' && s.charAt(endIdx) != '}') {
                endIdx++;
            }
            return s.substring(startIdx, endIdx).trim();
        }
    }
    
    /** Extrai um bloco aninhado (objeto dentro de objeto). */
    private static String extractBlock(String s, String k) {
        int i=s.indexOf("\""+k+"\":"); if(i==-1)return null;
        int ob=s.indexOf("{",i); int cb=s.indexOf("}",ob); if(ob==-1||cb==-1)return null;
        return s.substring(ob+1,cb);
    }
    
    /** Extrai o conteúdo de um array JSON. */
    private static String extractArrayContent(String s, String k) {
        int i=s.indexOf("\""+k+"\":"); if(i==-1)return null;
        int ob=s.indexOf("[",i); int cb=s.indexOf("]",ob); if(ob==-1||cb==-1)return null;
        return s.substring(ob+1,cb).trim();
    }
}