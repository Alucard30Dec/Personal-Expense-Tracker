package com.example.quanlychitieu;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager; // Hoặc androidx.preference.PreferenceManager nếu dùng AndroidX
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private SharedPreferences prefs;

    // Dùng chung key với LoginActivity
    private static final String PREF_USERNAME = "pref_username";
    private static final String PREF_PASSWORD = "pref_password"; // !! LƯU Ý BẢO MẬT !!
    private static final String PREF_LOGGED_IN = "pref_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        registerButton.setOnClickListener(v -> attemptRegister());
        loginTextView.setOnClickListener(v -> {
            // Quay lại màn hình Đăng nhập
            finish(); // Đóng màn hình đăng ký
        });
    }

    private void attemptRegister() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Kiểm tra nhập liệu
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem đã có tài khoản nào được lưu chưa
        if (prefs.contains(PREF_USERNAME)) {
            Toast.makeText(this, "Ứng dụng hiện chỉ hỗ trợ một tài khoản", Toast.LENGTH_LONG).show();
            return; // Dừng việc đăng ký
        }

        // Lưu thông tin đăng ký vào SharedPreferences (!! CẢNH BÁO BẢO MẬT !!)
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_USERNAME, username);
        editor.putString(PREF_PASSWORD, password); // Lưu mật khẩu trực tiếp
        editor.putBoolean(PREF_LOGGED_IN, true);   // Tự động đăng nhập sau khi đăng ký
        editor.apply();

        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển sang màn hình chính (MainActivity)
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        // Xóa các màn hình trước đó (Login) khỏi stack để không quay lại được
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Đóng tất cả Activity liên quan (LoginActivity)
    }
}