package com.mihalypapp.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.AutoCompleteTeacherAdapter;
import com.mihalypapp.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class AddGroupActivity extends AppCompatActivity {

    private static final String TAG = "AddGroupActivity";

    private String[] GROUP_TYPES = new String[]{"LITTLE", "MEDIUM", "BIG"};
    private AutoCompleteTextView autoCompleteGroupTypes;
    private TextInputLayout textInputLayoutGroupTypes;
    private String selectedGroupType;
    private boolean isGroupTypeSelected = false;

    private ArrayList<User> teacherList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteTeachers;
    private TextInputLayout textInputLayoutTeachers;
    private AutoCompleteTeacherAdapter autoCompleteTeacherAdapter;
    private User selectedTeacher;
    private boolean isTeacherSelected = false;

    private Button buttonAddGroup;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.add_a_new_group));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textInputLayoutGroupTypes = findViewById(R.id.text_input_group_types);
        autoCompleteGroupTypes = findViewById(R.id.auto_complete_group_types);
        autoCompleteGroupTypes.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, GROUP_TYPES));
        autoCompleteGroupTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGroupType = (String) adapterView.getAdapter().getItem(i);
                //Toast.makeText(AddGroupActivity.this, selectedGroupType, Toast.LENGTH_SHORT).show();
                isGroupTypeSelected = true;
                textInputLayoutGroupTypes.setError(null);
            }
        });

        textInputLayoutTeachers = findViewById(R.id.text_input_teachers);
        textInputLayoutTeachers.setEnabled(false);
        autoCompleteTeachers = findViewById(R.id.auto_complete_teachers);
        autoCompleteTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTeacher = (User) adapterView.getAdapter().getItem(i);
                //Toast.makeText(AddGroupActivity.this, "id: " + Integer.valueOf(selectedTeacher.getId()).toString(), Toast.LENGTH_SHORT).show();
                isTeacherSelected = true;
                textInputLayoutTeachers.setError(null);
            }
        });
        autoCompleteTeachers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isTeacherSelected = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        buttonAddGroup = findViewById(R.id.button_add_group);
        buttonAddGroup.setEnabled(false);
        buttonAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateGroupType() | !validateTeacher()) {
                    return;
                }
                addGroup();
            }
        });

        fetchTeachers();
    }

    private void fetchTeachers() {
        JsonObjectRequest fetchTeachersRequest = new JsonObjectRequest(Request.Method.GET, MainActivity.URL + "teachers/noGroup", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                teacherList.clear();
                                Log.i(TAG, response.toString());
                                JSONArray teachers = response.getJSONArray("teachers");

                                for (int i = 0; i < teachers.length(); i++) {
                                    JSONObject teacher = teachers.getJSONObject(i);
                                    teacherList.add(new User(
                                            teacher.getInt("userid"),
                                            teacher.getString("name"),
                                            teacher.getString("email")
                                    ));
                                    autoCompleteTeacherAdapter = new AutoCompleteTeacherAdapter(AddGroupActivity.this, teacherList);
                                    autoCompleteTeachers.setAdapter(autoCompleteTeacherAdapter);
                                }
                                textInputLayoutTeachers.setEnabled(true);
                                buttonAddGroup.setEnabled(true);
                            } else {
                                Log.i(TAG, getString(R.string.error));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddGroupActivity.this, getString(R.string.error) + " " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(fetchTeachersRequest);
    }

    private void addGroup() {
        JSONObject params = new JSONObject();
        try {
            params.put("groupType", selectedGroupType);
            params.put("teacherId", selectedTeacher.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addUserRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addGroup response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(AddGroupActivity.this, getString(R.string.group_suc_added), Toast.LENGTH_SHORT).show();
                            } else {
                                switch (response.getString("code")) {
                                    case "ER_DUP_ENTRY":
                                        textInputLayoutTeachers.setError(getString(R.string.teacher_already_has_group_this_year));
                                        break;
                                    default:
                                        Toast.makeText(AddGroupActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddGroupActivity.this, getString(R.string.error) + " " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addUserRequest);
    }

    private boolean validateGroupType() {
        if (isGroupTypeSelected) {
            textInputLayoutGroupTypes.setError(null);
            return true;
        } else {
            textInputLayoutGroupTypes.setError(getString(R.string.select_a_group_type));
            return false;
        }
    }

    private boolean validateTeacher() {
        if (isTeacherSelected) {
            textInputLayoutTeachers.setError(null);
            return true;
        } else {
            textInputLayoutTeachers.setError(getString(R.string.select_a_teacher));
            return false;
        }
    }

    private void clearFields() {
        textInputLayoutTeachers.getEditText().setText("");
        textInputLayoutGroupTypes.getEditText().setText("");
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
