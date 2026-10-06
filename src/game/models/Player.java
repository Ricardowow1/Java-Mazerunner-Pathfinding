package game.models;

import LinkedList.LinkedList;
import Stack.LinkedStack;
import game.enums.ItemType;
import game.enums.Difficulty;
import game.enums.PlayerClass;
import game.utils.JsonLoader;
import java.util.Random;

/**
 * Representa uma entidade participante no jogo (Humano ou Bot).
 * <p>
 * A classe Player é o "Avatar" que interage com o mundo. Responsável por:
 * <ul>
 * <li>Gerir estatísticas vitais (HP, Stamina).</li>
 * <li>Controlar o movimento e o histórico de posições (para recuar).</li>
 * <li>Gerir o inventário (Mochila limitada).</li>
 * <li>Aplicar efeitos de classe (Passivas do Ninja, Cooldowns do Mago).</li>
 * <li>Memorizar locais visitados (essencial para a IA dos Bots).</li>
 * </ul>
 * </p>
 */
public class Player {
    
    // --- 1. IDENTIFICAÇÃO ---
    private String name;
    private PlayerClass playerClass;
    private boolean isBot;
    private Difficulty difficulty;

    // --- 2. POSICIONAMENTO ---
    /** Localização atual no grafo do mapa. */
    private Location currentLocation;
    
    /** Pilha para permitir o movimento "Recuar" (Backtracking). */
    private LinkedStack<Location> movementStack; 
    
    /** Lista de locais já visitados (usada pela IA para evitar círculos). */
    private LinkedList<Location> visitedLocations;
    
    /** Histórico textual do caminho percorrido (para relatórios ou debug). */
    private LinkedList<String> pathHistory;

    // --- 3. ESTATÍSTICAS ---
    private int health;
    private int maxHealth;
    private int stamina;
    private int maxStamina;
    
    // --- 4. INVENTÁRIO ---
    private LinkedList<Item> backpack;
    private final int MAX_BACKPACK_SIZE = 3; 
    
    // --- 5. ESTADOS TEMPORÁRIOS ---
    /** Contador de turnos que o jogador deve saltar (Stun). */
    private int skipTurnCounter; 
    
    /** Pontos de escudo que absorvem dano (raro). */
    private int shield; 
    
    // --- 6. HABILIDADES ---
    private boolean banditAbilityUsed; 
    private int mageCooldown;

    /**
     * Constrói um novo jogador.
     * @param name Nome visível.
     * @param startLocation Ponto de entrada no mapa.
     * @param diff Dificuldade (afeta HP base se for Humano ou IA se for Bot).
     * @param pClass Classe escolhida.
     * @param isBot Define se é controlado pela IA.
     */
    public Player(String name, Location startLocation, Difficulty diff, PlayerClass pClass, boolean isBot) {
        this.name = name;
        this.currentLocation = startLocation;
        this.playerClass = pClass;
        this.isBot = isBot;
        this.difficulty = diff;
        
        this.movementStack = new LinkedStack<>();
        this.backpack = new LinkedList<>();
        this.visitedLocations = new LinkedList<>();
        this.pathHistory = new LinkedList<>();
        
        if (startLocation != null) {
            this.visitedLocations.add(startLocation);
            this.pathHistory.add(startLocation.getName());
        }

        // Stats Base
        int baseHealth = 100;
        int baseStamina = 50;
        
        // Ajuste por Dificuldade
        if (diff == Difficulty.FACIL) { baseHealth = 120; baseStamina = 70; }
        if (diff == Difficulty.DIFICIL) { baseHealth = 80; baseStamina = 40; }

        // Bónus de Classe: Herói
        if (playerClass == PlayerClass.HEROI) {
            this.maxHealth = (int)(baseHealth * 1.2); 
            this.maxStamina = (int)(baseStamina * 1.2);
        } else {
            this.maxHealth = baseHealth;
            this.maxStamina = baseStamina;
        }
        this.health = this.maxHealth;
        this.stamina = this.maxStamina;

        this.shield = 0;
        this.skipTurnCounter = 0; 

        // Bónus de Classe: Aventureiro (Começa com itens)
        if (playerClass == PlayerClass.AVENTUREIRO) {
            for (int i = 0; i < 2; i++) {
                Item item = JsonLoader.getRandomItem();
                if (item != null && backpack.size() < MAX_BACKPACK_SIZE) {
                    this.backpack.add(item);
                }
            }
        }
    }

