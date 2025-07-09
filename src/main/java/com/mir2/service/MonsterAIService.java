package com.mir2.service;

import com.mir2.core.model.BaseObject;
import com.mir2.core.model.Monster;
import com.mir2.core.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 怪物AI服务
 * 
 * <p>负责管理怪物的AI行为，包括怪物的寻路、攻击、巡逻等。
 * 对应原M2Engine中的MonsterAI模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class MonsterAIService {
    
    /** 怪物AI更新间隔（毫秒） */
    private static final long AI_UPDATE_INTERVAL = 1000;
    
    /** 最大寻路距离 */
    private static final int MAX_PATHFIND_DISTANCE = 15;
    
    /** 返回刷新点的距离阈值 */
    private static final int RETURN_SPAWN_DISTANCE = 20;
    
    @Autowired
    private MapService mapService;
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * 处理怪物AI逻辑
     * 
     * @param monster 怪物对象
     */
    public void processMonsterAI(Monster monster) {
        if (monster == null || monster.isDead()) {
            return;
        }
        
        // 检查AI更新间隔
        long currentTime = System.currentTimeMillis();
        if (currentTime - monster.getLastAIUpdateTime() < AI_UPDATE_INTERVAL) {
            return;
        }
        
        monster.setLastAIUpdateTime(currentTime);
        
        // 根据AI类型处理不同的行为
        switch (monster.getAiType()) {
            case PASSIVE:
                processPassiveAI(monster);
                break;
            case AGGRESSIVE:
                processAggressiveAI(monster);
                break;
            case PATROL:
                processPatrolAI(monster);
                break;
            case GUARD:
                processGuardAI(monster);
                break;
            case SMART:
                processSmartAI(monster);
                break;
            default:
                processAggressiveAI(monster);
                break;
        }
    }
    
    /**
     * 处理被动AI
     * 
     * @param monster 怪物对象
     */
    private void processPassiveAI(Monster monster) {
        // 被动怪物只在被攻击时才会反击
        if (monster.getTarget() != null) {
            if (isValidTarget(monster, monster.getTarget())) {
                // 攻击目标
                if (monster.distanceTo(monster.getTarget()) <= monster.getAttackRange()) {
                    monster.attackTarget(monster.getTarget());
                } else {
                    // 追击目标
                    moveToTarget(monster, monster.getTarget());
                }
            } else {
                // 清除无效目标
                monster.setTarget(null);
            }
        }
    }
    
    /**
     * 处理主动AI
     * 
     * @param monster 怪物对象
     */
    private void processAggressiveAI(Monster monster) {
        // 检查是否需要返回刷新点
        if (shouldReturnToSpawn(monster)) {
            returnToSpawn(monster);
            return;
        }
        
        // 寻找目标
        if (monster.getTarget() == null) {
            BaseObject target = findNearestTarget(monster);
            if (target != null) {
                monster.setTarget(target);
                log.debug("怪物 {} 发现目标: {}", monster.getName(), target.getName());
            }
        }
        
        // 处理目标
        if (monster.getTarget() != null) {
            if (isValidTarget(monster, monster.getTarget())) {
                double distance = monster.distanceTo(monster.getTarget());
                
                if (distance <= monster.getAttackRange()) {
                    // 攻击目标
                    monster.attackTarget(monster.getTarget());
                } else if (distance <= monster.getChaseRange()) {
                    // 追击目标
                    moveToTarget(monster, monster.getTarget());
                } else {
                    // 目标超出追击范围，清除目标
                    monster.setTarget(null);
                    monster.clearAllHatred();
                    log.debug("怪物 {} 放弃追击目标", monster.getName());
                }
            } else {
                // 清除无效目标
                monster.setTarget(null);
            }
        }
    }
    
    /**
     * 处理巡逻AI
     * 
     * @param monster 怪物对象
     */
    private void processPatrolAI(Monster monster) {
        // 如果有目标，优先处理目标
        if (monster.getTarget() != null && isValidTarget(monster, monster.getTarget())) {
            processAggressiveAI(monster);
            return;
        }
        
        // 巡逻逻辑
        if (monster.getPatrolPath() != null && !monster.getPatrolPath().isEmpty()) {
            Monster.Position currentTarget = monster.getPatrolPath().get(monster.getCurrentPatrolIndex());
            
            if (monster.getX() == currentTarget.getX() && monster.getY() == currentTarget.getY()) {
                // 到达巡逻点，选择下一个点
                monster.setCurrentPatrolIndex((monster.getCurrentPatrolIndex() + 1) % monster.getPatrolPath().size());
                log.debug("怪物 {} 到达巡逻点 {}", monster.getName(), monster.getCurrentPatrolIndex());
            } else {
                // 移动到巡逻点
                moveToPosition(monster, currentTarget.getX(), currentTarget.getY());
            }
        } else {
            // 没有巡逻路径，随机移动
            randomMove(monster);
        }
    }
    
    /**
     * 处理守卫AI
     * 
     * @param monster 怪物对象
     */
    private void processGuardAI(Monster monster) {
        // 守卫在指定范围内保护区域
        if (monster.getTarget() != null && isValidTarget(monster, monster.getTarget())) {
            double distance = monster.distanceTo(monster.getTarget());
            
            if (distance <= monster.getAttackRange()) {
                // 攻击目标
                monster.attackTarget(monster.getTarget());
            } else if (distance <= monster.getChaseRange() && 
                       monster.distanceToSpawn() <= monster.getChaseRange()) {
                // 在守卫范围内追击
                moveToTarget(monster, monster.getTarget());
            } else {
                // 超出守卫范围，返回刷新点
                monster.setTarget(null);
                monster.clearAllHatred();
                returnToSpawn(monster);
            }
        } else {
            // 寻找附近的敌人
            BaseObject target = findNearestTarget(monster);
            if (target != null && monster.distanceTo(target) <= monster.getViewRange()) {
                monster.setTarget(target);
            } else {
                // 返回刷新点
                returnToSpawn(monster);
            }
        }
    }
    
    /**
     * 处理智能AI
     * 
     * @param monster 怪物对象
     */
    private void processSmartAI(Monster monster) {
        // 智能AI结合多种行为模式
        
        // 检查仇恨列表
        String topHatredTarget = monster.getTopHatredTarget();
        if (topHatredTarget != null) {
            Player player = playerService.getPlayerByName(topHatredTarget);
            if (player != null && isValidTarget(monster, player)) {
                monster.setTarget(player);
            }
        }
        
        // 使用技能
        useMonsterSkills(monster);
        
        // 根据血量调整行为
        double healthPercentage = (double) monster.getHp() / monster.getMaxHp();
        if (healthPercentage < 0.3) {
            // 血量低时尝试逃跑
            if (monster.getTarget() != null) {
                fleeFromTarget(monster, monster.getTarget());
            } else {
                returnToSpawn(monster);
            }
        } else {
            // 正常战斗
            processAggressiveAI(monster);
        }
    }
    
    /**
     * 使用怪物技能
     * 
     * @param monster 怪物对象
     */
    private void useMonsterSkills(Monster monster) {
        for (Monster.MonsterSkill skill : monster.getSkills()) {
            if (skill.canUse() && Math.random() < skill.getUseRate()) {
                // 实现具体的技能效果
                executeMonsterSkill(monster, skill);
                skill.use();
                log.debug("怪物 {} 使用技能: {}", monster.getName(), skill.getSkillId());
            }
        }
    }
    
    /**
     * 执行怪物技能
     * 
     * @param monster 怪物对象
     * @param skill 技能对象
     */
    private void executeMonsterSkill(Monster monster, Monster.MonsterSkill skill) {
        BaseObject target = monster.getTarget();
        
        switch (skill.getSkillId()) {
            case 1: // 火球术
                castFireball(monster, target);
                break;
            case 2: // 治愈术
                castHeal(monster);
                break;
            case 3: // 召唤术
                castSummon(monster);
                break;
            case 4: // 毒云术
                castPoisonCloud(monster, target);
                break;
            case 5: // 冰冻术
                castFreeze(monster, target);
                break;
            case 6: // 传送术
                castTeleport(monster);
                break;
            case 7: // 群体攻击
                castAreaAttack(monster);
                break;
            case 8: // 魔法盾
                castMagicShield(monster);
                break;
            case 9: // 隐身术
                castInvisibility(monster);
                break;
            case 10: // 狂暴术
                castBerserk(monster);
                break;
            default:
                log.warn("未知的怪物技能ID: {}", skill.getSkillId());
                break;
        }
    }
    
    /**
     * 施放火球术
     */
    private void castFireball(Monster monster, BaseObject target) {
        if (target == null || monster.distanceTo(target) > 8) {
            return;
        }
        
        int damage = monster.getLevel() * 15 + 50;
        if (target instanceof Player) {
            Player player = (Player) target;
            player.takeDamage(damage);
            log.debug("怪物 {} 对玩家 {} 施放火球术造成 {} 点伤害", 
                    monster.getName(), player.getName(), damage);
        }
    }
    
    /**
     * 施放治愈术
     */
    private void castHeal(Monster monster) {
        int healAmount = monster.getLevel() * 20 + 100;
        int newHp = Math.min(monster.getMaxHp(), monster.getHp() + healAmount);
        monster.setHp(newHp);
        log.debug("怪物 {} 使用治愈术恢复 {} 点生命值", monster.getName(), healAmount);
    }
    
    /**
     * 施放召唤术
     */
    private void castSummon(Monster monster) {
        // 召唤小怪
        if (monster.getSummons().size() < 3) {
            // 创建召唤物
            log.debug("怪物 {} 召唤了小怪", monster.getName());
        }
    }
    
    /**
     * 施放毒云术
     */
    private void castPoisonCloud(Monster monster, BaseObject target) {
        if (target == null) {
            return;
        }
        
        // 在目标区域创建毒云
        int poisonDamage = monster.getLevel() * 5 + 10;
        if (target instanceof Player) {
            Player player = (Player) target;
            // 添加中毒效果
            log.debug("怪物 {} 对玩家 {} 施放毒云术", monster.getName(), player.getName());
        }
    }
    
    /**
     * 施放冰冻术
     */
    private void castFreeze(Monster monster, BaseObject target) {
        if (target == null) {
            return;
        }
        
        if (target instanceof Player) {
            Player player = (Player) target;
            // 添加冰冻效果
            log.debug("怪物 {} 对玩家 {} 施放冰冻术", monster.getName(), player.getName());
        }
    }
    
    /**
     * 施放传送术
     */
    private void castTeleport(Monster monster) {
        // 随机传送到附近位置
        int newX = monster.getX() + (int)(Math.random() * 6) - 3;
        int newY = monster.getY() + (int)(Math.random() * 6) - 3;
        
        if (mapService.canMove(monster.getMapName(), newX, newY)) {
            monster.moveTo(newX, newY);
            log.debug("怪物 {} 传送到 ({}, {})", monster.getName(), newX, newY);
        }
    }
    
    /**
     * 施放群体攻击
     */
    private void castAreaAttack(Monster monster) {
        // 攻击周围的所有玩家
        List<Player> players = mapService.getPlayersInMap(monster.getMapName());
        for (Player player : players) {
            if (monster.distanceTo(player) <= 3) {
                int damage = monster.getLevel() * 10 + 30;
                player.takeDamage(damage);
                log.debug("怪物 {} 群体攻击玩家 {} 造成 {} 点伤害", 
                        monster.getName(), player.getName(), damage);
            }
        }
    }
    
    /**
     * 施放魔法盾
     */
    private void castMagicShield(Monster monster) {
        // 增加魔法防御
        monster.setMagicDefense(monster.getMagicDefense() + 50);
        log.debug("怪物 {} 施放魔法盾", monster.getName());
    }
    
    /**
     * 施放隐身术
     */
    private void castInvisibility(Monster monster) {
        // 设置隐身状态
        monster.setInvisible(true);
        log.debug("怪物 {} 进入隐身状态", monster.getName());
    }
    
    /**
     * 施放狂暴术
     */
    private void castBerserk(Monster monster) {
        // 增加攻击力和攻击速度
        monster.setAttack(monster.getAttack() + 30);
        monster.setAttackSpeed(monster.getAttackSpeed() + 20);
        log.debug("怪物 {} 进入狂暴状态", monster.getName());
    }
    
    /**
     * 寻找最近的目标
     * 
     * @param monster 怪物对象
     * @return 最近的目标
     */
    private BaseObject findNearestTarget(Monster monster) {
        BaseObject nearestTarget = null;
        double minDistance = Double.MAX_VALUE;
        
        // 查找同一地图的玩家
        List<Player> players = mapService.getPlayersInMap(monster.getMapName());
        for (Player player : players) {
            if (isValidTarget(monster, player)) {
                double distance = monster.distanceTo(player);
                if (distance <= monster.getViewRange() && distance < minDistance) {
                    minDistance = distance;
                    nearestTarget = player;
                }
            }
        }
        
        return nearestTarget;
    }
    
    /**
     * 检查目标是否有效
     * 
     * @param monster 怪物对象
     * @param target 目标对象
     * @return 是否有效
     */
    private boolean isValidTarget(Monster monster, BaseObject target) {
        if (target == null) {
            return false;
        }
        
        // 检查是否在同一地图
        if (!monster.getMapName().equals(target.getMapName())) {
            return false;
        }
        
        // 检查玩家是否在线
        if (target instanceof Player) {
            Player player = (Player) target;
            return player.isOnline() && !player.isDead();
        }
        
        return true;
    }
    
    /**
     * 移动到目标位置
     * 
     * @param monster 怪物对象
     * @param target 目标对象
     */
    private void moveToTarget(Monster monster, BaseObject target) {
        if (target == null) {
            return;
        }
        
        // 简单的寻路算法
        int dx = target.getX() - monster.getX();
        int dy = target.getY() - monster.getY();
        
        int newX = monster.getX();
        int newY = monster.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            newX += dx > 0 ? 1 : -1;
        } else {
            newY += dy > 0 ? 1 : -1;
        }
        
        // 检查移动是否有效
        if (mapService.canMove(monster.getMapName(), newX, newY)) {
            monster.moveTo(newX, newY);
        }
    }
    
    /**
     * 移动到指定位置
     * 
     * @param monster 怪物对象
     * @param x 目标X坐标
     * @param y 目标Y坐标
     */
    private void moveToPosition(Monster monster, int x, int y) {
        int dx = x - monster.getX();
        int dy = y - monster.getY();
        
        int newX = monster.getX();
        int newY = monster.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            newX += dx > 0 ? 1 : -1;
        } else {
            newY += dy > 0 ? 1 : -1;
        }
        
        if (mapService.canMove(monster.getMapName(), newX, newY)) {
            monster.moveTo(newX, newY);
        }
    }
    
    /**
     * 随机移动
     * 
     * @param monster 怪物对象
     */
    private void randomMove(Monster monster) {
        int direction = (int) (Math.random() * 8);
        int newX = monster.getX();
        int newY = monster.getY();
        
        switch (direction) {
            case 0: newY--; break; // 上
            case 1: newX++; newY--; break; // 右上
            case 2: newX++; break; // 右
            case 3: newX++; newY++; break; // 右下
            case 4: newY++; break; // 下
            case 5: newX--; newY++; break; // 左下
            case 6: newX--; break; // 左
            case 7: newX--; newY--; break; // 左上
        }
        
        if (mapService.canMove(monster.getMapName(), newX, newY)) {
            monster.moveTo(newX, newY);
        }
    }
    
    /**
     * 从目标逃跑
     * 
     * @param monster 怪物对象
     * @param target 目标对象
     */
    private void fleeFromTarget(Monster monster, BaseObject target) {
        if (target == null) {
            return;
        }
        
        // 计算逃跑方向
        int dx = monster.getX() - target.getX();
        int dy = monster.getY() - target.getY();
        
        int newX = monster.getX();
        int newY = monster.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            newX += dx > 0 ? 1 : -1;
        } else {
            newY += dy > 0 ? 1 : -1;
        }
        
        if (mapService.canMove(monster.getMapName(), newX, newY)) {
            monster.moveTo(newX, newY);
        }
    }
    
    /**
     * 返回刷新点
     * 
     * @param monster 怪物对象
     */
    private void returnToSpawn(Monster monster) {
        if (monster.getX() != monster.getSpawnX() || monster.getY() != monster.getSpawnY()) {
            moveToPosition(monster, monster.getSpawnX(), monster.getSpawnY());
        }
    }
    
    /**
     * 检查是否应该返回刷新点
     * 
     * @param monster 怪物对象
     * @return 是否应该返回
     */
    private boolean shouldReturnToSpawn(Monster monster) {
        return monster.distanceToSpawn() > RETURN_SPAWN_DISTANCE;
    }
    
    /**
     * 计算A*寻路
     * 
     * @param monster 怪物对象
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 路径点列表
     */
    private List<Monster.Position> calculatePath(Monster monster, int targetX, int targetY) {
        // 实现A*寻路算法
        List<Monster.Position> path = new CopyOnWriteArrayList<>();
        
        // 检查距离是否过远
        int distance = Math.abs(targetX - monster.getX()) + Math.abs(targetY - monster.getY());
        if (distance > MAX_PATHFIND_DISTANCE) {
            return path; // 距离过远，不进行寻路
        }
        
        // A*算法的数据结构
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>((a, b) -> Integer.compare(a.f, b.f));
        Set<String> closedSet = new HashSet<>();
        Map<String, AStarNode> nodeMap = new HashMap<>();
        
        // 起始点
        AStarNode startNode = new AStarNode(monster.getX(), monster.getY());
        startNode.g = 0;
        startNode.h = manhattanDistance(monster.getX(), monster.getY(), targetX, targetY);
        startNode.f = startNode.g + startNode.h;
        
        openSet.offer(startNode);
        nodeMap.put(getNodeKey(startNode.x, startNode.y), startNode);
        
        // 8个方向
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        
        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();
            String currentKey = getNodeKey(current.x, current.y);
            
            // 到达目标
            if (current.x == targetX && current.y == targetY) {
                return reconstructPath(current);
            }
            
            closedSet.add(currentKey);
            
            // 检查所有邻居
            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                String neighborKey = getNodeKey(newX, newY);
                
                // 检查边界和阻挡
                if (!isValidPosition(monster.getMapName(), newX, newY) || closedSet.contains(neighborKey)) {
                    continue;
                }
                
                // 计算移动代价
                int moveCost = (dir[0] == 0 || dir[1] == 0) ? 10 : 14; // 直线10，对角线14
                int tentativeG = current.g + moveCost;
                
                AStarNode neighbor = nodeMap.get(neighborKey);
                if (neighbor == null) {
                    neighbor = new AStarNode(newX, newY);
                    neighbor.g = tentativeG;
                    neighbor.h = manhattanDistance(newX, newY, targetX, targetY);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    
                    openSet.offer(neighbor);
                    nodeMap.put(neighborKey, neighbor);
                } else if (tentativeG < neighbor.g) {
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                }
            }
        }
        
        // 未找到路径，返回空列表
        return path;
    }
    
    /**
     * A*算法节点类
     */
    private static class AStarNode {
        int x, y;
        int g, h, f; // g: 从起点到当前点的代价, h: 从当前点到终点的启发式代价, f: g + h
        AStarNode parent;
        
        public AStarNode(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * 计算曼哈顿距离
     */
    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * 生成节点键
     */
    private String getNodeKey(int x, int y) {
        return x + "," + y;
    }
    
    /**
     * 检查位置是否有效
     */
    private boolean isValidPosition(String mapName, int x, int y) {
        return mapService.canMove(mapName, x, y);
    }
    
    /**
     * 重建路径
     */
    private List<Monster.Position> reconstructPath(AStarNode node) {
        List<Monster.Position> path = new ArrayList<>();
        AStarNode current = node;
        
        while (current != null) {
            path.add(0, new Monster.Position(current.x, current.y));
            current = current.parent;
        }
        
        // 移除起始点
        if (!path.isEmpty()) {
            path.remove(0);
        }
        
        return path;
    }
    
    /**
     * 清理死亡怪物
     * 
     * @param monster 怪物对象
     */
    public void cleanupDeadMonster(Monster monster) {
        if (monster.isDead()) {
            monster.clearAllHatred();
            monster.setTarget(null);
            log.debug("清理死亡怪物: {}", monster.getName());
        }
    }
    
    /**
     * 重置怪物状态
     * 
     * @param monster 怪物对象
     */
    public void resetMonster(Monster monster) {
        monster.clearAllHatred();
        monster.setTarget(null);
        monster.setCurrentPatrolIndex(0);
        monster.setPosition(monster.getSpawnX(), monster.getSpawnY());
        log.debug("重置怪物状态: {}", monster.getName());
    }
} 