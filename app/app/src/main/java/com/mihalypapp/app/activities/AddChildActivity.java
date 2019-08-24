package com.mihalypapp.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.AutoCompleteGroupAdapter;
import com.mihalypapp.app.adapters.AutoCompleteParentAdapter;
import com.mihalypapp.app.adapters.AutoCompleteTeacherAdapter;
import com.mihalypapp.app.models.Group;
import com.mihalypapp.app.models.Teacher;
import com.mihalypapp.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class AddChildActivity extends AppCompatActivity {

    private static final String TAG = "AddChildActivity";

    private ArrayList<User> parentList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteParents;
    private TextInputLayout textInputLayoutParents;
    private AutoCompleteParentAdapter autoCompleteParentAdapter;

    private ArrayList<Group> groupList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteGroups;
    private TextInputLayout textInputLayoutGroups;
    private AutoCompleteGroupAdapter autoCompleteGroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add a new child");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        autoCompleteParents = findViewById(R.id.auto_complete_parents);
        textInputLayoutParents = findViewById(R.id.text_input_parents);
        textInputLayoutParents.setEnabled(false);

        autoCompleteGroups = findViewById(R.id.auto_complete_groups);
        textInputLayoutGroups = findViewById(R.id.text_input_groups);
        textInputLayoutGroups.setEnabled(false);

        Button buttonAddChild = findViewById(R.id.button_add_child);
        buttonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fetchTeachers();
            }
        });

        fetchParents();
        fetchGroups();
    }

    private void fetchParents() {
        JsonObjectRequest fetchParentsRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.157:3000/parents", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                parentList.clear();
                                Log.i(TAG, response.toString());
                                JSONArray parents = response.getJSONArray("parents");

                                for (int i = 0; i < parents.length(); i++) {
                                    JSONObject parent = parents.getJSONObject(i);
                                    parentList.add(new User(
                                            parent.getInt("userid"),
                                            parent.getString("name"),
                                            parent.getString("email")
                                    ));
                                    autoCompleteParentAdapter = new AutoCompleteParentAdapter(AddChildActivity.this, parentList);
                                    autoCompleteParents.setAdapter(autoCompleteParentAdapter);
                                }
                                textInputLayoutParents.setEnabled(true);
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
        requestQueue.add(fetchParentsRequest);
    }

    private void fetchGroups() {
        JsonObjectRequest fetchGroupsRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.157:3000/groups", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                groupList.clear();
                                Log.i(TAG, response.toString());
                                JSONArray groups = response.getJSONArray("groups");

                                for (int i = 0; i < groups.length(); i++) {
                                    JSONObject group = groups.getJSONObject(i);
                                    groupList.add(new Group(
                                            group.getInt("groupid"),
                                            group.getString("type"),
                                            group.getString("teacherName"),
                                            group.getString("date")
                                    ));
                                    autoCompleteGroupAdapter = new AutoCompleteGroupAdapter(AddChildActivity.this, groupList);
                                    autoCompleteGroups.setAdapter(autoCompleteGroupAdapter);
                                }
                                textInputLayoutGroups.setEnabled(true);
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
        requestQueue.add(fetchGroupsRequest);
    }
}
