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
import com.mihalypapp.app.models.Message;

import java.util.List;

public class MessageCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_MESSAGE_CARD = 1;
    private final int VIEW_TYPE_MESSAGE_CARD_REPLY = 2;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private List<Message> messages;

    private GroupCardAdapter.OnItemClickListener listener;

    public MessageCardAdapter(List<Message> messages) {this.messages = messages; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_MESSAGE_CARD) {
            View view = inflater.inflate(R.layout.item_message, parent, false);
            viewHolder = new MessageCardViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_CARD_REPLY) {
            View view = inflater.inflate(R.layout.item_message_reply, parent, false);
            viewHolder = new MessageCardReplyViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageCardAdapter.MessageCardViewHolder) {
            Message message = messages.get(position);
            ((MessageCardViewHolder) holder).textViewUserName.setText(message.getUserName());
            ((MessageCardViewHolder) holder).textViewMessage.setText(message.getMessage());
            ((MessageCardViewHolder) holder).textViewDatetime.setText(message.getDatetime());
        } else if (holder instanceof MessageCardAdapter.MessageCardReplyViewHolder) {
            Message message = messages.get(position);
            ((MessageCardReplyViewHolder) holder).textViewUserName.setText(message.getUserName());
            ((MessageCardReplyViewHolder) holder).textViewMessage.setText(message.getMessage());
            ((MessageCardReplyViewHolder) holder).textViewDatetime.setText(message.getDatetime());
            ((MessageCardReplyViewHolder) holder).textViewRepyToMessage.setText(message.getReplyToMessage());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position) != null) {
            if (messages.get(position).getHasReply() == 1) {
                return VIEW_TYPE_MESSAGE_CARD_REPLY;
            } else {
                return VIEW_TYPE_MESSAGE_CARD;
            }
        } else {
            return VIEW_TPYE_PROGRESS_BAR;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class MessageCardViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUserName;
        private TextView textViewMessage;
        private TextView textViewDatetime;

        private MessageCardViewHolder(final View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.text_view_user_name);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            textViewDatetime = itemView.findViewById(R.id.text_view_datetime);

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

    private class MessageCardReplyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUserName;
        private TextView textViewMessage;
        private TextView textViewDatetime;
        private TextView textViewRepyToMessage;

        private MessageCardReplyViewHolder(final View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.text_view_user_name);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            textViewDatetime = itemView.findViewById(R.id.text_view_datetime);
            textViewRepyToMessage = itemView.findViewById(R.id.text_view_reply_to_message);

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

    public void setOnItemClickListener(GroupCardAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
