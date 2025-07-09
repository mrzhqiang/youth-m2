package com.mir2.service;

import com.mir2.core.model.Item;
import com.mir2.core.model.Player;
import com.mir2.core.model.GameMap;
import com.mir2.core.model.SummonedCreature;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.SummonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.Random;

/**
 * 物品服务类
 * 
 * <p>提供物品相关的业务逻辑处理。
 * 对应原M2Engine中的物品系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ItemService {
    
    /** 物品模板缓存 */
    private final Map<Integer, Item> itemTemplates = new ConcurrentHashMap<>();
    
    /** 物品名称索引 */
    private final Map<String, Item> itemNameIndex = new ConcurrentHashMap<>();
    
    /** 地图服务 */
    @Autowired
    private MapService mapService;
    
    /**
     * 初始化物品模板
     */
    public void initializeItemTemplates() {
        log.info("开始初始化物品模板...");
        
        // 创建武器模板
        createWeaponTemplates();
        
        // 创建防具模板
        createArmorTemplates();
        
        // 创建首饰模板
        createJewelryTemplates();
        
        // 创建药品模板
        createPotionTemplates();
        
        // 创建材料模板
        createMaterialTemplates();
        
        log.info("物品模板初始化完成，共加载 {} 个物品模板", itemTemplates.size());
    }
    
    /**
     * 根据物品ID获取物品模板
     * 
     * @param itemId 物品ID
     * @return 物品模板
     */
    public Item getItemTemplate(int itemId) {
        return itemTemplates.get(itemId);
    }
    
    /**
     * 根据物品名称获取物品模板
     * 
     * @param itemName 物品名称
     * @return 物品模板
     */
    public Item getItemTemplateByName(String itemName) {
        return itemNameIndex.get(itemName);
    }
    
    /**
     * 创建物品实例
     * 
     * @param itemId 物品ID
     * @return 物品实例
     */
    public Item createItem(int itemId) {
        Item template = getItemTemplate(itemId);
        if (template == null) {
            log.warn("未找到物品模板: {}", itemId);
            return null;
        }
        
        // 创建物品副本
        Item item = new Item(template.getItemId(), template.getName(), template.getType());
        copyItemProperties(template, item);
        
        // 随机生成属性（如果是装备）
        if (isEquipment(item)) {
            generateRandomAttributes(item);
        }
        
        log.debug("创建物品: {} [ID: {}]", item.getName(), item.getItemId());
        return item;
    }
    
    /**
     * 创建指定数量的物品
     * 
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 物品实例
     */
    public Item createItem(int itemId, int quantity) {
        Item item = createItem(itemId);
        if (item != null) {
            item.setQuantity(Math.max(1, quantity));
        }
        return item;
    }
    
    /**
     * 检查玩家是否可以使用物品
     * 
     * @param player 玩家
     * @param item 物品
     * @return 是否可以使用
     */
    public boolean canPlayerUseItem(Player player, Item item) {
        if (player == null || item == null) {
            return false;
        }
        
        // 检查等级要求
        if (player.getLevel() < item.getRequiredLevel()) {
            log.debug("玩家 {} 等级不足，无法使用 {} (需要等级: {})", 
                    player.getName(), item.getName(), item.getRequiredLevel());
            return false;
        }
        
        // 检查职业要求
        if (item.getRequiredJob() != Job.ALL && item.getRequiredJob() != player.getJob()) {
            log.debug("玩家 {} 职业不符，无法使用 {} (需要职业: {})", 
                    player.getName(), item.getName(), item.getRequiredJob().getName());
            return false;
        }
        
        // 检查性别要求
        if (item.getRequiredGender() != -1 && item.getRequiredGender() != player.getGender()) {
            log.debug("玩家 {} 性别不符，无法使用 {} (需要性别: {})", 
                    player.getName(), item.getName(), item.getRequiredGender() == 0 ? "男" : "女");
            return false;
        }
        
        return true;
    }
    
    /**
     * 使用物品
     * 
     * @param player 玩家
     * @param item 物品
     * @return 是否使用成功
     */
    public boolean useItem(Player player, Item item) {
        if (!canPlayerUseItem(player, item)) {
            return false;
        }
        
        boolean success = false;
        
        // 根据物品类型处理使用效果
        switch (item.getType()) {
            case POTION_HP:
                success = useHpPotion(player, item);
                break;
            case POTION_MP:
                success = useMpPotion(player, item);
                break;
            case POTION_SPECIAL:
                success = useSpecialPotion(player, item);
                break;
            case SCROLL:
                success = useScroll(player, item);
                break;
            case BOOK:
                success = useBook(player, item);
                break;
            default:
                log.warn("未处理的物品使用类型: {} [{}]", item.getName(), item.getType());
                break;
        }
        
        if (success) {
            // 减少物品数量
            item.setQuantity(item.getQuantity() - 1);
            log.info("玩家 {} 使用物品: {}", player.getName(), item.getName());
        }
        
        return success;
    }
    
    /**
     * 装备物品
     * 
     * @param player 玩家
     * @param item 装备
     * @return 是否装备成功
     */
    public boolean equipItem(Player player, Item item) {
        if (!canPlayerUseItem(player, item)) {
            return false;
        }
        
        if (!isEquipment(item)) {
            log.warn("物品 {} 不是装备，无法装备", item.getName());
            return false;
        }
        
        // 获取装备槽位
        int equipSlot = getEquipSlot(item);
        if (equipSlot == -1) {
            log.warn("物品 {} 没有对应的装备槽位", item.getName());
            return false;
        }
        
        // 检查装备槽位是否已有装备
        PlayerEquipment equipment = player.getEquipment();
        if (equipment == null) {
            equipment = new PlayerEquipment();
            player.setEquipment(equipment);
        }
        
        Item currentEquip = equipment.getEquipment(equipSlot);
        if (currentEquip != null) {
            // 卸下当前装备到背包
            PlayerBag bag = player.getBag();
            if (bag != null && bag.addItem(currentEquip)) {
                log.info("玩家 {} 卸下装备 {} 到背包", player.getName(), currentEquip.getName());
            } else {
                log.warn("玩家 {} 背包空间不足，无法卸下装备 {}", player.getName(), currentEquip.getName());
                return false;
            }
        }
        
        // 装备新物品
        equipment.setEquipment(equipSlot, item);
        
        // 应用装备属性
        applyEquipmentAttributes(player, item, true);
        
        log.info("玩家 {} 装备物品: {}", player.getName(), item.getName());
        return true;
    }
    
    /**
     * 卸下装备
     * 
     * @param player 玩家
     * @param equipSlot 装备槽位
     * @return 是否卸下成功
     */
    public boolean unequipItem(Player player, int equipSlot) {
        PlayerEquipment equipment = player.getEquipment();
        if (equipment == null) {
            log.warn("玩家 {} 没有装备数据", player.getName());
            return false;
        }
        
        // 检查装备槽位是否有装备
        Item currentEquip = equipment.getEquipment(equipSlot);
        if (currentEquip == null) {
            log.warn("玩家 {} 装备槽位 {} 没有装备", player.getName(), equipSlot);
            return false;
        }
        
        // 检查背包是否有空间
        PlayerBag bag = player.getBag();
        if (bag == null) {
            bag = new PlayerBag();
            player.setBag(bag);
        }
        
        if (!bag.hasSpace()) {
            log.warn("玩家 {} 背包空间不足，无法卸下装备 {}", player.getName(), currentEquip.getName());
            return false;
        }
        
        // 移除装备属性效果
        applyEquipmentAttributes(player, currentEquip, false);
        
        // 卸下装备
        equipment.setEquipment(equipSlot, null);
        
        // 将装备放入背包
        if (bag.addItem(currentEquip)) {
            log.info("玩家 {} 卸下装备: {}", player.getName(), currentEquip.getName());
            return true;
        } else {
            // 如果背包添加失败，恢复装备
            equipment.setEquipment(equipSlot, currentEquip);
            applyEquipmentAttributes(player, currentEquip, true);
            log.error("玩家 {} 卸下装备失败: {}", player.getName(), currentEquip.getName());
            return false;
        }
    }
    
    /**
     * 获取指定类型的所有物品
     * 
     * @param itemType 物品类型
     * @return 物品列表
     */
    public List<Item> getItemsByType(Item.ItemType itemType) {
        return itemTemplates.values().stream()
                .filter(item -> item.getType() == itemType)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取指定职业可用的物品
     * 
     * @param job 职业
     * @return 物品列表
     */
    public List<Item> getItemsByJob(Job job) {
        return itemTemplates.values().stream()
                .filter(item -> item.getRequiredJob() == Job.ALL || item.getRequiredJob() == job)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取指定等级范围的物品
     * 
     * @param minLevel 最低等级
     * @param maxLevel 最高等级
     * @return 物品列表
     */
    public List<Item> getItemsByLevelRange(int minLevel, int maxLevel) {
        return itemTemplates.values().stream()
                .filter(item -> item.getRequiredLevel() >= minLevel && item.getRequiredLevel() <= maxLevel)
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索物品
     * 
     * @param keyword 关键词
     * @return 匹配的物品列表
     */
    public List<Item> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new CopyOnWriteArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return itemTemplates.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerKeyword) ||
                              item.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否为装备
     * 
     * @param item 物品
     * @return 是否为装备
     */
    private boolean isEquipment(Item item) {
        Item.ItemType type = item.getType();
        return type == Item.ItemType.WEAPON || type == Item.ItemType.ARMOR || 
               type == Item.ItemType.HELMET || type == Item.ItemType.NECKLACE ||
               type == Item.ItemType.RING || type == Item.ItemType.BRACELET ||
               type == Item.ItemType.BOOTS || type == Item.ItemType.BELT ||
               type == Item.ItemType.CHARM;
    }
    
    /**
     * 复制物品属性
     * 
     * @param source 源物品
     * @param target 目标物品
     */
    private void copyItemProperties(Item source, Item target) {
        target.setShape(source.getShape());
        target.setWeight(source.getWeight());
        target.setPrice(source.getPrice());
        target.setDescription(source.getDescription());
        target.setStackable(source.isStackable());
        target.setDroppable(source.isDroppable());
        target.setTradeable(source.isTradeable());
        target.setStorable(source.isStorable());
        target.setMaxQuantity(source.getMaxQuantity());
        target.setQuantity(1);
        target.setDurability(source.getMaxDurability());
        target.setMaxDurability(source.getMaxDurability());
        target.setRequiredLevel(source.getRequiredLevel());
        target.setRequiredJob(source.getRequiredJob());
        target.setRequiredGender(source.getRequiredGender());
        target.setAttributes(new ConcurrentHashMap<>(source.getAttributes()));
        target.setSpecialEffects(new CopyOnWriteArrayList<>(source.getSpecialEffects()));
    }
    
    /**
     * 随机生成装备属性
     * 
     * @param item 装备
     */
    private void generateRandomAttributes(Item item) {
        if (!isEquipment(item)) {
            return;
        }
        
        Random random = new Random();
        Map<String, Integer> attributes = item.getAttributes();
        
        // 基础属性随机加成 (±20%)
        enhanceBaseAttributes(attributes, random);
        
        // 随机附加属性
        addRandomAttributes(attributes, random);
        
        // 随机幸运值
        if (random.nextInt(100) < 10) { // 10%概率
            attributes.put("lucky", random.nextInt(7) + 1); // 1-7幸运
        }
        
        // 随机诅咒值
        if (random.nextInt(100) < 5) { // 5%概率
            attributes.put("cursed", random.nextInt(7) + 1); // 1-7诅咒
        }
        
        // 随机持久度浮动
        int baseDurability = item.getMaxDurability();
        int durabilityVariance = baseDurability * 20 / 100; // ±20%
        int newDurability = baseDurability - durabilityVariance + random.nextInt(durabilityVariance * 2);
        item.setMaxDurability(Math.max(1, newDurability));
        item.setDurability(item.getMaxDurability());
        
        log.debug("为装备 {} 生成随机属性: {}", item.getName(), attributes);
    }
    
    /**
     * 增强基础属性
     */
    private void enhanceBaseAttributes(Map<String, Integer> attributes, Random random) {
        String[] baseAttrs = {"dc", "mc", "sc", "ac", "mac", "hp", "mp"};
        
        for (String attr : baseAttrs) {
            if (attributes.containsKey(attr)) {
                int baseValue = attributes.get(attr);
                if (baseValue > 0) {
                    int variance = baseValue * 20 / 100; // ±20%
                    int newValue = baseValue - variance + random.nextInt(variance * 2);
                    attributes.put(attr, Math.max(1, newValue));
                }
            }
        }
    }
    
    /**
     * 添加随机属性
     */
    private void addRandomAttributes(Map<String, Integer> attributes, Random random) {
        // 随机属性池
        String[] randomAttrs = {
            "accuracy", "dodge", "hit_rate", "magic_resistance", 
            "poison_resistance", "hp_regen", "mp_regen", "exp_bonus"
        };
        
        // 随机添加1-3个属性
        int numAttributes = random.nextInt(3) + 1;
        
        for (int i = 0; i < numAttributes; i++) {
            String attr = randomAttrs[random.nextInt(randomAttrs.length)];
            if (!attributes.containsKey(attr)) {
                int value = random.nextInt(10) + 1; // 1-10
                attributes.put(attr, value);
            }
        }
    }
    
    /**
     * 使用血药
     * 
     * @param player 玩家
     * @param item 血药
     * @return 是否使用成功
     */
    private boolean useHpPotion(Player player, Item item) {
        int healAmount = item.getAttributes().getOrDefault("heal_hp", 0);
        if (healAmount > 0) {
            int oldHp = player.getHp();
            player.setHp(Math.min(player.getMaxHp(), player.getHp() + healAmount));
            int actualHeal = player.getHp() - oldHp;
            
            log.debug("玩家 {} 使用血药恢复 {} 点生命值", player.getName(), actualHeal);
            return actualHeal > 0;
        }
        return false;
    }
    
    /**
     * 使用蓝药
     * 
     * @param player 玩家
     * @param item 蓝药
     * @return 是否使用成功
     */
    private boolean useMpPotion(Player player, Item item) {
        int restoreAmount = item.getAttributes().getOrDefault("restore_mp", 0);
        if (restoreAmount > 0) {
            int oldMp = player.getMp();
            player.setMp(Math.min(player.getMaxMp(), player.getMp() + restoreAmount));
            int actualRestore = player.getMp() - oldMp;
            
            log.debug("玩家 {} 使用蓝药恢复 {} 点魔法值", player.getName(), actualRestore);
            return actualRestore > 0;
        }
        return false;
    }
    
    /**
     * 使用特殊药品
     * 
     * @param player 玩家
     * @param item 特殊药品
     * @return 是否使用成功
     */
    private boolean useSpecialPotion(Player player, Item item) {
        String itemName = item.getName();
        boolean success = false;
        
        switch (itemName) {
            case "太阳水":
                success = useSunWater(player, item);
                break;
            case "万年雪霜":
                success = useSnowFrost(player, item);
                break;
            case "强效金创药":
                success = useStrongHpPotion(player, item);
                break;
            case "强效魔法药":
                success = useStrongMpPotion(player, item);
                break;
            case "经验药水":
                success = useExpPotion(player, item);
                break;
            case "解毒剂":
                success = useAntidote(player, item);
                break;
            case "护身药水":
                success = useProtectionPotion(player, item);
                break;
            case "复活药水":
                success = useRevivePotion(player, item);
                break;
            case "传送药水":
                success = useTeleportPotion(player, item);
                break;
            case "幸运药水":
                success = useLuckyPotion(player, item);
                break;
            default:
                log.warn("未知的特殊药品: {}", itemName);
                break;
        }
        
        log.debug("玩家 {} 使用特殊药品: {} {}", player.getName(), item.getName(), 
                success ? "成功" : "失败");
        return success;
    }
    
    /**
     * 使用太阳水
     */
    private boolean useSunWater(Player player, Item item) {
        int healAmount = 100; // 恢复100点生命
        int oldHp = player.getHp();
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + healAmount));
        
        // 同时恢复少量魔法
        int mpRestore = 50;
        player.setMp(Math.min(player.getMaxMp(), player.getMp() + mpRestore));
        
        return player.getHp() > oldHp;
    }
    
    /**
     * 使用万年雪霜
     */
    private boolean useSnowFrost(Player player, Item item) {
        int mpRestore = 100; // 恢复100点魔法
        int oldMp = player.getMp();
        player.setMp(Math.min(player.getMaxMp(), player.getMp() + mpRestore));
        
        // 同时恢复少量生命
        int hpHeal = 50;
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + hpHeal));
        
        return player.getMp() > oldMp;
    }
    
    /**
     * 使用强效金创药
     */
    private boolean useStrongHpPotion(Player player, Item item) {
        int healAmount = 200; // 恢复200点生命
        int oldHp = player.getHp();
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + healAmount));
        return player.getHp() > oldHp;
    }
    
    /**
     * 使用强效魔法药
     */
    private boolean useStrongMpPotion(Player player, Item item) {
        int restoreAmount = 200; // 恢复200点魔法
        int oldMp = player.getMp();
        player.setMp(Math.min(player.getMaxMp(), player.getMp() + restoreAmount));
        return player.getMp() > oldMp;
    }
    
    /**
     * 使用经验药水
     */
    private boolean useExpPotion(Player player, Item item) {
        int expBonus = item.getAttributes().getOrDefault("exp_bonus", 1000);
        player.gainExperience(expBonus);
        log.info("玩家 {} 使用经验药水获得 {} 经验", player.getName(), expBonus);
        return true;
    }
    
    /**
     * 使用解毒剂
     */
    private boolean useAntidote(Player player, Item item) {
        // 移除中毒状态
        player.removeDebuffEffect("poison");
        log.info("玩家 {} 使用解毒剂，移除中毒状态", player.getName());
        return true;
    }
    
    /**
     * 使用护身药水
     */
    private boolean useProtectionPotion(Player player, Item item) {
        // 添加护身效果
        BuffEffect protection = new BuffEffect();
        protection.setType(BuffType.PROTECTION);
        protection.setDuration(300000); // 5分钟
        protection.setDefenseBonus(50);
        protection.setMagicDefenseBonus(50);
        protection.setDescription("护身效果：防御力+50，魔法防御+50");
        
        player.addBuffEffect(protection);
        log.info("玩家 {} 使用护身药水，获得护身效果", player.getName());
        return true;
    }
    
    /**
     * 使用复活药水
     */
    private boolean useRevivePotion(Player player, Item item) {
        if (player.getHp() > 0) {
            log.warn("玩家 {} 没有死亡，无法使用复活药水", player.getName());
            return false;
        }
        
        // 复活玩家
        player.setHp(player.getMaxHp() / 2); // 恢复50%生命
        player.setMp(player.getMaxMp() / 2); // 恢复50%魔法
        log.info("玩家 {} 使用复活药水复活", player.getName());
        return true;
    }
    
    /**
     * 使用传送药水
     */
    private boolean useTeleportPotion(Player player, Item item) {
        // 随机传送到安全区域
        String safeMap = "比奇城";
        int safeX = 330 + new Random().nextInt(10) - 5;
        int safeY = 330 + new Random().nextInt(10) - 5;
        
        player.setMapName(safeMap);
        player.setX(safeX);
        player.setY(safeY);
        
        log.info("玩家 {} 使用传送药水传送到安全区域", player.getName());
        return true;
    }
    
    /**
     * 使用幸运药水
     */
    private boolean useLuckyPotion(Player player, Item item) {
        // 添加幸运效果
        BuffEffect lucky = new BuffEffect();
        lucky.setType(BuffType.LUCKY);
        lucky.setDuration(600000); // 10分钟
        lucky.setLuckBonus(3);
        lucky.setDescription("幸运效果：幸运+3");
        
        player.addBuffEffect(lucky);
        log.info("玩家 {} 使用幸运药水，获得幸运效果", player.getName());
        return true;
    }
    
    /**
     * 使用卷轴
     * 
     * @param player 玩家
     * @param item 卷轴
     * @return 是否使用成功
     */
    private boolean useScroll(Player player, Item item) {
        String itemName = item.getName();
        boolean success = false;
        
        switch (itemName) {
            case "回城卷轴":
                success = useReturnScroll(player, item);
                break;
            case "随机传送卷轴":
                success = useRandomTeleportScroll(player, item);
                break;
            case "地牢逃脱卷轴":
                success = useDungeonEscapeScroll(player, item);
                break;
            case "修复卷轴":
                success = useRepairScroll(player, item);
                break;
            case "鉴定卷轴":
                success = useIdentifyScroll(player, item);
                break;
            case "祝福卷轴":
                success = useBlessScroll(player, item);
                break;
            case "诅咒卷轴":
                success = useCurseScroll(player, item);
                break;
            case "强化卷轴":
                success = useEnhanceScroll(player, item);
                break;
            case "复活卷轴":
                success = useReviveScroll(player, item);
                break;
            case "召唤卷轴":
                success = useSummonScroll(player, item);
                break;
            default:
                log.warn("未知的卷轴类型: {}", itemName);
                break;
        }
        
        log.debug("玩家 {} 使用卷轴: {} {}", player.getName(), item.getName(), 
                success ? "成功" : "失败");
        return success;
    }
    
    /**
     * 使用回城卷轴
     */
    private boolean useReturnScroll(Player player, Item item) {
        // 传送到主城
        String townMap = "比奇城";
        int townX = 330;
        int townY = 330;
        
        player.setMapName(townMap);
        player.setX(townX);
        player.setY(townY);
        
        log.info("玩家 {} 使用回城卷轴传送到主城", player.getName());
        return true;
    }
    
    /**
     * 使用随机传送卷轴
     */
    private boolean useRandomTeleportScroll(Player player, Item item) {
        // 在当前地图随机传送
        Random random = new Random();
        int newX = player.getX() + random.nextInt(20) - 10;
        int newY = player.getY() + random.nextInt(20) - 10;
        
        // 确保坐标在有效范围内
        newX = Math.max(50, Math.min(newX, 500));
        newY = Math.max(50, Math.min(newY, 500));
        
        player.setX(newX);
        player.setY(newY);
        
        log.info("玩家 {} 使用随机传送卷轴传送到 ({}, {})", player.getName(), newX, newY);
        return true;
    }
    
    /**
     * 使用地牢逃脱卷轴
     */
    private boolean useDungeonEscapeScroll(Player player, Item item) {
        // 检查是否在地牢中
        if (!player.getMapName().contains("地牢") && !player.getMapName().contains("洞穴")) {
            log.warn("玩家 {} 不在地牢中，无法使用地牢逃脱卷轴", player.getName());
            return false;
        }
        
        // 传送到地牢入口
        String exitMap = "比奇城";
        int exitX = 330;
        int exitY = 330;
        
        player.setMapName(exitMap);
        player.setX(exitX);
        player.setY(exitY);
        
        log.info("玩家 {} 使用地牢逃脱卷轴逃离地牢", player.getName());
        return true;
    }
    
    /**
     * 使用修复卷轴
     */
    private boolean useRepairScroll(Player player, Item item) {
        // 修复所有装备的持久度
        PlayerEquipment equipment = player.getEquipment();
        if (equipment == null) {
            log.warn("玩家 {} 没有装备需要修复", player.getName());
            return false;
        }
        
        boolean repaired = false;
        for (int slot = 0; slot < 12; slot++) { // 12个装备槽位
            Item equip = equipment.getEquipment(slot);
            if (equip != null && equip.getDurability() < equip.getMaxDurability()) {
                equip.setDurability(equip.getMaxDurability());
                repaired = true;
            }
        }
        
        if (repaired) {
            log.info("玩家 {} 使用修复卷轴修复所有装备", player.getName());
            return true;
        } else {
            log.warn("玩家 {} 没有装备需要修复", player.getName());
            return false;
        }
    }
    
    /**
     * 使用鉴定卷轴
     */
    private boolean useIdentifyScroll(Player player, Item item) {
        // 鉴定背包中的未鉴定物品
        PlayerBag bag = player.getBag();
        if (bag == null) {
            return false;
        }
        
        for (Item bagItem : bag.getItems()) {
            if (bagItem != null && !bagItem.isIdentified()) {
                bagItem.setIdentified(true);
                log.info("玩家 {} 鉴定了物品: {}", player.getName(), bagItem.getName());
                return true;
            }
        }
        
        log.warn("玩家 {} 没有未鉴定的物品", player.getName());
        return false;
    }
    
    /**
     * 使用祝福卷轴
     */
    private boolean useBlessScroll(Player player, Item item) {
        // 随机选择一件装备进行祝福
        PlayerEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return false;
        }
        
        List<Item> equipList = new ArrayList<>();
        for (int slot = 0; slot < 12; slot++) {
            Item equip = equipment.getEquipment(slot);
            if (equip != null) {
                equipList.add(equip);
            }
        }
        
        if (equipList.isEmpty()) {
            log.warn("玩家 {} 没有装备可以祝福", player.getName());
            return false;
        }
        
        // 随机选择一件装备
        Random random = new Random();
        Item targetEquip = equipList.get(random.nextInt(equipList.size()));
        
        // 增加幸运值
        Map<String, Integer> attrs = targetEquip.getAttributes();
        int currentLucky = attrs.getOrDefault("lucky", 0);
        attrs.put("lucky", Math.min(currentLucky + 1, 7)); // 最大7点幸运
        
        log.info("玩家 {} 使用祝福卷轴为装备 {} 增加幸运值", player.getName(), targetEquip.getName());
        return true;
    }
    
    /**
     * 使用诅咒卷轴
     */
    private boolean useCurseScroll(Player player, Item item) {
        // 随机选择一件装备进行诅咒
        PlayerEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return false;
        }
        
        List<Item> equipList = new ArrayList<>();
        for (int slot = 0; slot < 12; slot++) {
            Item equip = equipment.getEquipment(slot);
            if (equip != null) {
                equipList.add(equip);
            }
        }
        
        if (equipList.isEmpty()) {
            log.warn("玩家 {} 没有装备可以诅咒", player.getName());
            return false;
        }
        
        // 随机选择一件装备
        Random random = new Random();
        Item targetEquip = equipList.get(random.nextInt(equipList.size()));
        
        // 增加诅咒值
        Map<String, Integer> attrs = targetEquip.getAttributes();
        int currentCursed = attrs.getOrDefault("cursed", 0);
        attrs.put("cursed", Math.min(currentCursed + 1, 7)); // 最大7点诅咒
        
        log.info("玩家 {} 使用诅咒卷轴为装备 {} 增加诅咒值", player.getName(), targetEquip.getName());
        return true;
    }
    
    /**
     * 使用强化卷轴
     */
    private boolean useEnhanceScroll(Player player, Item item) {
        // 随机选择一件装备进行强化
        PlayerEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return false;
        }
        
        List<Item> equipList = new ArrayList<>();
        for (int slot = 0; slot < 12; slot++) {
            Item equip = equipment.getEquipment(slot);
            if (equip != null) {
                equipList.add(equip);
            }
        }
        
        if (equipList.isEmpty()) {
            log.warn("玩家 {} 没有装备可以强化", player.getName());
            return false;
        }
        
        // 随机选择一件装备
        Random random = new Random();
        Item targetEquip = equipList.get(random.nextInt(equipList.size()));
        
        // 随机强化属性
        Map<String, Integer> attrs = targetEquip.getAttributes();
        String[] enhanceAttrs = {"dc", "mc", "sc", "ac", "mac"};
        String attr = enhanceAttrs[random.nextInt(enhanceAttrs.length)];
        
        int currentValue = attrs.getOrDefault(attr, 0);
        attrs.put(attr, currentValue + random.nextInt(5) + 1); // 增加1-5点属性
        
        log.info("玩家 {} 使用强化卷轴强化装备 {} 的 {} 属性", 
                player.getName(), targetEquip.getName(), attr);
        return true;
    }
    
    /**
     * 使用复活卷轴
     */
    private boolean useReviveScroll(Player player, Item item) {
        if (player.getHp() > 0) {
            log.warn("玩家 {} 没有死亡，无法使用复活卷轴", player.getName());
            return false;
        }
        
        // 复活玩家
        player.setHp(player.getMaxHp());
        player.setMp(player.getMaxMp());
        
        log.info("玩家 {} 使用复活卷轴复活", player.getName());
        return true;
    }
    
    /**
     * 使用召唤卷轴
     */
    private boolean useSummonScroll(Player player, Item item) {
        // 召唤帮手
        log.info("玩家 {} 使用召唤卷轴召唤帮手", player.getName());
        
        // 检查召唤条件
        if (player.getSummonedCreatures().size() >= 3) {
            log.warn("玩家 {} 召唤数量已达上限", player.getName());
            return false;
        }
        
        // 获取地图
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap == null) {
            log.warn("玩家 {} 所在地图不存在", player.getName());
            return false;
        }
        
        // 找到合适的召唤位置
        int summonX = player.getX();
        int summonY = player.getY();
        
        for (int i = 1; i <= 3; i++) {
            for (int dx = -i; dx <= i; dx++) {
                for (int dy = -i; dy <= i; dy++) {
                    int testX = player.getX() + dx;
                    int testY = player.getY() + dy;
                    
                    if (gameMap.isValidPosition(testX, testY)) {
                        summonX = testX;
                        summonY = testY;
                        break;
                    }
                }
                if (summonX != player.getX() || summonY != player.getY()) {
                    break;
                }
            }
            if (summonX != player.getX() || summonY != player.getY()) {
                break;
            }
        }
        
        // 根据卷轴类型创建不同的召唤物
        SummonedCreature summon = new SummonedCreature();
        
        if (item.getName().contains("骷髅")) {
            summon.setName("骷髅战士");
            summon.setType(SummonType.SKELETON);
            summon.setLevel(player.getLevel());
            summon.setMaxHp(100 + player.getLevel() * 10);
            summon.setHp(summon.getMaxHp());
            summon.setAttack(15 + player.getLevel() * 3);
            summon.setDefense(8 + player.getLevel() * 2);
            summon.setMoveSpeed(300);
        } else if (item.getName().contains("神兽")) {
            summon.setName("召唤神兽");
            summon.setType(SummonType.BEAST);
            summon.setLevel(player.getLevel());
            summon.setMaxHp(200 + player.getLevel() * 20);
            summon.setHp(summon.getMaxHp());
            summon.setAttack(25 + player.getLevel() * 5);
            summon.setDefense(15 + player.getLevel() * 3);
            summon.setMoveSpeed(400);
        } else {
            // 默认召唤骷髅
            summon.setName("骷髅帮手");
            summon.setType(SummonType.SKELETON);
            summon.setLevel(player.getLevel());
            summon.setMaxHp(80 + player.getLevel() * 8);
            summon.setHp(summon.getMaxHp());
            summon.setAttack(12 + player.getLevel() * 2);
            summon.setDefense(6 + player.getLevel() * 1);
            summon.setMoveSpeed(300);
        }
        
        // 设置召唤物基本信息
        summon.setOwner(player.getName());
        summon.setX(summonX);
        summon.setY(summonY);
        summon.setMapName(player.getMapName());
        summon.setDirection(player.getDirection());
        summon.setCreateTime(System.currentTimeMillis());
        summon.setExpireTime(System.currentTimeMillis() + 300000); // 5分钟
        
        // 添加到地图
        gameMap.addObject(summon);
        
        // 添加到玩家召唤列表
        player.addSummonedCreature(summon);
        
        log.info("玩家 {} 使用召唤卷轴召唤了 {} 位置: ({}, {})", 
                player.getName(), summon.getName(), summonX, summonY);
        
        return true;
    }
    
    /**
     * 使用书籍
     * 
     * @param player 玩家
     * @param item 书籍
     * @return 是否使用成功
     */
    private boolean useBook(Player player, Item item) {
        String itemName = item.getName();
        boolean success = false;
        
        if (itemName.contains("技能书")) {
            success = useSkillBook(player, item);
        } else if (itemName.contains("经验书")) {
            success = useExpBook(player, item);
        } else if (itemName.contains("属性书")) {
            success = useAttributeBook(player, item);
        } else if (itemName.contains("知识书")) {
            success = useKnowledgeBook(player, item);
        } else {
            log.warn("未知的书籍类型: {}", itemName);
        }
        
        log.debug("玩家 {} 使用书籍: {} {}", player.getName(), item.getName(), 
                success ? "成功" : "失败");
        return success;
    }
    
    /**
     * 使用技能书
     */
    private boolean useSkillBook(Player player, Item item) {
        // 从物品名称中解析技能ID
        String skillName = item.getName().replace("技能书", "").trim();
        int skillId = getSkillIdByName(skillName);
        
        if (skillId == -1) {
            log.warn("无法解析技能书 {} 的技能ID", item.getName());
            return false;
        }
        
        // 检查玩家是否已经学会该技能
        if (player.hasSkill(skillId)) {
            log.warn("玩家 {} 已经学会技能 {}", player.getName(), skillName);
            return false;
        }
        
        // 学习技能
        if (player.learnSkill(skillId)) {
            log.info("玩家 {} 从技能书学会了技能: {}", player.getName(), skillName);
            return true;
        } else {
            log.warn("玩家 {} 学习技能 {} 失败", player.getName(), skillName);
            return false;
        }
    }
    
    /**
     * 使用经验书
     */
    private boolean useExpBook(Player player, Item item) {
        int expBonus = item.getAttributes().getOrDefault("exp_bonus", 5000);
        
        // 根据书籍名称设置经验值
        if (item.getName().contains("小经验书")) {
            expBonus = 1000;
        } else if (item.getName().contains("中经验书")) {
            expBonus = 5000;
        } else if (item.getName().contains("大经验书")) {
            expBonus = 10000;
        } else if (item.getName().contains("超级经验书")) {
            expBonus = 50000;
        }
        
        player.gainExperience(expBonus);
        log.info("玩家 {} 使用经验书获得 {} 经验", player.getName(), expBonus);
        return true;
    }
    
    /**
     * 使用属性书
     */
    private boolean useAttributeBook(Player player, Item item) {
        String itemName = item.getName();
        Player.PlayerAbility ability = player.getAbility();
        
        if (itemName.contains("力量书")) {
            ability.setStrength(ability.getStrength() + 1);
            log.info("玩家 {} 使用力量书，力量+1", player.getName());
        } else if (itemName.contains("敏捷书")) {
            ability.setAgility(ability.getAgility() + 1);
            log.info("玩家 {} 使用敏捷书，敏捷+1", player.getName());
        } else if (itemName.contains("体质书")) {
            ability.setConstitution(ability.getConstitution() + 1);
            log.info("玩家 {} 使用体质书，体质+1", player.getName());
        } else if (itemName.contains("智力书")) {
            ability.setIntelligence(ability.getIntelligence() + 1);
            log.info("玩家 {} 使用智力书，智力+1", player.getName());
        } else if (itemName.contains("综合属性书")) {
            ability.setStrength(ability.getStrength() + 1);
            ability.setAgility(ability.getAgility() + 1);
            ability.setConstitution(ability.getConstitution() + 1);
            ability.setIntelligence(ability.getIntelligence() + 1);
            log.info("玩家 {} 使用综合属性书，所有属性+1", player.getName());
        } else {
            log.warn("未知的属性书: {}", itemName);
            return false;
        }
        
        // 重新计算衍生属性
        player.recalculateStats();
        return true;
    }
    
    /**
     * 使用知识书
     */
    private boolean useKnowledgeBook(Player player, Item item) {
        String itemName = item.getName();
        
        if (itemName.contains("地图知识书")) {
            // 解锁地图信息
            player.unlockMap(item.getAttributes().getOrDefault("map_id", 0));
            log.info("玩家 {} 使用地图知识书解锁地图", player.getName());
        } else if (itemName.contains("怪物知识书")) {
            // 解锁怪物信息
            player.unlockMonsterInfo(item.getAttributes().getOrDefault("monster_id", 0));
            log.info("玩家 {} 使用怪物知识书解锁怪物信息", player.getName());
        } else if (itemName.contains("配方知识书")) {
            // 解锁制作配方
            player.unlockRecipe(item.getAttributes().getOrDefault("recipe_id", 0));
            log.info("玩家 {} 使用配方知识书解锁制作配方", player.getName());
        } else {
            log.warn("未知的知识书: {}", itemName);
            return false;
        }
        
        return true;
    }
    
    /**
     * 根据技能名称获取技能ID
     */
    private int getSkillIdByName(String skillName) {
        switch (skillName) {
            case "基础剑术": return 1001;
            case "攻杀剑术": return 1002;
            case "刺杀剑术": return 1003;
            case "半月弯刀": return 1004;
            case "烈火剑法": return 1005;
            case "小火球": return 2001;
            case "大火球": return 2002;
            case "火墙": return 2003;
            case "雷电术": return 2004;
            case "冰咆哮": return 2005;
            case "治愈术": return 3001;
            case "精神力战法": return 3002;
            case "施毒术": return 3003;
            case "灵魂火符": return 3004;
            case "召唤骷髅": return 3005;
            default: return -1;
        }
    }
    
    /**
     * 创建武器模板
     */
    private void createWeaponTemplates() {
        // 木剑
        Item woodenSword = new Item(1001, "木剑", Item.ItemType.WEAPON);
        woodenSword.setDescription("新手用的木制剑，攻击力较低");
        woodenSword.setRequiredLevel(1);
        woodenSword.setRequiredJob(Job.WARRIOR);
        woodenSword.getAttributes().put("attack_min", 1);
        woodenSword.getAttributes().put("attack_max", 4);
        woodenSword.setPrice(50);
        woodenSword.setWeight(10);
        addItemTemplate(woodenSword);
        
        // 铁剑
        Item ironSword = new Item(1002, "铁剑", Item.ItemType.WEAPON);
        ironSword.setDescription("普通的铁制剑，比木剑更锋利");
        ironSword.setRequiredLevel(7);
        ironSword.setRequiredJob(Job.WARRIOR);
        ironSword.getAttributes().put("attack_min", 5);
        ironSword.getAttributes().put("attack_max", 12);
        ironSword.setPrice(300);
        ironSword.setWeight(25);
        addItemTemplate(ironSword);
        
        // 魔法杖
        Item magicWand = new Item(1101, "魔法杖", Item.ItemType.WEAPON);
        magicWand.setDescription("法师专用的魔法杖，可以增强魔法威力");
        magicWand.setRequiredLevel(1);
        magicWand.setRequiredJob(Job.WIZARD);
        magicWand.getAttributes().put("magic_attack", 3);
        magicWand.getAttributes().put("accuracy", 2);
        magicWand.setPrice(80);
        magicWand.setWeight(5);
        addItemTemplate(magicWand);
        
        log.debug("创建武器模板完成");
    }
    
    /**
     * 创建防具模板
     */
    private void createArmorTemplates() {
        // 布衣
        Item clothArmor = new Item(2001, "布衣", Item.ItemType.ARMOR);
        clothArmor.setDescription("简单的布制衣服，提供基本防护");
        clothArmor.setRequiredLevel(1);
        clothArmor.getAttributes().put("defense", 2);
        clothArmor.setPrice(30);
        clothArmor.setWeight(5);
        addItemTemplate(clothArmor);
        
        // 皮甲
        Item leatherArmor = new Item(2002, "皮甲", Item.ItemType.ARMOR);
        leatherArmor.setDescription("兽皮制成的轻甲，防御力中等");
        leatherArmor.setRequiredLevel(5);
        leatherArmor.getAttributes().put("defense", 5);
        leatherArmor.setPrice(150);
        leatherArmor.setWeight(15);
        addItemTemplate(leatherArmor);
        
        log.debug("创建防具模板完成");
    }
    
    /**
     * 创建首饰模板
     */
    private void createJewelryTemplates() {
        // 铜戒指
        Item copperRing = new Item(3001, "铜戒指", Item.ItemType.RING);
        copperRing.setDescription("普通的铜制戒指");
        copperRing.setRequiredLevel(1);
        copperRing.getAttributes().put("accuracy", 1);
        copperRing.setPrice(100);
        copperRing.setWeight(1);
        addItemTemplate(copperRing);
        
        log.debug("创建首饰模板完成");
    }
    
    /**
     * 创建药品模板
     */
    private void createPotionTemplates() {
        // 小红药
        Item smallHpPotion = new Item(4001, "小红药", Item.ItemType.POTION_HP);
        smallHpPotion.setDescription("恢复少量生命值");
        smallHpPotion.setStackable(true);
        smallHpPotion.setMaxQuantity(100);
        smallHpPotion.getAttributes().put("heal_hp", 50);
        smallHpPotion.setPrice(10);
        smallHpPotion.setWeight(1);
        addItemTemplate(smallHpPotion);
        
        // 大红药
        Item largeHpPotion = new Item(4002, "大红药", Item.ItemType.POTION_HP);
        largeHpPotion.setDescription("恢复大量生命值");
        largeHpPotion.setStackable(true);
        largeHpPotion.setMaxQuantity(100);
        largeHpPotion.getAttributes().put("heal_hp", 200);
        largeHpPotion.setPrice(50);
        largeHpPotion.setWeight(1);
        addItemTemplate(largeHpPotion);
        
        // 小蓝药
        Item smallMpPotion = new Item(4101, "小蓝药", Item.ItemType.POTION_MP);
        smallMpPotion.setDescription("恢复少量魔法值");
        smallMpPotion.setStackable(true);
        smallMpPotion.setMaxQuantity(100);
        smallMpPotion.getAttributes().put("restore_mp", 30);
        smallMpPotion.setPrice(15);
        smallMpPotion.setWeight(1);
        addItemTemplate(smallMpPotion);
        
        log.debug("创建药品模板完成");
    }
    
    /**
     * 创建材料模板
     */
    private void createMaterialTemplates() {
        // 铁矿石
        Item ironOre = new Item(5001, "铁矿石", Item.ItemType.MATERIAL);
        ironOre.setDescription("制作铁制装备的材料");
        ironOre.setStackable(true);
        ironOre.setMaxQuantity(1000);
        ironOre.setPrice(5);
        ironOre.setWeight(2);
        addItemTemplate(ironOre);
        
        log.debug("创建材料模板完成");
    }
    
    /**
     * 添加物品模板
     * 
     * @param item 物品模板
     */
    private void addItemTemplate(Item item) {
        itemTemplates.put(item.getItemId(), item);
        itemNameIndex.put(item.getName(), item);
    }
    
    /**
     * 获取装备槽位
     */
    private int getEquipSlot(Item item) {
        switch (item.getType()) {
            case WEAPON: return 0;      // 武器
            case ARMOR: return 1;       // 衣服
            case HELMET: return 2;      // 头盔
            case NECKLACE: return 3;    // 项链
            case RING: return 4;        // 戒指
            case BRACELET: return 5;    // 手镯
            case BOOTS: return 6;       // 靴子
            case BELT: return 7;        // 腰带
            case CHARM: return 8;       // 护身符
            default: return -1;
        }
    }
    
    /**
     * 应用装备属性
     */
    private void applyEquipmentAttributes(Player player, Item item, boolean apply) {
        if (item == null) return;
        
        Map<String, Integer> attributes = item.getAttributes();
        int multiplier = apply ? 1 : -1;
        
        // 应用基础属性
        if (attributes.containsKey("dc")) {
            player.getAbility().setDamage(player.getAbility().getDamage() + attributes.get("dc") * multiplier);
        }
        if (attributes.containsKey("mc")) {
            player.getAbility().setMagicDamage(player.getAbility().getMagicDamage() + attributes.get("mc") * multiplier);
        }
        if (attributes.containsKey("sc")) {
            player.getAbility().setTaoismDamage(player.getAbility().getTaoismDamage() + attributes.get("sc") * multiplier);
        }
        if (attributes.containsKey("ac")) {
            player.getAbility().setDefense(player.getAbility().getDefense() + attributes.get("ac") * multiplier);
        }
        if (attributes.containsKey("mac")) {
            player.getAbility().setMagicDefense(player.getAbility().getMagicDefense() + attributes.get("mac") * multiplier);
        }
        if (attributes.containsKey("hp")) {
            int hpBonus = attributes.get("hp") * multiplier;
            player.setMaxHp(player.getMaxHp() + hpBonus);
            // 确保当前生命值不超过最大值
            player.setHp(Math.min(player.getHp(), player.getMaxHp()));
        }
        if (attributes.containsKey("mp")) {
            int mpBonus = attributes.get("mp") * multiplier;
            player.setMaxMp(player.getMaxMp() + mpBonus);
            // 确保当前魔法值不超过最大值
            player.setMp(Math.min(player.getMp(), player.getMaxMp()));
        }
        
        // 应用其他属性
        if (attributes.containsKey("accuracy")) {
            player.getAbility().setAccuracy(player.getAbility().getAccuracy() + attributes.get("accuracy") * multiplier);
        }
        if (attributes.containsKey("dodge")) {
            player.getAbility().setDodge(player.getAbility().getDodge() + attributes.get("dodge") * multiplier);
        }
        if (attributes.containsKey("lucky")) {
            player.getAbility().setLucky(player.getAbility().getLucky() + attributes.get("lucky") * multiplier);
        }
        
        log.debug("{}装备属性：{} 到玩家 {}", apply ? "应用" : "移除", item.getName(), player.getName());
    }
} 