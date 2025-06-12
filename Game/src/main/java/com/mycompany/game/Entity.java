package com.mycompany.game;

import java.util.LinkedList;
import java.util.Queue;

public abstract class Entity {
    public String name;
    public int hp, maxHp;
    public int[] stats = new int[5]; // [STR, INT, AGI, DEF, LUK]
    public Queue<StatusEffect> statusQueue = new LinkedList<>();

    /** Returns true if this entity is dead. */
    public boolean isDead() {
        return hp <= 0;
    }

    public boolean hasStatus(String effectName) {
        for (StatusEffect se : statusQueue) {
            if (se.name.equals(effectName) && se.duration > 0) return true;
        }
        return false;
    }

    public void displayStatusEffects() {
        if (statusQueue.isEmpty()) {
            System.out.print("[No status effects]");
        } else {
            for (StatusEffect se : statusQueue) {
                if (se.duration > 0) {
                    System.out.print(se.name + " (" + (se.isBuff ? "Buff" : "Debuff") +
                            ", " + se.duration + " turn" + (se.duration == 1 ? "" : "s") + ") | ");
                }
            }
        }
        System.out.println();
    }

    /**
     * Applies raw damage to the entity, accounting for SHIELD, Mark, and Sanctuary effects.
     * Called by Monster/Enemy attacks and player attacks.
     */
    public void takeDamage(int dmg) {
        if (dmg < 0) {
            System.err.println("[DEBUG] Negative damage attempted: " + dmg + ". Setting to 0.");
            dmg = 0;
        }
        if (name == null) {
            System.err.println("[DEBUG] Entity with null name is taking damage! Class: " + this.getClass().getSimpleName());
            name = this.getClass().getSimpleName();
        }
        // Sanctuary (damage reduction)
        int reduction = 0;
        for (StatusEffect se : statusQueue) {
            if (se.name.equals("Sanctuary") && se.duration > 0) {
                reduction += se.magnitude;
            }
        }
        if (reduction > 0) {
            dmg = dmg * (100 - reduction) / 100;
            System.out.println(name + "'s Sanctuary reduces damage!");
        }
        // Shield (absorb)
        for (StatusEffect se : statusQueue) {
            if (se.name.equals("Shield") && se.duration > 0 && se.magnitude > 0) {
                int absorb = Math.min(se.magnitude, dmg);
                dmg -= absorb;
                se.magnitude -= absorb;
                System.out.println(name + "'s Shield absorbs " + absorb + " damage!");
                if (se.magnitude <= 0) {
                    se.duration = 0;
                    System.out.println(name + "'s Shield is broken!");
                }
                if (dmg <= 0) dmg = 0;
                break;
            }
        }
        this.hp -= dmg;
        if (this.hp < 0) this.hp = 0;
        System.out.println(name + " took " + dmg + " damage! (" + hp + "/" + maxHp + " HP)");
    }

    /**
     * ---- Status Effects: Process and apply effects (buffs/debuffs) at the start of turn. ----
     * Pops expired effects (FIFO).
     * Should be called at the start of each turn.
     * Returns a list of just-faded effect names for user display.
     */
    public void processStatusEffects() {
        int n = statusQueue.size();
        for (int i = 0; i < n; i++) {
            StatusEffect effect = statusQueue.poll();
            if (effect.duration <= 0) {
                System.out.println(name + "'s " + effect.name + " has faded.");
                continue;
            }
            // Poison/Burn: always damage
            if (effect.name.equals("Poison") || effect.name.equals("Burn")) {
                this.hp -= effect.magnitude;
                if (this.hp < 0) this.hp = 0;
                System.out.println(name + " suffers " + effect.magnitude + " " + effect.name + " damage! (" + hp + "/" + maxHp + ")");
            }
            // Heal
            else if (effect.statTarget.equals("HP") && effect.isBuff) {
                this.hp += effect.magnitude;
                if (this.hp > this.maxHp) this.hp = this.maxHp;
                System.out.println(name + " is affected by " + effect.name + ": +" + effect.magnitude + " HP! (" + hp + "/" + maxHp + ")");
            }
            // Direct HP loss
            else if (effect.statTarget.equals("HP") && !effect.isBuff) {
                this.hp -= effect.magnitude;
                if (this.hp < 0) this.hp = 0;
                System.out.println(name + " is afflicted by " + effect.name + ": -" + effect.magnitude + " HP! (" + hp + "/" + maxHp + ")");
            }
            // Stat changes (Mark increases damage, Sanctuary handled in takeDamage)
            else if (!effect.statTarget.equals("HP")) {
                int idx = -1;
                switch (effect.statTarget) {
                    case "STR": idx = 0; break;
                    case "AGI": idx = 2; break;
                    case "INT": idx = 1; break;
                    case "DEF": idx = 3; break;
                    case "LUK": idx = 4; break;
                }
                if (idx >= 0) {
                    if (effect.isBuff) {
                        stats[idx] += effect.magnitude;
                        System.out.println(name + " is empowered by " + effect.name + ": +" + effect.magnitude + " " + effect.statTarget + "!");
                    } else {
                        stats[idx] -= effect.magnitude;
                        if (stats[idx] < 1) stats[idx] = 1;
                        System.out.println(name + " is weakened by " + effect.name + ": -" + effect.magnitude + " " + effect.statTarget + "!");
                    }
                }
            }
            effect.duration--;
            if (effect.duration > 0) {
                statusQueue.offer(effect);
            } else {
                // Special messaging for effects fading
                if (effect.name.equalsIgnoreCase("Mark")) {
                    System.out.println(name + "'s Mark fades!");
                } else if (effect.name.equalsIgnoreCase("Sanctuary")) {
                    System.out.println(name + "'s Sanctuary fades!");
                } else if (effect.name.equalsIgnoreCase("Shield")) {
                    System.out.println(name + "'s Shield fades!");
                } else {
                    System.out.println(name + "'s " + effect.name + " fades.");
                }
            }
        }
    }

    /**
     * Process passive effects (per turn, before actions). Default: do nothing.
     * Hero overrides this for class passives.
     * Monster can override if needed.
     */
    public void processTurnPassives() {
        // No-op by default
    }
}