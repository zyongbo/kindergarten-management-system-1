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
import com.mihalypapp.app.models.ItemUserCard;

import java.util.ArrayList;

public class UserCardListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private ArrayList<ItemUserCard> userCardList;

    public UserCardListAdapter(ArrayList<ItemUserCard> userCardList) {
        this.userCardList = userCardList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
                return new UserCardListViewHolder(view);
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(view);
            default:
                return null;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemUserCard currentItem = userCardList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_ITEM:
                UserCardListViewHolder userCardListViewHolder = (UserCardListViewHolder) holder;
                userCardListViewHolder.imageView.setImageResource(currentItem.getImageResource());
                userCardListViewHolder.textViewName.setText(currentItem.getName());
                userCardListViewHolder.textViewEmail.setText(currentItem.getEmail());
                break;
            case VIEW_TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return userCardList == null ? 0 : userCardList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return userCardList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private static class UserCardListViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewName;
        private TextView textViewEmail;

        private UserCardListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewEmail = itemView.findViewById(R.id.text_view_email);
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
