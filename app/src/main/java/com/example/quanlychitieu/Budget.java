package com.example.quanlychitieu;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "budgets_table", primaryKeys = {"categoryName", "monthYear"})
public class Budget {

    @NonNull
    private String categoryName; // Tên hạng mục (VD: "Ăn uống")

    @NonNull
    private String monthYear; // Tháng và năm (VD: "2025-10")

    private double budgetAmount; // Số tiền ngân sách

    public Budget(@NonNull String categoryName, @NonNull String monthYear, double budgetAmount) {
        this.categoryName = categoryName;
        this.monthYear = monthYear;
        this.budgetAmount = budgetAmount;
    }

    // --- Getters ---
    @NonNull
    public String getCategoryName() { return categoryName; }
    @NonNull
    public String getMonthYear() { return monthYear; }
    public double getBudgetAmount() { return budgetAmount; }

    // Setter (Room cần để cập nhật)
    public void setBudgetAmount(double budgetAmount) { this.budgetAmount = budgetAmount; }
}