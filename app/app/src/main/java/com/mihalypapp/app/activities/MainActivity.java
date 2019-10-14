package com.mihalypapp.app.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String SHARED_PREFS = "sessionSharedPrefs";
    private static final String ROLE = "role";
    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String SESSION_ID = "sessionId";
    private static final String SESSION_ID_DOMAIN = "sessionIdDomain";

    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_PARENT = "PARENT";

    private static final int RC_PRINCIPAL = 2;
    private static final int RC_TEACHER = 1;
    private static final int RC_PARENT = 0;

    private static final int RESULT_LOGGED_OUT = 100;

    private CookieManager cookieManager;
    private SharedPreferences sharedPreferences;

    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;

    private String emailInput;
    private String passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        loadSession();
    }

    public void login(View v) {
        if (!validateEmail() | !validatePassword()) {
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("email", emailInput);
            params.put("password", passwordInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/login", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Login response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                String role = response.getString("role");
                                String name = response.getString("name");
                                String email = response.getString("email");
                                for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                                    Log.i(TAG, "Cookie from login: " + cookie);
                                    if (cookie.getName().equals("connect.sid")) {
                                        saveSession(
                                                cookie.getValue(),
                                                cookie.getDomain(),
                                                role,
                                                name,
                                                email
                                        );
                                    }
                                }
                                startActivityByRole(role);
                            } else {
                                Toast.makeText(MainActivity.this, "Login was failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(loginRequest);
    }

    public void logout() {
        StringRequest logoutRequest = new StringRequest(Request.Method.GET, "http://192.168.0.157:3000/logout",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Logout response: " + response);
                        if (response.equals("OK")) {
                            Toast.makeText(MainActivity.this, "Successfully logged out!", Toast.LENGTH_SHORT).show();
                            clearSession();
                            clearFields();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                        Log.i(TAG, "Cookies after logout: " + cookieManager.getCookieStore().getCookies().toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(logoutRequest);
    }

    public void startActivityByRole(String role) {
        Intent intent;
        switch (role) {
            case ROLE_PRINCIPAL:
                intent = new Intent(MainActivity.this, PrincipalActivity.class);
                startActivityForResult(intent, RC_PRINCIPAL);
                break;
            case ROLE_TEACHER:
                intent = new Intent(MainActivity.this, TeacherActivity.class);
                startActivityForResult(intent, RC_TEACHER);
                break;
            case ROLE_PARENT:
                intent = new Intent(MainActivity.this, ParentActivity.class);
                startActivityForResult(intent, RC_PARENT);
                break;
            default:
                Log.e(TAG, "No role for starting activity");
                break;
        }
        Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, Integer.valueOf(requestCode).toString());
        switch (requestCode) {
            case RC_PRINCIPAL:
            case RC_PARENT:
            case RC_TEACHER:
                if (resultCode == RESULT_LOGGED_OUT) {
                    logout();
                } else if (resultCode == RESULT_CANCELED) {
                    Log.i(TAG, "Cookies after RESULT_CANCELED: " + cookieManager.getCookieStore().getCookies().toString());
                } else {
                    moveTaskToBack(true);
                }
                break;
        }
    }

    private void saveSession(String sessionId, String domain, String role, String name, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SESSION_ID, sessionId);
        editor.putString(SESSION_ID_DOMAIN, domain);
        editor.putString(ROLE, role);
        editor.putString(NAME, name);
        editor.putString(EMAIL, email);
        editor.apply();
        Log.i(TAG, "Saved session id: " + sessionId);
    }

    private void loadSession() {
        String savedSessionId = sharedPreferences.getString(SESSION_ID, "");
        String savedDomain = sharedPreferences.getString(SESSION_ID_DOMAIN, "");
        String role = sharedPreferences.getString(ROLE, "");

        HttpCookie cookie = new HttpCookie("connect.sid", savedSessionId);
        cookie.setPath("/");
        cookie.setVersion(0);
        cookie.setDomain(savedDomain);

        try {
            cookieManager.getCookieStore().add(new URI(savedDomain), cookie);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (!role.equals("")) startActivityByRole(role);

        Log.i(TAG, "Loaded session id: " + savedSessionId);
    }

    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equals("connect.sid")) {
                try {
                    cookieManager.getCookieStore().remove(new URI(cookie.getDomain()), cookie);
                    return;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
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

    private void clearFields() {
        textInputEmail.getEditText().setText("");
        textInputPassword.getEditText().setText("");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}