package game.models;

import LinkedList.LinkedList;
import game.enums.LocationType;

/**
 * Representa um nó no grafo do mapa do jogo (uma sala, corredor ou divisão).
 * <p>
 * A {@code Location} é o elemento central onde a ação decorre. Funciona como um contentor que pode albergar:
 * <ul>
 * <li><b>Estrutura:</b> Ligações a outras salas (vizinhos) e um tipo (ex: CORREDOR).</li>
 * <li><b>Recursos:</b> Itens colecionáveis ({@link Item}).</li>
 * <li><b>Obstáculos:</b> Enigmas ({@link Enigma}) ou Portas Trancadas.</li>
 * <li><b>Mecanismos:</b> Alavancas interativas ({@link Lever}).</li>
 * <li><b>Eventos:</b> Armadilhas ou surpresas aleatórias.</li>
 * </ul>
 * </p>
 */
public class Location {
    
    // ==========================================
    //          1. IDENTIFICAÇÃO E DADOS
    // ==========================================

    /** Identificador numérico único da sala (usado para mapeamento no JSON). */
    private int id;
    
    /** Nome visível da sala (ex: "Cozinha Real", "Corredor Escuro"). */
    private String name;
    
    /** Tipo da localização, que pode influenciar a geração de eventos (ex: CORREDOR tem mais eventos). */
    private LocationType type; 
    
    /** Texto descritivo para imersão narrativa quando o jogador entra na sala. */
    private String description;
    
    /** * Propriedade auxiliar carregada diretamente do JSON. 
     * Armazena "true" ou "false" indicando se esta sala <i>deveria</i> ter uma alavanca.
     * Usado durante o {@link game.utils.MapGenerator} para instanciar o objeto {@link Lever}.
     */
    private String lever; 

    // ==========================================
    //          2. ESTRUTURAS DE DADOS
    // ==========================================

    /** Lista de referências para as localizações adjacentes (grafo não direcionado). */
    private LinkedList<Location> neighbors;
    
    /** Lista de itens presentes no chão desta sala. */
    private LinkedList<Item> items;

    // ==========================================
    //          3. LÓGICA DE JOGO (Estado)
    // ==========================================

    /** Objeto Alavanca real, se existir (null caso contrário). */
    private Lever currentLever; 
    
    /** Objeto Enigma real, se existir (null caso contrário). */
    private Enigma currentEnigma; 
    
    /** Tipo de evento estático definido para esta sala (ex: "Surpresa"). */
    private String eventType; 
    
    /** * Indica se a sala está trancada. 
     * Se {@code true}, o jogador não pode entrar nem sair até resolver o bloqueio.
     */
    private boolean isLocked;       
    
    /** * ID da sala que esta alavanca abre (se aplicável).
     * Atualmente usado para lógica futura de chaves/portas específicas.
     */
    private int leverTargetId;      
    
    /** * Estado da alavanca desta sala.
     * {@code true} se o jogador já puxou a alavanca com sucesso.
     */
    private boolean isLeverActivated = false; 

    /**
     * Construtor principal para criar uma nova localização.
     * * @param id          Identificador único numérico.
     * @param name        Nome visível.
     * @param type        Categoria da sala (do Enum {@link LocationType}).
     * @param description Texto de ambiente.
     */
    public Location(int id, String name, LocationType type, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        
        this.neighbors = new LinkedList<>();
        this.items = new LinkedList<>();
        
        this.currentLever = null; 
        this.currentEnigma = null; 
        this.eventType = null;
        
        this.isLocked = false; 
        this.leverTargetId = -1; 
        this.isLeverActivated = false;
    }

    // ==========================================
    //            MÉTODOS DE AÇÃO
    // ==========================================

    /**
     * Tenta ativar a alavanca presente nesta sala.
     * <p>
     * Este método é chamado quando o jogador interage com sucesso com o mecanismo.
     * A ativação só ocorre se a propriedade {@code lever} do JSON for "true".
     * </p>
     */
    public void pullLever() {
        if (this.hasJsonLever()) {
            this.isLeverActivated = true;
        }
    }

    /**
     * Verifica se a alavanca desta sala já foi ativada anteriormente.
     * @return {@code true} se já foi puxada, {@code false} caso contrário.
     */
    public boolean isLeverActivated() {
        return isLeverActivated;
    }

    // ==========================================
    //               SETTERS
    // ==========================================

    /**
     * Define o objeto Alavanca para esta sala.
     * <p>
     * <b>Efeito Colateral:</b> Se a sala for do tipo {@code CORREDOR}, ela é automaticamente
     * promovida a {@code DIVISAO}, pois corredores com mecanismos são considerados salas de interesse.
     * </p>
     * @param lever O objeto {@link Lever} a instalar.
     */
    public void setLever(Lever lever) {
        this.currentLever = lever;
        if (this.type == LocationType.CORREDOR) this.type = LocationType.DIVISAO;
    }
    
