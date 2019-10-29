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
import com.mihalypapp.app.models.Group;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteGroupAdapter extends ArrayAdapter<Group> {
    private List<Group> groupListFull;

    public AutoCompleteGroupAdapter(@NonNull Context context, @NonNull List<Group> groupList) {
        super(context, 0, groupList);
        groupListFull = new ArrayList<>(groupList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return groupFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_menu_popup_item_group, parent, false);
        }

        MaterialTextView textViewGroupTeacherName = convertView.findViewById(R.id.text_view_group_teacher_name);
        MaterialTextView textViewGroupType = convertView.findViewById(R.id.text_view_group_type);
        MaterialTextView textViewGroupYear = convertView.findViewById(R.id.text_view_group_year);

        Group group = getItem(position);

        if (group != null) {
            textViewGroupTeacherName.setText(group.getTeacherName());
            textViewGroupType.setText(group.getType());
            textViewGroupYear.setText(group.getYear());
        }

        return convertView;
    }

    private Filter groupFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<Group> suggestions = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                suggestions.addAll(groupListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Group item : groupListFull) {
                    if (item.getTeacherName().toLowerCase().contains(filterPattern) || item.getType().toLowerCase().contains(filterPattern)) {
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
            return ((Group) resultValue).getTeacherName() + " (" +((Group) resultValue).getType() + " " + R.string.group + ")";
        }
    };
}
