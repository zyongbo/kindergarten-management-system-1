package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.fragments.ViewPollResultsDialog;
import com.mihalypapp.app.models.Poll;
import com.mihalypapp.app.models.PollOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class PollActivity extends AppCompatActivity {

    private static final String TAG = "PollActivity";

    private TextView textViewQuestion;
    private TextView textviewDate;
    private ListView listViewOptions;
    private Button button;

    private ArrayList<PollOption> optionList;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Poll");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewQuestion = findViewById(R.id.text_view_question);
        textviewDate = findViewById(R.id.text_view_date);
        listViewOptions = findViewById(R.id.list_view_options);
        button = findViewById(R.id.button);
        button.setVisibility(View.INVISIBLE);

        textViewQuestion.setText(getIntent().getStringExtra("question"));

        optionList = new ArrayList<>();

        listViewOptions.setChoiceMode(listViewOptions.CHOICE_MODE_SINGLE);

        adapter = new ArrayAdapter(PollActivity.this, android.R.layout.simple_list_item_single_choice, optionList);
        listViewOptions.setAdapter(adapter);

        listViewOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                saveAnswer(i);
                Toast.makeText(PollActivity.this, optionList.get(i).getOption(), Toast.LENGTH_SHORT).show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().getStringExtra("status").equals("ACTIVE")) {
                    endPoll();
                } else {
                    openResultDialog();
                }
            }
        });

        fetchOptions();
    }

    private void fetchOptions() {
        optionList.clear();

        JSONObject params = new JSONObject();
        try {
            params.put("pollId", getIntent().getIntExtra("pollId", -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest optionsRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "options", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, response.toString());
                            if (response.getString("status").equals("success")) {
                                if (response.getString("userRole").equals("TEACHER")) {
                                    button.setVisibility(View.VISIBLE);
                                }
                                JSONArray options = response.getJSONArray("options");
                                for (int i = 0; i < options.length(); i++) {
                                    JSONObject option = options.getJSONObject(i);
                                    optionList.add(new PollOption(
                                            option.getInt("optionId"),
                                            option.getInt("pollId"),
                                            option.getString("option")
                                    ));
                                }
                                adapter.notifyDataSetChanged();

                                if (getIntent().getStringExtra("status").equals("ENDED")) {
                                    listViewOptions.setEnabled(false);
                                    button.setText(getString(R.string.view_results));
                                }

                                if (response.getJSONObject("alreadyVoted").getInt("alreadyVoted") == 1) {
                                    listViewOptions.setItemChecked(response.getJSONObject("alreadyVoted").getInt("optionPos"), true);
                                }

                            } else if (response.getString("status").equals("failed")) {
                                if (response.getString("code").equals("ER_DUP_ENTRY")) {
                                    Toast.makeText(PollActivity.this, getString(R.string.already_voted), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(PollActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(optionsRequest);
    }

    private void saveAnswer(int pos) {
        JSONObject params = new JSONObject();
        try {
            params.put("optionId", optionList.get(pos).getOptionId());
            params.put("pollId", optionList.get(pos).getPollId());
            params.put("optionPos", pos);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest optionsRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "saveOptionAnswer", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(PollActivity.this,  getString(R.string.answer_suc_saved), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PollActivity.this,getString(R.string.error) + " ", Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(optionsRequest);
    }

    private void endPoll() {
        JSONObject params = new JSONObject();
        try {
            params.put("pollId", getIntent().getIntExtra("pollId", -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest endPollRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "endPoll", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(PollActivity.this, getString(R.string.poll_suc_ended), Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(PollActivity.this,getString(R.string.error), Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(endPollRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openResultDialog() {
        ViewPollResultsDialog pollResultsDialog = new ViewPollResultsDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("pollId", getIntent().getIntExtra("pollId", -1));
        pollResultsDialog.setArguments(bundle);
        pollResultsDialog.show(getSupportFragmentManager(), "poll results dialog");
    }
}
