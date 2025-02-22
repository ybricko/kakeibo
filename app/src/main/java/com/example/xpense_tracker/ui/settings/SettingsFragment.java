package com.example.xpense_tracker.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.xpense_tracker.R;
import com.example.xpense_tracker.data.CategoryDataSource;
import com.example.xpense_tracker.data.CategoryRepository;
import com.example.xpense_tracker.data.Currency;
import com.example.xpense_tracker.data.SharedPreferenceService;
import com.example.xpense_tracker.data.model.Category;
import com.example.xpense_tracker.data.model.CategoryType;
import com.example.xpense_tracker.data.model.SubCategory;
import com.example.xpense_tracker.databinding.FragmentSettingsBinding;
import com.example.xpense_tracker.ui.UIUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// https://developer.android.com/training/data-storage/shared-preferences
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferenceService sharedPreferenceService;
    private List<Category> incomeCategories = new ArrayList<>();
    private List<Category> expenseCategories = new ArrayList<>();
    private List<SubCategory> subCategories = new ArrayList<>();
    private CategoryRepository categoryRepository;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        this.sharedPreferenceService = SharedPreferenceService.getInstance(requireContext());
        this.categoryRepository = CategoryRepository.getInstance(CategoryDataSource.getInstance(requireContext()));

        addCurrencyChipListener();
        checkSelectedCurrencyChip();
        addAddCategoryButtonListener(root);
        addAddSubCategoryButtonListener(root);

        showExistingCategories();
        return root;
    }

    public void showExistingCategories() {
        binding.incomeCategoryChipGroup.removeAllViews();
        binding.expenseCategoryChipGroup.removeAllViews();
        getIncomeCategoryChips().forEach(income -> binding.incomeCategoryChipGroup.addView(income));
        getExpenseCategoryChips().forEach(expense -> binding.expenseCategoryChipGroup.addView(expense));
    }

    private List<Chip> getIncomeCategoryChips() {
        this.incomeCategories = categoryRepository.getCategories(CategoryType.INCOME);
        return getCategoryChipsWithSubCategoryChips(incomeCategories);
    }

    private List<Chip> getExpenseCategoryChips() {
        this.expenseCategories = categoryRepository.getCategories(CategoryType.EXPENSE);
        return getCategoryChipsWithSubCategoryChips(expenseCategories);
    }

    @NonNull
    private List<Chip> getCategoryChipsWithSubCategoryChips(List<Category> categories) {
        return categories.stream()
                .map(category -> {
                    int id = category.getId();
                    Chip categoryChip = UIUtil.createFilterChip(getLayoutInflater(), id, category.getName());
                    addCategoryListener(categoryChip, category);
                    return categoryChip;
                })
                .collect(Collectors.toList());
    }

    private void addCategoryListener(Chip categoryChip, Category category) {
        categoryChip.setOnClickListener(v ->
                CategoryDialogFragment.newInstanceWithUpdate(category).show(getChildFragmentManager(), "CategoryDialogFragment")
        );
    }

    private void addAddCategoryButtonListener(View root) {
        MaterialButton addNewCategoryButton = root.findViewById(R.id.addNewCategoryButton);
        addNewCategoryButton.setOnClickListener(v ->
                CategoryDialogFragment.newInstance().show(getChildFragmentManager(), "CategoryDialogFragment")
        );
    }

    private void addAddSubCategoryButtonListener(View root) {
        MaterialButton addNewSubCategoryButton = root.findViewById(R.id.addNewSubCategoryButton);
        addNewSubCategoryButton.setOnClickListener(v ->
                SubCategoryDialogFragment.newInstance().show(getChildFragmentManager(), "SubCategoryDialogFragment")
        );
    }

    private void checkSelectedCurrencyChip() {
        String currency = sharedPreferenceService.getCurrency();
        ChipGroup currencyChipGroup = binding.currencyChip;

        for (int i = 0; i < currencyChipGroup.getChildCount(); i++) {
            Chip selectedChip = (Chip) currencyChipGroup.getChildAt(i);
            if (currency.equals(selectedChip.getText().toString())) {
                currencyChipGroup.check(selectedChip.getId());
                return;
            }
        }
    }

    private void addCurrencyChipListener() {
        ChipGroup chipGroup = binding.currencyChip;
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) { // ✅ Cek apakah ada chip yang dipilih untuk mencegah error
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null && Arrays.stream(Currency.values()).anyMatch(currency -> currency.name().contentEquals(chip.getText()))) {
                    sharedPreferenceService.setCurrency(chip.getText().toString());
                } else {
                    Log.e("currency settings", "Error occurred during currency state changing process.");
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
