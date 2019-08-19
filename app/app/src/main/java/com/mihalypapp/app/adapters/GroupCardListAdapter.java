package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.GroupCard;

import java.util.List;

public class GroupCardListAdapter extends RecyclerView.Adapter<GroupCardListAdapter.ViewHolder> {
    private List<GroupCard> groupCards;

    public GroupCardListAdapter(List<GroupCard> groupCards) {
        this.groupCards = groupCards;
    }

    @NonNull
    @Override
    public GroupCardListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View groupCardView = inflater.inflate(R.layout.item_group_card, parent, false);

        return new ViewHolder(groupCardView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupCardListAdapter.ViewHolder holder, int position) {
        GroupCard groupCard = groupCards.get(position);

        holder.imageView.setImageResource(groupCard.getImageResource());
        holder.textViewTeacherName.setText(groupCard.getTeacherName());
        holder.textViewType.setText(groupCard.getType());
        holder.textViewYear.setText(groupCard.getYear());
    }

    @Override
    public int getItemCount() {
        return groupCards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewTeacherName;
        private TextView textViewType;
        private TextView textViewYear;

        private ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            textViewTeacherName = itemView.findViewById(R.id.text_view_teacher_name);
            textViewType = itemView.findViewById(R.id.text_view_type);
            textViewYear = itemView.findViewById(R.id.text_view_year);
        }
    }
}
