package com.mycompany.game;

public class Skill {
    public String name, desc;
    public int power, statIndex; // statIndex: 0-STR, 1-INT, 2-AGI, 3-DEF, 4-LUK
    public int manaCost;
    public int cooldown;
    public StatusEffect[] effects;
    public boolean isPassive;
    public boolean isUltimate;

    public Skill(String name, String desc, int power, int statIndex, int manaCost, int cooldown) {
        this(name, desc, power, statIndex, manaCost, cooldown, null, false, false);
    }

    public Skill(String name, String desc, int power, int statIndex, int manaCost, int cooldown, StatusEffect[] effects, boolean isPassive, boolean isUltimate) {
        this.name = name;
        this.desc = desc;
        this.power = power;
        this.statIndex = statIndex;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.effects = effects;
        this.isPassive = isPassive;
        this.isUltimate = isUltimate;
    }
}