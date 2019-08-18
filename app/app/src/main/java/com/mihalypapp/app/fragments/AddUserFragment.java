package com.mihalypapp.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class AddUserFragment extends Fragment {

    private static final String TAG = "AddUserFragment";

    private View view;
    private TextInputLayout textInputName;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputRole;

    private String nameInput;
    private String emailInput;
    private String passwordInput;
    private String roleInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_user, container, false);

        textInputName = view.findViewById(R.id.text_input_name);
        textInputEmail = view.findViewById(R.id.text_input_email);
        textInputPassword = view.findViewById(R.id.text_input_password);
        textInputRole = view.findViewById(R.id.text_input_role);

        String[] ROLES = new String[]{"Parent", "Teacher", "Principal"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, ROLES);
        AutoCompleteTextView exposedDropdown = view.findViewById(R.id.exposed_dropdown_role);
        exposedDropdown.setAdapter(adapter);

        Button buttonAddUser = view.findViewById(R.id.button_add_user);
        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateName() | !validateEmail() | !validatePassword() | !validateRole()) {
                    return;
                }
                addUser();
            }
        });
        return view;
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

        JsonObjectRequest addUserRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/addUser", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addUser response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(getContext(), "User successfully registered!", Toast.LENGTH_SHORT).show();
                            } else {
                                switch (response.getString("code")) {
                                    case "ER_DUP_ENTRY":
                                        textInputEmail.setError("Email is in use.");
                                        break;
                                    default:
                                        Toast.makeText(getContext(), "ERROR!?", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Error " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
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
        view.clearFocus();
    }
}
