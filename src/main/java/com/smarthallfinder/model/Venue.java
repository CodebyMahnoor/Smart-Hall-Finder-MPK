package com.smarthallfinder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean active = false;
    private String name;
    private String type;
    private Integer capacityFrom;
    private Integer capacityTo;
    private String location;
    private String ownerName;
    private String ownerPhone;

    @Column(unique = true)
    private String loginId; // Hall unique access key

    @Column(length = 1000)
    private String imageUrl;

    // Default No-Arg Constructor (Required by JPA)
    public Venue() {}

    // Parameterized Constructor (Used in Seed Data / CommandLineRunner)
    public Venue(String name, String type, Integer capacityFrom, Integer capacityTo, String location, String ownerName, String ownerPhone, String imageUrl, Boolean active) {
        this.name = name;
        this.type = type;
        this.capacityFrom = capacityFrom;
        this.capacityTo = capacityTo;
        this.location = location;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.imageUrl = imageUrl;
        this.active = active;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getCapacityFrom() { return capacityFrom; }
    public void setCapacityFrom(Integer capacityFrom) { this.capacityFrom = capacityFrom; }

    public Integer getCapacityTo() { return capacityTo; }
    public void setCapacityTo(Integer capacityTo) { this.capacityTo = capacityTo; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}