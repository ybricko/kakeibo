package com.example.xpense_tracker.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.xpense_tracker.BottomNavigationActivity;
import com.example.xpense_tracker.R;
import com.example.xpense_tracker.data.LoginRepository;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(this))
                .get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login);
        Button registerButton = findViewById(R.id.register);
        loadingProgressBar = findViewById(R.id.loading);

        loginButton.setOnClickListener(v -> onLoginClicked(v));
        registerButton.setOnClickListener(v -> onRegisterClicked(v));

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(LoginResult loginResult) {
                if (loginResult == null) return;

                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    public void onLoginClicked(View view) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        loginViewModel.login(username, password);
    }

    public void onRegisterClicked(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

        // ✅ Pindahkan ke halaman utama setelah login berhasil
        Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class); // Ganti dengan Activity utama
        startActivity(intent);
        finish(); // Tutup LoginActivity setelah berpindah halaman
    }


    private void showLoginFailed(int errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
