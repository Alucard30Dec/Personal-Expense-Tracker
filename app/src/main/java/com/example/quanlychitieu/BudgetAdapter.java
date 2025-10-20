package com.example.quanlychitieu;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<String> categories; // Danh sách các hạng mục chi tiêu
    private Map<String, Double> budgetMap; // Lưu trữ ngân sách hiện tại

    // Constructor nhận danh sách hạng mục và map ngân sách
    public BudgetAdapter(List<String> categories, Map<String, Double> budgetMap) {
        this.categories = categories;
        this.budgetMap = budgetMap != null ? budgetMap : new HashMap<>();
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

        // Hiển thị ngân sách hiện tại nếu có
        Double currentBudget = budgetMap.get(category);
        if (currentBudget != null && currentBudget > 0) {
            holder.budgetAmountEditText.setText(String.valueOf(currentBudget.longValue()));
        } else {
            holder.budgetAmountEditText.setText(""); // Để trống nếu chưa set
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // Lấy Map chứa các giá trị ngân sách người dùng đã nhập/sửa
    public Map<String, Double> getBudgetData() {
        return budgetMap;
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        EditText budgetAmountEditText;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            budgetAmountEditText = itemView.findViewById(R.id.budgetAmountEditText);

            // Lắng nghe sự thay đổi trong EditText để cập nhật budgetMap
            budgetAmountEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String category = categories.get(position);
                        try {
                            double amount = Double.parseDouble(s.toString());
                            budgetMap.put(category, amount);
                        } catch (NumberFormatException e) {
                            // Nếu người dùng xóa hết số thì coi như budget = 0
                            budgetMap.put(category, 0.0);
                        }
                    }
                }
            });
        }
    }
}