package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.mihalypapp.app.R;
import com.mihalypapp.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteParentAdapter extends ArrayAdapter<User> {
    private List<User> parentListFull;

    public AutoCompleteParentAdapter(@NonNull Context context, @NonNull List<User> parentList) {
        super(context, 0, parentList);
        parentListFull = new ArrayList<>(parentList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return parentFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_menu_popup_item_parent, viewGroup, false);
        }

        MaterialTextView textViewParentName = convertView.findViewById(R.id.text_view_parent_name);
        MaterialTextView textViewParentEmail = convertView.findViewById(R.id.text_view_parent_email);

        User parent = getItem(position);

        if (parent != null) {
            textViewParentName.setText(parent.getName());
            textViewParentEmail.setText(parent.getEmail());
        }

        return convertView;
    }

    private Filter parentFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<User> suggestions = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                suggestions.addAll(parentListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (User item : parentListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getEmail().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((User) resultValue).getEmail();
        }
    };
}
