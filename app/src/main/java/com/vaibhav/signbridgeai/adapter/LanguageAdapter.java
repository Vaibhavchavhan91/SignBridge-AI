package com.vaibhav.signbridgeai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.R;
import com.vaibhav.signbridgeai.model.LanguageItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying country and language choices.
 */
public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(LanguageItem item);
    }

    private final List<LanguageItem> items = new ArrayList<>();
    private String selectedLanguageCode = "en-US";
    private final OnLanguageSelectedListener listener;

    public LanguageAdapter(String currentCode, OnLanguageSelectedListener listener) {
        this.selectedLanguageCode = currentCode != null ? currentCode : "en-US";
        this.listener = listener;
    }

    public void setItems(List<LanguageItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setSelectedLanguageCode(String code) {
        this.selectedLanguageCode = code;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_language_select, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageItem item = items.get(position);
        boolean isSelected = item.getCode().equalsIgnoreCase(selectedLanguageCode);
        holder.bind(item, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvFlag;
        private final TextView tvCountry;
        private final TextView tvNameAndCode;
        private final TextView tvRegionBadge;
        private final ImageView ivCheck;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvFlag = itemView.findViewById(R.id.tvLangFlag);
            tvCountry = itemView.findViewById(R.id.tvLangCountry);
            tvNameAndCode = itemView.findViewById(R.id.tvLangNameAndCode);
            tvRegionBadge = itemView.findViewById(R.id.tvLangRegionBadge);
            ivCheck = itemView.findViewById(R.id.ivLangCheck);
        }

        public void bind(LanguageItem item, boolean isSelected, OnLanguageSelectedListener listener) {
            tvFlag.setText(item.getFlagEmoji());
            tvCountry.setText(item.getCountryName());
            tvNameAndCode.setText(item.getLanguageName() + " (" + item.getEnglishName() + ") • " + item.getCode());
            tvRegionBadge.setText(item.getRegion());

            if (isSelected) {
                ivCheck.setVisibility(View.VISIBLE);
                cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                cardView.setStrokeWidth(4);
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_container));
            } else {
                ivCheck.setVisibility(View.GONE);
                cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.divider_light));
                cardView.setStrokeWidth(1);
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.surface_light));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLanguageSelected(item);
                }
            });
        }
    }
}
