package com.mycompany.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;

public class Game {
    private static Scanner scanner = new Scanner(System.in);
    private static String playerName;

    // Lives and progress
    private static final int MAX_LIVES = 5;
    private static LinkedList<HeroType> usedTypes = new LinkedList<>();
    private static int livesLeft = MAX_LIVES;

    // Per-life state
    private static Hero player;
    private static HeroType chosenType;
    private static Map<String, Dungeon> dungeons;
    private static final List<String> DUNGEON_KEYS = Arrays.asList("1", "2", "3", "4", "5", "6", "7");
    private static Set<String> clearedDungeons = new HashSet<>();

    private static boolean isGameOver = false;

    public static void main(String[] args) {
        requestPlayerName();
        DeathDialogue.prologue(playerName);
        DeathDialogue.announceRequiredDungeons();
        runGameLoop();
        endGame();
    }

    private static void runGameLoop() {
        while (livesLeft > 0 && !isGameOver) {
            chosenType = chooseHeroTypeMenu();
            usedTypes.add(chosenType);
            livesLeft = MAX_LIVES - usedTypes.size();
            DeathDialogue.onClassChosen(chosenType, livesLeft);
            player = new Hero(playerName, chosenType);
            initializeDungeons();

            DeathDialogue.transmigration(playerName, chosenType.name, livesLeft, new HashSet<>(usedTypes));
            printTitle("SYSTEM WINDOW");
            System.out.println("  [A cold system window flickers before you, sharp and unreal.]");
            System.out.println("  Welcome to DEATH'S GAME");
            printSectionEnd();
            System.out.println("[System] Conquer the Nine Domains: Seven Sins, Your Shadow, and Death itself.");
            System.out.println("[System] Your mask: " + chosenType.name);
            System.out.println("[System] Masks remaining: " + livesLeft);
            DeathDialogue.deathMock("Let us see how long this mask will last. The domains await, and so do I.");
            pause();

            boolean allCleared = runDungeonSelectionMenu();

            if (allCleared) {
                if (mirrorBattle() && deathDomain()) {
                    isGameOver = true;
                }
            }

            if (!isGameOver && player.isDead() && livesLeft > 0) {
                DeathDialogue.onLifeLost(livesLeft, new HashSet<>(usedTypes));
            }
        }
    }

    private static void requestPlayerName() {
        printTitle("The End...?");
        printSectionEnd();
        System.out.print("Enter your name: ");
        while (true) {
            playerName = scanner.nextLine();
            if (playerName != null && !playerName.trim().isEmpty()) break;
            System.out.print("Name cannot be blank. Enter your name: ");
        }
        System.out.println();
    }

    private static HeroType chooseHeroTypeMenu() {
        while (true) {
            System.out.println("Choose your next mask:");
            int idx = 1;
            for (HeroType type : HeroType.values()) {
                if (usedTypes.contains(type)) continue;
                System.out.println("  " + idx + ". " + type.name + " - " + type.desc);
                idx++;
            }
            System.out.print("> ");
            String input = scanner.nextLine();
            int choice = -1;
            try {
                if (input == null || input.trim().isEmpty()) throw new Exception();
                choice = Integer.parseInt(input.trim());
            } catch (Exception e) {
                System.out.println("Invalid input. Enter a number for your class.");
                continue;
            }
            idx = 1;
            for (HeroType type : HeroType.values()) {
                if (usedTypes.contains(type)) continue;
                if (idx == choice) return type;
                idx++;
            }
            System.out.println("Invalid choice.");
        }
    }

