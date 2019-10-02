package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.mihalypapp.app.fragments.MyChildrenFragment;
import com.mihalypapp.app.models.Child;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ChildActivity extends AppCompatActivity {

    private static final String TAG = "ChildActivity";

    public static final String CHILD_ID = "com.mihalypapp.CHILD_ID";

    private TextView textViewChildName;
    private TextView textViewParentName;
    private TextView textViewDateOfBirth;
    private TextView textViewTeacherName;
    private TextView textViewGroupType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Child");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewChildName = findViewById(R.id.text_view_child_name);
        textViewParentName = findViewById(R.id.text_view_parent_name);
        textViewDateOfBirth = findViewById(R.id.text_view_date_of_birth);
        textViewTeacherName = findViewById(R.id.text_view_teacher_name);
        textViewGroupType = findViewById(R.id.text_view_group_type);

        fetchChild();
    }

    private void fetchChild() {
        JSONObject params = new JSONObject();
        Intent intent = getIntent();
        int childId = intent.getIntExtra(CHILD_ID, -1);
        if (childId == -1) {
            Log.e(TAG, "-1 childId");
            return;
        }
        try {
            params.put("childId", childId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest childRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/child", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONObject child = response.getJSONArray("child").getJSONObject(0);
                                textViewChildName.setText(child.getString("childName"));
                                textViewParentName.setText(child.getString("parentName"));
                                textViewDateOfBirth.setText(child.getString("childBirth"));
                                textViewTeacherName.setText(child.getString("teacherName"));
                                textViewGroupType.setText(child.getString("groupType"));
                            } else {
                                Toast.makeText(ChildActivity.this,"Error", Toast.LENGTH_SHORT).show();
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
