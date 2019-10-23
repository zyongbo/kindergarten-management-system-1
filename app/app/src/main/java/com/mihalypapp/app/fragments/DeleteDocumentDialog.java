package com.mihalypapp.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DeleteDocumentDialog extends AppCompatDialogFragment {
    private DeleteDocumentListener listener;
    private int docId;

    DeleteDocumentDialog(int docId) {
        this.docId = docId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle("Attention!")
                .setMessage("Do you want to delete this document?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDeleteYesClicked(docId);
                    }
                });
        return builder.create();
    }

    public interface DeleteDocumentListener {
        void onDeleteYesClicked(int docId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DeleteDocumentListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DeleteDocumentListener");
        }
    }
}
