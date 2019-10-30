package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public class AddNewsActivity extends AppCompatActivity {

    private static final String TAG = "AddUserActivity";

    private TextInputLayout textInputTitle;
    private TextInputLayout textInputContent;

    private String titleInput;
    private String contentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_news);

        textInputTitle = findViewById(R.id.text_input_title);
        textInputContent = findViewById(R.id.text_input_content);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.add_a_news));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonAddUser = findViewById(R.id.button_add_news);
        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateTitle() | !validateContent()) {
                    return;
                }
                addNews();
            }
        });
    }

    private void addNews() {
        JSONObject params = new JSONObject();
        try {
            params.put("title", titleInput);
            params.put("content", contentInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addNewsRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addNews", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addNews response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(AddNewsActivity.this, getString(R.string.news_suc_added), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddNewsActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddNewsActivity.this, getString(R.string.error) + " " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addNewsRequest);
    }

    private boolean validateTitle() {
        titleInput = textInputTitle.getEditText().getText().toString().trim();

        if (titleInput.isEmpty()) {
            textInputTitle.setError(getString(R.string.field_cant_be_empty));
            return false;
        } else {
            textInputTitle.setError(null);
            return true;
        }
    }

    private boolean validateContent() {
        contentInput = textInputContent.getEditText().getText().toString().trim();

        if (contentInput.isEmpty()) {
            textInputContent.setError(getString(R.string.field_cant_be_empty));
            return false;
        } else {
            textInputContent.setError(null);
            return true;
        }
    }

    private void clearFields() {
        textInputTitle.getEditText().setText("");
        textInputContent.getEditText().setText("");
        getCurrentFocus().clearFocus();
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
