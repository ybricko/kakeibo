package com.example.xpense_tracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xpense_tracker.data.Currency;
import com.example.xpense_tracker.data.DatabaseHelper;

public class ActivityCurrency extends AppCompatActivity {

    private Spinner currencySpinner;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        currencySpinner = findViewById(R.id.spinner_currency);
        btnContinue = findViewById(R.id.btn_continue);

        // Mengisi Spinner dengan mata uang yang ada di enum Currency
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        btnContinue.setOnClickListener(view -> {
            // Mengambil mata uang yang dipilih
            String selectedCurrency = currencySpinner.getSelectedItem().toString();
            Currency currency = Currency.valueOf(selectedCurrency); // Mengonversi ke enum Currency
            saveCurrency(currency); // Menyimpan ke database
            navigateToMainActivity();
        });
    }

    private void saveCurrency(Currency currency) {
        // Menyimpan mata uang yang dipilih ke dalam database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("currency", currency.name());  // Menyimpan nama mata uang dari enum (USD, EUR, HUF, IDR)
        db.update(DatabaseHelper.TABLE_EXPENSE, values, null, null);  // Update di tabel expense
    }

    private void navigateToMainActivity() {
        // Berpindah ke MainActivity setelah memilih mata uang
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
