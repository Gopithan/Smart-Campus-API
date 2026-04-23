package com.mycompany.smart.campus.api.db;

import com.mycompany.smart.campus.api.models.Room;
import com.mycompany.smart.campus.api.models.Sensor;
import com.mycompany.smart.campus.api.models.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe storage for the Smart Campus system.
 * This class acts as our "database".
 */
public class DataStore {
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Room operations
    public static Map<String, Room> getRooms() { return rooms; }
    
    public static Room getRoom(String id) { return rooms.get(id); }
    
    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }
    
    public static void deleteRoom(String id) {
        rooms.remove(id);
    }

    // Sensor operations
    public static Map<String, Sensor> getSensors() { return sensors; }
    
    public static Sensor getSensor(String id) { return sensors.get(id); }
    
    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Link to room if not already in the room's list
        Room room = rooms.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
    }

    // Reading operations
    public static List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }
    
    public static void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>())).add(reading);
        // Update sensor current value
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }
}
