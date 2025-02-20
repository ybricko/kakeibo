package com.example.xpense_tracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.xpense_tracker.data.model.CategoryType;
import com.example.xpense_tracker.data.model.Expense;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.xpense_tracker.data.QueryConstant.DATABASE;
import static com.example.xpense_tracker.data.model.ExpenseContract.TransactionContent.*;

public class ExpenseDataSource extends SQLiteOpenHelper {

    private static volatile ExpenseDataSource instance;

    public static ExpenseDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseDataSource(context);
        }
        return instance;
    }

    private ExpenseDataSource(@Nullable Context context) {
        super(context, DATABASE, null, 1);  // Only call execSQL in onUpgrade
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(QueryConstant.CREATE_EXPENSE_TABLE);  // Create table
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(QueryConstant.DROP_EXPENSE_TABLE);  // Drop old table on upgrade
        onCreate(sqLiteDatabase);  // Recreate table
    }

    // Save Expense with Hash
    // Proses menyimpan transaksi
    public void saveExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Debug: Data sebelum di-hash
        Log.d("ExpenseDataSource", "Data sebelum hash: " +
                expense.getAmount() + ", " + expense.getCategory() + ", " +
                expense.getSubCategory() + ", " + expense.getType() + ", " +
                expense.getCreatedAt() + ", " + expense.getNote());

        // Membuat string data untuk hashing
        String dataString = expense.getAmount() + expense.getCategory() +
                expense.getSubCategory() + expense.getType() +
                expense.getCreatedAt() + expense.getNote();

        // Cek apakah hash dihitung sebelum disimpan
        String hashedData = hashSHA256(dataString);

        // Debug log hash yang dihitung
        Log.d("ExpenseDataSource", "Hash yang dihitung: " + hashedData);
        expense.setHash(hashedData); // Pastikan hash diset dengan benar

        // Menyimpan hash yang sudah dihitung ke dalam database
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CATEGORY, expense.getCategory());
        values.put(COLUMN_NAME_SUB_CATEGORY, expense.getSubCategory());
        values.put(COLUMN_NAME_TYPE, expense.getType());
        values.put(COLUMN_NAME_NOTE, expense.getNote());
        values.put(COLUMN_NAME_CREATED_AT, expense.getCreatedAt().toString());
        values.put(COLUMN_NAME_AMOUNT, expense.getAmount());
        values.put(COLUMN_NAME_HASH, hashedData); // Simpan hash yang valid

        db.insert(TABLE_NAME, null, values);
    }


    // Fetch all expenses and verify hash
    public List<Expense> getAllExpense() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sortOrder = COLUMN_NAME_ID + " DESC";
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, sortOrder);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_CATEGORY));
                String subCategory = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SUB_CATEGORY));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TYPE));
                LocalDate createdAt = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_CREATED_AT)));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOTE));
                String amount = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_AMOUNT));
                String savedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_HASH));

                // Verify hash
                String dataString = amount + category + subCategory + type + createdAt + note;
                String computedHash = hashSHA256(dataString);

                // Detect data manipulation
                boolean isValid = savedHash.equals(computedHash);

                Expense expense = new Expense(id, category, subCategory, type, createdAt, amount, note);
                if (!isValid) {
                    expense.setNote("⚠ Data mungkin telah diubah!");
                }

                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    // Fetch expenses by category type
    public List<Expense> getExpenses(CategoryType type) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_NAME_TYPE + " = ?";
        String[] selectionArgs = {type.name()};
        String sortOrder = COLUMN_NAME_ID + " DESC";

        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SUB_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TYPE)),
                        LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_CREATED_AT))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOTE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_AMOUNT))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    // Delete expense by ID
    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_NAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    // Hashing SHA-256
    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing data", e);
        }
    }
}
