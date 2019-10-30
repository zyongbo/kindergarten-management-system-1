package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.MessageCardAdapter;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";

    public static final String PARTNER_ID = "com.mihalypapp.PARTNER_ID";
    public static final String PARTNER_NAME = "com.mihalypapp.PARTNER_NAME";

    private ArrayList<Message> messageList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean messageAdded = false;
    private boolean showingProgressBar = false;
    private int offset = 0;

    private RecyclerView recyclerView;
    private MessageCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;
    private EditText editTextMessage;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getIntent().getStringExtra(PARTNER_NAME));
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        adapter = new MessageCardAdapter(messageList);
        recyclerView = findViewById(R.id.recycler_view_messages);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching && !messageAdded) {
                    addProgressBar();
                    fetchMessages();
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        swipeContainer = findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!fetching) {
                    messageAdded = false;
                    refreshing = true;
                    offset = 0;
                    fetchMessages();
                }
            }
        });

        buttonSend = findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMessage();
            }
        });

        editTextMessage = findViewById(R.id.edit_text_message);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0) {
                    buttonSend.setEnabled(true);
                } else {
                    buttonSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        fetchMessages();
    }

    private void fetchMessages() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
            params.put("partnerId", getIntent().getIntExtra(PARTNER_ID, -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getMessageRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "messages", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray messages = response.getJSONArray("messages");

                                removeProgressBar();

                                if (refreshing) {
                                    messageList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < messages.length(); i++) {
                                    JSONObject message = messages.getJSONObject(i);
                                    String userName = getString(R.string.me);
                                    if(message.getInt("Own") == 0) {
                                        userName = getIntent().getStringExtra(PARTNER_NAME);
                                    }
                                    messageList.add(new Message(
                                            userName,
                                            message.getString("message"),
                                            message.getString("datetime")
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(messageList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                    recyclerView.scrollToPosition(0);
                                }

                            } else {
                                Log.e(TAG, "getMessageRequest ERROR");
                            }

                            swipeContainer.setRefreshing(false);
                            fetching = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(getMessageRequest);
    }

    private void addMessage() {
        messageAdded = true;
        Date dt = new Date();
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(dt);
        final Message newMessage = new Message("Me", editTextMessage.getText().toString(), currentTime);

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
            params.put("partnerId", getIntent().getIntExtra(PARTNER_ID, -1));
            params.put("message", editTextMessage.getText().toString());
            params.put("datetime", currentTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest addMessageRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addMessage", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                messageList.add(0, newMessage);
                                adapter.notifyItemInserted(0);
                                recyclerView.scrollToPosition(0);
                            } else {
                                Toast.makeText(MessageActivity.this, "Message wasn't sent!", Toast.LENGTH_SHORT).show();
                            }
                            editTextMessage.setText("");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addMessageRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        messageList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(messageList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            messageList.remove(messageList.size() - 1);
            adapter.notifyItemRemoved(messageList.size());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("lang", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("lang", "");
        setLocale(language);
    }
}
