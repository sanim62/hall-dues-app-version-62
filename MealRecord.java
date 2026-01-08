package com.example.halldues;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_records",
        foreignKeys = @ForeignKey(entity = User.class,
                                  parentColumns = "id",
                                  childColumns = "userId",
                                  onDelete = ForeignKey.CASCADE))
public class MealRecord {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String userId;

    private String date; // Format: "yyyy-MM-dd"
    private String status; // "MEAL_ON", "MEAL_OFF", "HALL_CLOSED"

    public MealRecord() {
    }

    public MealRecord(@NonNull String userId, String date, String status) {
        this.id = userId + "_" + date; // Create a composite key
        this.userId = userId;
        this.date = date;
        this.status = status;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
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
}
