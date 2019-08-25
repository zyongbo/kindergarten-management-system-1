package com.mihalypapp.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.models.Child;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupOverviewActivity extends AppCompatActivity {

    private static final String TAG = "GroupOverviewActivity";

    private int groupId;

    private TextView textViewTeacherName;
    private TextView textViewGroupType;
    private TextView textViewGroupYear;
    private TextView textViewGroupSize;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private Group group;
    private List<Child> childList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_overview);

        groupId = getIntent().getExtras().getInt("GROUP_ID");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Group overview");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewTeacherName = findViewById(R.id.text_view_teacher_name);
        textViewGroupType = findViewById(R.id.text_view_group_type);
        textViewGroupYear = findViewById(R.id.text_view_group_year);
        textViewGroupSize = findViewById(R.id.text_view_group_size);

        fetchGroup();
    }

    private void fetchGroup() {
        JSONObject params = new JSONObject();
        try {
            params.put("groupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest fetchGroupRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/group", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, "fetchGroup response: " + response.toString());
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
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            "",
                                            child.getString("parentName"),
                                            child.getString("parentEmail")
                                    ));
                                }
                            } else {
                                Log.e(TAG, "fetchGroupRequest ERROR");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GroupOverviewActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(fetchGroupRequest);
    }
}
