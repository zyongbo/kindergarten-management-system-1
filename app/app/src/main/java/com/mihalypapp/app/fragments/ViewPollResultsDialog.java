package com.mihalypapp.app.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.activities.PollActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewPollResultsDialog extends AppCompatDialogFragment {
    private static final String TAG = "ViewPollResultsDialog";

    private ListView listView;
    private ArrayList<String> gAnswers;
    ArrayAdapter<String> adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view_poll_results, null);

        builder.setView(view)
                .setTitle("Results")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        listView = view.findViewById(R.id.list_view_poll_results);

        gAnswers = new ArrayList<>();

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, gAnswers);
        listView.setAdapter(adapter);

        fetchAnswers();

        return  builder.create();
    }

    private void fetchAnswers() {
        gAnswers.clear();

        JSONObject params = new JSONObject();
        try {
            params.put("pollId", getArguments().getInt("pollId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest answersRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "answers", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, response.toString());
                            if (response.getString("status").equals("success")) {
                                JSONArray answers = response.getJSONArray("answers");
                                for (int i = 0; i < answers.length(); i++) {
                                    JSONObject answer = answers.getJSONObject(i);
                                    gAnswers.add(answer.getString("count") + " - " + answer.getString("answer"));
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getContext(),"Error", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(answersRequest);
    }
}
