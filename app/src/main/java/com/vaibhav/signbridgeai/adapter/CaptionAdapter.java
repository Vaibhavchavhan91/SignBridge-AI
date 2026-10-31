package com.vaibhav.signbridgeai.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.R;
import com.vaibhav.signbridgeai.model.CaptionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying live and saved conversation captions.
 * Supports dynamic font scaling for accessibility.
 */
public class CaptionAdapter extends RecyclerView.Adapter<CaptionAdapter.CaptionViewHolder> {

    private final List<CaptionItem> items = new ArrayList<>();
    private int fontSizeSp = 24;

    public CaptionAdapter() {
    }

    public void setFontSizeSp(int fontSizeSp) {
        this.fontSizeSp = fontSizeSp;
        notifyDataSetChanged();
    }

    public int getFontSizeSp() {
        return fontSizeSp;
    }

    public void setItems(List<CaptionItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void addCaption(CaptionItem item) {
        if (item != null) {
            this.items.add(item);
            notifyItemInserted(items.size() - 1);
        }
    }

    public void clear() {
        int count = items.size();
        items.clear();
        notifyItemRangeRemoved(0, count);
    }

    public List<CaptionItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public CaptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_caption, parent, false);
        return new CaptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaptionViewHolder holder, int position) {
        CaptionItem item = items.get(position);
        holder.bind(item, fontSizeSp);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CaptionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSpeaker;
        private final TextView tvTimestamp;
        private final TextView tvText;

        public CaptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpeaker = itemView.findViewById(R.id.tvCaptionSpeaker);
            tvTimestamp = itemView.findViewById(R.id.tvCaptionTime);
            tvText = itemView.findViewById(R.id.tvCaptionText);

            itemView.setOnLongClickListener(v -> {
                Context context = v.getContext();
                CharSequence text = tvText.getText();
                if (text != null && text.length() > 0) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Caption", text);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Caption copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
        }

        public void bind(CaptionItem item, int fontSizeSp) {
            tvSpeaker.setText(item.getSpeakerTag());
            tvTimestamp.setText(item.getFormattedTime());
            tvText.setText(item.getText());
            tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
        }
    }
}
