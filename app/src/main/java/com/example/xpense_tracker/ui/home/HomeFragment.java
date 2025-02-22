package com.example.xpense_tracker.ui.home;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xpense_tracker.R;
import com.example.xpense_tracker.data.Currency;
import com.example.xpense_tracker.data.ExpenseDataSource;
import com.example.xpense_tracker.data.ExpenseRepository;
import com.example.xpense_tracker.data.SharedPreferenceService;
import com.example.xpense_tracker.databinding.FragmentHomeBinding;
import com.example.xpense_tracker.ui.UIUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ExpenseListAdapter adapter;
    private ExpenseRepository expenseRepository;
    private SharedPreferenceService sharedPreferenceService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        this.adapter = ExpenseListAdapter.getInstance(getContext());
        this.expenseRepository = ExpenseRepository.getInstance(ExpenseDataSource.getInstance(getContext()));
        this.sharedPreferenceService = SharedPreferenceService.getInstance(getContext());

        addAddButtonListener(root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }







    private void addAddButtonListener(View root) {
        FloatingActionButton addExpenseButton = root.findViewById(R.id.floatingActionButton);
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AddExpenseOrIncomeDialogFragment.newInstance().show(getChildFragmentManager(), "addExpenseOrIncomeDialog");
            }
        });
    }



}