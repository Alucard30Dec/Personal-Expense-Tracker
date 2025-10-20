package com.example.quanlychitieu;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // QUAN TRỌNG: Thêm import này
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    // ✅ THÊM HÀM SỐ 1 VÀO ĐÂY
    public void filterList(List<Transaction> filteredList) {
        transactionList = filteredList;
        notifyDataSetChanged();
    }

    // ✅ THÊM HÀM SỐ 2 VÀO ĐÂY
    public Transaction getTransactionAt(int position) {
        if (position >= 0 && position < transactionList.size()) {
            return transactionList.get(position);
        }
        return null;
    }
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.categoryName.setText(transaction.getCategory());
        holder.note.setText(transaction.getNote());

        // GỌI HÀM MỚI ĐỂ LẤY ICON VÀ SET CHO IMAGEVIEW
        holder.categoryIcon.setImageResource(getIconForCategory(transaction.getCategory()));

        if (transaction.isExpense()) {
            String expenseAmount = String.format("-%,.0f đ", transaction.getAmount());
            holder.amount.setText(expenseAmount);
            holder.amount.setTextColor(Color.RED);
        } else {
            String incomeAmount = String.format("+%,.0f đ", transaction.getAmount());
            holder.amount.setText(incomeAmount);
            holder.amount.setTextColor(Color.parseColor("#4CAF50"));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // HÀM MỚI ĐỂ CHỌN ICON DỰA TRÊN TÊN HẠNG MỤC
    private int getIconForCategory(String category) {
        switch (category) {
            case "Ăn uống":
                return R.drawable.ic_eating;
            case "Mua sắm":
                return R.drawable.ic_shopping;
            case "Di chuyển":
                return R.drawable.ic_transport;
            case "Hóa đơn":
                return R.drawable.ic_bill;
            case "Giải trí":
                return R.drawable.ic_entertainment;
            case "Sức khỏe":
                return R.drawable.ic_health;
            case "Lương":
            case "Thưởng":
            case "Bán hàng":
            case "Quà tặng":
            case "Đầu tư":
                return R.drawable.ic_wallet;
            default:
                // Icon mặc định nếu không khớp
                return R.drawable.ic_expense;
        }
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        // THÊM BIẾN CHO ICON IMAGEVIEW
        ImageView categoryIcon;
        TextView categoryName, note, amount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            // ÁNH XẠ ICON IMAGEVIEW TỪ LAYOUT
            categoryIcon = itemView.findViewById(R.id.categoryIconImageView);
            categoryName = itemView.findViewById(R.id.categoryNameTextView);
            note = itemView.findViewById(R.id.transactionNoteTextView);
            amount = itemView.findViewById(R.id.amountTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}