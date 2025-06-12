package com.mycompany.game;

/**
 * ---- Status Effect System Feature ----
 * Represents a buff or debuff (e.g., poison, regen, strength up).
 * Used in a Stack for each Entity (Hero/Monster).
 */
public class StatusEffect {
    public String name;
    public String description;
    public int duration;  // turns remaining
    public int magnitude; // effect power (e.g., HP lost/gained, stat change)
    public boolean isBuff; // true: buff, false: debuff
    public String statTarget; // e.g., "HP", "STR", etc.

    /**
     * Create a status effect.
     * @param name - effect name (e.g., "Poison")
     * @param description - effect description
     * @param duration - in turns
     * @param magnitude - effect power
     * @param isBuff - true if buff, false if debuff
     * @param statTarget - which stat to affect ("HP", "STR", etc)
     */
    public StatusEffect(String name, String description, int duration, int magnitude, boolean isBuff, String statTarget) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.magnitude = magnitude;
        this.isBuff = isBuff;
        this.statTarget = statTarget;
    }

    /** Clones this status effect for fresh application. */
    public StatusEffect copy() {
        return new StatusEffect(name, description, duration, magnitude, isBuff, statTarget);
    }
}