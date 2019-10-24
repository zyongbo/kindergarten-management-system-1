package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.util.Objects;

public class AddUserActivity extends AppCompatActivity {

    private static final String TAG = "AddUserActivity";

    private TextInputLayout textInputName;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputRole;

    private String nameInput;
    private String emailInput;
    private String passwordInput;
    private String roleInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        textInputName = findViewById(R.id.text_input_name);
        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);
        textInputRole = findViewById(R.id.text_input_role);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add a new User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] ROLES = new String[]{"Parent", "Teacher", "Principal"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, ROLES);
        AutoCompleteTextView exposedDropdown = findViewById(R.id.exposed_dropdown_role);
        exposedDropdown.setAdapter(adapter);

        Button buttonAddUser = findViewById(R.id.button_add_user);
        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateName() | !validateEmail() | !validatePassword() | !validateRole()) {
                    return;
                }
                addUser();
            }
        });
    }

    private void addUser() {
        JSONObject params = new JSONObject();
        try {
            params.put("email", emailInput);
            params.put("password", passwordInput);
            params.put("role", roleInput);
            params.put("name", nameInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addUserRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addUser", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addUser response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(AddUserActivity.this, "User successfully registered!", Toast.LENGTH_SHORT).show();
                            } else {
                                switch (response.getString("code")) {
                                    case "ER_DUP_ENTRY":
                                        textInputEmail.setError("Email is in use.");
                                        break;
                                    default:
                                        Toast.makeText(AddUserActivity.this, "ERROR!?", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddUserActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addUserRequest);
    }

    private boolean validateName() {
        nameInput = textInputName.getEditText().getText().toString().trim();

        if (nameInput.isEmpty()) {
            textInputName.setError("Field can't be empty.");
            return false;
        } else {
            textInputName.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        emailInput = textInputEmail.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Field can't be empty.");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Field can't be empty.");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    private boolean validateRole() {
        roleInput = textInputRole.getEditText().getText().toString().trim();

        if (roleInput.isEmpty()) {
            textInputRole.setError("Field can't be empty.");
            return false;
        } else {
            textInputRole.setError(null);
            return true;
        }
    }

    private void clearFields() {
        textInputName.getEditText().setText("");
        textInputEmail.getEditText().setText("");
        textInputPassword.getEditText().setText("");
        textInputRole.getEditText().setText(null);
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
}