    private static boolean runDungeonSelectionMenu() {
        clearedDungeons.clear();
        String firstKey = DUNGEON_KEYS.get(0);

        // Force the first dungeon to be cleared
        Dungeon firstDungeon = dungeons.get(firstKey);
        System.out.println();
        DeathDialogue.beforeDomain(firstDungeon.name, firstDungeon.theme);
        firstDungeon.runDungeon(player, scanner);
        if (player.isDead()) {
            DeathDialogue.onDeath(livesLeft);
            return false;
        }
        DeathDialogue.afterDomain(firstDungeon.name);
        clearedDungeons.add(firstKey);

        // Main menu loop for the rest of the dungeons
        while (clearedDungeons.size() < DUNGEON_KEYS.size()) {
            printDivider();
            System.out.println("=== MAIN MENU ===");
            System.out.println("Select a domain to enter:");
            for (int i = 0; i < DUNGEON_KEYS.size(); i++) {
                String key = DUNGEON_KEYS.get(i);
                Dungeon dungeon = dungeons.get(key);
                if (clearedDungeons.contains(key) || isUnlocked(key)) {
                    System.out.println((i + 1) + ". " + dungeon.name + (clearedDungeons.contains(key) ? " (cleared)" : ""));
                } else {
                    System.out.println((i + 1) + ". ????????");
                }
            }
            System.out.println("0. Check Status");
            System.out.print("> ");
            String input = scanner.nextLine();
            int choice;
            try {
                if (input == null || input.trim().isEmpty()) throw new Exception();
                choice = Integer.parseInt(input.trim());
            } catch (Exception e) {
                System.out.println("Invalid input.");
                continue;
            }
            if (choice == 0) {
                player.printStatus();
                continue;
            }
            if (choice < 1 || choice > DUNGEON_KEYS.size()) {
                System.out.println("Invalid choice.");
                continue;
            }
            String selectedKey = DUNGEON_KEYS.get(choice - 1);
            if (!clearedDungeons.contains(selectedKey) && !isUnlocked(selectedKey)) {
                System.out.println("That domain is not yet available.");
                continue;
            }

            Dungeon dungeon = dungeons.get(selectedKey);
            DeathDialogue.beforeDomain(dungeon.name, dungeon.theme);
            dungeon.runDungeon(player, scanner);
            if (player.isDead()) {
                DeathDialogue.onDeath(livesLeft);
                return false;
            }
            DeathDialogue.afterDomain(dungeon.name);
            clearedDungeons.add(selectedKey);
        }
        DeathDialogue.deathMock("Impressive! You've survived the sins. But can you survive yourself?");
        return true;
    }

    // Simple unlock logic: always allow any previously cleared dungeon and the next uncleared one
    private static boolean isUnlocked(String key) {
        if (clearedDungeons.contains(key)) return true;
        // Find the lowest uncleared index
        for (String k : DUNGEON_KEYS) {
            if (!clearedDungeons.contains(k)) {
                return k.equals(key);
            }
        }
        return false;
    }

    private static boolean mirrorBattle() {
        DeathDialogue.beforeMirror();
        System.out.println("--- DOMAIN VIII: MIRROR ---");
        System.out.println("You stand before an abyssal mirror. Your own reflection steps out, grinning.");
        Monster shadow = Monster.createShadow(player);
        while (player.hp > 0 && shadow.hp > 0) {
            // --- Battle Turn Processing for both sides ---
            player.processTurnPassives();
            shadow.processTurnPassives();
            player.processStatusEffects();
            shadow.processStatusEffects();
            player.tickSkillCooldowns();
            Hero.printBattleStatus(player, shadow);
            System.out.println("Choose your action:");
            System.out.println("  1. Attack");
            System.out.println("  2. Skill");
            String action = null;
            while (true) {
                System.out.print("> ");
                action = scanner.nextLine();
                if ("1".equals(action) || "2".equals(action)) break;
                System.out.println("Invalid input. Enter 1 or 2.");
            }
            switch (action) {
                case "1":
                    if (player.attack(shadow)) {
                        System.out.println("Your shadow collapses.");
                        DeathDialogue.onMirrorClear();
                        return true;
                    }
                    break;
                case "2":
                    if (player.useSkillMenu(shadow, scanner)) {
                        System.out.println("Your shadow collapses.");
                        DeathDialogue.onMirrorClear();
                        return true;
                    }
                    break;
            }
            if (shadow.hp > 0) {
                // Enemy turn (simple AI)
                if (Math.random() < 0.5) {
                    int dmg = shadow.atk + 10 - player.stats[3]/2;
                    System.out.println(shadow.name + " uses Mirror Strike!");
                    if (dmg < 1) dmg = 1;
                    player.takeDamage(dmg);
                } else {
                    int dmg = shadow.atk - player.stats[3]/2;
                    System.out.println(shadow.name + " attacks!");
                    if (dmg < 1) dmg = 1;
                    player.takeDamage(dmg);
                }
            }
        }
        if (player.isDead()) {
            DeathDialogue.onDeath(livesLeft);
            return false;
        }
        DeathDialogue.deathMock("Your own shadow, defeated? Perhaps you aren't all bluster after all.");
        return player.hp > 0;
    }

