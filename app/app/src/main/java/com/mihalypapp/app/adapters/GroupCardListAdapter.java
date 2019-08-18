package com.mihalypapp.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.ItemGroupCard;
import com.mihalypapp.app.models.ItemUserCard;

import java.util.ArrayList;

public class GroupCardListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private ArrayList<ItemGroupCard> groupCardList;

    public GroupCardListAdapter(ArrayList<ItemGroupCard> groupCardList) {
        this.groupCardList = groupCardList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_card, parent, false);
                return new GroupCardListViewHolder(view);
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(view);
            default:
                return null;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemGroupCard currentItem = groupCardList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_ITEM:
                GroupCardListViewHolder userCardListViewHolder = (GroupCardListViewHolder) holder;
                userCardListViewHolder.imageView.setImageResource(currentItem.getImageResource());
                userCardListViewHolder.textViewTeacherName.setText(currentItem.getTeacherName());
                userCardListViewHolder.textViewType.setText(currentItem.getType());
                userCardListViewHolder.textViewYear.setText(currentItem.getYear());
                break;
            case VIEW_TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return groupCardList == null ? 0 : groupCardList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return groupCardList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private static class GroupCardListViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewTeacherName;
        private TextView textViewType;
        private TextView textViewYear;

        private GroupCardListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewTeacherName = itemView.findViewById(R.id.text_view_teacher_name);
            textViewType = itemView.findViewById(R.id.text_view_type);
            textViewYear = itemView.findViewById(R.id.text_view_year);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        private LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