    /**
     * Define a propriedade bruta lida do JSON indicando a existência de alavanca.
     * @param leverVal String "true" ou "false".
     */
    public void setLeverProperty(String leverVal) {
        this.lever = leverVal;
    }

    /**
     * Define o Enigma que protege esta sala.
     * <p>
     * <b>Efeito Colateral:</b> Tal como nas alavancas, promove {@code CORREDOR} a {@code DIVISAO}.
     * </p>
     * @param enigma O objeto {@link Enigma} a associar.
     */
    public void setEnigma(Enigma enigma) {
        this.currentEnigma = enigma;
        if (this.type == LocationType.CORREDOR) this.type = LocationType.DIVISAO;
    }

    /** Define o tipo de evento especial para esta sala. */
    public void setEvent(String eventType) { this.eventType = eventType; }
    
    /** Define se a sala está trancada (impede movimento). */
    public void setLocked(boolean locked) { this.isLocked = locked; }
    
    /** Define o ID da sala alvo que esta alavanca afeta (opcional). */
    public void setLeverTargetId(int id) { this.leverTargetId = id; }
    
    /** Destranca a sala imediatamente (atalho para {@code setLocked(false)}). */
    public void unlock() { this.isLocked = false; }

    /**
     * Adiciona uma conexão bidirecional (vizinho) a esta sala.
     * @param neighbor A localização adjacente.
     */
    public void addNeighbor(Location neighbor) { this.neighbors.add(neighbor); }
    
    /** Adiciona um item ao "chão" da sala. */
    public void addItem(Item item) { items.add(item); }
    
    /** Remove um item da sala (geralmente quando um jogador o apanha). */
    public void removeItem(Item item) { items.remove(item); }

    // ==========================================
    //           LÓGICA DE ENIGMA
    // ==========================================

    /**
     * Tenta resolver o enigma presente na sala.
     * <p>
     * Delega a validação para o objeto {@link Enigma}. Se não houver enigma,
     * considera-se "resolvido" por defeito (retorna true).
     * </p>
     * @param attempt A resposta fornecida pelo jogador.
     * @return {@code true} se a resposta está correta ou não há enigma, {@code false} se errou.
     */
    public boolean solveEnigma(String attempt) {
        if (currentEnigma != null) {
            return currentEnigma.solve(attempt);
        }
        return true; 
    }
    
    // ==========================================
    //               GETTERS
    // ==========================================

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocationType getType() { return type; }
    public LinkedList<Location> getNeighbors() { return neighbors; }
    
    public LinkedList<Item> getItems() { return items; }
    
    /** Verifica se existe algum item na sala. */
    public boolean hasItems() { return !items.isEmpty(); }
    
    /** Verifica se existe um enigma ativo (ainda não resolvido). */
    public boolean hasEnigma() { return currentEnigma != null && !currentEnigma.isSolved(); }
    
    /** Retorna a pergunta do enigma ou string vazia. */
    public String getEnigmaQuestion() { return (currentEnigma != null) ? currentEnigma.getQuestion() : ""; }
    
    /**
     * Obtém o objeto Enigma desta sala.
     * <p>
     * <b>Uso Interno:</b> Este método é utilizado pela IA (Bots) de dificuldade elevada
     * para consultar a resposta correta ("cheat" inteligente) através de {@code getAnswer()}.
     * </p>
     * @return O objeto Enigma ou null.
     */
    public Enigma getEnigma() { 
        return currentEnigma; 
    }
    
    public boolean hasEvent() { return eventType != null; }
    public String getEventType() { return eventType; }
    
    /** Verifica se existe um objeto {@link Lever} instanciado e funcional nesta sala. */
    public boolean hasLever() { return currentLever != null; } 
    
    /** * Verifica se o JSON de configuração indicava que esta sala devia ter uma alavanca.
     * Utilizado para validação durante o carregamento do mapa.
     */
    public boolean hasJsonLever() { return "true".equalsIgnoreCase(this.lever); }
    
    public Lever getLever() { return currentLever; }
    public int getLeverTargetId() { return leverTargetId; } 
    public boolean isLocked() { return isLocked; }

    /**
     * Retorna uma representação textual da sala, incluindo o seu estado (Trancado, Evento, etc.).
     * Útil para menus de debug ou listagem de vizinhos.
     */
    @Override
    public String toString() {
        String status = "";
        if (isLocked) status = " [TRANCADO]";
        else if (hasEnigma()) status = " [ENIGMA]";
        else if (hasEvent()) status = " [EVENTO]";
        else if (hasLever()) status = " [ALAVANCA]";
        
        return "[" + id + "] " + name + " (" + type + ")" + status;
    }
}