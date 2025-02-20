package com.example.xpense_tracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.xpense_tracker.data.model.LoggedInUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginRepository {
    private static volatile LoginRepository instance;
    private final SQLiteDatabase database;
    private LoggedInUser user = null;

    // Singleton pattern to get a single instance of LoginRepository
    private LoginRepository(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.database = dbHelper.getWritableDatabase();
    }

    public static LoginRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (LoginRepository.class) {
                if (instance == null) {
                    instance = new LoginRepository(context);
                }
            }
        }
        return instance;
    }

    // Check if the user is logged in
    public boolean isLoggedIn() {
        return user != null;
    }

    // Log the user out
    public void logout() {
        user = null;
    }

    // Set the current logged-in user
    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
    }

    // Register a new user (check if username exists first)
    public boolean register(String username, String password) {
        if (isUserExists(username)) {
            return false; // ❌ Jangan lanjutkan jika user sudah ada
        }

        String hashedPassword = hashPassword(password);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);

        SQLiteDatabase db = database;
        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        return result != -1; // ✅ Return `true` jika insert berhasil
    }


    // Log in a user (check username and hashed password)
    public LoggedInUser login(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (Cursor cursor = database.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_USERNAME},
                DatabaseHelper.COLUMN_USERNAME + "=? AND " + DatabaseHelper.COLUMN_PASSWORD + "=?",
                new String[]{username, hashedPassword}, null, null, null)) {

            if (cursor.moveToFirst()) {
                String userId = cursor.getString(0); // Get the user's ID
                String displayName = cursor.getString(1); // Get the user's username
                LoggedInUser user = new LoggedInUser(userId, displayName);
                setLoggedInUser(user); // Set the current logged-in user
                return user;
            }
        }
        return null; // Return null if login failed
    }

    // Check if a user already exists by username
    public boolean isUserExists(String username) {
        SQLiteDatabase db = database; // Pastikan database terbuka
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USERNAME + "=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0; // ✅ Pastikan kita membaca nilai dari cursor
            }
        }
        return false; // ✅ Jika cursor kosong, user tidak ada
    }


    // Hash password using SHA-256 algorithm
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e); // Handle any hashing errors
        }
    }
}
