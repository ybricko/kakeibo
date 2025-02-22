package com.example.xpense_tracker;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xpense_tracker.data.Currency;
import com.example.xpense_tracker.data.DatabaseHelper;
import com.example.xpense_tracker.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topAppBar = findViewById(R.id.topAppBar);

        loadUserData();
    }

    private void loadUserData() {
        // Memuat data mata uang dari tabel expense
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_EXPENSE, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // Ambil informasi mata uang dan saldo
            String currencyString = cursor.getString(cursor.getColumnIndex("currency"));
            Currency currency = Currency.valueOf(currencyString); // Mengonversi nama currency menjadi enum Currency

            // Ambil saldo, misalnya dari database
            String saldo = cursor.getString(cursor.getColumnIndex("balance"));

            // Menampilkan mata uang dan saldo
            topAppBar.setSubtitle("Saldo: " + currency.getCurrencySymbol() + " " + saldo);

            cursor.close();
        }
    }
}