    // --- GETTERS PRINCIPAIS ---
    public boolean isBot() { return isBot; }
    public Difficulty getDifficulty() { return difficulty; }
    public LinkedList<String> getPathHistory() { return pathHistory; }
    public boolean isBackpackFull() { return backpack.size() >= MAX_BACKPACK_SIZE; }

    // --- LÓGICA DE MOVIMENTO ---

    /**
     * Tenta mover o jogador para uma nova localização.
     * Verifica portas trancadas e stamina suficiente.
     * @param newLocation O destino.
     * @return true se o movimento foi bem sucedido.
     */
    public boolean move(Location newLocation) {
        // Bloqueio físico (Portas trancadas)
        if (newLocation.isLocked()) {
            System.out.println(">> BLOQUEADO: A porta está trancada por um mecanismo."); 
            return false; 
        }
        
        // Custo de Stamina
        if (stamina < 5) { 
            System.out.println(">> CANSADO. Precisas de descansar."); 
            return false; 
        }
        
        stamina -= 5;
        movementStack.push(currentLocation); // Guarda para recuar depois
        currentLocation = newLocation;
        
        pathHistory.add(newLocation.getName());
        
        if (!wasVisited(newLocation)) visitedLocations.add(newLocation);
        System.out.println(">> Entraste em: " + newLocation.getName());
        return true;
    }
    
    /**
     * Teletransporta o jogador (usado em Eventos como SWAP).
     * Ignora regras de movimento e stamina.
     */
    public void forceLocation(Location newLocation) {
        if (currentLocation != null) movementStack.push(currentLocation);
        this.currentLocation = newLocation;
        pathHistory.add(newLocation.getName());
        if (!wasVisited(newLocation)) visitedLocations.add(newLocation);
    }
    
    /**
     * Recua para a sala anterior (Backtracking).
     * @return true se foi possível recuar.
     */
    public boolean moveBack() {
        if (!movementStack.isEmpty()) {
            currentLocation = movementStack.pop();
            pathHistory.add(currentLocation.getName());
            return true;
        }
        return false;
    }

    // --- INVENTÁRIO ---

    /**
     * Tenta apanhar um item do chão.
     * @param item O item a recolher.
     * @return true se apanhou com sucesso.
     */
    public boolean pickUpItem(Item item) {
        if (isBackpackFull()) {
            if (!this.isBot) System.out.println(">> MOCHILA CHEIA!"); 
            return false;
        }
        backpack.add(item);
        currentLocation.removeItem(item);
        System.out.println(">> " + name + " apanhou " + item.getName());
        return true;
    }

    // --- EVENTOS E EFEITOS (String Based) ---

    /**
     * Aplica o efeito de um evento (String simples) ao jogador.
     * Inclui a passiva do Ninja (Esquiva de TUDO).
     * @param event O nome do evento (ex: "Dano", "Trap", "PerderTurno").
     */
    public void applyEventEffect(String event) {
        // Verifica escudo primeiro
        if (shield > 0 && (event.equals("Dano") || event.equals("PerderTurno"))) { 
            shield--; 
            return; 
        }
        if (event == null) return;
        
        // Passiva do Ninja: 50% de chance de evitar RECUAR
        // (Nota: A defesa contra Dano e Stun é feita nos métodos específicos abaixo)
        if (this.playerClass == PlayerClass.NINJA && event.equalsIgnoreCase("Recuar")) {
             if (new Random().nextBoolean()) {
                 System.out.println(">> 💨 NINJA! Fixaste os pés no chão e não foste empurrado!");
                 return;
             }
        }

        // Lógica de efeitos padrão
        if (event.equals("PerderTurno") || event.equalsIgnoreCase("Stun")) {
            addSkipTurn(1); // Ninja defende-se lá dentro
        }
        else if (event.equalsIgnoreCase("Dano") || event.equalsIgnoreCase("Trap")) {
            takeDamage(15); // Ninja defende-se lá dentro
        }
        else if (event.equals("Recuar")) {
            if (moveBack()) {
                System.out.println(">> Foste empurrado para trás!");
            }
        }
    }

