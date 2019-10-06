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
import com.mihalypapp.app.models.User;
import com.mihalypapp.app.models.UserCard;

import java.util.ArrayList;

public class UserCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_USER_CARD = 1;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private ArrayList<User> userCards;

    private OnItemClickListener listener;

    public UserCardAdapter(ArrayList<User> userCards) {
        this.userCards = userCards;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_USER_CARD) {
            View view = inflater.inflate(R.layout.item_user_card, parent, false);
            viewHolder = new UserCardAdapter.UserCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new UserCardAdapter.ProgressBarViewHolder(view);
        }

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserCardViewHolder) {
            User userCard = userCards.get(position);
            ((UserCardViewHolder) holder).imageView.setImageResource(userCard.getImageResource());
            ((UserCardViewHolder) holder).textViewName.setText(userCard.getName());
            ((UserCardViewHolder) holder).textViewEmail.setText(userCard.getEmail());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return userCards.get(position) != null ? VIEW_TYPE_USER_CARD : VIEW_TPYE_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return userCards.size();
    }

    private class UserCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewName;
        private TextView textViewEmail;

        private UserCardViewHolder(final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewEmail = itemView.findViewById(R.id.text_view_email);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }

    private class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        private ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(UserCardAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
