package com.example.quanlychitieu;

public class CategoryPrediction {
    private String categoryName;
    private double predictedAmount;

    public CategoryPrediction(String categoryName, double predictedAmount) {
        this.categoryName = categoryName;
        this.predictedAmount = predictedAmount;
    }

    public String getCategoryName() { return categoryName; }
    public double getPredictedAmount() { return predictedAmount; }
}