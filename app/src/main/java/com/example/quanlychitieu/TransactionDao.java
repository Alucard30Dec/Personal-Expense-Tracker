package com.example.quanlychitieu;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update; // ✅ Thêm import này
import java.util.List;

@Dao
public interface TransactionDao {
    @Query("SELECT * FROM transactions_table ORDER BY date DESC")
    List<Transaction> getAllTransactions();

    @Insert
    void insert(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Update // ✅ THÊM PHƯƠNG THỨC NÀY
    void update(Transaction transaction);
}