package com.example.quanlychitieu;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
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
import android.text.SpannableString; // Thêm import
import android.text.style.ForegroundColorSpan; // Thêm import
import android.graphics.Color; // Thêm import
import android.util.Log; // ✅ THÊM DÒNG NÀY
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

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
    private ChipGroup filterChipGroup; // ✅ Đổi kiểu và tên biến
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // --- KIỂM TRA ĐĂNG NHẬP (Dùng Preferences tạm thời) ---
        SharedPreferences tempPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!tempPrefs.getBoolean(PREF_LOGGED_IN, false)) {
            // Chuyển về LoginActivity nếu chưa đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Dừng thực thi onCreate
        }

        // --- KHỞI TẠO BIẾN prefs CHO TOÀN BỘ CLASS ---
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // --- ÁP DỤNG THEME ĐÃ LƯU ---
        int currentNightMode = prefs.getInt(PREF_NIGHT_MODE, MODE_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(currentNightMode);

        // ✅ DI CHUYỂN super.onCreate XUỐNG ĐÂY
        super.onCreate(savedInstanceState);

        // --- THIẾT LẬP GIAO DIỆN ---
        setContentView(R.layout.activity_main);
        // ✅ BƯỚC 1: KHỞI TẠO DATABASE VÀ DAO
        db = AppDatabase.getDatabase(getApplicationContext());
        transactionDao = db.transactionDao();

        // --- Cài đặt Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- Ánh xạ các View ---
        totalBalanceTextView = findViewById(R.id.totalBalanceTextView);
        filterChipGroup = findViewById(R.id.filterChipGroup); // ✅ Ánh xạ ChipGroup
        mainAddFab = findViewById(R.id.mainAddFab);
        addIncomeFab = findViewById(R.id.addIncomeFab);
        addExpenseFab = findViewById(R.id.addExpenseFab);
        // ✅ ÁNH XẠ CÁC CARDVIEW
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

        // ✅ BƯỚC 2: NẠP DỮ LIỆU TỪ DATABASE KHI KHỞI ĐỘNG
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

        // --- XỬ LÝ KẾT QUẢ TỪ MÀN HÌNH "THÊM MỚI" (Code cũ của bạn) ---
        if ((requestCode == ADD_EXPENSE_REQUEST || requestCode == ADD_INCOME_REQUEST) && resultCode == Activity.RESULT_OK && data != null) {
            // Lấy dữ liệu thêm mới từ Intent
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
                // Thêm vào database và nạp lại dữ liệu
                transactionDao.insert(newTransaction);
                Toast.makeText(this, "Đã thêm giao dịch!", Toast.LENGTH_SHORT).show();
                loadTransactionsFromDb();
            }
        }
        // --- KẾT THÚC XỬ LÝ THÊM MỚI ---


        // --- XỬ LÝ KẾT QUẢ TỪ MÀN HÌNH "CHI TIẾT" (Phần logic mới) ---
        else if (requestCode == DETAIL_REQUEST_CODE && data != null) {
            // Trường hợp người dùng chọn XÓA
            if (resultCode == TransactionDetailActivity.RESULT_DELETED) {
                Transaction transactionToDelete = (Transaction) data.getSerializableExtra("TRANSACTION_TO_DELETE");
                if (transactionToDelete != null) {
                    transactionDao.delete(transactionToDelete);
                    Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    loadTransactionsFromDb(); // Nạp lại dữ liệu sau khi xóa
                }
            }
            // Trường hợp người dùng chọn SỬA và đã CẬP NHẬT
            else if (resultCode == TransactionDetailActivity.RESULT_EDITED) {
                Transaction editedTransaction = (Transaction) data.getSerializableExtra("EDITED_TRANSACTION_DATA");
                if (editedTransaction != null) {
                    transactionDao.update(editedTransaction);
                    Toast.makeText(this, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
                    loadTransactionsFromDb(); // Nạp lại dữ liệu sau khi sửa
                }
            }
        }
    }

    /**
     * Xử lý sự kiện nhấn để XÓA một giao dịch khỏi database.
     */
    @Override
    public void onItemClick(int position) {
        Transaction clickedTransaction = transactionAdapter.getTransactionAt(position);
        if (clickedTransaction == null) return;

        Intent intent = new Intent(this, TransactionDetailActivity.class);
        intent.putExtra("TRANSACTION_DATA", clickedTransaction);
        startActivityForResult(intent, DETAIL_REQUEST_CODE);
    }

    // --------------------------------------------------------------------
    // CÁC HÀM TIỆN ÍCH (Giữ nguyên, không thay đổi)
    // --------------------------------------------------------------------

    private void setupFabListeners() {
        isAllFabsVisible = false;
        mainAddFab.setOnClickListener(view -> {
            if (!isAllFabsVisible) {
                // ✅ HIỆN CARDVIEW
                addIncomeCard.setVisibility(View.VISIBLE);
                addExpenseCard.setVisibility(View.VISIBLE);
                isAllFabsVisible = true;
            } else {
                // ✅ ẨN CARDVIEW
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
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // checkedIds là một List<Integer> chứa ID của chip được chọn
            // Vì là singleSelection nên list này thường chỉ có 1 phần tử hoặc rỗng
            if (!checkedIds.isEmpty()) {
                filterTransactions(getSelectedFilter()); // Gọi hàm lọc như cũ
            } else {
                // Xử lý trường hợp không có chip nào được chọn (nếu cần)
                // Ví dụ: Mặc định về "Tất cả"
                filterChipGroup.check(R.id.chip_all); // Tự động chọn lại "Tất cả"
            }
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
                // 1. KIỂM TRA MỐC THỜI GIAN
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

                // 2. NẾU KHỚP MỐC THỜI GIAN, KIỂM TRA TIẾP TỪ KHÓA TÌM KIẾM
                if (matchesPeriod) {
                    boolean matchesSearch = true; // Mặc định là khớp nếu không có từ khóa
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

        // ✅ CẬP NHẬT TEXTVIEW MỚI
        summaryIncomeTextView.setText(String.format("%,.0f đ", totalIncome));
        summaryExpenseTextView.setText(String.format("%,.0f đ", totalExpense));

        // Cập nhật tổng số dư (giữ nguyên)
        totalBalanceTextView.setText(String.format("%,.0f đ", balance));
    }

    private String getSelectedFilter() {
        // ✅ Lấy ID chip được chọn từ ChipGroup
        int checkedId = filterChipGroup.getCheckedChipId();

        if (checkedId == R.id.chip_today) return "today";
        if (checkedId == R.id.chip_week) return "week";
        if (checkedId == R.id.chip_month) return "month";
        // Mặc định là "all" nếu không có gì được chọn hoặc ID là chip_all
        return "all";
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // --- KIỂM TRA NỢ ĐẾN HẠN/QUÁ HẠN ---
        Calendar cal = Calendar.getInstance();
        // Set time to the end of the day to include today's due dates
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date today = cal.getTime();

        int overdueCount = 0;
        // Make sure db and transactionDao are initialized before calling this
        if (db != null && db.debtDao() != null) {
            DebtDao debtDao = db.debtDao(); // Get the DAO instance
            overdueCount = debtDao.countOverdueOrDueTodayDebts(today);
        } else {
            Log.e("MainActivity", "Database or DebtDao not initialized in onCreateOptionsMenu");
            // Handle the case where the database is not ready yet, maybe just don't highlight
        }


        MenuItem debtMenuItem = menu.findItem(R.id.action_debt);
        if (debtMenuItem != null) {
            if (overdueCount > 0) {
                // Apply red color span if there are overdue debts
                SpannableString spannableTitle = new SpannableString(debtMenuItem.getTitle());
                spannableTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableTitle.length(), 0);
                debtMenuItem.setTitle(spannableTitle);
                // Optionally add a warning icon: debtMenuItem.setIcon(R.drawable.ic_warning);
            } else {
                // No overdue debts, ensure default title/color (usually handled automatically by menu inflation)
                // You could explicitly reset it if needed, but often not necessary:
                // debtMenuItem.setTitle(getString(R.string.action_debt_title)); // Assuming you have this string resource
            }
        }
        // --- KẾT THÚC KIỂM TRA NỢ ---

        // --- CÀI ĐẶT SEARCHVIEW (Code cũ của bạn) ---
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint("Tìm theo ghi chú, hạng mục...");

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true; // Allow expand
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // When SearchView collapses
                    currentSearchQuery = ""; // Clear search filter
                    filterTransactions(getSelectedFilter()); // Refilter list
                    return true; // Allow collapse
                }
            });
            searchView.setOnCloseListener(() -> {
                currentSearchQuery = "";
                filterTransactions(getSelectedFilter());
                return false; // Allow SearchView to close
            });
        }
        return true; // Return true to display the menu
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
            String facebookUrl = "https://www.facebook.com/Nhan271104"; // Thay bằng link FB của bạn
            try {
                // Thử mở bằng app Facebook
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + facebookUrl)));
            } catch (Exception e) {
                // Nếu không có app, mở bằng trình duyệt
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
            return true;
        }
        else if (itemId == R.id.action_budget) {
            Intent intent = new Intent(this, BudgetActivity.class);
            startActivity(intent);
            return true;
        }
        // ✅ THÊM XỬ LÝ CHO action_theme
        else if (itemId == R.id.action_theme) {
            showThemeDialog(); // Gọi hàm hiển thị hộp thoại
            return true;
        }
        // ✅ THÊM XỬ LÝ CHO NÚT ĐĂNG XUẤT
        else if (itemId == R.id.action_logout) {
            logoutUser(); // Gọi hàm đăng xuất
            return true;
        }
        else if (itemId == R.id.action_debt) {
            Intent intent = new Intent(this, DebtListActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.action_predict) {
            Intent intent = new Intent(this, PredictionActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // ✅ HÀM MỚI ĐỂ HIỂN THỊ HỘP THOẠI CHỌN THEME
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
                        case 0: // Sáng
                            selectedMode = MODE_LIGHT;
                            break;
                        case 1: // Tối
                            selectedMode = MODE_DARK;
                            break;
                        case 2: // Theo hệ thống
                        default:
                            selectedMode = MODE_SYSTEM;
                            break;
                    }
                    // Lưu lựa chọn mới
                    prefs.edit().putInt(PREF_NIGHT_MODE, selectedMode).apply();
                    // Áp dụng theme ngay lập tức
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    dialog.dismiss();
                    // recreate(); // Có thể gọi recreate() để Activity vẽ lại ngay lập tức, nhưng setDefaultNightMode thường đủ
                })
                .show();
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        currentSearchQuery = query != null ? query.toLowerCase().trim() : ""; // ✅ CẬP NHẬT BIẾN
        filterTransactions(getSelectedFilter());
        // Bạn có thể ẩn bàn phím ở đây nếu muốn
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentSearchQuery = newText != null ? newText.toLowerCase().trim() : ""; // ✅ CẬP NHẬT BIẾN
        filterTransactions(getSelectedFilter()); // Lọc lại danh sách
        return true;
    }
    // ✅ HÀM ĐĂNG XUẤT
    private void logoutUser() {
        // Xóa trạng thái đăng nhập SharedPreferences
        prefs.edit().putBoolean(PREF_LOGGED_IN, false).apply();
        // (Tùy chọn) Xóa ID/email người dùng đã lưu
        // prefs.edit().remove("CURRENT_USER_ID").apply();
        // prefs.edit().remove("CURRENT_USER_EMAIL").apply();


        // ✅ ĐĂNG XUẤT KHỎI GOOGLE
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Phải giống với GSO khi đăng nhập
                .build();
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener(task -> {
            Log.d("MainActivity", "Đã đăng xuất khỏi Google.");
        });

        // Chuyển về màn hình Đăng nhập
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu(); // Yêu cầu vẽ lại menu để cập nhật màu sắc
        // Có thể gọi lại loadTransactionsFromDb() ở đây nếu cần cập nhật cả danh sách
        // loadTransactionsFromDb();
    }
}