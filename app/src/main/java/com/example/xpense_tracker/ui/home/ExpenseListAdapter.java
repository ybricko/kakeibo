package com.example.xpense_tracker.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xpense_tracker.R;
import com.example.xpense_tracker.data.ExpenseDataSource;
import com.example.xpense_tracker.data.ExpenseRepository;
import com.example.xpense_tracker.data.SharedPreferenceService;
import com.example.xpense_tracker.data.model.CategoryType;
import com.example.xpense_tracker.data.model.Expense;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter untuk menampilkan daftar transaksi dalam RecyclerView.
 */
public class ExpenseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SharedPreferenceService sharedPreferenceService;
    private List<Expense> mItem;
    private static volatile ExpenseListAdapter instance;
    private ExpenseRepository expenseRepository;

    public static ExpenseListAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseListAdapter(context);
        }
        return instance;
    }

    private ExpenseListAdapter(Context context) {
        expenseRepository = ExpenseRepository.getInstance(ExpenseDataSource.getInstance(context));
        List<Expense> allExpense = getAllExpense();
        this.sharedPreferenceService = SharedPreferenceService.getInstance(context);
        mItem = allExpense;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        mItem = getAllExpense();
        return ThreeLineItemViewHolder.create(parent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        bind((ThreeLineItemViewHolder) viewHolder, mItem.get(position));

        // Buka dialog untuk update transaksi saat item diklik
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = viewHolder.getBindingAdapterPosition();
                Expense updatableExpense = mItem.get(itemPosition);
                AddExpenseOrIncomeDialogFragment
                        .newInstanceWithUpdate(updatableExpense)
                        .show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "AddExpenseOrIncomeDialogFragment for update");
            }
        });
    }

    public void addSwipeListener(RecyclerView recyclerView) {
        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int itemPosition = viewHolder.getBindingAdapterPosition();
                Expense deletableExpense = mItem.get(itemPosition);
                mItem.remove(itemPosition);
                expenseRepository.deleteExpense(deletableExpense.getId());
                notifyItemRemoved(itemPosition);
                Toast.makeText(recyclerView.getContext(), "Deleted: " + deletableExpense.getCategory() + ", " + deletableExpense.getAmount(), Toast.LENGTH_LONG).show();
            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void addExpense(Expense expense) {
        mItem.add(0, expense);
        mItem.sort(sortComparatorByDate());
        notifyDataSetChanged();
    }

    public void filter(String filterCategoryName) {
        List<Expense> allExpense = getAllExpense();
        if ("ALL".equals(filterCategoryName)) {
            mItem = allExpense;
            notifyDataSetChanged();
            return;
        }

        mItem = allExpense.stream()
                .filter(e -> e.getType().equals(filterCategoryName))
                .collect(Collectors.toList());
        notifyDataSetChanged();
    }

    // 🔍 Fungsi untuk menampilkan data di UI dengan verifikasi keamanan
    private void bind(ThreeLineItemViewHolder vh, Expense expense) {
        // Jika transaksi telah diubah secara eksternal, tampilkan peringatan
        if (expense.getNote().contains("⚠")) {
            vh.text.setTextColor(Color.RED); // ❌ Warna merah untuk transaksi yang dicurigai
            vh.secondary.setTextColor(Color.RED);
            vh.tertiary.setTextColor(Color.RED);
        } else {
            vh.text.setTextColor(Color.BLACK); // ✅ Warna normal untuk transaksi valid
            vh.secondary.setTextColor(Color.BLACK);
            vh.tertiary.setTextColor(Color.BLACK);
        }

        // Tampilkan data transaksi
        vh.text.setText(String.format(
                sharedPreferenceService.applySelectedCurrency(
                        Integer.parseInt(expense.getAmount())
                ).toString()
        ));

        vh.secondary.setText(String.format(
                "%s, %s, %s", expense.getCategory(), expense.getSubCategory(), expense.getNote()
        ));

        vh.tertiary.setText(expense.getCreatedAt().toString());

        // Ganti ikon berdasarkan tipe transaksi (Income / Expense)
        if (CategoryType.INCOME.name().equals(expense.getType())) {
            vh.icon.setImageResource(R.drawable.revenue);
        } else {
            vh.icon.setImageResource(R.drawable.earning);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position % 3;
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public void filter(LocalDate from, LocalDate to) {
        mItem = getAllExpense().stream().filter(e -> !e.getCreatedAt().isBefore(from) && !e.getCreatedAt().isAfter(to)).collect(Collectors.toList());
        notifyDataSetChanged();
    }

    private List<Expense> getAllExpense() {
        List<Expense> allExpense = expenseRepository.getAllExpense();
        allExpense.sort(sortComparatorByDate());
        return allExpense;
    }

    @NonNull
    public static Comparator<Expense> sortComparatorByDate() {
        return new Comparator<Expense>() {
            @Override
            public int compare(Expense o1, Expense o2) {
                return o1.getCreatedAt().compareTo(o2.getCreatedAt());
            }
        };
    }
}
