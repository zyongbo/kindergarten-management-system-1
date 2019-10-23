package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mihalypapp.app.R;
import com.mihalypapp.app.fragments.ListDocumentsFragment;
import com.mihalypapp.app.models.Document;

import java.util.ArrayList;

public class DocumentCardArrayAdapter extends ArrayAdapter<Document> {
    private int viewType;
    private OnDownloadButtonClickListener downloadButtonListener;
    private OnDeleteButtonClickListener deleteButtonListener;

    public DocumentCardArrayAdapter(Context context, ArrayList<Document> document, int viewType) {
        super(context, 0, document);
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Document document = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_document_card, parent, false);
        }

        TextView textViewDocumentName = convertView.findViewById(R.id.text_view_document_name);
        TextView textViewDocumentDescription = convertView.findViewById(R.id.text_view_document_description);
        TextView textViewDocumentRole = convertView.findViewById(R.id.text_view_document_role);
        TextView textViewDate = convertView.findViewById(R.id.text_view_date);
        Button buttonDelete = convertView.findViewById(R.id.button_delete);
        Button buttonDownload = convertView.findViewById(R.id.button_download);

        textViewDocumentName.setText(document.getName());
        textViewDocumentDescription.setText(document.getDescription());
        textViewDocumentRole.setText(document.getRole());
        textViewDate.setText(document.getDate());

        final View finalConvertView = convertView;
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), Integer.valueOf(document.getId()).toString(), Toast.LENGTH_SHORT).show();
                if (downloadButtonListener != null) {
                    downloadButtonListener.onItemClick(finalConvertView, position);
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteButtonListener != null) {
                    deleteButtonListener.onItemClick(finalConvertView, position);
                }
            }
        });

        if (viewType == 0) {
            buttonDelete.setVisibility(View.GONE);
            textViewDocumentRole.setVisibility(View.GONE);
        }

        return convertView;
    }

    public interface OnDownloadButtonClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnDeleteButtonClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnDownloadButtonClickListener(OnDownloadButtonClickListener downloadButtonListener) {
        this.downloadButtonListener = downloadButtonListener;
    }

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener deleteButtonListener) {
        this.deleteButtonListener = deleteButtonListener;
    }
}
