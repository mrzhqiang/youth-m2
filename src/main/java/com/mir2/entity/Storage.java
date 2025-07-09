package com.mir2.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "storage")
public class Storage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_name", nullable = false)
    private String ownerName;
    
    @Column(name = "type", nullable = false)
    private String type; // PERSONAL, GUILD
    
    @Column(name = "max_size", nullable = false)
    private int maxSize;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "expanded")
    private boolean expanded;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private List<Item> items;
    
    // Constructors
    public Storage() {}
    
    public Storage(String ownerName, String type, int maxSize) {
        this.ownerName = ownerName;
        this.type = type;
        this.maxSize = maxSize;
        this.expanded = false;
        this.password = "";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getMaxSize() { return maxSize; }
    public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
} 