package com.example.quanlychitieu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.android.material.card.MaterialCardView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements
        TransactionAdapter.OnItemClickListener,
        SearchView.OnQueryTextListener {

    private static final String PREF_NIGHT_MODE = "pref_night_mode";
    private static final String PREF_LOGGED_IN = "pref_logged_in";
    private static final int MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    private static final int MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES;
    private static final int MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    // --- Biến giao diện ---
    private RecyclerView transactionsRecyclerView;
    private TransactionAdapter transactionAdapter;
    private RadioGroup filterRadioGroup;
    private TextView totalBalanceTextView;
    private ImageButton mainAddFab, addIncomeFab, addExpenseFab;
    private String currentSearchQuery = "";

    // --- Biến dữ liệu & Database ---
    private List<Transaction> allTransactionsList; // Danh sách này sẽ chứa TẤT CẢ giao dịch từ DB
    private AppDatabase db;
    private TransactionDao transactionDao;
    private boolean isAllFabsVisible;
    private static final int ADD_EXPENSE_REQUEST = 1;
    private static final int ADD_INCOME_REQUEST = 2;
    private static final int DETAIL_REQUEST_CODE = 3;
    private TextView summaryIncomeTextView;
    private TextView summaryExpenseTextView;
    private MaterialCardView addIncomeCard, addExpenseCard;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ 1) Luôn gọi super trước khi có bất kỳ return nào
        super.onCreate(savedInstanceState);

        // ✅ 2) Khởi tạo SharedPreferences dùng chung
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // ✅ 3) Áp dụng theme (Night Mode) trước khi setContentView
        int currentNightMode = prefs.getInt(PREF_NIGHT_MODE, MODE_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(currentNightMode);

        // ✅ 4) Kiểm tra đăng nhập: nếu chưa login → sang LoginActivity và kết thúc Main
        if (!prefs.getBoolean(PREF_LOGGED_IN, false)) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();   // Kết thúc Activity đúng vòng đời
            return;     // an toàn
        }

        // ✅ 5) Inflate UI
        setContentView(R.layout.activity_main);

        // ✅ 6) Khởi tạo DB/DAO
        db = AppDatabase.getDatabase(getApplicationContext());
        transactionDao = db.transactionDao();

        // --- Cài đặt Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- Ánh xạ các View ---
        totalBalanceTextView = findViewById(R.id.totalBalanceTextView);
        filterRadioGroup = findViewById(R.id.filterRadioGroup);
        mainAddFab = findViewById(R.id.mainAddFab);
        addIncomeFab = findViewById(R.id.addIncomeFab);
        addExpenseFab = findViewById(R.id.addExpenseFab);
        addIncomeCard = findViewById(R.id.addIncomeCard);
        addExpenseCard = findViewById(R.id.addExpenseCard);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        summaryIncomeTextView = findViewById(R.id.summaryIncomeTextView);
        summaryExpenseTextView = findViewById(R.id.summaryExpenseTextView);

        // --- Cài đặt RecyclerView ---
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>()); // Bắt đầu với danh sách rỗng
        transactionsRecyclerView.setAdapter(transactionAdapter);
        transactionAdapter.setOnItemClickListener(this);

        // --- Cài đặt Listeners ---
        setupFabListeners();
        setupFilterListener();

        // ✅ 7) Nạp dữ liệu DB
        loadTransactionsFromDb();
    }

    /**
     * Nạp tất cả giao dịch từ database và cập nhật giao diện.
     */
    private void loadTransactionsFromDb() {
        this.allTransactionsList = transactionDao.getAllTransactions();
        // Áp dụng bộ lọc hiện tại để hiển thị đúng danh sách
        filterTransactions(getSelectedFilter());
    }

    /**
     * Xử lý kết quả trả về từ màn hình Thêm/Sửa để LƯU vào database.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // --- Thêm mới ---
        if ((requestCode == ADD_EXPENSE_REQUEST || requestCode == ADD_INCOME_REQUEST)
                && resultCode == Activity.RESULT_OK && data != null) {

            double amount = data.getDoubleExtra("EXTRA_AMOUNT", 0);
            String category = data.getStringExtra("EXTRA_CATEGORY");
            String note = data.getStringExtra("EXTRA_NOTE");
            long dateInMillis = data.getLongExtra("EXTRA_DATE", System.currentTimeMillis());
            Date date = new Date(dateInMillis);

            Transaction newTransaction = null;
            if (requestCode == ADD_EXPENSE_REQUEST) {
                newTransaction = new Transaction(category, note, amount, true, date);
            } else if (requestCode == ADD_INCOME_REQUEST) {
                newTransaction = new Transaction(category, note, amount, false, date);
            }

            if (newTransaction != null) {
                transactionDao.insert(newTransaction);
                Toast.makeText(this, "Đã thêm giao dịch!", Toast.LENGTH_SHORT).show();
                loadTransactionsFromDb();
            }
        }
        // --- Chi tiết: xóa / sửa ---
        else if (requestCode == DETAIL_REQUEST_CODE && data != null) {
            if (resultCode == TransactionDetailActivity.RESULT_DELETED) {
                Transaction transactionToDelete = (Transaction) data.getSerializableExtra("TRANSACTION_TO_DELETE");
                if (transactionToDelete != null) {
                    transactionDao.delete(transactionToDelete);
                    Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    loadTransactionsFromDb();
                }
            } else if (resultCode == TransactionDetailActivity.RESULT_EDITED) {
                Transaction editedTransaction = (Transaction) data.getSerializableExtra("EDITED_TRANSACTION_DATA");
                if (editedTransaction != null) {
                    transactionDao.update(editedTransaction);
                    Toast.makeText(this, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
                    loadTransactionsFromDb();
                }
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        Transaction clickedTransaction = transactionAdapter.getTransactionAt(position);
        if (clickedTransaction == null) return;

        Intent intent = new Intent(this, TransactionDetailActivity.class);
        intent.putExtra("TRANSACTION_DATA", clickedTransaction);
        startActivityForResult(intent, DETAIL_REQUEST_CODE);
    }

    private void setupFabListeners() {
        isAllFabsVisible = false;
        mainAddFab.setOnClickListener(view -> {
            if (!isAllFabsVisible) {
                addIncomeCard.setVisibility(View.VISIBLE);
                addExpenseCard.setVisibility(View.VISIBLE);
                isAllFabsVisible = true;
            } else {
                addIncomeCard.setVisibility(View.GONE);
                addExpenseCard.setVisibility(View.GONE);
                isAllFabsVisible = false;
            }
        });
        addExpenseFab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivityForResult(intent, ADD_EXPENSE_REQUEST);
        });
        addIncomeFab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddIncomeActivity.class);
            startActivityForResult(intent, ADD_INCOME_REQUEST);
        });
    }

    private void setupFilterListener() {
        filterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            filterTransactions(getSelectedFilter());
        });
    }

    private void filterTransactions(String period) {
        List<Transaction> filteredList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date startOfToday = cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date startOfWeek = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();

        if (allTransactionsList != null) {
            for (Transaction transaction : allTransactionsList) {
                boolean matchesPeriod = false;
                switch (period) {
                    case "today":
                        if (!transaction.getDate().before(startOfToday)) matchesPeriod = true;
                        break;
                    case "week":
                        if (!transaction.getDate().before(startOfWeek)) matchesPeriod = true;
                        break;
                    case "month":
                        if (!transaction.getDate().before(startOfMonth)) matchesPeriod = true;
                        break;
                    case "all":
                    default:
                        matchesPeriod = true;
                        break;
                }

                if (matchesPeriod) {
                    boolean matchesSearch = true;
                    if (!currentSearchQuery.isEmpty()) {
                        String noteLower = transaction.getNote() != null ? transaction.getNote().toLowerCase() : "";
                        String categoryLower = transaction.getCategory() != null ? transaction.getCategory().toLowerCase() : "";
                        matchesSearch = noteLower.contains(currentSearchQuery) || categoryLower.contains(currentSearchQuery);
                    }

                    if (matchesSearch) {
                        filteredList.add(transaction);
                    }
                }
            }
        }
        transactionAdapter.filterList(filteredList);
        updateTotalBalance(filteredList);
    }

    private void updateTotalBalance(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        summaryIncomeTextView.setText(String.format("%,.0f đ", totalIncome));
        summaryExpenseTextView.setText(String.format("%,.0f đ", totalExpense));
        totalBalanceTextView.setText(String.format("%,.0f đ", balance));
    }

    private String getSelectedFilter() {
        int checkedId = filterRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_today) return "today";
        if (checkedId == R.id.radio_week) return "week";
        if (checkedId == R.id.radio_month) return "month";
        return "all";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint("Tìm theo ghi chú, hạng mục...");

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) { return true; }
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    currentSearchQuery = "";
                    filterTransactions(getSelectedFilter());
                    return true;
                }
            });
            searchView.setOnCloseListener(() -> {
                currentSearchQuery = "";
                filterTransactions(getSelectedFilter());
                return false;
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_statistics) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("TRANSACTION_LIST", (ArrayList<Transaction>) allTransactionsList);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_support) {
            String facebookUrl = "https://www.facebook.com/Nhan271104";
            try {
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + facebookUrl)));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
            return true;
        } else if (itemId == R.id.action_budget) {
            Intent intent = new Intent(this, BudgetActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_theme) {
            showThemeDialog();
            return true;
        } else if (itemId == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showThemeDialog() {
        final String[] themes = {"Sáng", "Tối", "Theo hệ thống"};
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentNightMode = prefs.getInt(PREF_NIGHT_MODE, MODE_SYSTEM);

        int checkedItem;
        if (currentNightMode == MODE_LIGHT) {
            checkedItem = 0;
        } else if (currentNightMode == MODE_DARK) {
            checkedItem = 1;
        } else {
            checkedItem = 2;
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn Chế độ Giao diện")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedMode;
                    switch (which) {
                        case 0: selectedMode = MODE_LIGHT; break;
                        case 1: selectedMode = MODE_DARK; break;
                        case 2:
                        default: selectedMode = MODE_SYSTEM; break;
                    }
                    prefs.edit().putInt(PREF_NIGHT_MODE, selectedMode).apply();
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        currentSearchQuery = query != null ? query.toLowerCase().trim() : "";
        filterTransactions(getSelectedFilter());
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentSearchQuery = newText != null ? newText.toLowerCase().trim() : "";
        filterTransactions(getSelectedFilter());
        return true;
    }

    private void logoutUser() {
        prefs.edit().putBoolean(PREF_LOGGED_IN, false).apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}
