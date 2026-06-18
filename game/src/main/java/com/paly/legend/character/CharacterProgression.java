package com.paly.legend.character;

import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ClassConfig;
import org.springframework.stereotype.Service;

@Service
public class CharacterProgression {

    private final GameConfigService gameConfigService;

    public CharacterProgression(GameConfigService gameConfigService) {
        this.gameConfigService = gameConfigService;
    }

    public CharacterProgressionResult applyBattleReward(PlayerCharacter character, int expGained, int goldGained) {
        int level = character.getLevel();
        int exp = character.getExp() + expGained;
        int hp = character.getHp();
        int attack = character.getAttack();
        int defense = character.getDefense();
        int attackSpeed = character.getAttackSpeed();
        int levelUps = 0;

        while (true) {
            Integer requiredExp = gameConfigService.getRequiredExpToNextLevel(level);
            if (requiredExp == null || exp < requiredExp) {
                break;
            }
            exp -= requiredExp;
            level += 1;
            levelUps += 1;
            ClassConfig classConfig = gameConfigService.getClassRequired(character.getClassName());
            hp += classConfig.getHpPerLevel();
            attack += classConfig.getAttackPerLevel();
            defense += classConfig.getDefensePerLevel();
            attackSpeed += classConfig.getAttackSpeedPerLevel();
        }

        int gold = character.getGold() + goldGained;
        int power = calculatePower(hp, attack, defense, attackSpeed);
        return new CharacterProgressionResult(level, exp, gold, hp, attack, defense, attackSpeed, power, levelUps);
    }

    public int calculatePower(int hp, int attack, int defense, int attackSpeed) {
        return hp + attack * 8 + defense * 6 + attackSpeed * 2;
    }

    public int baseHp(int level) {
        return baseHp(level, "warrior");
    }

    public int baseAttack(int level) {
        return baseAttack(level, "warrior");
    }

    public int baseDefense(int level) {
        return baseDefense(level, "warrior");
    }

    public int baseAttackSpeed(int level) {
        return baseAttackSpeed(level, "warrior");
    }

    public int baseHp(int level, String className) {
        ClassConfig config = gameConfigService.getClassRequired(className);
        return config.getHp() + Math.max(0, level - 1) * config.getHpPerLevel();
    }

    public int baseAttack(int level, String className) {
        ClassConfig config = gameConfigService.getClassRequired(className);
        return config.getAttack() + Math.max(0, level - 1) * config.getAttackPerLevel();
    }

    public int baseDefense(int level, String className) {
        ClassConfig config = gameConfigService.getClassRequired(className);
        return config.getDefense() + Math.max(0, level - 1) * config.getDefensePerLevel();
    }

    public int baseAttackSpeed(int level, String className) {
        ClassConfig config = gameConfigService.getClassRequired(className);
        return config.getAttackSpeed() + Math.max(0, level - 1) * config.getAttackSpeedPerLevel();
    }
}
