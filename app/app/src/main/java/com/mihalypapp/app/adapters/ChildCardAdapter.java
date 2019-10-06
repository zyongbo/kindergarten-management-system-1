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
import com.mihalypapp.app.models.Child;

import java.util.List;

public class ChildCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_CHILDREN_CARD = 1;
    private final int VIEW_TPYE_PROGRESS_BAR = 0;

    private List<Child> children;

    private OnItemClickListener listener;

    public ChildCardAdapter(List<Child> children) {
        this.children = children;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_TYPE_CHILDREN_CARD) {
            View view = inflater.inflate(R.layout.item_child_card, parent, false);
            viewHolder = new ChildrenCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChildrenCardViewHolder) {
            Child child = children.get(position);
            ((ChildrenCardViewHolder) holder).imageView.setImageResource(child.getImageResource());
            ((ChildrenCardViewHolder) holder).textViewChildName.setText(child.getName());
            ((ChildrenCardViewHolder) holder).textViewGroupType.setText(child.getGroupType());
            ((ChildrenCardViewHolder) holder).textViewParentName.setText(child.getParentName());
            ((ChildrenCardViewHolder) holder).textViewParentEmail.setText(child.getParentEmail());
        } else {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return children.get(position) != null ? VIEW_TYPE_CHILDREN_CARD : VIEW_TPYE_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    private class ChildrenCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewChildName;
        private TextView textViewGroupType;
        private TextView textViewParentName;
        private TextView textViewParentEmail;

        private ChildrenCardViewHolder(final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewChildName = itemView.findViewById(R.id.text_view_child_name);
            textViewGroupType = itemView.findViewById(R.id.text_view_group_type);
            textViewParentName = itemView.findViewById(R.id.text_view_absentees);
            textViewParentEmail = itemView.findViewById(R.id.text_view_parent_email);

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
