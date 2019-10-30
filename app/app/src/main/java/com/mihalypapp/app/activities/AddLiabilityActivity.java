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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
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

public class AddLiabilityActivity extends AppCompatActivity {

    private static final String TAG = "AddLiabilityActivity";

    public static final String GROUP_ID = "com.mihalypapp.GROUP_ID";
    public static final String CHILD_ID = "com.mihalypapp.CHILD_ID";

    private TextInputLayout textInputLiabilityType;
    private TextInputLayout textInputLiabilityCharge;

    private String chargeInput;
    private String liabilityTypeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_liability);

        textInputLiabilityType = findViewById(R.id.text_input_liability_type);
        textInputLiabilityCharge = findViewById(R.id.text_input_liability_charge);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_liability);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] TYPES = new String[]{"groupCharge", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, TYPES);
        AutoCompleteTextView exposedDropdown = findViewById(R.id.exposed_dropdown_type);
        exposedDropdown.setAdapter(adapter);

        Button buttonAddLiability = findViewById(R.id.button_add_liability);
        buttonAddLiability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateCharge() | !validateType()) {
                    return;
                }
                if(getIntent().hasExtra(GROUP_ID)) {
                    addGroupLiability();
                } else if (getIntent().hasExtra(CHILD_ID)) {
                    addChildLiability();
                }
            }
        });
    }

    private void addGroupLiability() {
        JSONObject params = new JSONObject();
        try {
            params.put("groupId", getIntent().getIntExtra(GROUP_ID, -1));
            params.put("charge", chargeInput);
            params.put("type", liabilityTypeInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addGroupLiabilityRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addGroupLiability", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addUser response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(AddLiabilityActivity.this, getString(R.string.liability_suc_added), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddLiabilityActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddLiabilityActivity.this, getString(R.string.error) + " " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addGroupLiabilityRequest);
    }

    private void addChildLiability() {
        JSONObject params = new JSONObject();
        try {
            params.put("childId", getIntent().getIntExtra(CHILD_ID, -1));
            params.put("charge", chargeInput);
            params.put("type", liabilityTypeInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addChildLiabilityRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addChildLiability", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addUser response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(AddLiabilityActivity.this, getString(R.string.liability_suc_added), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddLiabilityActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddLiabilityActivity.this, getString(R.string.error) + " " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addChildLiabilityRequest);
    }

    private boolean validateCharge() {
        chargeInput = textInputLiabilityCharge.getEditText().getText().toString().trim();

        if (chargeInput.isEmpty()) {
            textInputLiabilityCharge.setError(getString(R.string.field_cant_be_empty));
            return false;
        } else {
            try {
                Integer.parseInt(chargeInput);
            } catch (NumberFormatException e) {
                textInputLiabilityCharge.setError(getString(R.string.use_only_num));
                return false;
            }
            textInputLiabilityCharge.setError(null);
            return true;
        }
    }

    private boolean validateType() {
        liabilityTypeInput = textInputLiabilityType.getEditText().getText().toString().trim();

        if (liabilityTypeInput.isEmpty()) {
            textInputLiabilityType.setError(getString(R.string.field_cant_be_empty));
            return false;
        } else {
            textInputLiabilityType.setError(null);
            return true;
        }
    }

    private void clearFields() {
        textInputLiabilityCharge.getEditText().setText("");
        textInputLiabilityType.getEditText().setText("");
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
