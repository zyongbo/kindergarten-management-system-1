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
import com.mihalypapp.app.models.MessagePartner;

import java.util.List;

public class MessagePartnerCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_MESSAGE_PARTNER_CARD = 1;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private List<MessagePartner> messagePartners;

    private OnItemClickListener listener;

    public MessagePartnerCardAdapter(List<MessagePartner> messagePartners) {this.messagePartners = messagePartners; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_MESSAGE_PARTNER_CARD) {
            View view = inflater.inflate(R.layout.item_message_partner_card, parent, false);
            viewHolder = new MessagePartnerCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessagePartnerCardViewHolder) {
            MessagePartner messagePartner = messagePartners.get(position);
            ((MessagePartnerCardViewHolder) holder).imageView.setImageResource(messagePartner.getImageResource());
            ((MessagePartnerCardViewHolder) holder).textViewPartnerName.setText(messagePartner.getPartnerName());
            ((MessagePartnerCardViewHolder) holder).textViewDatetime.setText(messagePartner.getDatetime());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messagePartners.get(position) != null ? VIEW_TYPE_MESSAGE_PARTNER_CARD : VIEW_TPYE_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return messagePartners.size();
    }

    private class MessagePartnerCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewPartnerName;
        private TextView textViewDatetime;

        private MessagePartnerCardViewHolder(final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewDatetime = itemView.findViewById(R.id.text_view_datetime);
            textViewPartnerName = itemView.findViewById(R.id.text_partner_name);

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
