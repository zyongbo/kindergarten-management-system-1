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
import com.mihalypapp.app.models.Group;
import com.mihalypapp.app.models.Poll;

import java.util.List;

public class PollCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_POLL_CARD = 1;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private List<Poll> pollCards;

    private OnItemClickListener listener;

    public PollCardAdapter(List<Poll> pollCards) {
        this.pollCards = pollCards;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_POLL_CARD) {
            View view = inflater.inflate(R.layout.item_poll_card, parent, false);
            viewHolder = new PollCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PollCardViewHolder) {
            Poll pollCard = pollCards.get(position);
            ((PollCardViewHolder) holder).textViewQuestion.setText(pollCard.getQuestion());
            ((PollCardViewHolder) holder).textViewDate.setText(pollCard.getDate());
            ((PollCardViewHolder) holder).textViewStatus.setText(pollCard.getStatus());
            ((PollCardViewHolder) holder).textViewChildName.setText(pollCard.getChildren());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return pollCards.get(position) != null ? VIEW_TYPE_POLL_CARD : VIEW_TPYE_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return pollCards.size();
    }

    private class PollCardViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewQuestion;
        private TextView textViewDate;
        private TextView textViewStatus;
        private TextView textViewChildName;

        private PollCardViewHolder(final View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.text_view_question);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewStatus = itemView.findViewById(R.id.text_view_status);
            textViewChildName = itemView.findViewById(R.id.text_view_child_name);

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

        private ProgressBarViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
