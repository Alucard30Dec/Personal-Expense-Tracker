package com.example.quanlychitieu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // ✅ Thêm import
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder> {

    private List<CategoryPrediction> predictionList;

    public PredictionAdapter(List<CategoryPrediction> predictionList) {
        this.predictionList = predictionList;
    }

    @NonNull
    @Override
    public PredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prediction, parent, false);
        return new PredictionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionViewHolder holder, int position) {
        CategoryPrediction prediction = predictionList.get(position);

        // ✅ Gán Icon
        holder.categoryIconImageView.setImageResource(getIconForCategory(prediction.getCategoryName()));

        holder.categoryNameTextView.setText(prediction.getCategoryName());
        holder.predictedAmountTextView.setText(String.format(Locale.getDefault(),
                "~ %,.0f đ", prediction.getPredictedAmount()));
    }

    @Override
    public int getItemCount() {
        return predictionList.size();
    }

    // ✅ Hàm lấy icon (Copy từ TransactionAdapter và chỉnh sửa nếu cần)
    private int getIconForCategory(String category) {
        if (category == null) return R.drawable.ic_expense;
        switch (category) {
            case "Ăn uống": return R.drawable.ic_eating;
            case "Mua sắm": return R.drawable.ic_shopping;
            case "Di chuyển": return R.drawable.ic_transport;
            case "Hóa đơn": return R.drawable.ic_bill;
            case "Giải trí": return R.drawable.ic_entertainment;
            case "Sức khỏe": return R.drawable.ic_health;
            case "Khác": return R.drawable.ic_question_mark; // Icon dấu hỏi cho Khác
            // Thêm các case khác nếu có
            default: return R.drawable.ic_expense; // Icon mặc định
        }
    }

    // ✅ Cập nhật ViewHolder
    static class PredictionViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIconImageView; // Thêm ImageView
        TextView categoryNameTextView, predictedAmountTextView;

        public PredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIconImageView = itemView.findViewById(R.id.categoryIconImageView); // Ánh xạ ImageView
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            predictedAmountTextView = itemView.findViewById(R.id.predictedAmountTextView);
        }
    }
}