package com.mir2.service;

import com.mir2.core.model.BaseObject;
import com.mir2.core.model.Monster;
import com.mir2.core.model.Player;
import com.mir2.core.model.PlayerSkill;
import com.mir2.core.model.Skill;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.SkillType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 伤害计算器
 * 
 * <p>负责计算游戏中的各种伤害，包括物理伤害、魔法伤害、技能伤害等。
 * 对应原M2Engine中的DamageCalculator模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@Slf4j
public class DamageCalculator {
    
    /** 随机数生成器 */
    private final Random random = new Random();
    
    /**
     * 计算物理伤害
     * 
     * @param attacker 攻击者
     * @param target 目标
     * @return 伤害值
     */
    public int calculatePhysicalDamage(Player attacker, BaseObject target) {
        if (attacker == null || target == null) {
            return 0;
        }
        
        // 基础攻击力
        int baseAttack = attacker.getAttack();
        
        // 武器攻击力
        int weaponAttack = attacker.getWeaponAttack();
        
        // 总攻击力
        int totalAttack = baseAttack + weaponAttack;
        
        // 攻击力浮动 (90% - 110%)
        double attackMultiplier = 0.9 + (random.nextDouble() * 0.2);
        int finalAttack = (int) (totalAttack * attackMultiplier);
        
        // 获取目标防御力
        int targetDefense = getTargetDefense(target);
        
        // 计算伤害
        int damage = Math.max(1, finalAttack - targetDefense);
        
        // 等级差异修正
        int levelDifference = attacker.getLevel() - getTargetLevel(target);
        if (levelDifference > 0) {
            damage = (int) (damage * (1 + levelDifference * 0.02)); // 每级高2%伤害
        } else if (levelDifference < 0) {
            damage = (int) (damage * (1 + levelDifference * 0.03)); // 每级低3%伤害
        }
        
        log.debug("物理伤害计算: {} 攻击 {} = {} 伤害", 
                attacker.getName(), target.getName(), damage);
        
        return Math.max(1, damage);
    }
    
    /**
     * 计算魔法伤害
     * 
     * @param attacker 攻击者
     * @param target 目标
     * @return 伤害值
     */
    public int calculateMagicDamage(Player attacker, BaseObject target) {
        if (attacker == null || target == null) {
            return 0;
        }
        
        // 基础魔法攻击力
        int baseMagicAttack = attacker.getMagicAttack();
        
        // 武器魔法攻击力
        int weaponMagicAttack = attacker.getWeaponMagicAttack();
        
        // 总魔法攻击力
        int totalMagicAttack = baseMagicAttack + weaponMagicAttack;
        
        // 魔法攻击力浮动 (85% - 115%)
        double magicMultiplier = 0.85 + (random.nextDouble() * 0.3);
        int finalMagicAttack = (int) (totalMagicAttack * magicMultiplier);
        
        // 获取目标魔法防御力
        int targetMagicDefense = getTargetMagicDefense(target);
        
        // 计算伤害
        int damage = Math.max(1, finalMagicAttack - targetMagicDefense);
        
        // 职业修正
        if (attacker.getJob() == Job.MAGE) {
            damage = (int) (damage * 1.2); // 法师魔法伤害提升20%
        }
        
        // 等级差异修正
        int levelDifference = attacker.getLevel() - getTargetLevel(target);
        if (levelDifference > 0) {
            damage = (int) (damage * (1 + levelDifference * 0.025)); // 每级高2.5%伤害
        } else if (levelDifference < 0) {
            damage = (int) (damage * (1 + levelDifference * 0.035)); // 每级低3.5%伤害
        }
        
        log.debug("魔法伤害计算: {} 攻击 {} = {} 伤害", 
                attacker.getName(), target.getName(), damage);
        
        return Math.max(1, damage);
    }
    
    /**
     * 计算道术伤害
     * 
     * @param attacker 攻击者
     * @param target 目标
     * @return 伤害值
     */
    public int calculateSpiritDamage(Player attacker, BaseObject target) {
        if (attacker == null || target == null) {
            return 0;
        }
        
        // 基础道术攻击力
        int baseSpiritAttack = attacker.getSpiritAttack();
        
        // 武器道术攻击力
        int weaponSpiritAttack = attacker.getWeaponSpiritAttack();
        
        // 总道术攻击力
        int totalSpiritAttack = baseSpiritAttack + weaponSpiritAttack;
        
        // 道术攻击力浮动 (80% - 120%)
        double spiritMultiplier = 0.8 + (random.nextDouble() * 0.4);
        int finalSpiritAttack = (int) (totalSpiritAttack * spiritMultiplier);
        
        // 获取目标魔法防御力（道术伤害对魔法防御）
        int targetMagicDefense = getTargetMagicDefense(target);
        
        // 计算伤害
        int damage = Math.max(1, finalSpiritAttack - targetMagicDefense / 2);
        
        // 职业修正
        if (attacker.getJob() == Job.TAOIST) {
            damage = (int) (damage * 1.15); // 道士道术伤害提升15%
        }
        
        // 等级差异修正
        int levelDifference = attacker.getLevel() - getTargetLevel(target);
        if (levelDifference > 0) {
            damage = (int) (damage * (1 + levelDifference * 0.02)); // 每级高2%伤害
        } else if (levelDifference < 0) {
            damage = (int) (damage * (1 + levelDifference * 0.03)); // 每级低3%伤害
        }
        
        log.debug("道术伤害计算: {} 攻击 {} = {} 伤害", 
                attacker.getName(), target.getName(), damage);
        
        return Math.max(1, damage);
    }
    
