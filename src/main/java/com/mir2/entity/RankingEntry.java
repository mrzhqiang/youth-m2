package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ranking_entry")
public class RankingEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "player_name", nullable = false)
    private String playerName;
    
    @Column(name = "level")
    private int level;
    
    @Column(name = "value")
    private long value;
    
    @Column(name = "job")
    private String job;
    
    @Column(name = "guild_name")
    private String guildName;
    
    @Column(name = "ranking_type")
    private String rankingType;
    
    @Column(name = "rank_position")
    private int rankPosition;
    
    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
    
    // Constructors
    public RankingEntry() {}
    
    public RankingEntry(String playerName, int level, long value, String job, String guildName, String rankingType) {
        this.playerName = playerName;
        this.level = level;
        this.value = value;
        this.job = job;
        this.guildName = guildName;
        this.rankingType = rankingType;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
    
    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }
    
    public String getGuildName() { return guildName; }
    public void setGuildName(String guildName) { this.guildName = guildName; }
    
    public String getRankingType() { return rankingType; }
    public void setRankingType(String rankingType) { this.rankingType = rankingType; }
    
    public int getRankPosition() { return rankPosition; }
    public void setRankPosition(int rankPosition) { this.rankPosition = rankPosition; }
    
    public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
} 