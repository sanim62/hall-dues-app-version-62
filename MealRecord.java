
package com.example.halldues;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MealRecord {
    private String id;
    private String userId;
    private String date; // Format: "yyyy-MM-dd"
    private String status; // "MEAL_ON", "MEAL_OFF", "HALL_CLOSED"
    private long timestamp;

    public MealRecord() {
    }

    public MealRecord(String userId, String date, String status) {
        this.userId = userId;
        this.date = date;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}