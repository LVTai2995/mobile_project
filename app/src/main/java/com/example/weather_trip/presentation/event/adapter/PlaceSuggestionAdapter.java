package com.example.weather_trip.presentation.event.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather_trip.R;

import java.util.ArrayList;
import java.util.List;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {

    private final List<PlaceSuggestion> suggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String placeName);
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void setSuggestions(List<PlaceSuggestion> newSuggestions) {
        suggestions.clear();
        suggestions.addAll(newSuggestions);
        notifyDataSetChanged();
    }

    public void clear() {
        suggestions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(suggestions.get(position));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }

        void bind(PlaceSuggestion suggestion) {
            tvName.setText(suggestion.name);

            StringBuilder subtext = new StringBuilder();
            if (suggestion.city != null) subtext.append(suggestion.city);
            if (suggestion.country != null) {
                if (subtext.length() > 0) subtext.append(", ");
                subtext.append(suggestion.country);
            }
            tvName.setTag(subtext.toString());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSuggestionClick(suggestion.name);
                }
            });
        }
    }

    public static class PlaceSuggestion {
        public String name;
        public String city;
        public String country;

        public PlaceSuggestion(String name, String city, String country) {
            this.name = name;
            this.city = city;
            this.country = country;
        }
    }
}
