package com.example.quanlychitieu;

import android.content.Context; // Import Context
import android.graphics.Color;
// import android.graphics.PorterDuff; // No longer needed for LinearProgressIndicator tint
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import ImageView
// import android.widget.ProgressBar; // Replace with LinearProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator; // Import Material Progress
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class BudgetProgressAdapter extends RecyclerView.Adapter<BudgetProgressAdapter.BudgetProgressViewHolder> {

    private List<BudgetProgress> budgetProgressList;
    private OnCategoryClickListener listener;
    private Context context; // Store context for colors

    public interface OnCategoryClickListener {
        void onCategoryClick(int position);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    // Modified constructor to get context
    public BudgetProgressAdapter(Context context, List<BudgetProgress> budgetProgressList) {
        this.context = context;
        this.budgetProgressList = budgetProgressList;
    }

    @NonNull
    @Override
    public BudgetProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_progress, parent, false);
        return new BudgetProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetProgressViewHolder holder, int position) {
        BudgetProgress item = budgetProgressList.get(position);

        holder.categoryNameTextView.setText(item.getCategoryName());
        holder.categoryIconImageView.setImageResource(getIconForCategory(item.getCategoryName()));

        double spentAmount = item.getSpentAmount();
        double budgetAmount = item.getBudgetAmount();

        holder.spentTextView.setText(String.format(Locale.getDefault(), "Đã chi: %,.0f đ", spentAmount));

        if (budgetAmount > 0) {
            holder.budgetTextView.setText(String.format(Locale.getDefault(), "Ngân sách: %,.0f đ", budgetAmount));
            int progress = (int) ((spentAmount / budgetAmount) * 100);
            holder.budgetProgressBar.setProgressCompat(Math.min(progress, 100), true); // Animate progress

            // Set status text and progress bar color based on spending
            if (spentAmount > budgetAmount) {
                double overspent = spentAmount - budgetAmount;
                holder.statusTextView.setText(String.format(Locale.getDefault(), "⚠️ Vượt %,.0f đ", overspent));
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.error_dark)); // Use defined error color
                holder.budgetProgressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.error_dark));
            } else {
                double remaining = budgetAmount - spentAmount;
                holder.statusTextView.setText(String.format(Locale.getDefault(), "Còn lại: %,.0f đ (%d%%)", remaining, 100 - progress));
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray)); // Use a less prominent color

                // Set progress bar color based on percentage
                if (progress >= 90) { // Nearing limit
                    holder.budgetProgressBar.setIndicatorColor(Color.parseColor("#FFA500")); // Orange
                } else { // Normal spending
                    holder.budgetProgressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.my_green_primary)); // Use your primary color
                }
            }
            holder.budgetTextView.setVisibility(View.VISIBLE); // Show budget text
            holder.budgetProgressBar.setVisibility(View.VISIBLE); // Show progress bar

        } else {
            // No budget set
            holder.budgetTextView.setVisibility(View.GONE); // Hide budget text
            holder.budgetProgressBar.setVisibility(View.INVISIBLE); // Hide progress bar (INVISIBLE keeps space)
            holder.statusTextView.setText("Chưa đặt ngân sách");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return budgetProgressList.size();
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
            case "Khác": return R.drawable.ic_question_mark;
            default: return R.drawable.ic_expense;
        }
    }


    // ✅ Cập nhật ViewHolder
    class BudgetProgressViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIconImageView;
        TextView categoryNameTextView, spentTextView, budgetTextView, statusTextView;
        LinearProgressIndicator budgetProgressBar; // Use LinearProgressIndicator

        public BudgetProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIconImageView = itemView.findViewById(R.id.categoryIconImageView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            spentTextView = itemView.findViewById(R.id.spentTextView);
            budgetTextView = itemView.findViewById(R.id.budgetTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            budgetProgressBar = itemView.findViewById(R.id.budgetProgressBar);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCategoryClick(position);
                    }
                }
            });
        }
    }
}