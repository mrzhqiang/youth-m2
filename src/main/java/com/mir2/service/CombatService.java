package com.mir2.service;

import com.mir2.core.model.BaseObject;
import com.mir2.core.model.Monster;
import com.mir2.core.model.Player;
import com.mir2.core.model.PlayerSkill;
import com.mir2.core.model.Skill;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.SkillType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗系统服务
 * 
 * <p>负责处理游戏中的战斗逻辑，包括伤害计算、技能释放、状态效果等。
 * 对应原M2Engine中的Combat模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class CombatService {
    
    /** 随机数生成器 */
    private final Random random = new Random();
    
    /** 伤害计算器 */
    @Autowired
    private DamageCalculator damageCalculator;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private MonsterService monsterService;
    
    /**
     * 玩家攻击目标
     * 
     * @param attacker 攻击者
     * @param target 目标
     * @return 攻击结果
     */
    public AttackResult playerAttack(Player attacker, BaseObject target) {
        if (attacker == null || target == null) {
            return new AttackResult(false, "攻击者或目标为空");
        }
        
        // 检查攻击条件
        if (!canAttack(attacker, target)) {
            return new AttackResult(false, "无法攻击该目标");
        }
        
        // 检查攻击距离
        if (attacker.distanceTo(target) > attacker.getAttackRange()) {
            return new AttackResult(false, "目标超出攻击范围");
        }
        
        // 检查攻击冷却
        if (!attacker.canAttack()) {
            return new AttackResult(false, "攻击冷却中");
        }
        
        // 计算伤害
        int damage = damageCalculator.calculatePhysicalDamage(attacker, target);
        
        // 检查命中
        if (!calculateHitChance(attacker, target)) {
            attacker.setLastAttackTime(System.currentTimeMillis());
            return new AttackResult(false, "攻击未命中", 0, AttackResult.AttackType.MISS);
        }
        
        // 检查暴击
        boolean isCritical = calculateCriticalChance(attacker);
        if (isCritical) {
            damage = (int) (damage * 1.5); // 暴击伤害提升50%
        }
        
        // 应用伤害
        int actualDamage = applyDamage(target, damage, attacker);
        
        // 更新攻击者状态
        attacker.setLastAttackTime(System.currentTimeMillis());
        
        // 记录战斗日志
        log.debug("玩家 {} 攻击 {} 造成 {} 点伤害 {}", 
                attacker.getName(), target.getName(), actualDamage, 
                isCritical ? "(暴击)" : "");
        
        return new AttackResult(true, "攻击成功", actualDamage, 
                isCritical ? AttackResult.AttackType.CRITICAL : AttackResult.AttackType.NORMAL);
    }
    
    /**
     * 玩家使用技能
     * 
     * @param player 玩家
     * @param skillId 技能ID
     * @param target 目标
     * @return 技能使用结果
     */
    public SkillResult useSkill(Player player, int skillId, BaseObject target) {
        if (player == null) {
            return new SkillResult(false, "玩家为空");
        }
        
        // 检查技能是否存在
        PlayerSkill playerSkill = player.getSkills().get(skillId);
        if (playerSkill == null) {
            return new SkillResult(false, "未学会该技能");
        }
        
        // 检查技能冷却
        if (!playerSkill.canUse()) {
            return new SkillResult(false, "技能冷却中");
        }
        
        // 获取技能模板
        Skill skillTemplate = skillService.getSkill(skillId);
        if (skillTemplate == null) {
            return new SkillResult(false, "技能模板不存在");
        }
        
        // 检查魔法值
        int mpCost = skillTemplate.getMpCost();
        if (player.getMp() < mpCost) {
            return new SkillResult(false, "魔法值不足");
        }
        
        // 检查技能类型和目标
        if (!validateSkillTarget(skillTemplate, target)) {
            return new SkillResult(false, "技能目标无效");
        }
        
        // 检查技能距离
        if (target != null && player.distanceTo(target) > skillTemplate.getRange()) {
            return new SkillResult(false, "目标超出技能范围");
        }
        
        // 消耗魔法值
        player.setMp(player.getMp() - mpCost);
        
        // 设置技能冷却
        playerSkill.use();
        
        // 执行技能效果
        SkillResult result = executeSkillEffect(player, skillTemplate, playerSkill, target);
        
        // 增加技能经验
        playerSkill.addExperience(10);
        
        log.debug("玩家 {} 使用技能 {} 对 {}", 
                player.getName(), skillTemplate.getName(), 
                target != null ? target.getName() : "无目标");
        
        return result;
    }
    
    /**
     * 执行技能效果
     */
    private SkillResult executeSkillEffect(Player player, Skill skill, PlayerSkill playerSkill, BaseObject target) {
        switch (skill.getType()) {
            case ATTACK:
                return executeAttackSkill(player, skill, playerSkill, target);
            case HEAL:
                return executeHealSkill(player, skill, playerSkill, target);
            case BUFF:
                return executeBuffSkill(player, skill, playerSkill, target);
            case DEBUFF:
                return executeDebuffSkill(player, skill, playerSkill, target);
            case SUMMON:
                return executeSummonSkill(player, skill, playerSkill);
            case TELEPORT:
                return executeTeleportSkill(player, skill, playerSkill);
            default:
                return new SkillResult(false, "未知技能类型");
        }
    }
    
    /**
     * 执行攻击技能
     */
    private SkillResult executeAttackSkill(Player player, Skill skill, PlayerSkill playerSkill, BaseObject target) {
        if (target == null) {
            return new SkillResult(false, "攻击技能需要目标");
        }
        
        // 计算技能伤害
        int damage = damageCalculator.calculateSkillDamage(player, skill, playerSkill, target);
        
        // 检查命中
        if (!calculateSkillHitChance(player, skill, target)) {
            return new SkillResult(false, "技能未命中", 0);
        }
        
        // 应用伤害
        int actualDamage = applyDamage(target, damage, player);
        
        return new SkillResult(true, "技能释放成功", actualDamage);
    }
    
    /**
     * 执行治疗技能
     */
    private SkillResult executeHealSkill(Player player, Skill skill, PlayerSkill playerSkill, BaseObject target) {
        BaseObject healTarget = target != null ? target : player;
        
        if (!(healTarget instanceof Player)) {
            return new SkillResult(false, "治疗技能只能对玩家使用");
        }
        
        Player targetPlayer = (Player) healTarget;
        
        // 计算治疗量
        int healAmount = damageCalculator.calculateHealAmount(player, skill, playerSkill);
        
        // 应用治疗
        int oldHp = targetPlayer.getHp();
        int newHp = Math.min(targetPlayer.getMaxHp(), oldHp + healAmount);
        targetPlayer.setHp(newHp);
        
        int actualHeal = newHp - oldHp;
        
        log.debug("玩家 {} 治疗 {} 恢复 {} 点生命值", 
                player.getName(), targetPlayer.getName(), actualHeal);
        
        return new SkillResult(true, "治疗成功", actualHeal);
    }
    
    /**
     * 执行增益技能
     */
    private SkillResult executeBuffSkill(Player player, Skill skill, PlayerSkill playerSkill, BaseObject target) {
        BaseObject buffTarget = target != null ? target : player;
        
        if (!(buffTarget instanceof Player)) {
            return new SkillResult(false, "增益技能只能对玩家使用");
        }
        
        Player targetPlayer = (Player) buffTarget;
        
        // 应用增益效果
        applyBuffEffect(targetPlayer, skill, playerSkill);
        
        return new SkillResult(true, "增益效果施加成功");
    }
    
    /**
     * 执行减益技能
     */
    private SkillResult executeDebuffSkill(Player player, Skill skill, PlayerSkill playerSkill, BaseObject target) {
        if (target == null) {
            return new SkillResult(false, "减益技能需要目标");
        }
        
        // 应用减益效果
        applyDebuffEffect(target, skill, playerSkill);
        
        return new SkillResult(true, "减益效果施加成功");
    }
    
    /**
     * 执行召唤技能
     */
    private SkillResult executeSummonSkill(Player player, Skill skill, PlayerSkill playerSkill) {
        // 检查玩家是否有足够的MP
        if (player.getMp() < skill.getMpCost()) {
            return new SkillResult(false, "MP不足");
        }
        
        // 消耗MP
        player.setMp(player.getMp() - skill.getMpCost());
        
        // 获取当前召唤物数量
        int currentSummons = player.getSummonedCreatures().size();
        int maxSummons = playerSkill.getLevel(); // 技能等级决定最大召唤数量
        
        if (currentSummons >= maxSummons) {
            return new SkillResult(false, "召唤物数量已达上限");
        }
        
        // 创建召唤物
        SummonedCreature summon = createSummonedCreature(player, skill, playerSkill);
        if (summon == null) {
            return new SkillResult(false, "召唤失败");
        }
        
        // 寻找合适的召唤位置
        int summonX = player.getX();
        int summonY = player.getY();
        
        // 在玩家周围寻找空闲位置
        boolean foundPosition = false;
        for (int radius = 1; radius <= 3 && !foundPosition; radius++) {
            for (int dx = -radius; dx <= radius && !foundPosition; dx++) {
                for (int dy = -radius; dy <= radius && !foundPosition; dy++) {
                    int testX = player.getX() + dx;
                    int testY = player.getY() + dy;
                    
                    // 检查位置是否可用
                    if (mapService.canMove(player.getMapName(), testX, testY)) {
                        summonX = testX;
                        summonY = testY;
                        foundPosition = true;
                    }
                }
            }
        }
        
        if (!foundPosition) {
            return new SkillResult(false, "找不到合适的召唤位置");
        }
        
        // 设置召唤物位置
        summon.setX(summonX);
        summon.setY(summonY);
        summon.setMapName(player.getMapName());
        
        // 添加到玩家的召唤物列表
        player.getSummonedCreatures().add(summon);
        
        // 添加到地图
        mapService.addObject(player.getMapName(), summon);
        
        log.info("玩家 {} 召唤了 {} 到位置 ({}, {})", 
                player.getName(), summon.getName(), summonX, summonY);
        
        return new SkillResult(true, "召唤成功", 1);
    }
    
    /**
     * 创建召唤物
     */
    private SummonedCreature createSummonedCreature(Player player, Skill skill, PlayerSkill playerSkill) {
        String skillName = skill.getName();
        int level = playerSkill.getLevel();
        
        SummonedCreature summon = new SummonedCreature();
        summon.setOwner(player);
        summon.setDuration(skill.getDuration() * 1000); // 转换为毫秒
        summon.setStartTime(System.currentTimeMillis());
        
        // 根据技能名称设置不同的召唤物
        switch (skillName) {
            case "召唤骷髅":
                summon.setName("骷髅战士");
                summon.setMaxHp(50 + level * 20);
                summon.setAttack(10 + level * 5);
                summon.setDefense(5 + level * 2);
                summon.setSummonType(SummonType.SKELETON);
                break;
            case "召唤僵尸":
                summon.setName("僵尸");
                summon.setMaxHp(100 + level * 30);
                summon.setAttack(15 + level * 7);
                summon.setDefense(10 + level * 3);
                summon.setSummonType(SummonType.ZOMBIE);
                break;
            case "召唤神兽":
                summon.setName("神兽");
                summon.setMaxHp(200 + level * 50);
                summon.setAttack(25 + level * 10);
                summon.setDefense(15 + level * 5);
                summon.setSummonType(SummonType.BEAST);
                break;
            default:
                log.warn("未知的召唤技能: {}", skillName);
                return null;
        }
        
        summon.setHp(summon.getMaxHp());
        return summon;
    }
    
    /**
     * 执行传送技能
     */
    private SkillResult executeTeleportSkill(Player player, Skill skill, PlayerSkill playerSkill) {
        // 检查玩家是否有足够的MP
        if (player.getMp() < skill.getMpCost()) {
            return new SkillResult(false, "MP不足");
        }
        
        // 消耗MP
        player.setMp(player.getMp() - skill.getMpCost());
        
        String skillName = skill.getName();
        int level = playerSkill.getLevel();
        
        // 根据技能名称执行不同的传送逻辑
        switch (skillName) {
            case "瞬息移动":
                return executeInstantTeleport(player, level);
            case "随机传送":
                return executeRandomTeleport(player, level);
            case "回城传送":
                return executeTownTeleport(player, level);
            case "地牢逃脱":
                return executeDungeonEscape(player, level);
            default:
                return new SkillResult(false, "未知的传送技能");
        }
    }
    
    /**
     * 执行瞬息移动
     */
    private SkillResult executeInstantTeleport(Player player, int level) {
        // 在当前位置周围传送
        int maxDistance = 3 + level; // 技能等级越高，传送距离越远
        
        // 寻找合适的传送位置
        for (int attempts = 0; attempts < 10; attempts++) {
            int angle = random.nextInt(360);
            int distance = random.nextInt(maxDistance) + 1;
            
            int newX = player.getX() + (int)(distance * Math.cos(Math.toRadians(angle)));
            int newY = player.getY() + (int)(distance * Math.sin(Math.toRadians(angle)));
            
            // 检查位置是否可用
            if (mapService.canMove(player.getMapName(), newX, newY)) {
                player.setX(newX);
                player.setY(newY);
                
                log.info("玩家 {} 瞬息移动到 ({}, {})", player.getName(), newX, newY);
                return new SkillResult(true, "瞬息移动成功");
            }
        }
        
        return new SkillResult(false, "找不到合适的传送位置");
    }
    
    /**
     * 执行随机传送
     */
    private SkillResult executeRandomTeleport(Player player, int level) {
        // 在地图内随机传送
        int maxDistance = 10 + level * 5; // 技能等级越高，传送范围越大
        
        // 寻找合适的传送位置
        for (int attempts = 0; attempts < 20; attempts++) {
            int newX = player.getX() + random.nextInt(maxDistance * 2) - maxDistance;
            int newY = player.getY() + random.nextInt(maxDistance * 2) - maxDistance;
            
            // 确保在地图范围内
            newX = Math.max(10, Math.min(newX, 490));
            newY = Math.max(10, Math.min(newY, 490));
            
            // 检查位置是否可用
            if (mapService.canMove(player.getMapName(), newX, newY)) {
                player.setX(newX);
                player.setY(newY);
                
                log.info("玩家 {} 随机传送到 ({}, {})", player.getName(), newX, newY);
                return new SkillResult(true, "随机传送成功");
            }
        }
        
        return new SkillResult(false, "找不到合适的传送位置");
    }
    
    /**
     * 执行回城传送
     */
    private SkillResult executeTownTeleport(Player player, int level) {
        // 传送到主城
        String townMap = "比奇城";
        int townX = 330;
        int townY = 330;
        
        // 高级技能可以传送到其他城市
        if (level >= 5) {
            String[] cities = {"比奇城", "盟重城", "白日门"};
            int[] cityX = {330, 340, 350};
            int[] cityY = {330, 340, 350};
            
            // 玩家可以选择城市（这里随机选择）
            int cityIndex = random.nextInt(cities.length);
            townMap = cities[cityIndex];
            townX = cityX[cityIndex];
            townY = cityY[cityIndex];
        }
        
        player.setMapName(townMap);
        player.setX(townX);
        player.setY(townY);
        
        log.info("玩家 {} 回城传送到 {} ({}, {})", player.getName(), townMap, townX, townY);
        return new SkillResult(true, "回城传送成功");
    }
    
    /**
     * 执行地牢逃脱
     */
    private SkillResult executeDungeonEscape(Player player, int level) {
        // 检查是否在地牢中
        if (!player.getMapName().contains("地牢") && 
            !player.getMapName().contains("洞穴") && 
            !player.getMapName().contains("矿洞")) {
            return new SkillResult(false, "当前不在地牢中，无法使用地牢逃脱");
        }
        
        // 传送到地牢入口或安全区域
        String exitMap = "比奇城";
        int exitX = 330;
        int exitY = 330;
        
        // 高级技能可以传送到最近的安全区域
        if (level >= 3) {
            // 根据当前地牢选择合适的出口
            if (player.getMapName().contains("矿洞")) {
                exitMap = "比奇城";
                exitX = 320;
                exitY = 320;
            } else if (player.getMapName().contains("石墓")) {
                exitMap = "盟重城";
                exitX = 340;
                exitY = 340;
            }
        }
        
        player.setMapName(exitMap);
        player.setX(exitX);
        player.setY(exitY);
        
        log.info("玩家 {} 地牢逃脱到 {} ({}, {})", player.getName(), exitMap, exitX, exitY);
        return new SkillResult(true, "地牢逃脱成功");
    }
    
    /**
     * 应用伤害
     */
    private int applyDamage(BaseObject target, int damage, BaseObject attacker) {
        if (target instanceof Player) {
            return ((Player) target).takeDamage(damage);
        } else if (target instanceof Monster) {
            return ((Monster) target).takeDamage(damage, attacker);
        }
        return 0;
    }
    
    /**
     * 检查是否可以攻击
     */
    private boolean canAttack(Player attacker, BaseObject target) {
        if (attacker.isDead() || target == null) {
            return false;
        }
        
        // 检查目标是否在同一地图
        if (!attacker.getMapName().equals(target.getMapName())) {
            return false;
        }
        
        // 检查目标是否死亡
        if (target instanceof Player && ((Player) target).isDead()) {
            return false;
        }
        
        if (target instanceof Monster && ((Monster) target).isDead()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 计算命中率
     */
    private boolean calculateHitChance(Player attacker, BaseObject target) {
        int attackerAccuracy = attacker.getAccuracy();
        int targetAgility = 0;
        
        if (target instanceof Player) {
            targetAgility = ((Player) target).getAgility();
        } else if (target instanceof Monster) {
            targetAgility = ((Monster) target).getAgility();
        }
        
        // 基础命中率 + 准确度 - 敏捷度
        int hitChance = 80 + attackerAccuracy - targetAgility;
        hitChance = Math.max(5, Math.min(95, hitChance)); // 限制在5%-95%之间
        
        return random.nextInt(100) < hitChance;
    }
    
    /**
     * 计算暴击率
     */
    private boolean calculateCriticalChance(Player attacker) {
        int criticalChance = 5 + attacker.getAgility() / 10; // 基础5% + 敏捷/10
        criticalChance = Math.min(50, criticalChance); // 最大50%
        
        return random.nextInt(100) < criticalChance;
    }
    
    /**
     * 计算技能命中率
     */
    private boolean calculateSkillHitChance(Player attacker, Skill skill, BaseObject target) {
        // 技能命中率通常比普通攻击高
        return calculateHitChance(attacker, target) || random.nextInt(100) < 10;
    }
    
    /**
     * 验证技能目标
     */
    private boolean validateSkillTarget(Skill skill, BaseObject target) {
        switch (skill.getType()) {
            case ATTACK:
            case DEBUFF:
                return target != null;
            case HEAL:
            case BUFF:
                return true; // 治疗和增益可以没有目标（默认自己）
            case SUMMON:
            case TELEPORT:
                return target == null; // 召唤和传送不需要目标
            default:
                return false;
        }
    }
    
    /**
     * 应用增益效果
     */
    private void applyBuffEffect(Player target, Skill skill, PlayerSkill playerSkill) {
        String skillName = skill.getName();
        int level = playerSkill.getLevel();
        int duration = skill.getDuration() * 1000; // 转换为毫秒
        
        BuffEffect buffEffect = new BuffEffect();
        buffEffect.setDuration(duration);
        buffEffect.setLevel(level);
        buffEffect.setStartTime(System.currentTimeMillis());
        
        // 根据技能名称应用不同的增益效果
        switch (skillName) {
            case "神圣战甲术":
                buffEffect.setType(BuffType.DEFENSE);
                buffEffect.setDefenseBonus(level * 10);
                buffEffect.setMagicDefenseBonus(level * 5);
                buffEffect.setDescription("防御力+" + (level * 10) + "，魔法防御+" + (level * 5));
                break;
            case "幽灵盾":
                buffEffect.setType(BuffType.MAGIC_DEFENSE);
                buffEffect.setMagicDefenseBonus(level * 15);
                buffEffect.setDescription("魔法防御+" + (level * 15));
                break;
            case "神圣战甲术":
                buffEffect.setType(BuffType.ATTACK);
                buffEffect.setAttackBonus(level * 8);
                buffEffect.setDescription("攻击力+" + (level * 8));
                break;
            case "魔法盾":
                buffEffect.setType(BuffType.MAGIC_SHIELD);
                buffEffect.setMagicShieldValue(level * 20);
                buffEffect.setDescription("魔法盾值+" + (level * 20));
                break;
            case "隐身术":
                buffEffect.setType(BuffType.INVISIBLE);
                buffEffect.setDescription("隐身状态");
                break;
            case "加速术":
                buffEffect.setType(BuffType.SPEED);
                buffEffect.setSpeedBonus(level * 10);
                buffEffect.setDescription("移动速度+" + (level * 10) + "%");
                break;
            case "治愈术":
                buffEffect.setType(BuffType.HP_REGEN);
                buffEffect.setHpRegenBonus(level * 3);
                buffEffect.setDescription("生命值回复+" + (level * 3) + "/秒");
                break;
            case "精神力战法":
                buffEffect.setType(BuffType.MP_REGEN);
                buffEffect.setMpRegenBonus(level * 5);
                buffEffect.setDescription("魔法值回复+" + (level * 5) + "/秒");
                break;
            case "护身气幕":
                buffEffect.setType(BuffType.REFLECTION);
                buffEffect.setReflectionRate(level * 5);
                buffEffect.setDescription("反射伤害几率+" + (level * 5) + "%");
                break;
            case "群体治愈术":
                buffEffect.setType(BuffType.GROUP_HEAL);
                buffEffect.setHealBonus(level * 15);
                buffEffect.setDescription("群体治愈效果+" + (level * 15));
                break;
            default:
                log.warn("未知的增益技能: {}", skillName);
                return;
        }
        
        // 移除相同类型的旧效果
        target.getBuffEffects().removeIf(effect -> effect.getType() == buffEffect.getType());
        
        // 添加新效果
        target.getBuffEffects().add(buffEffect);
        
        log.debug("对 {} 施加增益效果: {} ({})", target.getName(), skill.getName(), buffEffect.getDescription());
    }
    
    /**
     * 应用减益效果
     */
    private void applyDebuffEffect(BaseObject target, Skill skill, PlayerSkill playerSkill) {
        String skillName = skill.getName();
        int level = playerSkill.getLevel();
        int duration = skill.getDuration() * 1000; // 转换为毫秒
        
        DebuffEffect debuffEffect = new DebuffEffect();
        debuffEffect.setDuration(duration);
        debuffEffect.setLevel(level);
        debuffEffect.setStartTime(System.currentTimeMillis());
        
        // 根据技能名称应用不同的减益效果
        switch (skillName) {
            case "施毒术":
                debuffEffect.setType(DebuffType.POISON);
                debuffEffect.setDamagePerSecond(level * 5);
                debuffEffect.setDescription("中毒状态，每秒-" + (level * 5) + "HP");
                break;
            case "灵魂火符":
                debuffEffect.setType(DebuffType.BURN);
                debuffEffect.setDamagePerSecond(level * 8);
                debuffEffect.setDescription("燃烧状态，每秒-" + (level * 8) + "HP");
                break;
            case "冰咆哮":
                debuffEffect.setType(DebuffType.FREEZE);
                debuffEffect.setSpeedReduction(level * 20);
                debuffEffect.setDescription("冰冻状态，移动速度-" + (level * 20) + "%");
                break;
            case "缓慢术":
                debuffEffect.setType(DebuffType.SLOW);
                debuffEffect.setSpeedReduction(level * 15);
                debuffEffect.setDescription("缓慢状态，移动速度-" + (level * 15) + "%");
                break;
            case "虚弱术":
                debuffEffect.setType(DebuffType.WEAKNESS);
                debuffEffect.setAttackReduction(level * 10);
                debuffEffect.setDescription("虚弱状态，攻击力-" + (level * 10));
                break;
            case "沉默术":
                debuffEffect.setType(DebuffType.SILENCE);
                debuffEffect.setDescription("沉默状态，无法使用技能");
                break;
            case "麻痹术":
                debuffEffect.setType(DebuffType.PARALYSIS);
                debuffEffect.setDescription("麻痹状态，无法移动");
                break;
            case "诅咒术":
                debuffEffect.setType(DebuffType.CURSE);
                debuffEffect.setLuckReduction(level * 2);
                debuffEffect.setDescription("诅咒状态，幸运值-" + (level * 2));
                break;
            case "失明术":
                debuffEffect.setType(DebuffType.BLIND);
                debuffEffect.setAccuracyReduction(level * 20);
                debuffEffect.setDescription("失明状态，命中率-" + (level * 20) + "%");
                break;
            case "恐惧术":
                debuffEffect.setType(DebuffType.FEAR);
                debuffEffect.setDescription("恐惧状态，无法主动攻击");
                break;
            default:
                log.warn("未知的减益技能: {}", skillName);
                return;
        }
        
        // 添加减益效果到目标
        if (target instanceof Player) {
            Player player = (Player) target;
            // 移除相同类型的旧效果
            player.getDebuffEffects().removeIf(effect -> effect.getType() == debuffEffect.getType());
            // 添加新效果
            player.getDebuffEffects().add(debuffEffect);
        } else if (target instanceof Monster) {
            Monster monster = (Monster) target;
            // 移除相同类型的旧效果
            monster.getDebuffEffects().removeIf(effect -> effect.getType() == debuffEffect.getType());
            // 添加新效果
            monster.getDebuffEffects().add(debuffEffect);
        }
        
        log.debug("对 {} 施加减益效果: {} ({})", target.getName(), skill.getName(), debuffEffect.getDescription());
    }
    
    /**
     * 攻击结果类
     */
    public static class AttackResult {
        private boolean success;
        private String message;
        private int damage;
        private AttackType type;
        
        public AttackResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.damage = 0;
            this.type = AttackType.NORMAL;
        }
        
        public AttackResult(boolean success, String message, int damage, AttackType type) {
            this.success = success;
            this.message = message;
            this.damage = damage;
            this.type = type;
        }
        
        public enum AttackType {
            NORMAL, CRITICAL, MISS
        }
        
        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getDamage() { return damage; }
        public void setDamage(int damage) { this.damage = damage; }
        public AttackType getType() { return type; }
        public void setType(AttackType type) { this.type = type; }
    }
    
    /**
     * 技能结果类
     */
    public static class SkillResult {
        private boolean success;
        private String message;
        private int value;
        
        public SkillResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.value = 0;
        }
        
        public SkillResult(boolean success, String message, int value) {
            this.success = success;
            this.message = message;
            this.value = value;
        }
        
        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
} 