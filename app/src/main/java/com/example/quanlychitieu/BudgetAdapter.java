package com.example.quanlychitieu;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText; // Import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Import Objects for null check

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<String> categories;
    private Map<String, Double> budgetMap;

    public BudgetAdapter(List<String> categories, Map<String, Double> budgetMap) {
        this.categories = categories;
        // Đảm bảo budgetMap không bao giờ null
        this.budgetMap = (budgetMap != null) ? new HashMap<>(budgetMap) : new HashMap<>();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryNameTextView.setText(category);
        holder.categoryIconImageView.setImageResource(getIconForCategory(category));

        // Hiển thị ngân sách hiện tại nếu có
        Double currentBudget = budgetMap.get(category);
        if (currentBudget != null && currentBudget > 0) {
            // Sử dụng Objects.requireNonNull để tránh cảnh báo NPE tiềm ẩn
            holder.budgetAmountEditText.setText(String.valueOf(currentBudget.longValue()));
        } else {
            holder.budgetAmountEditText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public Map<String, Double> getBudgetData() {
        return budgetMap;
    }

    // ✅ Hàm lấy icon
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
    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIconImageView;
        TextView categoryNameTextView;
        TextInputEditText budgetAmountEditText; // Đổi kiểu thành TextInputEditText

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIconImageView = itemView.findViewById(R.id.categoryIconImageView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            budgetAmountEditText = itemView.findViewById(R.id.budgetAmountEditText); // ID của TextInputEditText bên trong

            // TextWatcher giữ nguyên logic, chỉ cần đảm bảo nó áp dụng cho TextInputEditText
            budgetAmountEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String category = categories.get(position);
                        try {
                            // Sử dụng Objects.requireNonNull để tránh cảnh báo
                            double amount = Double.parseDouble(Objects.requireNonNull(s).toString());
                            budgetMap.put(category, amount);
                        } catch (NumberFormatException | NullPointerException e) {
                            budgetMap.put(category, 0.0);
                        }
                    }
                }
            });
        }
    }
}