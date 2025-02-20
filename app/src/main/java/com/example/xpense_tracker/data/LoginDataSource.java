package com.example.xpense_tracker.data;

import static com.example.xpense_tracker.data.QueryConstant.DATABASE;
import static com.example.xpense_tracker.data.model.UserContract.UserContent.COLUMN_NAME_EMAIL;
import static com.example.xpense_tracker.data.model.UserContract.UserContent.COLUMN_NAME_PASSWORD;
import static com.example.xpense_tracker.data.model.UserContract.UserContent.TABLE_NAME;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.xpense_tracker.data.model.LoggedInUser;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that handles authentication with login credentials and retrieves user information.
 */
public class LoginDataSource extends SQLiteOpenHelper {

    private static volatile LoginDataSource instance;

    public static synchronized LoginDataSource getInstance(Context context) { // ✅ Perbaiki tipe return
        if (instance == null) {
            instance = new LoginDataSource(context);
        }
        return instance;
    }

    private LoginDataSource(@Nullable Context context) {
        super(context, DATABASE, null, 1);
        this.getWritableDatabase().execSQL(QueryConstant.CREATE_USERS_TABLE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QueryConstant.CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(QueryConstant.DROP_USERS_TABLE);
    }

    public Result<LoggedInUser> login(String username, String password) {
        try {
            String editedUsername = username.contains("@") ? username.substring(0, username.indexOf("@")) : username;

            if (hasAlreadyRegistered(username)) {
                if (isPasswordValid(username, password)) { // ✅ Bandingkan password dengan hashing
                    LoggedInUser loggedInUser = new LoggedInUser(username, editedUsername);
                    return new Result.Success<>(loggedInUser);
                } else {
                    return new Result.Error(new IOException("Password salah"));
                }
            } else {
                registerUser(username, password);
                LoggedInUser loggedInUser = new LoggedInUser(username, editedUsername);
                return new Result.Success<>(loggedInUser);
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    private void registerUser(String username, String password) {
        String hashedPassword = hashPassword(password); // ✅ Hash password sebelum disimpan

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_EMAIL, username);
        values.put(COLUMN_NAME_PASSWORD, hashedPassword);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
    }

    private boolean hasAlreadyRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_NAME_EMAIL + " = ?";
        String[] selectionArgs = {email};

        try (Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    private boolean isPasswordValid(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password); // ✅ Hash password sebelum pengecekan

        String selection = COLUMN_NAME_EMAIL + " = ? AND " + COLUMN_NAME_PASSWORD + " = ?";
        String[] selectionArgs = {username, hashedPassword};

        try (Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

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
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
