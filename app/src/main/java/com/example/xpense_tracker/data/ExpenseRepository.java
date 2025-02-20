package com.example.xpense_tracker.data;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.xpense_tracker.data.model.CategoryType;
import com.example.xpense_tracker.data.model.Expense;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseRepository {

    private static volatile ExpenseRepository instance;
    private ExpenseDataSource dataSource;

    private ExpenseRepository(ExpenseDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static ExpenseRepository getInstance(ExpenseDataSource dataSource) {
        if (instance == null) {
            instance = new ExpenseRepository(dataSource);
        }
        return instance;
    }

    public List<Expense> getAllExpense() {
        List<Expense> expenses = dataSource.getAllExpense();
        return expenses; // Verifikasi hash saat mengambil data
    }

    public void addExpense(Expense expense) {
        dataSource.saveExpense(expense); // Simpan transaksi setelah dihitung hash-nya
    }



    public void deleteExpense(int id) {
        dataSource.deleteExpense(id); // Menghapus transaksi berdasarkan ID
    }

    // Metode untuk memverifikasi hash dari setiap transaksi
    // Verifikasi hash saat mengambil transaksi


    // Fungsi untuk menghasilkan hash SHA-256 dari sebuah input
    public static String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8)); // Mendapatkan hash
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b)); // Mengubah byte menjadi format hexadecimal
            }
            return hexString.toString(); // Kembalikan string hash
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing data", e); // Menangani error jika hashing gagal
        }
    }

    // Metode untuk mendapatkan pengeluaran berdasarkan kategori dan tipe
    public Map<String, Integer> getExpensesByCategoryType(CategoryType type) {
        return getAllExpense().stream()
                .filter(e -> type.name().equals(e.getType()))
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingInt(e -> new BigDecimal(e.getAmount()).intValue())
                ));
    }



    // Mendapatkan total pengeluaran 6 bulan terakhir
    public List<Integer> getLast6MonthExpensesByMonth() {
        LocalDate now = LocalDate.now().withDayOfMonth(1);
        LocalDate from = now.minusMonths(5);
        return getLast6MonthsData(from, CategoryType.EXPENSE);
    }

    // Mendapatkan total pendapatan 6 bulan terakhir
    public List<Integer> getLast6MonthIncomesByMonth() {
        LocalDate now = LocalDate.now().withDayOfMonth(1);
        LocalDate from = now.minusMonths(5);
        return getLast6MonthsData(from, CategoryType.INCOME);
    }

    // Mendapatkan data per bulan untuk 6 bulan terakhir
    @NonNull
    private List<Integer> getLast6MonthsData(LocalDate from, CategoryType type) {
        Map<    Month, Integer> dataByMonth = getAllSummingByMonth(from, type);
        List<Integer> values = new ArrayList<>();

        LocalDate currentMonth = from;
        LocalDate now = LocalDate.now().withDayOfMonth(1);

        while (currentMonth.isBefore(now) || currentMonth.equals(now)) {
            values.add(dataByMonth.getOrDefault(currentMonth.getMonth(), 0));
            currentMonth = currentMonth.plusMonths(1);
        }
        return values;
    }

    // Mendapatkan total pengeluaran atau pendapatan berdasarkan bulan
    @NonNull
    private Map<Month, Integer> getAllSummingByMonth(LocalDate from, CategoryType type) {
        return dataSource.getAllExpense().stream()
                .filter(e -> type.name().equals(e.getType()))
                .collect(Collectors.groupingBy(e -> e.getCreatedAt().getMonth(),
                        Collectors.summingInt(e -> new BigDecimal(e.getAmount()).intValue())));
    }
    public List<Integer> getCurrentMonthIncomesDayByDay() {
        return getCurrentMonthData(CategoryType.INCOME); // Ambil data pendapatan bulan ini
    }

    // Ambil data pengeluaran bulan ini
    public List<Integer> getCurrentMonthExpensesDayByDay() {
        return getCurrentMonthData(CategoryType.EXPENSE); // Ambil data pengeluaran bulan ini
    }

    // Mendapatkan total pendapatan atau pengeluaran per hari untuk bulan berjalan
    @NonNull
    private List<Integer> getCurrentMonthData(CategoryType type) {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate end = firstDayOfMonth.plusMonths(1);

        Map<LocalDate, Integer> sumDataPerDays = getAllSummingByDay(firstDayOfMonth, type);
        List<Integer> values = new ArrayList<>();
        LocalDate start = firstDayOfMonth;

        while (start.isBefore(end)) {
            values.add(sumDataPerDays.getOrDefault(start, 0));
            start = start.plusDays(1);
        }
        return values;
    }

    // Mendapatkan total pengeluaran atau pendapatan per hari
    @NonNull
    private Map<LocalDate, Integer> getAllSummingByDay(LocalDate from, CategoryType type) {
        return dataSource.getAllExpense().stream()
                .filter(e -> type.name().equals(e.getType()))
                .collect(Collectors.groupingBy(Expense::getCreatedAt,
                        Collectors.summingInt(e -> new BigDecimal(e.getAmount()).intValue())));
    }

    // Mendapatkan total pendapatan dan pengeluaran bulan ini
    public Pair<Integer, Integer> getAllFromCurrentMonth() {
        List<Expense> allOfCurrentMonth = getAllExpense()
                .stream()
                .filter(e -> LocalDate.now().getMonth().equals(e.getCreatedAt().getMonth()))
                .collect(Collectors.toList());

        int allIncomes = allOfCurrentMonth.stream()
                .filter(e -> CategoryType.INCOME.name().equals(e.getType()))
                .mapToInt(e -> new BigDecimal(e.getAmount()).intValue())
                .sum();

        int allExpenses = allOfCurrentMonth.stream()
                .filter(e -> CategoryType.EXPENSE.name().equals(e.getType()))
                .mapToInt(e -> new BigDecimal(e.getAmount()).intValue())
                .sum();

        return Pair.create(allIncomes, allExpenses); // Mengembalikan pendapatan dan pengeluaran bulan ini
    }
}
