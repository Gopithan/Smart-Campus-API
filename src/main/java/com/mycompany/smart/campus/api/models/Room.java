package com.mycompany.smart.campus.api.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical Room in the campus.
 */
public class Room {
    private String id; // Unique identifier, e.g., "LIB-301"
    private String name; // Human-readable name, e.g., "Library Quiet Study"
    private int capacity; // Maximum occupancy for safety regulations
    private List<String> sensorIds = new ArrayList<>(); // Collection of IDs of sensors deployed in this room

    // Default constructor for JSON deserialization
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
