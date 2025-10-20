package com.example.quanlychitieu;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BudgetProgressAdapter extends RecyclerView.Adapter<BudgetProgressAdapter.BudgetProgressViewHolder> {

    private List<BudgetProgress> budgetProgressList;
    private OnCategoryClickListener listener;
    // ✅ Interface for click events
    public interface OnCategoryClickListener {
        void onCategoryClick(int position);
    }

    // ✅ Method to set the listener from the Activity
    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
    public BudgetProgressAdapter(List<BudgetProgress> budgetProgressList) {
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

        if (item.getBudgetAmount() > 0) {
            double spentAmount = item.getSpentAmount();
            double budgetAmount = item.getBudgetAmount();
            int progress = (int) ((spentAmount / budgetAmount) * 100);

            holder.budgetProgressBar.setProgress(Math.min(progress, 100)); // Không vượt quá 100

            // ✅ KIỂM TRA VƯỢT NGÂN SÁCH
            if (spentAmount > budgetAmount) {
                // Vượt ngân sách
                holder.progressTextView.setText(String.format(
                        "⚠️ Đã chi: %,.0f / %,.0f đ (Vượt %,.0f đ)", // Thêm icon ⚠️ và thông báo vượt
                        spentAmount,
                        budgetAmount,
                        spentAmount - budgetAmount // Số tiền vượt
));
                holder.progressTextView.setTextColor(Color.RED); // Đổi màu chữ thành đỏ
                holder.budgetProgressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN); // Đổi màu progress bar thành đỏ
            } else {
                // Chưa vượt ngân sách
                holder.progressTextView.setText(String.format("Đã chi: %,.0f / %,.0f đ (%d%%)", spentAmount, budgetAmount, progress));
                holder.progressTextView.setTextColor(Color.DKGRAY); // Màu chữ bình thường (hoặc màu mặc định)

                // Đổi màu thanh progress nếu gần đạt
                if (progress >= 80) {
                    holder.budgetProgressBar.getProgressDrawable().setColorFilter(Color.parseColor("#FFA500"), PorterDuff.Mode.SRC_IN); // Màu cam
                } else {
                    // Reset về màu mặc định
                    holder.budgetProgressBar.getProgressDrawable().clearColorFilter();
                }
            }

        } else {
            // Không có ngân sách
            holder.budgetProgressBar.setProgress(0);
            holder.progressTextView.setText("Chưa đặt ngân sách");
            holder.progressTextView.setTextColor(Color.GRAY); // Màu chữ khác
            holder.budgetProgressBar.getProgressDrawable().clearColorFilter();
        }
    }

    @Override
    public int getItemCount() {
        return budgetProgressList.size();
    }

    class BudgetProgressViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView, progressTextView;
        ProgressBar budgetProgressBar;

        public BudgetProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            progressTextView = itemView.findViewById(R.id.progressTextView);
            budgetProgressBar = itemView.findViewById(R.id.budgetProgressBar);

            // ✅ Set OnClickListener for the whole item
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