    /**
     * 计算技能伤害
     * 
     * @param attacker 攻击者
     * @param skill 技能
     * @param playerSkill 玩家技能
     * @param target 目标
     * @return 伤害值
     */
    public int calculateSkillDamage(Player attacker, Skill skill, PlayerSkill playerSkill, BaseObject target) {
        if (attacker == null || skill == null || playerSkill == null || target == null) {
            return 0;
        }
        
        int baseDamage = 0;
        
        // 根据技能类型计算基础伤害
        switch (skill.getType()) {
            case ATTACK:
                baseDamage = calculateSkillAttackDamage(attacker, skill, playerSkill);
                break;
            default:
                return 0;
        }
        
        // 技能等级修正
        int skillLevel = playerSkill.getLevel();
        double skillMultiplier = 1.0 + (skillLevel - 1) * 0.1; // 每级提升10%
        baseDamage = (int) (baseDamage * skillMultiplier);
        
        // 技能伤害浮动 (80% - 120%)
        double damageMultiplier = 0.8 + (random.nextDouble() * 0.4);
        int finalDamage = (int) (baseDamage * damageMultiplier);
        
        // 获取目标防御力
        int targetDefense = skill.getType() == SkillType.ATTACK ? 
                getTargetDefense(target) : getTargetMagicDefense(target);
        
        // 计算最终伤害
        int damage = Math.max(1, finalDamage - targetDefense);
        
        // 职业技能修正
        damage = applyJobSkillModifier(attacker, skill, damage);
        
        log.debug("技能伤害计算: {} 使用 {} 对 {} = {} 伤害", 
                attacker.getName(), skill.getName(), target.getName(), damage);
        
        return Math.max(1, damage);
    }
    
    /**
     * 计算技能攻击伤害
     */
    private int calculateSkillAttackDamage(Player attacker, Skill skill, PlayerSkill playerSkill) {
        int baseDamage = 0;
        
        // 根据职业和技能类型计算基础伤害
        switch (attacker.getJob()) {
            case WARRIOR:
                baseDamage = attacker.getAttack() + attacker.getWeaponAttack();
                break;
            case MAGE:
                baseDamage = attacker.getMagicAttack() + attacker.getWeaponMagicAttack();
                break;
            case TAOIST:
                baseDamage = attacker.getSpiritAttack() + attacker.getWeaponSpiritAttack();
                break;
        }
        
        // 技能基础伤害加成
        baseDamage += skill.getBaseDamage();
        
        return baseDamage;
    }
    
    /**
     * 计算治疗量
     * 
     * @param caster 施法者
     * @param skill 技能
     * @param playerSkill 玩家技能
     * @return 治疗量
     */
    public int calculateHealAmount(Player caster, Skill skill, PlayerSkill playerSkill) {
        if (caster == null || skill == null || playerSkill == null) {
            return 0;
        }
        
        // 基础治疗量
        int baseHeal = skill.getBaseDamage(); // 治疗技能的baseDamage表示基础治疗量
        
        // 道术加成（道士的治疗技能）
        if (caster.getJob() == Job.TAOIST) {
            baseHeal += caster.getSpiritAttack() / 2;
        }
        
        // 魔法攻击加成（法师的治疗技能）
        if (caster.getJob() == Job.MAGE) {
            baseHeal += caster.getMagicAttack() / 3;
        }
        
        // 技能等级修正
        int skillLevel = playerSkill.getLevel();
        double skillMultiplier = 1.0 + (skillLevel - 1) * 0.15; // 每级提升15%
        baseHeal = (int) (baseHeal * skillMultiplier);
        
        // 治疗量浮动 (90% - 110%)
        double healMultiplier = 0.9 + (random.nextDouble() * 0.2);
        int finalHeal = (int) (baseHeal * healMultiplier);
        
        log.debug("治疗量计算: {} 使用 {} = {} 治疗", 
                caster.getName(), skill.getName(), finalHeal);
        
        return Math.max(1, finalHeal);
    }
    
