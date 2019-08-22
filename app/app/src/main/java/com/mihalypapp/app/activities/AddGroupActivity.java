package com.mihalypapp.app.activities;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.mihalypapp.app.models.Teacher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class AddGroupActivity extends AppCompatActivity {

    private static final String TAG = "AddGroupActivity";

    private ArrayList<Teacher> teacherList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteTeachers;
    private TextInputLayout textInputLayoutTeachers;
    private AutoCompleteTeacherAdapter autoCompleteTeacherAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add a new group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] GROUP_TYPES = new String[]{"LITTLE", "MEDIUM", "BIG"};
        AutoCompleteTextView autoCompleteGroupTypes = findViewById(R.id.exposed_dropdown_type);
        autoCompleteGroupTypes.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, GROUP_TYPES));

        autoCompleteTeachers = findViewById(R.id.exposed_dropdown_teacher);
        textInputLayoutTeachers = findViewById(R.id.text_input_teacher);
        textInputLayoutTeachers.setEnabled(false);

        /*exposedDropdown2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(AddGroupActivity.this, "lel", Toast.LENGTH_SHORT).show();
            }
        });

        exposedDropdown2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Teacher selected = (Teacher) adapterView.getAdapter().getItem(i);
                Toast.makeText(AddGroupActivity.this, Integer.valueOf(selected.getId()).toString(), Toast.LENGTH_SHORT).show();
            }
        });*/

        /*exposedDropdown2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fetchTeachers();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/

        Button buttonAddGroup = findViewById(R.id.button_add_group);
        buttonAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fetchTeachers();
            }
        });

        fetchTeachers();
    }

    private void fetchTeachers() {
        JsonObjectRequest fetchTeachersRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.157:3000/teachers/noGroup", null,
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
                                    teacherList.add(new Teacher(
                                            teacher.getInt("userid"),
                                            teacher.getString("name"),
                                            teacher.getString("email")
                                    ));
                                    autoCompleteTeacherAdapter = new AutoCompleteTeacherAdapter(AddGroupActivity.this, teacherList);
                                    autoCompleteTeachers.setAdapter(autoCompleteTeacherAdapter);
                                }
                                textInputLayoutTeachers.setEnabled(true);
                            } else {
                                Log.i(TAG, "Smthg wrong!");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(fetchTeachersRequest);
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
