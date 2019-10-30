package com.mihalypapp.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mihalypapp.app.R;

public class MessageReplyDialog extends AppCompatDialogFragment {
    private TextView textViewReplyToMessage;
    private EditText editTextMessage;
    private MessageReplyDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_message_reply, null);

        builder.setView(view)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String message = editTextMessage.getText().toString();
                        String replyToMessage = textViewReplyToMessage.getText().toString();
                        listener.applyMessage(message, replyToMessage);
                    }
                });

        editTextMessage = view.findViewById(R.id.edit_text_message);
        textViewReplyToMessage = view.findViewById(R.id.text_view_reply_to_message);
        textViewReplyToMessage.setText(getArguments().getString("replyToMessage"));

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (MessageReplyDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement MessageRelpyDialog");
        }
    }

    public interface MessageReplyDialogListener {
        void applyMessage(String message, String replyToMessage);
    }
}
