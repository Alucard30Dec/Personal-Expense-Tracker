package com.example.quanlychitieu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DebtListActivity extends AppCompatActivity {

    private RecyclerView debtRecyclerView;
    private DebtAdapter adapter;
    private FloatingActionButton fabAddDebt;
    private DebtDao debtDao;

    private static final int ADD_DEBT_REQUEST = 1;
    private static final int EDIT_DEBT_REQUEST = 2; // Thêm mã cho Sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        debtRecyclerView = findViewById(R.id.debtRecyclerView);
        fabAddDebt = findViewById(R.id.fabAddDebt);
        debtDao = AppDatabase.getDatabase(this).debtDao();

        setupRecyclerView();

        fabAddDebt.setOnClickListener(v -> {
            Intent intent = new Intent(DebtListActivity.this, AddEditDebtActivity.class);
            startActivityForResult(intent, ADD_DEBT_REQUEST);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDebts(); // Luôn tải lại dữ liệu khi quay lại
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter với listener mới
        adapter = new DebtAdapter(this, new ArrayList<>(), new DebtAdapter.OnDebtActionListener() {
            @Override
            public void onEditClick(Debt debt) {
                Intent intent = new Intent(DebtListActivity.this, AddEditDebtActivity.class);
                // Giờ đây 'debt' đã là một Serializable
                intent.putExtra("EDIT_DEBT_DATA", debt); // ✅ Bỏ ép kiểu (Serializable)
                startActivityForResult(intent, EDIT_DEBT_REQUEST);
            }

            @Override
            public void onDeleteClick(Debt debt) {
                // Hiển thị hộp thoại xác nhận Xóa
                new AlertDialog.Builder(DebtListActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa khoản mục '" + debt.getName() + "' không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            debtDao.delete(debt); // Xóa khỏi DB
                            loadDebts();          // Tải lại danh sách
                            Toast.makeText(DebtListActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        debtRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        debtRecyclerView.setAdapter(adapter);
    }

    private void loadDebts() {
        List<Debt> updatedList = debtDao.getAllDebts();
        adapter.updateList((updatedList != null) ? updatedList : new ArrayList<>());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_DEBT_REQUEST || requestCode == EDIT_DEBT_REQUEST) {
                // Không cần gọi loadDebts() ở đây nữa vì nó đã được gọi trong onResume()
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}