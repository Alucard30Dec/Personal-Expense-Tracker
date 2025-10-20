package com.example.quanlychitieu;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BudgetDao {
    // Lệnh Insert hoặc Update nếu đã tồn tại
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Budget budget);

    @Query("SELECT * FROM budgets_table WHERE monthYear = :monthYear")
    List<Budget> getBudgetsForMonth(String monthYear);

    @Query("SELECT * FROM budgets_table WHERE categoryName = :category AND monthYear = :monthYear LIMIT 1")
    Budget getBudgetForCategory(String category, String monthYear);
}