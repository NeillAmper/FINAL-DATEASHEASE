package com.mycompany.game;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack; 

public class Dungeon {
    public String name, theme, desc, bossName, monsterType;
    public int minLevel, minFloor;
    private boolean cleared = false;

    // LinkedList for old room history (still used if you want)
    private LinkedList<String> roomHistory = new LinkedList<>();
    // NEW: Stack for LIFO room traversal (Stack used for "move back" feature)
    private Stack<String> roomStack = new Stack<>();

    public Dungeon(String name, String theme, String desc, String bossName, String monsterType, int minLevel, int minFloor) {
        this.name = name;
        this.theme = theme;
        this.desc = desc;
        this.bossName = bossName;
        this.monsterType = monsterType;
        this.minLevel = minLevel;
        this.minFloor = minFloor;
    }

    public boolean isCleared() { return cleared; }

    public void runDungeon(Hero player, Scanner scanner) {
        System.out.println("=== " + name.toUpperCase() + " ===");
        System.out.println("Theme: " + theme + " | " + desc);

        int moves = 0;
        boolean bossFoyer = false;
        boolean bossDefeated = false;

        roomHistory.clear();
        roomStack.clear();

        while (!bossDefeated) {
            if (moves >= 5 && !bossFoyer) {
                bossFoyer = true;
                System.out.println("You sense a foreboding power ahead. The boss room is near.");
            }

            // Dungeon choices
            System.out.println("\n--- Dungeon Exploration ---");
            System.out.println("  1. Move Forward");
            if (moves > 0) System.out.println("  2. Move Back");
            if (bossFoyer) System.out.println("  3. Enter the Boss Room");
            System.out.println((bossFoyer ? "  4" : "  3") + ". Check Status");
            System.out.println((bossFoyer ? "  5" : "  4") + ". Rest (restore minor mana)");
            System.out.print("> ");
            String action = scanner.nextLine();

            int statusChoice = bossFoyer ? 4 : 3;
            int restChoice = bossFoyer ? 5 : 4;

            if (action.equals("1")) {
                moves++;
                String roomId = "Room " + moves;
                roomHistory.add(roomId);
                roomStack.push(roomId); // <-- PUSH onto stack for undo/back functionality
                if (Math.random() < 0.5) {
                    // Enemy encounter!
                    Monster enemy = Monster.generate(monsterType, minLevel + moves);
                    System.out.println("A " + enemy.name + " appears!");

                    boolean fled = false;
                    boolean enemyDefeated = false;
                    while (!enemyDefeated && player.getHp() > 0 && !fled) {
                        // === Battle Turn Processing for both sides ===
                        player.processStatusEffects();
                        enemy.processStatusEffects();
                        player.tickSkillCooldowns();

                        Hero.printBattleStatus(player, enemy);
                        System.out.println("Choose your action:");
                        System.out.println("  1. Attack");
                        System.out.println("  2. Skill");
                        System.out.println("  3. Run");
                        System.out.println("  4. Check Status");
                        System.out.print("> ");
                        String fightChoice = scanner.nextLine();
                        switch (fightChoice) {
                            case "1":
                                if (player.attack(enemy)) {
                                    System.out.println("Enemy defeated!");
                                    player.gainExp(8 + minLevel * 2);
                                    enemyDefeated = true;
                                }
                                break;
                            case "2":
                                if (player.useSkillMenu(enemy, scanner)) {
                                    System.out.println("Enemy defeated!");
                                    player.gainExp(12 + minLevel * 2);
                                    enemyDefeated = true;
                                }
                                break;
                            case "3":
                                if (Math.random() < 0.5) {
                                    System.out.println("You successfully run away!");
                                    fled = true;
                                } else {
                                    System.out.println("You try to run, but the " + enemy.name + " blocks your escape!");
                                    enemy.enemyAttack(player);
                                    if (player.isDead()) break;
                                }
                                break;
                            case "4":
                                player.printStatus();
                                break;
                            default:
                                System.out.println("You hesitate and miss your chance!");
                        }
                        if (!enemyDefeated && !fled && !player.isDead() && !fightChoice.equals("3")) {
                            enemy.enemyAttack(player);
                        }
                    }
                    if (player.isDead()) {
                        System.out.println("You have fallen in battle...");
                        return;
                    }
                } else {
                    System.out.println("You move quietly forward. The path is eerily empty...");
                    player.tickSkillCooldowns();
                }
            } else if (moves > 0 && action.equals("2")) {
                // Move Back
                if (!roomStack.isEmpty()) {
                    System.out.println("You move back to the previous room.");
                    roomStack.pop(); // <-- POP from stack (undo last move)
                    if (!roomHistory.isEmpty()) {
                        roomHistory.removeLast();
                    }
                    moves--;
                    player.tickSkillCooldowns();
                } else {
                    System.out.println("You are at the entrance and cannot go back further.");
                }
            } else if (bossFoyer && action.equals("3")) {
                System.out.println("You steel your resolve and enter the boss room.");
                Monster boss = Monster.boss(bossName, minLevel + moves + 2);
                while (boss.hp > 0 && player.getHp() > 0) {
                    // === Battle Turn Processing for both sides ===
                    player.processStatusEffects();
                    boss.processStatusEffects();
                    player.tickSkillCooldowns();

                    Hero.printBattleStatus(player, boss);
                    System.out.println("Choose your action:");
                    System.out.println("  1. Attack");
                    System.out.println("  2. Skill");
                    System.out.println("  3. Run");
                    System.out.println("  4. Check Status");
                    System.out.print("> ");
                    String fightChoice = scanner.nextLine();
                    switch (fightChoice) {
                        case "1":
                            if (player.attack(boss)) {
                                System.out.println("Boss defeated!");
                                player.gainExp(22 + minLevel * 2);
                                DeathDialogue.onDungeonClear(bossName);
                                cleared = true;
                                bossDefeated = true;
                                return;
                            }
                            break;
                        case "2":
                            if (player.useSkillMenu(boss, scanner)) {
                                System.out.println("Boss defeated!");
                                player.gainExp(28 + minLevel * 2);
                                DeathDialogue.onDungeonClear(bossName);
                                cleared = true;
                                bossDefeated = true;
                                return;
                            }
                            break;
                        case "3":
                            if (Math.random() < 0.2) {
                                System.out.println("You miraculously escape the boss room!");
                                break;
                            } else {
                                System.out.println("You try to run, but " + boss.name + " blocks your escape!");
                                boss.enemyAttack(player);
                                if (player.isDead()) return;
                            }
                            break;
                        case "4":
                            player.printStatus();
                            break;
                        default:
                            System.out.println("You hesitate and miss your chance!");
                    }
                    if (boss.hp > 0 && !player.isDead()) {
                        boss.enemyAttack(player);
                    }
                }
                if (player.isDead()) {
                    System.out.println("You have fallen in battle...");
                    return;
                }
            } else if (action.equals(String.valueOf(statusChoice))) {
                player.printStatus();
            } else if (action.equals(String.valueOf(restChoice))) {
                if (Math.random() < 0.7) {
                    int manaRestored = 5 + player.getLevel() / 2;
                    player.restoreMana(manaRestored);
                    System.out.println("You take a short rest and recover " + manaRestored + " mana.");
                } else {
                    System.out.println("You try to rest, but something stirs in the darkness. No rest for now!");
                }
                player.tickSkillCooldowns();
            } else {
                System.out.println("You hesitate, doing nothing...");
            }
        }
    }
}