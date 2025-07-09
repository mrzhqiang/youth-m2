package com.mir2.service;

import com.mir2.core.model.Skill;
import com.mir2.core.model.Player;
import com.mir2.core.model.PlayerSkill;
import com.mir2.core.model.GameMap;
import com.mir2.core.model.BaseObject;
import com.mir2.core.model.Monster;
import com.mir2.core.model.SummonedCreature;
import com.mir2.core.model.BuffEffect;
import com.mir2.core.model.DebuffEffect;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.BuffType;
import com.mir2.core.enums.DebuffType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 技能服务类
 * 
 * <p>提供技能相关的业务逻辑处理。
 * 对应原M2Engine中的技能管理系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class SkillService {
    
    /** 技能模板缓存 */
    private final Map<Integer, Skill> skillTemplates = new ConcurrentHashMap<>();
    
    /** 技能名称索引 */
    private final Map<String, Skill> skillNameIndex = new ConcurrentHashMap<>();
    
    /** 职业技能索引 */
    private final Map<Job, List<Skill>> jobSkillIndex = new ConcurrentHashMap<>();
    
    /** 地图服务 */
    @Autowired
    private MapService mapService;
    
    /**
     * 初始化技能模板
     */
    public void initializeSkillTemplates() {
        log.info("开始初始化技能模板...");
        
        // 创建战士技能
        createWarriorSkills();
        
        // 创建法师技能
        createWizardSkills();
        
        // 创建道士技能
        createTaoistSkills();
        
        // 创建通用技能
        createCommonSkills();
        
        // 建立职业技能索引
        buildJobSkillIndex();
        
        log.info("技能模板初始化完成，共加载 {} 个技能模板", skillTemplates.size());
    }
    
    /**
     * 根据技能ID获取技能模板
     * 
     * @param skillId 技能ID
     * @return 技能模板
     */
    public Skill getSkillTemplate(int skillId) {
        return skillTemplates.get(skillId);
    }
    
    /**
     * 根据技能名称获取技能模板
     * 
     * @param skillName 技能名称
     * @return 技能模板
     */
    public Skill getSkillTemplateByName(String skillName) {
        return skillNameIndex.get(skillName);
    }
    
    /**
     * 获取职业技能列表
     * 
     * @param job 职业
     * @return 技能列表
     */
    public List<Skill> getSkillsByJob(Job job) {
        return jobSkillIndex.getOrDefault(job, List.of());
    }
    
    /**
     * 玩家学习技能
     * 
     * @param player 玩家
     * @param skillId 技能ID
     * @return 是否学习成功
     */
    public boolean learnSkill(Player player, int skillId) {
        Skill skill = getSkillTemplate(skillId);
        if (skill == null) {
            log.warn("技能模板不存在: {}", skillId);
            return false;
        }
        
        // 检查职业要求
        if (skill.getRequiredJob() != Job.ALL && skill.getRequiredJob() != player.getJob()) {
            log.warn("玩家 {} 职业不符，无法学习技能: {} (需要职业: {})", 
                    player.getName(), skill.getName(), skill.getRequiredJob().getName());
            return false;
        }
        
        // 检查等级要求
        if (player.getLevel() < skill.getRequiredLevel()) {
            log.warn("玩家 {} 等级不足，无法学习技能: {} (需要等级: {})", 
                    player.getName(), skill.getName(), skill.getRequiredLevel());
            return false;
        }
        
        // 检查前置技能
        if (!hasRequiredSkills(player, skill)) {
            log.warn("玩家 {} 缺少前置技能，无法学习技能: {}", player.getName(), skill.getName());
            return false;
        }
        
        // 学习技能
        return player.learnSkill(skill, 1);
    }
    
    /**
     * 升级技能
     * 
     * @param player 玩家
     * @param skillId 技能ID
     * @return 是否升级成功
     */
    public boolean upgradeSkill(Player player, int skillId) {
        PlayerSkill playerSkill = player.getSkills().get(skillId);
        if (playerSkill == null) {
            log.warn("玩家 {} 没有学会技能 ID: {}", player.getName(), skillId);
            return false;
        }
        
        // 检查是否可以升级
        if (!playerSkill.canLevelUp()) {
            log.warn("玩家 {} 技能 {} 经验不足，无法升级", player.getName(), playerSkill.getSkill().getName());
            return false;
        }
        
        // 升级技能
        return playerSkill.forceUpgrade();
    }
    
    /**
     * 使用技能
     * 
     * @param player 玩家
     * @param skillId 技能ID
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 是否使用成功
     */
    public boolean useSkill(Player player, int skillId, int targetX, int targetY) {
        PlayerSkill playerSkill = player.getSkills().get(skillId);
        if (playerSkill == null) {
            log.warn("玩家 {} 没有学会技能 ID: {}", player.getName(), skillId);
            return false;
        }
        
        // 检查是否可以使用
        if (!playerSkill.canUse()) {
            log.debug("玩家 {} 技能 {} 冷却中", player.getName(), playerSkill.getSkill().getName());
            return false;
        }
        
        // 检查魔法值
        Skill.SkillAttributes attributes = playerSkill.getCurrentAttributes();
        if (player.getMp() < attributes.getMpCost()) {
            log.debug("玩家 {} 魔法值不足，无法使用技能: {}", player.getName(), playerSkill.getSkill().getName());
            return false;
        }
        
        // 检查距离
        if (!isInRange(player, targetX, targetY, attributes.getRange())) {
            log.debug("玩家 {} 距离不足，无法使用技能: {}", player.getName(), playerSkill.getSkill().getName());
            return false;
        }
        
        // 使用技能
        if (playerSkill.use()) {
            player.setMp(player.getMp() - attributes.getMpCost());
            
            // 处理技能效果
            processSkillEffect(player, playerSkill, targetX, targetY);
            
            log.info("玩家 {} 使用技能: {} 目标: ({}, {})", 
                    player.getName(), playerSkill.getSkill().getName(), targetX, targetY);
            return true;
        }
        
        return false;
    }
    
    /**
     * 重置技能冷却
     * 
     * @param player 玩家
     * @param skillId 技能ID
     */
    public void resetSkillCooldown(Player player, int skillId) {
        PlayerSkill playerSkill = player.getSkills().get(skillId);
        if (playerSkill != null) {
            playerSkill.resetCooldown();
            log.info("重置玩家 {} 技能 {} 冷却", player.getName(), playerSkill.getSkill().getName());
        }
    }
    
    /**
     * 重置所有技能冷却
     * 
     * @param player 玩家
     */
    public void resetAllSkillCooldowns(Player player) {
        player.getSkills().values().forEach(PlayerSkill::resetCooldown);
        log.info("重置玩家 {} 所有技能冷却", player.getName());
    }
    
    /**
     * 获取技能信息
     * 
     * @param skillId 技能ID
     * @return 技能信息
     */
    public String getSkillInfo(int skillId) {
        Skill skill = getSkillTemplate(skillId);
        if (skill == null) {
            return "技能不存在";
        }
        
        return String.format("技能: %s [类型: %s] [职业: %s] [等级要求: %d] [最大等级: %d]",
                skill.getName(), skill.getType().getName(), skill.getRequiredJob().getName(),
                skill.getRequiredLevel(), skill.getMaxLevel());
    }
    
    /**
     * 获取玩家技能信息
     * 
     * @param player 玩家
     * @param skillId 技能ID
     * @return 玩家技能信息
     */
    public String getPlayerSkillInfo(Player player, int skillId) {
        PlayerSkill playerSkill = player.getSkills().get(skillId);
        if (playerSkill == null) {
            return "玩家未学会此技能";
        }
        
        return playerSkill.getSkillInfo();
    }
    
    /**
     * 搜索技能
     * 
     * @param keyword 关键词
     * @return 匹配的技能列表
     */
    public List<Skill> searchSkills(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return skillTemplates.values().stream()
                .filter(skill -> skill.getName().toLowerCase().contains(lowerKeyword) ||
                               skill.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否有前置技能
     * 
     * @param player 玩家
     * @param skill 技能
     * @return 是否有前置技能
     */
    private boolean hasRequiredSkills(Player player, Skill skill) {
        // 根据技能ID检查前置技能
        List<PlayerSkill> playerSkills = player.getSkills();
        
        switch (skill.getId()) {
            case 1002: // 攻杀剑术需要基本剑术
                return hasSkillLevel(playerSkills, 1001, 1);
            case 1003: // 刺杀剑术需要攻杀剑术
                return hasSkillLevel(playerSkills, 1002, 2);
            case 1004: // 半月弯刀需要基本剑术
                return hasSkillLevel(playerSkills, 1001, 2);
            case 1005: // 烈火剑法需要攻杀剑术
                return hasSkillLevel(playerSkills, 1002, 3);
            case 2002: // 大火球需要小火球
                return hasSkillLevel(playerSkills, 2001, 2);
            case 2003: // 火墙需要大火球
                return hasSkillLevel(playerSkills, 2002, 1);
            case 2004: // 雷电术需要大火球
                return hasSkillLevel(playerSkills, 2002, 2);
            case 2005: // 冰咆哮需要雷电术
                return hasSkillLevel(playerSkills, 2004, 1);
            case 3002: // 精神力战法需要治愈术
                return hasSkillLevel(playerSkills, 3001, 1);
            case 3003: // 施毒术需要治愈术
                return hasSkillLevel(playerSkills, 3001, 2);
            case 3004: // 灵魂火符需要施毒术
                return hasSkillLevel(playerSkills, 3003, 1);
            case 3005: // 召唤骷髅需要精神力战法
                return hasSkillLevel(playerSkills, 3002, 2);
            default:
                return true; // 基础技能或没有前置要求
        }
    }
    
    /**
     * 检查玩家是否有指定技能和等级
     */
    private boolean hasSkillLevel(List<PlayerSkill> playerSkills, int skillId, int requiredLevel) {
        for (PlayerSkill playerSkill : playerSkills) {
            if (playerSkill.getSkill().getId() == skillId && playerSkill.getLevel() >= requiredLevel) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否在技能范围内
     * 
     * @param player 玩家
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @param range 技能范围
     * @return 是否在范围内
     */
    private boolean isInRange(Player player, int targetX, int targetY, int range) {
        double distance = player.distanceTo(targetX, targetY);
        return distance <= range;
    }
    
    /**
     * 处理技能效果
     * 
     * @param player 玩家
     * @param playerSkill 玩家技能
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void processSkillEffect(Player player, PlayerSkill playerSkill, int targetX, int targetY) {
        Skill skill = playerSkill.getSkill();
        Skill.SkillAttributes attributes = playerSkill.getCurrentAttributes();
        
        // 根据技能类型处理不同效果
        switch (skill.getType()) {
            case ATTACK:
                processAttackSkill(player, attributes, targetX, targetY);
                break;
            case HEAL:
                processHealSkill(player, attributes);
                break;
            case BUFF:
                processBuffSkill(player, attributes);
                break;
            case DEBUFF:
                processDebuffSkill(player, attributes, targetX, targetY);
                break;
            case SUMMON:
                processSummonSkill(player, attributes, targetX, targetY);
                break;
            case TELEPORT:
                processTeleportSkill(player, attributes, targetX, targetY);
                break;
            default:
                log.warn("未处理的技能类型: {}", skill.getType());
                break;
        }
    }
    
    /**
     * 处理攻击技能
     * 
     * @param player 玩家
     * @param attributes 技能属性
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void processAttackSkill(Player player, Skill.SkillAttributes attributes, int targetX, int targetY) {
        // 计算技能伤害
        int baseDamage = attributes.getPower();
        int skillDamage = calculateSkillDamage(player, baseDamage);
        
        // 获取目标位置的对象
        GameMap gameMap = getGameMap(player.getMapName());
        if (gameMap == null) {
            log.warn("玩家 {} 所在地图 {} 不存在", player.getName(), player.getMapName());
            return;
        }
        
        BaseObject target = gameMap.getObjectAt(targetX, targetY);
        if (target instanceof Monster) {
            Monster monster = (Monster) target;
            
            // 应用伤害
            monster.takeDamage(skillDamage);
            
            // 检查击杀
            if (monster.isDead()) {
                handleMonsterKilled(player, monster);
            }
            
            log.debug("玩家 {} 对 {} 造成 {} 点技能伤害", 
                    player.getName(), monster.getName(), skillDamage);
        } else if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            
            // PK判断
            if (canAttackPlayer(player, targetPlayer)) {
                targetPlayer.takeDamage(skillDamage);
                log.debug("玩家 {} 对玩家 {} 造成 {} 点技能伤害", 
                        player.getName(), targetPlayer.getName(), skillDamage);
            }
        }
        
        log.debug("玩家 {} 释放攻击技能，威力: {}, 目标: ({}, {})", 
                player.getName(), attributes.getPower(), targetX, targetY);
    }
    
    /**
     * 计算技能伤害
     */
    private int calculateSkillDamage(Player player, int baseDamage) {
        int damage = baseDamage;
        
        // 根据职业和属性计算伤害
        switch (player.getJob()) {
            case WARRIOR:
                damage += player.getAbility().getStrength() * 2;
                break;
            case WIZARD:
                damage += player.getAbility().getIntelligence() * 3;
                break;
            case TAOIST:
                damage += player.getAbility().getIntelligence() * 2;
                break;
        }
        
        // 随机浮动 ±20%
        int variance = damage * 20 / 100;
        damage = damage - variance + (int)(Math.random() * variance * 2);
        
        return Math.max(1, damage);
    }
    
    /**
     * 处理怪物被击杀
     */
    private void handleMonsterKilled(Player player, Monster monster) {
        // 给予经验值
        int exp = monster.getExpReward();
        player.gainExperience(exp);
        
        // 掉落物品
        monster.dropItems(player.getMapName(), monster.getX(), monster.getY());
        
        log.debug("玩家 {} 击杀了 {}，获得经验: {}", 
                player.getName(), monster.getName(), exp);
    }
    
    /**
     * 检查是否可以攻击玩家
     */
    private boolean canAttackPlayer(Player attacker, Player target) {
        // 检查PK模式
        if (attacker.getPkMode() == 0) { // 和平模式
            return false;
        }
        
        // 检查行会关系
        if (attacker.getGuildName() != null && 
            attacker.getGuildName().equals(target.getGuildName())) {
            return false; // 不能攻击同行会成员
        }
        
        // 检查师徒关系
        if (attacker.getMasterName() != null && 
            attacker.getMasterName().equals(target.getName())) {
            return false; // 不能攻击师父
        }
        
        if (target.getMasterName() != null && 
            target.getMasterName().equals(attacker.getName())) {
            return false; // 不能攻击徒弟
        }
        
        return true;
    }
    
    /**
     * 获取地图对象
     */
    private GameMap getGameMap(String mapName) {
        // 从MapService获取地图对象
        try {
            return mapService.getMap(mapName);
        } catch (Exception e) {
            log.error("获取地图失败: {}", mapName, e);
            return null;
        }
    }
    
    /**
     * 处理治疗技能
     * 
     * @param player 玩家
     * @param attributes 技能属性
     */
    private void processHealSkill(Player player, Skill.SkillAttributes attributes) {
        int healAmount = attributes.getPower();
        player.restoreHp(healAmount);
        log.debug("玩家 {} 释放治疗技能，治疗量: {}", player.getName(), healAmount);
    }
    
    /**
     * 处理增益技能
     * 
     * @param player 玩家
     * @param attributes 技能属性
     */
    private void processBuffSkill(Player player, Skill.SkillAttributes attributes) {
        // 创建增益效果
        BuffEffect buffEffect = new BuffEffect();
        buffEffect.setType(BuffType.STAT_BOOST);
        buffEffect.setDuration(attributes.getDuration() * 1000); // 转换为毫秒
        buffEffect.setStartTime(System.currentTimeMillis());
        
        // 根据增益类型设置效果
        switch (attributes.getSubType()) {
            case 1: // 攻击力提升
                buffEffect.setStrengthBonus(attributes.getPower());
                buffEffect.setDescription("攻击力提升 " + attributes.getPower() + " 点");
                break;
            case 2: // 防御力提升
                buffEffect.setDefenseBonus(attributes.getPower());
                buffEffect.setDescription("防御力提升 " + attributes.getPower() + " 点");
                break;
            case 3: // 魔法力提升
                buffEffect.setIntelligenceBonus(attributes.getPower());
                buffEffect.setDescription("魔法力提升 " + attributes.getPower() + " 点");
                break;
            case 4: // 敏捷提升
                buffEffect.setAgilityBonus(attributes.getPower());
                buffEffect.setDescription("敏捷提升 " + attributes.getPower() + " 点");
                break;
            case 5: // 生命恢复
                buffEffect.setType(BuffType.HP_REGEN);
                buffEffect.setHpRegenRate(attributes.getPower());
                buffEffect.setDescription("每秒恢复 " + attributes.getPower() + " 点生命值");
                break;
            case 6: // 魔法恢复
                buffEffect.setType(BuffType.MP_REGEN);
                buffEffect.setMpRegenRate(attributes.getPower());
                buffEffect.setDescription("每秒恢复 " + attributes.getPower() + " 点魔法值");
                break;
            case 7: // 移动速度提升
                buffEffect.setType(BuffType.SPEED_BOOST);
                buffEffect.setSpeedBonus(attributes.getPower());
                buffEffect.setDescription("移动速度提升 " + attributes.getPower() + "%");
                break;
            default:
                buffEffect.setDescription("未知增益效果");
                break;
        }
        
        // 应用增益效果
        player.addBuffEffect(buffEffect);
        
        log.debug("玩家 {} 释放增益技能: {}", player.getName(), buffEffect.getDescription());
    }
    
    /**
     * 处理减益技能
     * 
     * @param player 玩家
     * @param attributes 技能属性
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void processDebuffSkill(Player player, Skill.SkillAttributes attributes, int targetX, int targetY) {
        // 获取目标
        GameMap gameMap = getGameMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        BaseObject target = gameMap.getObjectAt(targetX, targetY);
        if (target == null) {
            return;
        }
        
        // 创建减益效果
        DebuffEffect debuffEffect = new DebuffEffect();
        debuffEffect.setDuration(attributes.getDuration() * 1000);
        debuffEffect.setStartTime(System.currentTimeMillis());
        debuffEffect.setCaster(player.getName());
        
        // 根据减益类型设置效果
        switch (attributes.getSubType()) {
            case 1: // 中毒
                debuffEffect.setType(DebuffType.POISON);
                debuffEffect.setDamagePerSecond(attributes.getPower());
                debuffEffect.setDescription("中毒：每秒失去 " + attributes.getPower() + " 点生命值");
                break;
            case 2: // 缓慢
                debuffEffect.setType(DebuffType.SLOW);
                debuffEffect.setSpeedReduction(attributes.getPower());
                debuffEffect.setDescription("缓慢：移动速度降低 " + attributes.getPower() + "%");
                break;
            case 3: // 虚弱
                debuffEffect.setType(DebuffType.WEAKNESS);
                debuffEffect.setAttackReduction(attributes.getPower());
                debuffEffect.setDescription("虚弱：攻击力降低 " + attributes.getPower() + " 点");
                break;
            case 4: // 沉默
                debuffEffect.setType(DebuffType.SILENCE);
                debuffEffect.setDescription("沉默：无法使用技能");
                break;
            case 5: // 眩晕
                debuffEffect.setType(DebuffType.STUN);
                debuffEffect.setDescription("眩晕：无法行动");
                break;
            case 6: // 诅咒
                debuffEffect.setType(DebuffType.CURSE);
                debuffEffect.setAllStatsReduction(attributes.getPower());
                debuffEffect.setDescription("诅咒：所有属性降低 " + attributes.getPower() + " 点");
                break;
            default:
                debuffEffect.setDescription("未知减益效果");
                break;
        }
        
        // 应用减益效果
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            if (canAttackPlayer(player, targetPlayer)) {
                targetPlayer.addDebuffEffect(debuffEffect);
                log.debug("玩家 {} 对玩家 {} 施加减益: {}", 
                        player.getName(), targetPlayer.getName(), debuffEffect.getDescription());
            }
        } else if (target instanceof Monster) {
            Monster monster = (Monster) target;
            monster.addDebuffEffect(debuffEffect);
            log.debug("玩家 {} 对怪物 {} 施加减益: {}", 
                    player.getName(), monster.getName(), debuffEffect.getDescription());
        }
        
        log.debug("玩家 {} 释放减益技能，目标: ({}, {})", player.getName(), targetX, targetY);
    }
    
    /**
     * 处理召唤技能
     * 
     * @param player 玩家
     * @param attributes 技能属性
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void processSummonSkill(Player player, Skill.SkillAttributes attributes, int targetX, int targetY) {
        // 检查召唤位置是否有效
        GameMap gameMap = getGameMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        // 检查目标位置是否可以召唤
        if (!gameMap.isValidPosition(targetX, targetY)) {
            log.debug("召唤位置无效: ({}, {})", targetX, targetY);
            return;
        }
        
        // 检查距离
        double distance = Math.sqrt(Math.pow(targetX - player.getX(), 2) + Math.pow(targetY - player.getY(), 2));
        if (distance > attributes.getRange()) {
            log.debug("召唤距离过远: {}", distance);
            return;
        }
        
        // 根据召唤类型创建召唤物
        SummonedCreature summon = null;
        switch (attributes.getSubType()) {
            case 1: // 召唤骷髅
                summon = createSkeletonSummon(player, attributes, targetX, targetY);
                break;
            case 2: // 召唤僵尸
                summon = createZombieSummon(player, attributes, targetX, targetY);
                break;
            case 3: // 召唤神兽
                summon = createBeastSummon(player, attributes, targetX, targetY);
                break;
            case 4: // 召唤火魔
                summon = createFireDemonSummon(player, attributes, targetX, targetY);
                break;
            default:
                log.warn("未知召唤类型: {}", attributes.getSubType());
                return;
        }
        
        if (summon != null) {
            // 检查玩家召唤物数量限制
            int currentSummons = player.getSummonedCreatures().size();
            int maxSummons = attributes.getMaxTargets();
            
            if (currentSummons >= maxSummons) {
                // 移除最早的召唤物
                SummonedCreature oldestSummon = player.getSummonedCreatures().get(0);
                oldestSummon.dismiss();
                player.getSummonedCreatures().remove(0);
            }
            
            // 添加召唤物到游戏世界
            gameMap.addObject(summon);
            player.getSummonedCreatures().add(summon);
            
            log.debug("玩家 {} 召唤了 {} 到位置 ({}, {})", 
                    player.getName(), summon.getName(), targetX, targetY);
        }
        
        log.debug("玩家 {} 释放召唤技能，目标: ({}, {})", player.getName(), targetX, targetY);
    }
    
    /**
     * 创建骷髅召唤物
     */
    private SummonedCreature createSkeletonSummon(Player player, Skill.SkillAttributes attributes, int x, int y) {
        SummonedCreature skeleton = new SummonedCreature();
        skeleton.setName("骷髅战士");
        skeleton.setOwner(player);
        skeleton.setX(x);
        skeleton.setY(y);
        skeleton.setMapName(player.getMapName());
        skeleton.setHp(attributes.getPower() * 5);
        skeleton.setMaxHp(attributes.getPower() * 5);
        skeleton.setAttack(attributes.getPower());
        skeleton.setDefense(attributes.getPower() / 2);
        skeleton.setDuration(attributes.getDuration() * 1000);
        skeleton.setSummonType(SummonType.SKELETON);
        
        return skeleton;
    }
    
    /**
     * 创建僵尸召唤物
     */
    private SummonedCreature createZombieSummon(Player player, Skill.SkillAttributes attributes, int x, int y) {
        SummonedCreature zombie = new SummonedCreature();
        zombie.setName("僵尸");
        zombie.setOwner(player);
        zombie.setX(x);
        zombie.setY(y);
        zombie.setMapName(player.getMapName());
        zombie.setHp(attributes.getPower() * 8);
        zombie.setMaxHp(attributes.getPower() * 8);
        zombie.setAttack(attributes.getPower() * 2);
        zombie.setDefense(attributes.getPower());
        zombie.setDuration(attributes.getDuration() * 1000);
        zombie.setSummonType(SummonType.ZOMBIE);
        
        return zombie;
    }
    
    /**
     * 创建神兽召唤物
     */
    private SummonedCreature createBeastSummon(Player player, Skill.SkillAttributes attributes, int x, int y) {
        SummonedCreature beast = new SummonedCreature();
        beast.setName("神兽");
        beast.setOwner(player);
        beast.setX(x);
        beast.setY(y);
        beast.setMapName(player.getMapName());
        beast.setHp(attributes.getPower() * 12);
        beast.setMaxHp(attributes.getPower() * 12);
        beast.setAttack(attributes.getPower() * 3);
        beast.setDefense(attributes.getPower() * 2);
        beast.setDuration(attributes.getDuration() * 1000);
        beast.setSummonType(SummonType.BEAST);
        
        return beast;
    }
    
    /**
     * 创建火魔召唤物
     */
    private SummonedCreature createFireDemonSummon(Player player, Skill.SkillAttributes attributes, int x, int y) {
        SummonedCreature fireDemon = new SummonedCreature();
        fireDemon.setName("火魔");
        fireDemon.setOwner(player);
        fireDemon.setX(x);
        fireDemon.setY(y);
        fireDemon.setMapName(player.getMapName());
        fireDemon.setHp(attributes.getPower() * 6);
        fireDemon.setMaxHp(attributes.getPower() * 6);
        fireDemon.setAttack(attributes.getPower() * 4);
        fireDemon.setDefense(attributes.getPower() / 2);
        fireDemon.setDuration(attributes.getDuration() * 1000);
        fireDemon.setSummonType(SummonType.FIRE_DEMON);
        
        return fireDemon;
    }
    
    /**
     * 处理传送技能
     * 
     * @param player 玩家
     * @param attributes 技能属性
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void processTeleportSkill(Player player, Skill.SkillAttributes attributes, int targetX, int targetY) {
        // 检查传送距离
        double distance = Math.sqrt(Math.pow(targetX - player.getX(), 2) + Math.pow(targetY - player.getY(), 2));
        if (distance > attributes.getRange()) {
            log.debug("传送距离过远: {} > {}", distance, attributes.getRange());
            return;
        }
        
        // 检查目标位置是否有效
        GameMap gameMap = getGameMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        // 检查目标位置是否可以传送
        if (!gameMap.isValidPosition(targetX, targetY)) {
            log.debug("传送位置无效: ({}, {})", targetX, targetY);
            return;
        }
        
        // 检查目标位置是否被占用
        BaseObject objectAtTarget = gameMap.getObjectAt(targetX, targetY);
        if (objectAtTarget != null) {
            log.debug("传送位置被占用: ({}, {})", targetX, targetY);
            return;
        }
        
        // 根据传送类型处理
        switch (attributes.getSubType()) {
            case 1: // 瞬间移动
                performInstantTeleport(player, targetX, targetY);
                break;
            case 2: // 随机传送
                performRandomTeleport(player, attributes.getRange());
                break;
            case 3: // 回城卷轴
                performTownTeleport(player);
                break;
            case 4: // 地牢逃脱
                performDungeonEscape(player);
                break;
            default:
                performInstantTeleport(player, targetX, targetY);
                break;
        }
        
        log.debug("玩家 {} 释放传送技能到: ({}, {})", player.getName(), targetX, targetY);
    }
    
    /**
     * 瞬间移动
     */
    private void performInstantTeleport(Player player, int targetX, int targetY) {
        // 从当前位置移除玩家
        GameMap currentMap = getGameMap(player.getMapName());
        if (currentMap != null) {
            currentMap.removeObject(player);
        }
        
        // 更新玩家位置
        player.setX(targetX);
        player.setY(targetY);
        
        // 添加到新位置
        if (currentMap != null) {
            currentMap.addObject(player);
        }
        
        log.debug("玩家 {} 瞬间移动到: ({}, {})", player.getName(), targetX, targetY);
    }
    
    /**
     * 随机传送
     */
    private void performRandomTeleport(Player player, int range) {
        GameMap gameMap = getGameMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        // 尝试找到有效的随机位置
        for (int attempts = 0; attempts < 10; attempts++) {
            int randomX = player.getX() + (int)(Math.random() * range * 2) - range;
            int randomY = player.getY() + (int)(Math.random() * range * 2) - range;
            
            if (gameMap.isValidPosition(randomX, randomY) && 
                gameMap.getObjectAt(randomX, randomY) == null) {
                performInstantTeleport(player, randomX, randomY);
                log.debug("玩家 {} 随机传送到: ({}, {})", player.getName(), randomX, randomY);
                return;
            }
        }
        
        log.debug("玩家 {} 随机传送失败，找不到有效位置", player.getName());
    }
    
    /**
     * 回城传送
     */
    private void performTownTeleport(Player player) {
        // 根据职业设置不同的回城位置
        String townMap = "比奇城";
        int townX = 330;
        int townY = 330;
        
        switch (player.getJob()) {
            case WARRIOR:
                townX = 330;
                townY = 330;
                break;
            case WIZARD:
                townX = 335;
                townY = 330;
                break;
            case TAOIST:
                townX = 340;
                townY = 330;
                break;
        }
        
        // 跨地图传送
        GameMap currentMap = getGameMap(player.getMapName());
        if (currentMap != null) {
            currentMap.removeObject(player);
        }
        
        player.setMapName(townMap);
        player.setX(townX);
        player.setY(townY);
        
        GameMap townMapObj = getGameMap(townMap);
        if (townMapObj != null) {
            townMapObj.addObject(player);
        }
        
        log.debug("玩家 {} 回城传送到: {} ({}, {})", player.getName(), townMap, townX, townY);
    }
    
    /**
     * 地牢逃脱
     */
    private void performDungeonEscape(Player player) {
        // 检查是否在地牢中
        if (!isDungeonMap(player.getMapName())) {
            log.debug("玩家 {} 不在地牢中，无法使用地牢逃脱", player.getName());
            return;
        }
        
        // 传送到地牢入口
        String exitMap = "比奇城";
        int exitX = 330;
        int exitY = 330;
        
        // 跨地图传送
        GameMap currentMap = getGameMap(player.getMapName());
        if (currentMap != null) {
            currentMap.removeObject(player);
        }
        
        player.setMapName(exitMap);
        player.setX(exitX);
        player.setY(exitY);
        
        GameMap exitMapObj = getGameMap(exitMap);
        if (exitMapObj != null) {
            exitMapObj.addObject(player);
        }
        
        log.debug("玩家 {} 地牢逃脱到: {} ({}, {})", player.getName(), exitMap, exitX, exitY);
    }
    
    /**
     * 检查是否为地牢地图
     */
    private boolean isDungeonMap(String mapName) {
        return mapName.contains("地牢") || mapName.contains("洞穴") || mapName.contains("矿洞");
    }
    
    /**
     * 创建战士技能
     */
    private void createWarriorSkills() {
        // 基本剑术
        Skill basicSword = new Skill(1001, "基本剑术", Skill.SkillType.ATTACK, Job.WARRIOR);
        basicSword.setRequiredLevel(1);
        basicSword.setMaxLevel(3);
        basicSword.setDescription("战士的基本剑术，增加攻击力");
        addSkillTemplate(basicSword);
        
        // 攻杀剑术
        Skill attackSword = new Skill(1002, "攻杀剑术", Skill.SkillType.ATTACK, Job.WARRIOR);
        attackSword.setRequiredLevel(7);
        attackSword.setMaxLevel(3);
        attackSword.setDescription("强力的剑术攻击，造成大量伤害");
        addSkillTemplate(attackSword);
        
        // 刺杀剑术
        Skill thrustSword = new Skill(1003, "刺杀剑术", Skill.SkillType.ATTACK, Job.WARRIOR);
        thrustSword.setRequiredLevel(25);
        thrustSword.setMaxLevel(3);
        thrustSword.setDescription("穿透攻击，可以攻击直线上的多个目标");
        addSkillTemplate(thrustSword);
        
        // 半月弯刀
        Skill halfMoon = new Skill(1004, "半月弯刀", Skill.SkillType.ATTACK, Job.WARRIOR);
        halfMoon.setRequiredLevel(28);
        halfMoon.setMaxLevel(3);
        halfMoon.setDescription("范围攻击，可以攻击周围的敌人");
        addSkillTemplate(halfMoon);
        
        // 烈火剑法
        Skill fireSword = new Skill(1005, "烈火剑法", Skill.SkillType.ATTACK, Job.WARRIOR);
        fireSword.setRequiredLevel(35);
        fireSword.setMaxLevel(3);
        fireSword.setDescription("附带火焰伤害的强力剑术");
        addSkillTemplate(fireSword);
        
        log.debug("创建战士技能完成");
    }
    
    /**
     * 创建法师技能
     */
    private void createWizardSkills() {
        // 小火球
        Skill fireball = new Skill(2001, "小火球", Skill.SkillType.ATTACK, Job.WIZARD);
        fireball.setRequiredLevel(1);
        fireball.setMaxLevel(3);
        fireball.setDescription("法师的基本攻击魔法");
        addSkillTemplate(fireball);
        
        // 大火球
        Skill bigFireball = new Skill(2002, "大火球", Skill.SkillType.ATTACK, Job.WIZARD);
        bigFireball.setRequiredLevel(15);
        bigFireball.setMaxLevel(3);
        bigFireball.setDescription("威力更强的火球术");
        addSkillTemplate(bigFireball);
        
        // 火墙
        Skill fireWall = new Skill(2003, "火墙", Skill.SkillType.ATTACK, Job.WIZARD);
        fireWall.setRequiredLevel(24);
        fireWall.setMaxLevel(3);
        fireWall.setDescription("在地面创造火墙，持续伤害敌人");
        addSkillTemplate(fireWall);
        
        // 雷电术
        Skill lightning = new Skill(2004, "雷电术", Skill.SkillType.ATTACK, Job.WIZARD);
        lightning.setRequiredLevel(17);
        lightning.setMaxLevel(3);
        lightning.setDescription("召唤雷电攻击敌人");
        addSkillTemplate(lightning);
        
        // 冰咆哮
        Skill iceRoar = new Skill(2005, "冰咆哮", Skill.SkillType.ATTACK, Job.WIZARD);
        iceRoar.setRequiredLevel(31);
        iceRoar.setMaxLevel(3);
        iceRoar.setDescription("冰系魔法，造成冰冻效果");
        addSkillTemplate(iceRoar);
        
        log.debug("创建法师技能完成");
    }
    
    /**
     * 创建道士技能
     */
    private void createTaoistSkills() {
        // 治愈术
        Skill heal = new Skill(3001, "治愈术", Skill.SkillType.HEAL, Job.TAOIST);
        heal.setRequiredLevel(1);
        heal.setMaxLevel(3);
        heal.setDescription("恢复生命值的治疗魔法");
        addSkillTemplate(heal);
        
        // 精神力战法
        Skill spiritSword = new Skill(3002, "精神力战法", Skill.SkillType.ATTACK, Job.TAOIST);
        spiritSword.setRequiredLevel(9);
        spiritSword.setMaxLevel(3);
        spiritSword.setDescription("使用精神力进行攻击");
        addSkillTemplate(spiritSword);
        
        // 召唤骷髅
        Skill summonSkeleton = new Skill(3003, "召唤骷髅", Skill.SkillType.SUMMON, Job.TAOIST);
        summonSkeleton.setRequiredLevel(19);
        summonSkeleton.setMaxLevel(3);
        summonSkeleton.setDescription("召唤骷髅战士协助战斗");
        addSkillTemplate(summonSkeleton);
        
        // 施毒术
        Skill poison = new Skill(3004, "施毒术", Skill.SkillType.DEBUFF, Job.TAOIST);
        poison.setRequiredLevel(14);
        poison.setMaxLevel(3);
        poison.setDescription("给敌人施加毒素，持续造成伤害");
        addSkillTemplate(poison);
        
        // 灵魂火符
        Skill soulFireball = new Skill(3005, "灵魂火符", Skill.SkillType.ATTACK, Job.TAOIST);
        soulFireball.setRequiredLevel(18);
        soulFireball.setMaxLevel(3);
        soulFireball.setDescription("道士的火系攻击魔法");
        addSkillTemplate(soulFireball);
        
        log.debug("创建道士技能完成");
    }
    
    /**
     * 创建通用技能
     */
    private void createCommonSkills() {
        // 基本内功
        Skill basicInner = new Skill(9001, "基本内功", Skill.SkillType.BUFF, Job.ALL);
        basicInner.setRequiredLevel(1);
        basicInner.setMaxLevel(3);
        basicInner.setDescription("增加内力恢复速度");
        addSkillTemplate(basicInner);
        
        log.debug("创建通用技能完成");
    }
    
    /**
     * 建立职业技能索引
     */
    private void buildJobSkillIndex() {
        for (Job job : Job.values()) {
            List<Skill> skills = skillTemplates.values().stream()
                    .filter(skill -> skill.getRequiredJob() == job || skill.getRequiredJob() == Job.ALL)
                    .collect(Collectors.toList());
            jobSkillIndex.put(job, skills);
        }
        
        log.debug("建立职业技能索引完成");
    }
    
    /**
     * 添加技能模板
     * 
     * @param skill 技能模板
     */
    private void addSkillTemplate(Skill skill) {
        skillTemplates.put(skill.getId(), skill);
        skillNameIndex.put(skill.getName(), skill);
    }
} 