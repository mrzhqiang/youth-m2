package com.mir2.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * 
 * <p>对应数据库中的角色表，存储角色基本信息。
 * 对应原M2Engine中的角色数据。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "characters")
@Data
@EntityListeners(AuditingEntityListener.class)
public class CharacterEntity {
    
    /** 角色ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 用户ID */
    @Column(nullable = false)
    private Long userId;
    
    /** 角色名称 */
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    /** 职业 (0: 战士, 1: 法师, 2: 道士) */
    @Column(nullable = false)
    private Integer job;
    
    /** 性别 (0: 男, 1: 女) */
    @Column(nullable = false)
    private Integer gender;
    
    /** 发型 */
    @Column(nullable = false)
    private Integer hair = 0;
    
    /** 等级 */
    @Column(nullable = false)
    private Integer level = 1;
    
    /** 当前经验值 */
    @Column(nullable = false)
    private Long experience = 0L;
    
    /** 生命值 */
    @Column(nullable = false)
    private Integer hp = 100;
    
    /** 最大生命值 */
    @Column(nullable = false)
    private Integer maxHp = 100;
    
    /** 魔法值 */
    @Column(nullable = false)
    private Integer mp = 100;
    
    /** 最大魔法值 */
    @Column(nullable = false)
    private Integer maxMp = 100;
    
    /** 力量 */
    @Column(nullable = false)
    private Integer strength = 10;
    
    /** 敏捷 */
    @Column(nullable = false)
    private Integer agility = 10;
    
    /** 体力 */
    @Column(nullable = false)
    private Integer constitution = 10;
    
    /** 智力 */
    @Column(nullable = false)
    private Integer intelligence = 10;
    
    /** 属性点 */
    @Column(nullable = false)
    private Integer statPoints = 0;
    
    /** 技能点 */
    @Column(nullable = false)
    private Integer skillPoints = 0;
    
    /** 金币 */
    @Column(nullable = false)
    private Long gold = 0L;
    
    /** 游戏点数 */
    @Column(nullable = false)
    private Long gamePoint = 0L;
    
    /** 游戏钻石 */
    @Column(nullable = false)
    private Long gameDiamond = 0L;
    
    /** PK值 */
    @Column(nullable = false)
    private Integer pkValue = 0;
    
    /** 声望值 */
    @Column(nullable = false)
    private Integer reputation = 0;
    
    /** 当前地图 */
    @Column(nullable = false, length = 50)
    private String currentMap = "0";
    
    /** 当前X坐标 */
    @Column(nullable = false)
    private Integer currentX = 330;
    
    /** 当前Y坐标 */
    @Column(nullable = false)
    private Integer currentY = 330;
    
    /** 当前方向 */
    @Column(nullable = false)
    private Integer direction = 0;
    
    /** 师傅名称 */
    @Column(length = 50)
    private String masterName;
    
    /** 配偶名称 */
    @Column(length = 50)
    private String spouseName;
    
    /** 行会名称 */
    @Column(length = 50)
    private String guildName;
    
    /** 行会职位 */
    @Column(length = 50)
    private String guildRank;
    
    /** 在线时长（分钟） */
    @Column(nullable = false)
    private Integer onlineTime = 0;
    
    /** 最后登录时间 */
    @Column
    private LocalDateTime lastLoginTime;
    
    /** 最后登出时间 */
    @Column
    private LocalDateTime lastLogoutTime;
    
    /** 最后保存时间 */
    @Column
    private LocalDateTime lastSaveTime;
    
    /** 角色状态 (0: 正常, 1: 删除) */
    @Column(nullable = false)
    private Integer status = 0;
    
    /** 创建时间 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    /** 是否删除 */
    @Column(nullable = false)
    private Boolean deleted = false;
    
    /**
     * 职业枚举
     */
    public enum Job {
        WARRIOR(0, "战士"),
        WIZARD(1, "法师"),
        TAOIST(2, "道士");
        
        private final int code;
        private final String name;
        
        Job(int code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        public static Job fromCode(int code) {
            for (Job job : values()) {
                if (job.getCode() == code) {
                    return job;
                }
            }
            return WARRIOR;
        }
    }
    
    /**
     * 性别枚举
     */
    public enum Gender {
        MALE(0, "男"),
        FEMALE(1, "女");
        
        private final int code;
        private final String name;
        
        Gender(int code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        public static Gender fromCode(int code) {
            for (Gender gender : values()) {
                if (gender.getCode() == code) {
                    return gender;
                }
            }
            return MALE;
        }
    }
    
    /**
     * 角色状态枚举
     */
    public enum Status {
        NORMAL(0, "正常"),
        DELETED(1, "删除");
        
        private final int code;
        private final String description;
        
        Status(int code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 