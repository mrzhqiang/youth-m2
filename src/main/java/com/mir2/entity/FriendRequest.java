package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friend_request")
public class FriendRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "requester_name", nullable = false)
    private String requesterName;
    
    @Column(name = "target_name", nullable = false)
    private String targetName;
    
    @Column(name = "request_time")
    private LocalDateTime requestTime;
    
    @Column(name = "status")
    private String status; // PENDING, ACCEPTED, REJECTED
    
    @Column(name = "message")
    private String message;
    
    // Constructors
    public FriendRequest() {}
    
    public FriendRequest(String requesterName, String targetName, String message) {
        this.requesterName = requesterName;
        this.targetName = targetName;
        this.message = message;
        this.requestTime = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 