package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.GroupCard;

import java.util.List;

public class GroupCardListAdapter extends ListAdapter<GroupCard, GroupCardListAdapter.ViewHolder> {

    public static final DiffUtil.ItemCallback<GroupCard> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GroupCard>() {
                @Override
                public boolean areItemsTheSame(GroupCard oldItem, GroupCard newItem) {
                    return oldItem.getId() == newItem.getId();
                }
                @Override
                public boolean areContentsTheSame(GroupCard oldItem, GroupCard newItem) {
                    return (oldItem.getTeacherName().equals(newItem.getTeacherName()) && oldItem.getType().equals(newItem.getType()) && oldItem.getYear().equals(newItem.getYear()));
                }
            };

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewTeacherName;
        public TextView textViewType;
        public TextView textViewYear;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            textViewTeacherName = itemView.findViewById(R.id.text_view_teacher_name);
            textViewType = itemView.findViewById(R.id.text_view_type);
            textViewYear = itemView.findViewById(R.id.text_view_year);
        }
    }


    public GroupCardListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public GroupCardListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View groupCardView = inflater.inflate(R.layout.item_group_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(groupCardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupCard groupCard = getItem(position);

        holder.imageView.setImageResource(groupCard.getImageResource());
        holder.textViewTeacherName.setText(groupCard.getTeacherName());
        holder.textViewType.setText(groupCard.getType());
        holder.textViewYear.setText(groupCard.getYear());
    }
}
