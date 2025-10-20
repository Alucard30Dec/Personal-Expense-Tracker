package com.example.quanlychitieu;

public class BudgetProgress {
    private String categoryName;
    private double budgetAmount;
    private double spentAmount;

    public BudgetProgress(String categoryName, double budgetAmount, double spentAmount) {
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
    }

    public String getCategoryName() { return categoryName; }
    public double getBudgetAmount() { return budgetAmount; }
    public double getSpentAmount() { return spentAmount; }
}