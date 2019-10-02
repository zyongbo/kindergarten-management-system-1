package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.ChildCardArrayAdapter;
import com.mihalypapp.app.adapters.GroupCardArrayAdapter;
import com.mihalypapp.app.fragments.MyChildrenFragment;
import com.mihalypapp.app.fragments.MyGroupsFragment;
import com.mihalypapp.app.models.Child;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {

    private static final String TAG = "GroupActivity";

    public static final String GROUP_ID = "com.mihalypapp.GROUP_ID";

    private TextView textViewTeacherName;
    private TextView textViewGroupType;
    private TextView textViewGroupYear;

    private Group group;
    private ArrayList<Child> childList = new ArrayList<>();
    ChildCardArrayAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewTeacherName = findViewById(R.id.text_view_teacher_name);
        textViewGroupType = findViewById(R.id.text_view_group_type);
        textViewGroupYear = findViewById(R.id.text_view_group_year);

        listView = findViewById(R.id.list_view_children);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Child child = (Child) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(GroupActivity.this, ChildActivity.class);
                intent.putExtra(ChildActivity.CHILD_ID, child.getId());
                startActivity(intent);
            }
        });

        fetchGroup();
    }

    private void fetchGroup() {
        JSONObject params = new JSONObject();
        Intent intent = getIntent();
        final int groupId = intent.getIntExtra(GROUP_ID, -1);
        if (groupId == -1) {
            Log.e(TAG, "-1 groupId");
            return;
        }
        try {
            params.put("groupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest childRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/group", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONObject resGroup = response.getJSONArray("group").getJSONObject(0);
                                group = new Group(
                                        resGroup.getInt("groupid"),
                                        resGroup.getString("type"),
                                        resGroup.getString("teacherName"),
                                        resGroup.getString("year")
                                );
                                textViewTeacherName.setText(group.getTeacherName());
                                textViewGroupType.setText(group.getType());
                                textViewGroupYear.setText(group.getYear());

                                JSONArray children = response.getJSONArray("children");
                                for (int i = 0; i < children.length(); i++) {
                                    JSONObject child = children.getJSONObject(i);
                                    childList.add(new Child(
                                            child.getInt("childId"),
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            "",
                                            child.getString("parentName"),
                                            child.getString("parentEmail")
                                    ));
                                    adapter = new ChildCardArrayAdapter(GroupActivity.this, childList, 1);
                                    listView.setAdapter(adapter);
                                }
                            } else {
                                Toast.makeText(GroupActivity.this,"Error", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(childRequest);
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
}