    /**
     * 应用职业技能修正
     */
    private int applyJobSkillModifier(Player attacker, Skill skill, int damage) {
        double modifier = 1.0;
        
        // 职业技能专精
        switch (attacker.getJob()) {
            case WARRIOR:
                if (skill.getType() == SkillType.ATTACK) {
                    modifier = 1.1; // 战士攻击技能伤害提升10%
                }
                break;
            case MAGE:
                if (skill.getType() == SkillType.ATTACK) {
                    modifier = 1.15; // 法师攻击技能伤害提升15%
                }
                break;
            case TAOIST:
                if (skill.getType() == SkillType.ATTACK) {
                    modifier = 1.05; // 道士攻击技能伤害提升5%
                } else if (skill.getType() == SkillType.HEAL) {
                    modifier = 1.2; // 道士治疗技能效果提升20%
                }
                break;
        }
        
        return (int) (damage * modifier);
    }
    
    /**
     * 计算反弹伤害
     * 
     * @param originalDamage 原始伤害
     * @param reflectRate 反弹率
     * @return 反弹伤害
     */
    public int calculateReflectDamage(int originalDamage, double reflectRate) {
        if (originalDamage <= 0 || reflectRate <= 0) {
            return 0;
        }
        
        int reflectDamage = (int) (originalDamage * reflectRate);
        
        // 反弹伤害浮动 (80% - 120%)
        double reflectMultiplier = 0.8 + (random.nextDouble() * 0.4);
        reflectDamage = (int) (reflectDamage * reflectMultiplier);
        
        return Math.max(1, reflectDamage);
    }
    
    /**
     * 计算护盾吸收
     * 
     * @param damage 伤害
     * @param shieldValue 护盾值
     * @return 实际伤害
     */
    public int calculateShieldAbsorption(int damage, int shieldValue) {
        if (damage <= 0 || shieldValue <= 0) {
            return damage;
        }
        
        return Math.max(0, damage - shieldValue);
    }
    
    /**
     * 计算中毒伤害
     * 
     * @param baseDamage 基础伤害
     * @param duration 持续时间
     * @return 每秒中毒伤害
     */
    public int calculatePoisonDamage(int baseDamage, int duration) {
        if (baseDamage <= 0 || duration <= 0) {
            return 0;
        }
        
        // 中毒伤害 = 基础伤害 / 持续时间
        int poisonDamage = baseDamage / duration;
        
        return Math.max(1, poisonDamage);
    }
    
    /**
     * 获取目标防御力
     */
    private int getTargetDefense(BaseObject target) {
        if (target instanceof Player) {
            return ((Player) target).getDefense();
        } else if (target instanceof Monster) {
            return ((Monster) target).getDefense();
        }
        return 0;
    }
    
    /**
     * 获取目标魔法防御力
     */
    private int getTargetMagicDefense(BaseObject target) {
        if (target instanceof Player) {
            return ((Player) target).getMagicDefense();
        } else if (target instanceof Monster) {
            return ((Monster) target).getMagicDefense();
        }
        return 0;
    }
    
    /**
     * 获取目标等级
     */
    private int getTargetLevel(BaseObject target) {
        if (target instanceof Player) {
            return ((Player) target).getLevel();
        } else if (target instanceof Monster) {
            return ((Monster) target).getLevel();
        }
        return 1;
    }
    
    /**
     * 计算经验值获得
     * 
     * @param killer 杀死者
     * @param killed 被杀死者
     * @return 经验值
     */
    public int calculateExperience(Player killer, BaseObject killed) {
        if (killer == null || killed == null) {
            return 0;
        }
        
        int baseExp = 0;
        int killedLevel = 0;
        
        if (killed instanceof Monster) {
            Monster monster = (Monster) killed;
            baseExp = monster.getExperience();
            killedLevel = monster.getLevel();
        } else if (killed instanceof Player) {
            Player player = (Player) killed;
            baseExp = player.getLevel() * 20; // PK经验
            killedLevel = player.getLevel();
        }
        
        // 等级差异修正
        int levelDifference = killedLevel - killer.getLevel();
        double expMultiplier = 1.0;
        
        if (levelDifference > 0) {
            expMultiplier = 1.0 + levelDifference * 0.1; // 杀高级怪物经验提升
        } else if (levelDifference < -10) {
            expMultiplier = 0.1; // 杀低级怪物经验大幅降低
        } else if (levelDifference < 0) {
            expMultiplier = 1.0 + levelDifference * 0.05; // 杀低级怪物经验略微降低
        }
        
        int finalExp = (int) (baseExp * expMultiplier);
        
        log.debug("经验值计算: {} 击杀 {} 获得 {} 经验", 
                killer.getName(), killed.getName(), finalExp);
        
        return Math.max(1, finalExp);
    }
} 