package com.vaibhav.signbridgeai.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.R;
import com.vaibhav.signbridgeai.adapter.LanguageAdapter;
import com.vaibhav.signbridgeai.model.LanguageItem;
import com.vaibhav.signbridgeai.util.LanguageHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * BottomSheet modal allowing users to search and select from 30+ worldwide countries and languages.
 */
public class LanguageSelectBottomSheet extends BottomSheetDialogFragment {

    public interface OnLanguageSelectedCallback {
        void onSelected(LanguageItem selectedItem);
    }

    private static final String ARG_CURRENT_CODE = "arg_current_code";

    private String currentLanguageCode = "en-US";
    private String currentRegionFilter = LanguageHelper.REGION_ALL;
    private OnLanguageSelectedCallback callback;

    private EditText etSearch;
    private ChipGroup chipGroupRegions;
    private RecyclerView rvLanguages;
    private TextView tvEmptySearch;
    private LanguageAdapter adapter;

    public static LanguageSelectBottomSheet newInstance(String currentCode, OnLanguageSelectedCallback callback) {
        LanguageSelectBottomSheet sheet = new LanguageSelectBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_CODE, currentCode);
        sheet.setArguments(args);
        sheet.setCallback(callback);
        return sheet;
    }

    public void setCallback(OnLanguageSelectedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLanguageCode = getArguments().getString(ARG_CURRENT_CODE, "en-US");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_language_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etLangSearch);
        chipGroupRegions = view.findViewById(R.id.chipGroupRegions);
        rvLanguages = view.findViewById(R.id.rvLanguages);
        tvEmptySearch = view.findViewById(R.id.tvEmptySearch);

        view.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dismiss());

        setupRecyclerView();
        setupSearchAndFilters();

        // Expand bottom sheet
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            dialog.getBehavior().setSkipCollapsed(true);
        }

        updateList();
    }

    private void setupRecyclerView() {
        adapter = new LanguageAdapter(currentLanguageCode, item -> {
            if (callback != null) {
                callback.onSelected(item);
            }
            dismiss();
        });
        rvLanguages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLanguages.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupRegions.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipRegionAmericas) {
                currentRegionFilter = LanguageHelper.REGION_AMERICAS;
            } else if (id == R.id.chipRegionAsia) {
                currentRegionFilter = LanguageHelper.REGION_ASIA;
            } else if (id == R.id.chipRegionEurope) {
                currentRegionFilter = LanguageHelper.REGION_EUROPE;
            } else if (id == R.id.chipRegionMEAfrica) {
                currentRegionFilter = LanguageHelper.REGION_ME_AFRICA;
            } else {
                currentRegionFilter = LanguageHelper.REGION_ALL;
            }
            updateList();
        });
    }

    private void updateList() {
        String query = etSearch.getText() != null ? etSearch.getText().toString() : "";
        List<LanguageItem> results = LanguageHelper.search(query, currentRegionFilter);

        if (results.isEmpty()) {
            tvEmptySearch.setVisibility(View.VISIBLE);
            rvLanguages.setVisibility(View.GONE);
        } else {
            tvEmptySearch.setVisibility(View.GONE);
            rvLanguages.setVisibility(View.VISIBLE);
            adapter.setItems(results);
        }
    }
}
