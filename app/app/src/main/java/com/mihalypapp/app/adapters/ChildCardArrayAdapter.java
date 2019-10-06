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
import com.mihalypapp.app.models.Child;

import java.util.ArrayList;

public class ChildCardArrayAdapter extends ArrayAdapter<Child> {
    private int viewType;

    public ChildCardArrayAdapter(Context context, ArrayList<Child> child, int viewType) {
        super(context, 0, child);
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Child child = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_child_card, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image_view);
        TextView textViewChildName = convertView.findViewById(R.id.text_view_child_name);
        TextView textViewGroupType = convertView.findViewById(R.id.text_view_group_type);
        TextView textViewParentName = convertView.findViewById(R.id.text_view_absentees);
        TextView textViewParentEmail = convertView.findViewById(R.id.text_view_parent_email);

        imageView.setImageResource(child.getImageResource());
        textViewChildName.setText(child.getName());
        textViewGroupType.setText(child.getGroupType());

        if (viewType == 1) {
            textViewParentName.setText(child.getParentName());
            textViewParentEmail.setText(child.getParentEmail());
        } else {
            textViewParentName.setVisibility(View.INVISIBLE);
            textViewParentEmail.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
