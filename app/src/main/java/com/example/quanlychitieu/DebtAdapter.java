package com.example.quanlychitieu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    private List<Debt> debtList;
    private OnDebtActionListener listener; // Sử dụng interface mới
    private Context context;

    // Interface mới chỉ có Edit và Delete
    public interface OnDebtActionListener {
        void onEditClick(Debt debt);
        void onDeleteClick(Debt debt);
    }

    public DebtAdapter(Context context, List<Debt> debtList, OnDebtActionListener listener) {
        this.context = context;
        this.debtList = debtList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        Debt debt = debtList.get(position);

        holder.debtNameTextView.setText(debt.getName());

        String amountPrefix;
        int amountColor;
        int iconResId;
        String statusText;

        if (debt.isLending()) { // Tôi cho vay
            iconResId = R.drawable.ic_debt_lend;
            amountPrefix = "+ ";
            amountColor = ContextCompat.getColor(context, R.color.my_green_primary); // Đảm bảo màu này tồn tại
            statusText = "Đã thu";
        } else { // Tôi đi vay
            iconResId = R.drawable.ic_debt_loan;
            amountPrefix = "- ";
            amountColor = Color.RED;
            statusText = "Đã trả";
        }

        holder.debtTypeIcon.setImageResource(iconResId);
        holder.debtAmountTextView.setText(String.format(Locale.getDefault(),
                "Số tiền: %s%,.0f đ", amountPrefix, debt.getAmount()));
        holder.debtAmountTextView.setTextColor(amountColor);
        holder.paidCheckBox.setText(statusText);

        if (debt.getDueDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.debtDueDateTextView.setText("Hạn trả: " + sdf.format(debt.getDueDate()));
            holder.debtDueDateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.debtDueDateTextView.setVisibility(View.GONE);
        }

        // --- Xử lý CheckBox (Chỉ hiển thị) ---
        holder.paidCheckBox.setOnCheckedChangeListener(null); // Bỏ listener
        holder.paidCheckBox.setChecked(debt.isPaid());
        holder.paidCheckBox.setEnabled(false); // ✅ Vô hiệu hóa CheckBox

        if (debt.isPaid()) {
            holder.debtNameTextView.setPaintFlags(holder.debtNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.debtNameTextView.setAlpha(0.6f);
            holder.paidCheckBox.setTextColor(ContextCompat.getColor(context, R.color.my_green_primary));
        } else {
            holder.debtNameTextView.setPaintFlags(holder.debtNameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.debtNameTextView.setAlpha(1.0f);
            holder.paidCheckBox.setTextColor(ContextCompat.getColor(context, android.R.color.tab_indicator_text));
        }
    }

    @Override
    public int getItemCount() {
        return (debtList != null) ? debtList.size() : 0;
    }

    public void updateList(List<Debt> newList) {
        debtList = newList;
        notifyDataSetChanged();
    }

    // Hàm lấy Giao dịch tại vị trí (dùng cho MainActivity)
    public Debt getDebtAt(int position) {
        if (position >= 0 && position < debtList.size()) {
            return debtList.get(position);
        }
        return null;
    }

    // ViewHolder với các nút Edit/Delete
    public class DebtViewHolder extends RecyclerView.ViewHolder {
        ImageView debtTypeIcon;
        TextView debtNameTextView, debtAmountTextView, debtDueDateTextView;
        CheckBox paidCheckBox;
        ImageButton editDebtButton, deleteDebtButton; // Nút Sửa/Xóa

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            debtTypeIcon = itemView.findViewById(R.id.debtTypeIcon);
            debtNameTextView = itemView.findViewById(R.id.debtDescriptionTextView); // ID đã sửa
            debtAmountTextView = itemView.findViewById(R.id.debtAmountTextView);
            debtDueDateTextView = itemView.findViewById(R.id.dueDateTextView);
            paidCheckBox = itemView.findViewById(R.id.statusCheckBox);
            editDebtButton = itemView.findViewById(R.id.editDebtButton); // Ánh xạ
            deleteDebtButton = itemView.findViewById(R.id.deleteDebtButton); // Ánh xạ

            editDebtButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && debtList != null) {
                    listener.onEditClick(debtList.get(position));
                }
            });

            deleteDebtButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && debtList != null) {
                    listener.onDeleteClick(debtList.get(position));
                }
            });
        }
    }
}