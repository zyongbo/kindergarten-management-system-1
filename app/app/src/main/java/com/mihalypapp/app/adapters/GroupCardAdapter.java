package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.GroupCard;

import java.util.List;

public class GroupCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_GROUP_CARD = 1;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private List<GroupCard> groupCards;

    public GroupCardAdapter(List<GroupCard> groupCards) {
        this.groupCards = groupCards;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_GROUP_CARD) {
            View view = inflater.inflate(R.layout.item_group_card, parent, false);
            viewHolder = new GroupCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GroupCardViewHolder) {
            GroupCard groupCard = groupCards.get(position);
            ((GroupCardViewHolder) holder).imageView.setImageResource(groupCard.getImageResource());
            ((GroupCardViewHolder) holder).textViewTeacherName.setText(groupCard.getTeacherName());
            ((GroupCardViewHolder) holder).textViewType.setText(groupCard.getType());
            ((GroupCardViewHolder) holder).textViewYear.setText(groupCard.getYear());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return groupCards.get(position) != null ? VIEW_TYPE_GROUP_CARD : VIEW_TPYE_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return groupCards.size();
    }

    private class GroupCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewTeacherName;
        private TextView textViewType;
        private TextView textViewYear;

        private GroupCardViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            textViewTeacherName = itemView.findViewById(R.id.text_view_teacher_name);
            textViewType = itemView.findViewById(R.id.text_view_type);
            textViewYear = itemView.findViewById(R.id.text_view_year);
        }
    }

    private class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        private ProgressBarViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
