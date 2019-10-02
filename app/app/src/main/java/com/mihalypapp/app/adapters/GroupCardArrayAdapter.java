package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.Group;

import java.util.ArrayList;

public class GroupCardArrayAdapter extends ArrayAdapter<Group> {
    public GroupCardArrayAdapter(Context context, ArrayList<Group> group) {
        super(context, 0, group);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Group group = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_group_card, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image_view);
        TextView textViewTeacherName = convertView.findViewById(R.id.text_view_teacher_name);
        TextView textViewType = convertView.findViewById(R.id.text_view_type);
        TextView textViewYear = convertView.findViewById(R.id.text_view_year);

        imageView.setImageResource(group.getImageResource());
        textViewTeacherName.setText(group.getTeacherName());
        textViewType.setText(group.getType());
        textViewYear.setText(group.getYear());

        return convertView;
    }
}
