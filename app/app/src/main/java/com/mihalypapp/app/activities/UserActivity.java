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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.ChildCardArrayAdapter;
import com.mihalypapp.app.adapters.GroupCardArrayAdapter;
import com.mihalypapp.app.models.Child;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";

    public static final String USER_ID = "com.mihalypapp.USER_ID";

    private TextView textViewFullName;
    private TextView textViewEmail;
    private TextView textViewRole;
    private TextView textView;


    private ArrayList<Child> childList = new ArrayList<>();
    private ArrayList<Group> groupList = new ArrayList<>();
    ChildCardArrayAdapter childAdapter;
    GroupCardArrayAdapter groupAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("User info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewFullName = findViewById(R.id.text_view_full_name);
        textViewEmail = findViewById(R.id.text_view_email);
        textViewRole = findViewById(R.id.text_view_role);
        textView = findViewById(R.id.text_view);

        listView = findViewById(R.id.list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getAdapter() instanceof ChildCardArrayAdapter) {
                    Child child = (Child) adapterView.getItemAtPosition(i);
                    Intent intent = new Intent(UserActivity.this, ChildActivity.class);
                    intent.putExtra(ChildActivity.CHILD_ID, child.getId());
                    startActivity(intent);
                } else if (adapterView.getAdapter() instanceof GroupCardArrayAdapter){
                    Group group = (Group) adapterView.getItemAtPosition(i);
                    Intent intent = new Intent(UserActivity.this, GroupActivity.class);
                    intent.putExtra(GroupActivity.GROUP_ID, group.getId());
                    startActivity(intent);
                }
            }
        });

        fetchUser();
    }

    private void fetchUser() {
        JSONObject params = new JSONObject();
        Intent intent = getIntent();
        int userId = intent.getIntExtra(USER_ID, -1);
        if (userId == -1) {
            Log.i(TAG, "missing userId (-1)");
        }

        try {
            params.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest userRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/user", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONObject user = response.getJSONArray("user").getJSONObject(0);
                                String role = user.getString("Role");
                                textViewFullName.setText(user.getString("Name"));
                                textViewEmail.setText(user.getString("Email"));
                                textViewRole.setText(role);

                                if (role.equals("PARENT")) {
                                    textView.setText("Children");
                                    JSONArray children = response.getJSONArray("data");
                                    for (int i = 0; i < children.length(); i++) {
                                        JSONObject child = children.getJSONObject(i);
                                        childList.add(new Child(
                                                child.getInt("childId"),
                                                R.drawable.ic_launcher_foreground,
                                                child.getString("childName"),
                                                child.getString("groupYear"),
                                                child.getString("teacherName"),
                                                child.getString("teacherEmail")
                                        ));
                                        childAdapter = new ChildCardArrayAdapter(UserActivity.this, childList, 1);
                                        listView.setAdapter(childAdapter);
                                    }
                                } else if (role.equals("TEACHER")) {
                                    textView.setText("Groups");
                                    JSONArray groups = response.getJSONArray("data");
                                    for (int i = 0; i < groups.length(); i++) {
                                        JSONObject group = groups.getJSONObject(i);
                                        groupList.add(new Group(
                                                group.getInt("groupId"),
                                                group.getString("groupType"),
                                                group.getString("groupYear"),
                                                R.drawable.ic_launcher_foreground
                                        ));
                                        groupAdapter = new GroupCardArrayAdapter(UserActivity.this, groupList, 1);
                                        listView.setAdapter(groupAdapter);
                                    }
                                }


                            } else {
                                Toast.makeText(UserActivity.this,"Error", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(userRequest);
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
