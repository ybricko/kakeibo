package com.example.xpense_tracker.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xpense_tracker.R;
import com.example.xpense_tracker.data.LoginRepository;

public class RegisterActivity extends AppCompatActivity {

    private LoginRepository loginRepository;
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginRepository = LoginRepository.getInstance(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button registerButton = findViewById(R.id.register);

        // Menambahkan listener untuk tombol register
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean exists = loginRepository.isUserExists(username);
                Log.d("RegisterActivity", "User exists: " + exists); // 🔍 Debug apakah user sudah ada

                if (exists) {
                    Toast.makeText(RegisterActivity.this, "Username sudah digunakan!", Toast.LENGTH_SHORT).show();
                } else {
                    boolean success = loginRepository.register(username, password);
                    if (success) {
                        Toast.makeText(RegisterActivity.this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Gagal mendaftar!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}
