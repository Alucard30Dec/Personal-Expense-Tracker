package com.example.quanlychitieu;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import java.util.Date;


@Dao
public interface DebtDao {
    @Insert
    void insert(Debt debt);

    @Update // ✅ Đảm bảo có phương thức này
    void update(Debt debt);

    @Delete
        // ✅ Đảm bảo có phương thức này
    void delete(Debt debt);

    @Query("SELECT * FROM debts_table ORDER BY isPaid ASC, dueDate DESC") // Ưu tiên chưa trả, ngày gần nhất
    List<Debt> getAllDebts();

    @Query("SELECT * FROM debts_table WHERE id = :id LIMIT 1")
    Debt getDebtById(int id); // Nếu cần lấy chi tiết
    // ✅ THÊM PHƯƠNG THỨC NÀY
    @Query("SELECT COUNT(*) FROM debts_table WHERE isPaid = 0 AND dueDate <= :today")
    int countOverdueOrDueTodayDebts(Date today);
}