    // --- VIDA E STAMINA ---

    public void heal(int amount) {
        if (amount < 0) { takeDamage(Math.abs(amount)); return; }
        this.health += amount;
        if (this.health > maxHealth) this.health = maxHealth;
        System.out.println("   💚 Vida recuperada! (HP: " + health + ")");
    }

    public void recoverStamina(int amount) {
        this.stamina += amount;
        if (this.stamina > maxStamina) this.stamina = maxStamina;
        if (this.stamina < 0) this.stamina = 0;
        
        if (amount > 0) System.out.println("   ⚡ Energia restaurada! (STM: " + stamina + ")");
        else System.out.println("   ⚡ Perdeste energia! (STM: " + stamina + ")");
    }
    
    public void consumeStamina(int amount) { recoverStamina(-amount); }

    /**
     * Aplica dano ao jogador.
     * Inclui lógica de Escudo e PASSIVA DO NINJA.
     */
    public void takeDamage(int amount) {
        
        // Se o dano for massivo (>= 9999), é porque o jogador escolheu "Sair".
        // Ignora escudos e passivas de Ninja.
        if (amount >= 9999) {
            this.health = 0;
            System.out.println("   💀 Desististe da aventura.");
            return;
        }

        // 1. Passiva do Ninja (Funciona para qualquer dano NORMAL)
        if (this.playerClass == PlayerClass.NINJA) {
            if (new Random().nextBoolean()) { // 50%
                System.out.println(">> 💨 NINJA! Esquivaste-te do dano com um mortal à retaguarda!");
                return; // Anula o dano
            }
        }

        // 2. Escudo
        if (this.shield > 0) {
            this.shield--;
            System.out.println("   🛡️ Escudo bloqueou o dano!");
            return;
        }

        // 3. Aplicar Dano
        this.health -= amount;
        if (this.health < 0) this.health = 0;
        System.out.println("   💥 Sofreste " + amount + " de dano! (HP: " + health + ")");
    }

    /**
     * Aplica Stun (Perder Turno).
     * Inclui lógica de Escudo e PASSIVA DO NINJA.
     */
    public void addSkipTurn(int turns) {
        // 1. Passiva do Ninja
        if (this.playerClass == PlayerClass.NINJA) {
            if (new Random().nextBoolean()) {
                System.out.println(">> 💨 NINJA! Resististe ao atordoamento!");
                return; 
            }
        }
        
        // 2. Escudo
        if (this.shield > 0) {
            this.shield--;
            System.out.println("   🛡️ Escudo protegeu-te do atordoamento!");
            return;
        }

        this.skipTurnCounter += turns;
        System.out.println(">> Ficaste atordoado por " + turns + " turno(s)!");
    }

    public boolean shouldSkipTurn() { 
        if(skipTurnCounter > 0){ 
            skipTurnCounter--; 
            return true;
        } 
        return false; 
    }

    // --- HELPERS E GETTERS DIVERSOS ---

    /** Ação de descansar para recuperar stamina (usada no menu). */
    public void rest() { 
        recoverStamina(20); 
        System.out.println(">> Descansaste um pouco."); 
    }
    
    /** Chamado no início de cada turno para processar cooldowns. */
    public void onTurnStart() { 
        if (mageCooldown > 0) mageCooldown--; 
    }
    
    /** Verifica se o jogador já esteve numa localização (memória). */
    public boolean wasVisited(Location loc) {
        for (int i = 0; i < visitedLocations.size(); i++) {
            if (visitedLocations.get(i) == loc) return true;
        }
        return false;
    }

    public LinkedList<Item> getBackpack() { return backpack; }
    public Location getCurrentLocation() { return currentLocation; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getStamina() { return stamina; }
    public boolean isAlive() { return health > 0; }
    public PlayerClass getPlayerClass() { return playerClass; }
    public int getMaxHealth() { return maxHealth; }
    public int getMaxStamina() { return maxStamina; }
    
    // Habilidades
    public boolean canUseBanditSteal() { return !banditAbilityUsed; }
    public void setBanditStealUsed() { this.banditAbilityUsed = true; }
    public boolean canUseMageSpell() { return mageCooldown == 0; }
    public void triggerMageCooldown() { this.mageCooldown = 5; }
    public int getMageCooldown() { return mageCooldown; }
}