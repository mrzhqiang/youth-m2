package com.mir2.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * <p>对应数据库中的用户表，存储用户账号信息。
 * 对应原M2Engine中的用户数据。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "users")
@Data
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    
    /** 用户ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 用户名（账号） */
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    /** 密码 */
    @Column(nullable = false, length = 100)
    private String password;
    
    /** 邮箱 */
    @Column(unique = true, length = 100)
    private String email;
    
    /** 手机号 */
    @Column(unique = true, length = 20)
    private String phone;
    
    /** 真实姓名 */
    @Column(length = 50)
    private String realName;
    
    /** 身份证号 */
    @Column(length = 18)
    private String idCard;
    
    /** 账号状态 (0: 正常, 1: 封禁, 2: 冻结) */
    @Column(nullable = false)
    private Integer status = 0;
    
    /** 用户类型 (0: 普通用户, 1: VIP用户, 2: 管理员) */
    @Column(nullable = false)
    private Integer userType = 0;
    
    /** 点数余额 */
    @Column(nullable = false)
    private Long pointBalance = 0L;
    
    /** 充值总额 */
    @Column(nullable = false)
    private Long totalRecharge = 0L;
    
    /** 最后登录时间 */
    @Column
    private LocalDateTime lastLoginTime;
    
    /** 最后登录IP */
    @Column(length = 50)
    private String lastLoginIp;
    
    /** 登录次数 */
    @Column(nullable = false)
    private Integer loginCount = 0;
    
    /** 在线时长（分钟） */
    @Column(nullable = false)
    private Integer onlineTime = 0;
    
    /** 注册IP */
    @Column(length = 50)
    private String registerIp;
    
    /** 推荐人 */
    @Column(length = 50)
    private String referrer;
    
    /** QQ号 */
    @Column(length = 20)
    private String qq;
    
    /** 微信号 */
    @Column(length = 50)
    private String wechat;
    
    /** 备注 */
    @Column(length = 500)
    private String remark;
    
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
     * 用户状态枚举
     */
    public enum Status {
        NORMAL(0, "正常"),
        BANNED(1, "封禁"),
        FROZEN(2, "冻结");
        
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
    
    /**
     * 用户类型枚举
     */
    public enum UserType {
        NORMAL(0, "普通用户"),
        VIP(1, "VIP用户"),
        ADMIN(2, "管理员");
        
        private final int code;
        private final String description;
        
        UserType(int code, String description) {
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