package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihalypapp.app.R;
import com.mihalypapp.app.models.News;

import java.util.List;

public class NewsCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_NEWS_CARD = 1;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private List<News> newsCards;

    public NewsCardAdapter(List<News> newsCards) {
        this.newsCards = newsCards;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_NEWS_CARD) {
            View view = inflater.inflate(R.layout.item_news_card, parent, false);
            viewHolder = new NewsCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsCardViewHolder) {
            News newsCard = newsCards.get(position);
            ((NewsCardViewHolder) holder).textViewContent.setText(newsCard.getContent());
            ((NewsCardViewHolder) holder).textViewDate.setText(newsCard.getDate());
            ((NewsCardViewHolder) holder).textViewTitle.setText(newsCard.getTitle());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return newsCards.get(position) != null ? VIEW_TYPE_NEWS_CARD : VIEW_TPYE_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return newsCards.size();
    }

    private class NewsCardViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDate;
        private TextView textViewContent;
        private TextView textViewTitle;

        private NewsCardViewHolder(final View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewContent = itemView.findViewById(R.id.text_view_content);
            textViewDate = itemView.findViewById(R.id.text_view_date);
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
