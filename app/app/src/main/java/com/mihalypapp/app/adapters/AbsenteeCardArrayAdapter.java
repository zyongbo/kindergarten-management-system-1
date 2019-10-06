package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.Child;

import java.util.ArrayList;

public class AbsenteeCardArrayAdapter extends ArrayAdapter<Child> {

    public AbsenteeCardArrayAdapter(Context context, ArrayList<Child> child) {
        super(context, 0, child);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Child child = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_absentee_card, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image_view);
        TextView textViewChildName = convertView.findViewById(R.id.text_view_child_name);
        TextView textViewAbsentees = convertView.findViewById(R.id.text_view_absentees);
        CheckBox checkBox = convertView.findViewById(R.id.check_box);

        imageView.setImageResource(child.getImageResource());
        textViewChildName.setText(child.getName());
        textViewAbsentees.setText(child.getAbsentees());

        return super.getView(position, convertView, parent);
    }
}
