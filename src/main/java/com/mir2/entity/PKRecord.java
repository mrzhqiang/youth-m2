package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pk_record")
public class PKRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "killer_name", nullable = false)
    private String killerName;
    
    @Column(name = "victim_name", nullable = false)
    private String victimName;
    
    @Column(name = "killer_level")
    private int killerLevel;
    
    @Column(name = "victim_level")
    private int victimLevel;
    
    @Column(name = "killer_pk_change")
    private int killerPKChange;
    
    @Column(name = "victim_pk_change")
    private int victimPKChange;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "map_name")
    private String mapName;
    
    @Column(name = "x")
    private int x;
    
    @Column(name = "y")
    private int y;
    
    // Constructors
    public PKRecord() {}
    
    public PKRecord(String killerName, String victimName, int killerLevel, int victimLevel) {
        this.killerName = killerName;
        this.victimName = victimName;
        this.killerLevel = killerLevel;
        this.victimLevel = victimLevel;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getKillerName() { return killerName; }
    public void setKillerName(String killerName) { this.killerName = killerName; }
    
    public String getVictimName() { return victimName; }
    public void setVictimName(String victimName) { this.victimName = victimName; }
    
    public int getKillerLevel() { return killerLevel; }
    public void setKillerLevel(int killerLevel) { this.killerLevel = killerLevel; }
    
    public int getVictimLevel() { return victimLevel; }
    public void setVictimLevel(int victimLevel) { this.victimLevel = victimLevel; }
    
    public int getKillerPKChange() { return killerPKChange; }
    public void setKillerPKChange(int killerPKChange) { this.killerPKChange = killerPKChange; }
    
    public int getVictimPKChange() { return victimPKChange; }
    public void setVictimPKChange(int victimPKChange) { this.victimPKChange = victimPKChange; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
} 