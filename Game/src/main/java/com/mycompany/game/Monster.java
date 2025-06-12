package com.mycompany.game;

public class Monster extends Entity {
    public int atk, def;

    public Monster(String name, int hp, int atk, int def) {
        this.name = name;
        this.maxHp = hp;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
    }

    // Example generator, you may have more logic in your actual code
    public static Monster generate(String baseName, int level) {
        String name = baseName + " Lv." + level;
        int baseHp = 40 + level * 3;
        int baseAtk = 8 + level;
        int baseDef = 5 + level / 2;
        return new Monster(name, baseHp, baseAtk, baseDef);
    }

    // Example boss generator
    public static Monster boss(String bossName, int level) {
        String name = bossName + " (Boss Lv." + level + ")";
        int hp = 120 + level * 7;
        int atk = 15 + level * 2;
        int def = 10 + level;
        return new Monster(name, hp, atk, def);
    }

    // Example for shadow/mirror/unique bosses if needed
    public static Monster createShadow(Hero hero) {
        String name = hero.name + "'s Shadow";
        int hp = hero.maxHp;
        int atk = hero.stats[0] + hero.level * 2;
        int def = hero.stats[3] + hero.level;
        return new Monster(name, hp, atk, def);
    }

    public static Monster deathBoss(int level) {
        String name = "DEATH";
        int hp = 300 + level * 10;
        int atk = 30 + level * 2;
        int def = 20 + level;
        return new Monster(name, hp, atk, def);
    }

    // Monster attacks the hero
    public void enemyAttack(Hero hero) {
        int damage = Math.max(1, atk - hero.stats[3]);
        hero.takeDamage(damage);
        System.out.println(name + " attacks! You take " + damage + " damage.");
    }
}