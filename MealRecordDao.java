package com.example.halldues;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MealRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MealRecord> mealRecords);

    @Query("SELECT * FROM meal_records WHERE userId = :userId AND date LIKE :yearMonth || '%' ")
    List<MealRecord> getRecordsForMonth(String userId, String yearMonth);
}
