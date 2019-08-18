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
import com.mihalypapp.app.models.ItemGroupCard;

import java.util.List;

public class GroupCardListAdapter extends RecyclerView.Adapter<GroupCardListAdapter.ViewHolder> {

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

    private List<ItemGroupCard> groupCards;

    public GroupCardListAdapter(List<ItemGroupCard> groupCards) {
        this.groupCards = groupCards;
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
    public void onBindViewHolder(@NonNull GroupCardListAdapter.ViewHolder holder, int position) {
        ItemGroupCard groupCard = groupCards.get(position);

        holder.imageView.setImageResource(groupCard.getImageResource());
        holder.textViewTeacherName.setText(groupCard.getTeacherName());
        holder.textViewType.setText(groupCard.getType());
        holder.textViewYear.setText(groupCard.getYear());
    }

    @Override
    public int getItemCount() {
        return groupCards.size();
    }
}