    private static boolean deathDomain() {
        DeathDialogue.beforeDeath();
        System.out.println("--- DOMAIN IX: DEATH'S THRONE ---");
        System.out.println("Death sits upon a throne of bone and shadow.");
        System.out.println("DEATH: \"You have come far. Do you wish to claim your reward, or fight me for true freedom?\"");
        String choice;
        while (true) {
            System.out.print("Fight Death? (y/n): ");
            choice = scanner.nextLine();
            if (choice == null || choice.trim().isEmpty()) {
                System.out.println("Please answer y or n.");
                continue;
            }
            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) break;
            System.out.println("Please answer y or n.");
        }
        if (choice.equalsIgnoreCase("y")) {
            DeathDialogue.deathMock("Bold. Or perhaps foolish. I do love a challenge!");
            Monster death = Monster.deathBoss(player.level + 15);
            while (player.hp > 0 && death.hp > 0) {
                player.processTurnPassives();
                death.processTurnPassives();
                player.processStatusEffects();
                death.processStatusEffects();
                player.tickSkillCooldowns();
                Hero.printBattleStatus(player, death);
                System.out.println("Choose your action:");
                System.out.println("  1. Attack");
                System.out.println("  2. Skill");
                String action = null;
                while (true) {
                    System.out.print("> ");
                    action = scanner.nextLine();
                    if ("1".equals(action) || "2".equals(action)) break;
                    System.out.println("Invalid input. Enter 1 or 2.");
                }
                switch (action) {
                    case "1":
                        if (player.attack(death)) {
                            System.out.println("Death falls silent.");
                            DeathDialogue.trueEnding(playerName);
                            return true;
                        }
                        break;
                    case "2":
                        if (player.useSkillMenu(death, scanner)) {
                            System.out.println("Death falls silent.");
                            DeathDialogue.trueEnding(playerName);
                            return true;
                        }
                        break;
                }
                if (death.hp > 0) {
                    if (Math.random() < 0.7) {
                        int dmg = death.atk + 15 - player.stats[3]/2;
                        System.out.println("Death uses Reaping Scythe!");
                        if (dmg < 1) dmg = 1;
                        player.takeDamage(dmg);
                    } else {
                        int dmg = death.atk - player.stats[3]/2;
                        System.out.println("Death attacks!");
                        if (dmg < 1) dmg = 1;
                        player.takeDamage(dmg);
                    }
                }
            }
            if (player.isDead()) {
                DeathDialogue.onDeath(livesLeft);
            }
            return player.hp > 0;
        } else {
            DeathDialogue.deathMock("A wise choice... or perhaps you fear me more than you admit.");
            DeathDialogue.goodEnding(playerName);
            return true;
        }
    }

    private static void endGame() {
        printDivider();
        if (livesLeft == 0) {
            DeathDialogue.finalBadEnding();
        } else {
            System.out.println("[Curtain Call]");
        }
        System.out.println();
        System.out.println("=== Your Journey Summary ===");
        System.out.println("Masks used (" + usedTypes.size() + "):");
        for (HeroType t : usedTypes) System.out.println(" - " + t.name);
        System.out.println("Dungeons cleared (" + clearedDungeons.size() + "):");
        for (String key : clearedDungeons) {
            Dungeon d = dungeons.get(key);
            if (d != null) System.out.println(" - " + d.name);
        }
        if (player != null)
            System.out.println("Final mask/class: " + player.type.name + " (Level " + player.level + ")");
        printDivider();
    }

    private static void initializeDungeons() {
        dungeons = new HashMap<>();
        dungeons.put("1", new Dungeon("Sloth", "Sloth", "First Domain", "Lazarin", "Slothling", 3, 1));
        dungeons.put("2", new Dungeon("Lust", "Lust", "Second Domain", "Succubus", "Tempted", 4, 5));
        dungeons.put("3", new Dungeon("Gluttony", "Gluttony", "Third Domain", "Devourer", "Glutton Imp", 5, 10));
        dungeons.put("4", new Dungeon("Greed", "Greed", "Fourth Domain", "Gilded Wraith", "Miserling", 6, 15));
        dungeons.put("5", new Dungeon("Wrath", "Wrath", "Fifth Domain", "Berserker Fiend", "Rager", 7, 20));
        dungeons.put("6", new Dungeon("Envy", "Envy", "Sixth Domain", "Jealous Shade", "Covetor", 8, 25));
        dungeons.put("7", new Dungeon("Pride", "Pride", "Seventh Domain", "Mirror Knight", "Boaster", 9, 30));
    }

    public static void printTitle(String s) {
        System.out.println();
        System.out.println("--- " + s + " ---");
    }

    public static void printSectionEnd() {
        System.out.println();
    }

    public static void printDivider() {
        System.out.println("-------------------------------");
    }

    private static void pause() {
        System.out.print("[Press Enter to continue]");
        scanner.nextLine();
        System.out.println();
    }
}