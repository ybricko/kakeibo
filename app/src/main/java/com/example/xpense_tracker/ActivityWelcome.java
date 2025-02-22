package com.example.xpense_tracker;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xpense_tracker.data.DatabaseHelper;

public class ActivityWelcome extends AppCompatActivity {

    private EditText etUsername;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        etUsername = findViewById(R.id.et_username);
        btnContinue = findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            if (!username.isEmpty()) {
                saveUsername(username);
                navigateToCurrency();
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUsername(String username) {
        // Menyimpan username ke tabel expense (bukan tabel users)
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER, username);  // Menyimpan username di kolom user
        db.insert(DatabaseHelper.TABLE_EXPENSE, null, values);
    }

    private void navigateToCurrency() {
        // Berpindah ke ActivityCurrency
        Intent intent = new Intent(this, ActivityCurrency.class);
        startActivity(intent);
    }
}
