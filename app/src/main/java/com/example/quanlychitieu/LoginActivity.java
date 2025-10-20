package com.example.quanlychitieu;

import static com.example.quanlychitieu.R.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

// AndroidX Preference (khuyên dùng, thay cho android.preference.PreferenceManager)
import androidx.preference.PreferenceManager;

// Material
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // View (hỗ trợ cả TextInputEditText và EditText thường)
    private TextInputEditText usernameEditText, passwordEditText;
    private MaterialButton loginButton;
    // Nút/Link Đăng ký: hỗ trợ cả id mới (registerTextButton) và id cũ (registerTextView)
    private MaterialButton registerTextButton;
    private android.widget.TextView registerTextView; // fallback

    private SharedPreferences prefs;

    // Keys
    private static final String PREF_USERNAME = "pref_username";
    private static final String PREF_PASSWORD = "pref_password";     // *Chỉ demo*
    private static final String PREF_LOGGED_IN = "pref_logged_in";   // trạng thái login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 2) Nếu đã đăng nhập -> vào thẳng Main
        if (prefs.getBoolean(PREF_LOGGED_IN, false)) {
            goToMainActivity();
            return;
        }

        // 3) Hiển thị layout
        // Đổi đúng tên file layout bạn đang dùng (activity_login2 hoặc activity_login)
        setContentView(R.layout.activity_login2);

        // 4) Ánh xạ View (ưu tiên TextInputEditText; nếu bạn dùng EditText thường, cast vẫn OK)
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton      = findViewById(R.id.loginButton);

        // Đăng ký: id mới (MaterialButton)
        registerTextButton = findViewById(R.id.registerTextButton);


        // 5) Sự kiện
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> attemptLogin());
        }

        if (registerTextButton != null) {
            registerTextButton.setOnClickListener(v -> openRegister());
        } else if (registerTextView != null) {
            registerTextView.setOnClickListener(v -> openRegister());
        }

        // Cho phép bấm "Done" trên bàn phím để đăng nhập
        if (passwordEditText != null) {
            passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            });
        }
    }

    private void attemptLogin() {
        String username = safeText(usernameEditText);
        String password = safeText(passwordEditText);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin đã đăng ký (đang lưu bằng SharedPreferences đơn giản để demo)
        String savedUsername = prefs.getString(PREF_USERNAME, null);
        String savedPassword = prefs.getString(PREF_PASSWORD, null);

        if (savedUsername == null || savedPassword == null) {
            Toast.makeText(this, "Chưa có tài khoản. Vui lòng đăng ký trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            // Đăng nhập thành công
            prefs.edit().putBoolean(PREF_LOGGED_IN, true).apply();
            goToMainActivity();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // đóng LoginActivity
    }

    // Helper: lấy chuỗi an toàn từ TextInputEditText
    private String safeText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }
}
