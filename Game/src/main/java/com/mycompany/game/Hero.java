package com.mycompany.game;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Hero extends Entity {
    public HeroType type;
    public int level, exp, maxMana, mana;
    public Random rand = new Random();

    private List<Skill> skillList;
    private int[] skillCooldowns;

    public Hero(String name, HeroType type) {
        this.name = name;
        this.type = type;
        this.level = 1;
        this.exp = 0;
        switch (type) {
            case WARRIOR:
                this.stats = new int[]{18, 6, 9, 15, 7};
                this.maxHp = 150;
                this.maxMana = 35;
                break;
            case MAGE:
                this.stats = new int[]{7, 22, 9, 8, 12};
                this.maxHp = 95;
                this.maxMana = 80;
                break;
            case ROGUE:
                this.stats = new int[]{12, 9, 22, 10, 13};
                this.maxHp = 120;
                this.maxMana = 50;
                break;
            case PRIEST:
                this.stats = new int[]{10, 18, 12, 13, 14};
                this.maxHp = 110;
                this.maxMana = 75;
                break;
            case HUNTER:
                this.stats = new int[]{14, 8, 19, 11, 12};
                this.maxHp = 130;
                this.maxMana = 45;
                break;
            default:
                this.stats = new int[]{12, 12, 12, 12, 12};
                this.maxHp = 110;
                this.maxMana = 50;
        }
        this.hp = maxHp;
        this.mana = maxMana;
        this.skillList = type.getSkills();
        this.skillCooldowns = new int[skillList.size()];
        applyPassiveBonuses();
    }

    public int getHp() { return this.hp; }
    public int getLevel() { return this.level; }

    public boolean isDead() { return hp <= 0; }

    public void printStatus() {
        System.out.println("Name: " + name + " | Mask: " + type.name);
        System.out.println("Level: " + level + " | EXP: " + exp);
        System.out.println("HP: " + hp + "/" + maxHp + " | Mana: " + mana + "/" + maxMana);
        System.out.println("Stats: Atk " + stats[0] + ", Int " + stats[1] + ", Agi " + stats[2] + ", Def " + stats[3] + ", Luck " + stats[4]);
        System.out.print("Skill Cooldowns: ");
        for (int i = 0; i < skillList.size(); i++) {
            Skill s = skillList.get(i);
            if (s.isPassive) continue;
            System.out.print(s.name + ": " + (skillCooldowns[i] > 0 ? skillCooldowns[i] + " " : "Ready "));
        }
        System.out.println();
        System.out.println("Passive: " + getPassiveSkill().name + " - " + getPassiveSkill().desc);
        System.out.print("Active Effects: ");
        displayStatusEffects();
    }

    public static void printBattleStatus(Hero player, Monster enemy) {
        System.out.println("You: " + player.hp + "/" + player.maxHp + " HP | " + player.mana + "/" + player.maxMana + " Mana");
        System.out.print("Your effects: ");
        player.displayStatusEffects();
        System.out.println(enemy.name + ": " + enemy.hp + "/" + enemy.maxHp + " HP");
        System.out.print("Enemy effects: ");
        enemy.displayStatusEffects();
    }

    // Stronger attack based on main stat and level
    public boolean attack(Monster enemy) {
        int mainStat = switch (type) {
            case WARRIOR, ROGUE, HUNTER -> stats[0];
            case MAGE, PRIEST -> stats[1];
            default -> stats[0];
        };
        int damage = Math.max(1, (mainStat * 2 + level * 2) - enemy.def + rand.nextInt(8));
        int markBonus = 0;
        for (StatusEffect se : enemy.statusQueue) {
            if (se.name.equals("Mark") && se.duration > 0) markBonus += se.magnitude;
        }
        damage += markBonus;
        enemy.takeDamage(damage);
        System.out.println("You attack! " + enemy.name + " takes " + damage + " damage.");
        int manaGain = switch (this.type) {
            case MAGE -> 6;
            case PRIEST, HUNTER -> 5;
            case WARRIOR, ROGUE -> 4;
            default -> 4;
        };
        restoreMana(manaGain);
        System.out.println("You recover " + manaGain + " mana from your attack.");
        return enemy.hp <= 0;
    }

    public boolean useSkillMenu(Monster enemy, Scanner scanner) {
        int menuCount = 1;
        int[] idxMap = new int[skillList.size()];
        while (true) {
            System.out.println("Choose a skill:");
            menuCount = 1;
            for (int i = 0; i < skillList.size(); i++) {
                Skill s = skillList.get(i);
                if (s.isPassive) continue;
                String ready = (skillCooldowns[i] == 0 ? "Ready" : ("Cooldown: " + skillCooldowns[i]));
                String ultLabel = s.isUltimate ? " (Ultimate)" : "";
                System.out.printf("  %d. %s%s (Mana: %d, %s) - %s%n", menuCount, s.name, ultLabel, s.manaCost, ready, s.desc);
                idxMap[menuCount - 1] = i;
                menuCount++;
            }
            System.out.print("> ");
            String input = scanner.nextLine();
            int choice;
            try {
                if (input == null || input.trim().isEmpty()) throw new Exception();
                choice = Integer.parseInt(input.trim()) - 1;
                if (choice < 0 || choice >= menuCount - 1) throw new Exception();
            } catch (Exception e) {
                System.out.println("Invalid skill, you fumble and miss!");
                continue;
            }
            int skillIdx = idxMap[choice];
            Skill s = skillList.get(skillIdx);
            if (skillCooldowns[skillIdx] > 0) {
                System.out.println(s.name + " is still on cooldown!");
                continue;
            }
            if (mana < s.manaCost) {
                System.out.println("Not enough mana for " + s.name + "!");
                continue;
            }
            mana -= s.manaCost;
            skillCooldowns[skillIdx] = s.cooldown;
            return useSkill(skillIdx, enemy);
        }
    }

    private Skill getPassiveSkill() {
        for (Skill s : skillList) {
            if (s.isPassive) return s;
        }
        return null;
    }

    private void applyPassiveBonuses() {
        Skill passive = getPassiveSkill();
        if (passive == null) return;
        switch (type) {
            case WARRIOR:
                stats[3] += 4;
                break;
            case MAGE:
                break;
            case ROGUE:
                break;
            case PRIEST:
                break;
            case HUNTER:
                stats[0] += 4;
                break;
        }
    }

    // Skill logic for each hero type; only Resurrection, Sanctuary, Mana Shield go to self
    private boolean useSkill(int idx, Monster enemy) {
        Skill s = skillList.get(idx);
        if (s.isPassive) return false;

        // Apply status effects to correct target
        if (s.effects != null) {
            for (StatusEffect eff : s.effects) {
                if (eff.name.equalsIgnoreCase("Resurrection") ||
                    eff.name.equalsIgnoreCase("Sanctuary") ||
                    eff.name.equalsIgnoreCase("Mana Shield")) {
                    this.statusQueue.offer(eff.copy());
                    System.out.println("You are protected by " + eff.name + " for the next " + eff.duration + " turns!");
                } else if (eff.name.equalsIgnoreCase("Mark") && type == HeroType.HUNTER) {
                    enemy.statusQueue.offer(eff.copy());
                    System.out.println(enemy.name + " is marked! (Mark: +" + eff.magnitude + " damage for " + eff.duration + " turns)");
                } else {
                    enemy.statusQueue.offer(eff.copy());
                    System.out.println(enemy.name + " is afflicted with " + eff.name + "!");
                }
            }
        }

        switch (type) {
            case WARRIOR:
                if (s.name.equals("Blade Tempest")) {
                    int damage = stats[0] * 2 + s.power + level * 2 + rand.nextInt(10);
                    int markBonus = 0;
                    for (StatusEffect se : enemy.statusQueue) {
                        if (se.name.equals("Mark") && se.duration > 0) markBonus += se.magnitude;
                    }
                    damage += markBonus;
                    enemy.takeDamage(damage);
                    System.out.println("You unleash Blade Tempest! " + enemy.name + " is shredded for " + damage + " damage!");
                    return enemy.hp <= 0;
                }
                break;
            case MAGE:
                if (s.name.equals("Arcane Burst")) {
                    int damage = stats[1] * 2 + s.power + level * 2 + rand.nextInt(12);
                    int markBonus = 0;
                    for (StatusEffect se : enemy.statusQueue) {
                        if (se.name.equals("Mark") && se.duration > 0) markBonus += se.magnitude;
                    }
                    damage += markBonus;
                    enemy.takeDamage(damage);
                    System.out.println("You cast Arcane Burst! " + enemy.name + " takes " + damage + " damage!");
                    return enemy.hp <= 0;
                }
                break;
            case ROGUE:
                if (s.name.equals("Shadow Strike")) {
                    int damage = stats[2] * 2 + stats[0] + s.power + level * 2 + rand.nextInt(10);
                    enemy.takeDamage(damage);
                    System.out.println("You dash through the shadows and strike for " + damage + " damage!");
                    return enemy.hp <= 0;
                }
                break;
            case PRIEST:
                if (s.name.equals("Radiant Heal")) {
                    int heal = stats[1] * 2 + s.power + level * 2 + rand.nextInt(10);
                    this.hp = Math.min(this.maxHp, this.hp + heal);
                    System.out.println("You heal yourself for " + heal + " HP!");
                } else if (s.name.equals("Divine Smite")) {
                    int damage = stats[1] * 2 + s.power + level * 2 + rand.nextInt(8);
                    enemy.takeDamage(damage);
                    System.out.println("You smite your foe for " + damage + " damage!");
                    return enemy.hp <= 0;
                }
                break;
            case HUNTER:
                if (s.name.equals("Piercing Arrow")) {
                    int damage = stats[2] * 2 + stats[0] + s.power + level * 2 + rand.nextInt(10);
                    enemy.takeDamage(damage);
                    System.out.println("You fire a piercing arrow for " + damage + " damage!");
                    return enemy.hp <= 0;
                } else if (s.name.equals("Prey Mark")) {
                    // Mark is handled above, but print a special message when it fades (handled in processStatusEffects)
                }
                break;
        }
        return false;
    }

    public void tickSkillCooldowns() {
        for (int i = 0; i < skillCooldowns.length; i++) {
            if (skillCooldowns[i] > 0) skillCooldowns[i]--;
        }
    }

    // Improved level up: big stat, HP, and mana gains
    public void gainExp(int amount) {
        this.exp += amount;
        while (this.exp >= 100) {
            this.exp -= 100;
            this.level++;
            System.out.println("LEVEL UP! You are now level " + this.level + "!");
            switch (type) {
                case WARRIOR:
                    stats[0] += 4;
                    stats[3] += 3;
                    stats[2] += 2;
                    break;
                case MAGE:
                    stats[1] += 5;
                    stats[2] += 3;
                    stats[4] += 2;
                    break;
                case ROGUE:
                    stats[2] += 5;
                    stats[0] += 3;
                    stats[4] += 2;
                    break;
                case PRIEST:
                    stats[1] += 4;
                    stats[3] += 3;
                    stats[4] += 2;
                    break;
                case HUNTER:
                    stats[2] += 4;
                    stats[0] += 3;
                    stats[3] += 2;
                    break;
            }
            this.maxHp += 35;
            this.hp = this.maxHp;
            this.maxMana += 15;
            this.mana = this.maxMana;
            applyPassiveBonuses();
        }
    }

    public void restoreMana(int amount) {
        mana = Math.min(maxMana, mana + amount);
    }

    @Override
    public void processTurnPassives() {
        // Mage: Mana Regen
        if (type == HeroType.MAGE) {
            int manaRegen = 5 + level / 2;
            mana = Math.min(maxMana, mana + manaRegen);
            System.out.println("Your mana surges (+ " + manaRegen + ") from Arcane Wisdom.");
        }
        // Priest: Heal
        if (type == HeroType.PRIEST) {
            int heal = 4 + level / 2;
            hp = Math.min(maxHp, hp + heal);
            System.out.println("You recover " + heal + " HP from Blessing passive.");
        }
        // Other hero types: add passives if needed
    }
}