package com.mir2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * 系统初始化服务
 * 
 * <p>系统启动时的初始化服务，负责初始化所有模板数据和缓存。
 * 对应原M2Engine中的系统初始化过程。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class SystemInitializationService implements CommandLineRunner {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private MapService mapService;
    
    @Autowired
    private GuildService guildService;
    
    @Autowired
    private MonsterService monsterService;
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private EconomyService economyService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始系统初始化...");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 初始化物品模板
            log.info("正在初始化物品模板...");
            itemService.initializeItemTemplates();
            
            // 初始化技能模板
            log.info("正在初始化技能模板...");
            skillService.initializeSkillTemplates();
            
            // 初始化地图
            log.info("正在初始化地图...");
            mapService.initializeMaps();
            
            // 初始化行会系统
            log.info("正在初始化行会系统...");
            guildService.initializeGuildSystem();
            
            // 初始化怪物系统
            log.info("正在初始化怪物系统...");
            monsterService.initializeMonsterTemplates();
            monsterService.initializeMonsterSpawns();
            
            // 初始化商店系统
            log.info("正在初始化商店系统...");
            shopService.initializeShops();
            
            // 初始化经济系统
            log.info("正在初始化经济系统...");
            economyService.initializeEconomy();
            
            long endTime = System.currentTimeMillis();
            log.info("系统初始化完成，耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("系统初始化失败", e);
            throw e;
        }
    }
    
    /**
     * 验证系统状态
     * 
     * @return 是否验证成功
     */
    public boolean validateSystemStatus() {
        try {
            // 检查物品模板是否加载
            if (itemService.getItemTemplate(1001) == null) {
                log.error("物品模板验证失败");
                return false;
            }
            
            // 检查技能模板是否加载
            if (skillService.getSkillTemplate(1001) == null) {
                log.error("技能模板验证失败");
                return false;
            }
            
            // 检查地图是否加载
            if (mapService.getMap("比奇城") == null) {
                log.error("地图验证失败");
                return false;
            }
            
            log.info("系统状态验证成功");
            return true;
            
        } catch (Exception e) {
            log.error("系统状态验证失败", e);
            return false;
        }
    }
    
    /**
     * 获取系统状态报告
     * 
     * @return 状态报告
     */
    public String getSystemStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 系统状态报告 ===\n");
        
        try {
            // 物品系统状态
            report.append("物品模板数量: ").append(getItemTemplateCount()).append("\n");
            
            // 技能系统状态
            report.append("技能模板数量: ").append(getSkillTemplateCount()).append("\n");
            
            // 地图系统状态
            report.append("地图数量: ").append(getMapCount()).append("\n");
            
            // 系统运行时间
            report.append("系统运行时间: ").append(getSystemUptime()).append(" 秒\n");
            
        } catch (Exception e) {
            report.append("获取状态报告失败: ").append(e.getMessage()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * 获取物品模板数量
     * 
     * @return 物品模板数量
     */
    private int getItemTemplateCount() {
        try {
            // 通过反射获取私有字段
            var field = itemService.getClass().getDeclaredField("itemTemplates");
            field.setAccessible(true);
            var itemTemplates = (java.util.Map<?, ?>) field.get(itemService);
            return itemTemplates.size();
        } catch (Exception e) {
            log.warn("获取物品模板数量失败", e);
            return -1;
        }
    }
    
    /**
     * 获取技能模板数量
     * 
     * @return 技能模板数量
     */
    private int getSkillTemplateCount() {
        try {
            // 通过反射获取私有字段
            var field = skillService.getClass().getDeclaredField("skillTemplates");
            field.setAccessible(true);
            var skillTemplates = (java.util.Map<?, ?>) field.get(skillService);
            return skillTemplates.size();
        } catch (Exception e) {
            log.warn("获取技能模板数量失败", e);
            return -1;
        }
    }
    
    /**
     * 获取地图数量
     * 
     * @return 地图数量
     */
    private int getMapCount() {
        try {
            // 通过反射获取私有字段
            var field = mapService.getClass().getDeclaredField("mapCache");
            field.setAccessible(true);
            var mapCache = (java.util.Map<?, ?>) field.get(mapService);
            return mapCache.size();
        } catch (Exception e) {
            log.warn("获取地图数量失败", e);
            return -1;
        }
    }
    
    /**
     * 获取系统运行时间
     * 
     * @return 运行时间（秒）
     */
    private long getSystemUptime() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
    }
    
    /**
     * 重新加载系统配置
     * 
     * @return 是否重新加载成功
     */
    public boolean reloadSystem() {
        try {
            log.info("开始重新加载系统配置...");
            
            // 重新初始化物品模板
            itemService.initializeItemTemplates();
            
            // 重新初始化技能模板
            skillService.initializeSkillTemplates();
            
            // 重新初始化地图
            mapService.initializeMaps();
            
            log.info("系统配置重新加载完成");
            return true;
            
        } catch (Exception e) {
            log.error("重新加载系统配置失败", e);
            return false;
        }
    }
} 