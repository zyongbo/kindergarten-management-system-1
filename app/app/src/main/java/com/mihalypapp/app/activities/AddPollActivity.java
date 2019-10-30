package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class AddPollActivity extends AppCompatActivity {

    private static final String TAG = "AddPollActivity";

    private Button buttonAddNewOption;
    private MenuItem itemAddPoll;
    private TextInputLayout textInputQuestion;
    private String questionInput;
    private ArrayList<String> optionInputs = new ArrayList<>();

    private int editTextID = 4999;
    private int inputLayoutID = -1;
    private String optionHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        optionHint = getString(R.string.option);
        setContentView(R.layout.activity_add_poll);

        buttonAddNewOption = findViewById(R.id.button_add_new_option);
        textInputQuestion = findViewById(R.id.text_input_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_a_poll);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addNewOption();

        buttonAddNewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewOption();
            }
        });
    }

    private void addNewOption() {
        editTextID++;
        inputLayoutID++;
        LinearLayout linearLayout = findViewById(R.id.linear_layout_options);

        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setId(inputLayoutID);
        textInputLayout.setErrorEnabled(true);

        TextInputEditText textInputEditText = new TextInputEditText(this);
        textInputEditText.setId(editTextID);
        textInputEditText.setHint(optionHint + " " + Integer.valueOf(inputLayoutID + 1).toString());
        textInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);


        textInputLayout.addView(textInputEditText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(textInputLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addPoll() {
        if (!validateOption() | !validateQuestion()) {
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (String option : optionInputs) {
            jsonArray.put(option);
        }

        JSONObject params = new JSONObject();
        try {
            params.put("question", questionInput);
            params.put("options", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addPollRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addPoll", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, response.toString());
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(AddPollActivity.this, getString(R.string.poll_suc_added), Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(AddPollActivity.this,getString(R.string.error), Toast.LENGTH_SHORT).show();
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
        requestQueue.add(addPollRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_poll_menu, menu);

        itemAddPoll = menu.findItem(R.id.item_add_poll);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            case R.id.item_add_poll:
                addPoll();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean validateQuestion() {
        questionInput = textInputQuestion.getEditText().getText().toString().trim();

        if (questionInput.isEmpty()) {
            textInputQuestion.setError(getString(R.string.question_cant_be_empty));
            return false;
        } else {
            textInputQuestion.setError(null);
            return true;
        }
    }

    private boolean validateOption() {
        optionInputs.clear();
        boolean hasEmpty = false;
        for (int i = 0; i <= inputLayoutID; ++i) {
            TextInputLayout textInputLayout = findViewById(i);
            String optionInput = textInputLayout.getEditText().getText().toString().trim();

            if (optionInput.isEmpty()) {
                textInputLayout.setError(getString(R.string.option_cant_be_empty));
                hasEmpty = true;
            } else {
                optionInputs.add(optionInput);
                textInputLayout.setError(null);
            }
        }
        if (hasEmpty) {
            return false;
        } else {
            return true;
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

