package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.mihalypapp.app.fragments.FinishGroupDialog;
import com.mihalypapp.app.fragments.UpgradeGroupDialog;
import com.mihalypapp.app.models.Child;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity implements FinishGroupDialog.FinishGroupListener, UpgradeGroupDialog.UpgradeGroupListener {

    private static final String TAG = "GroupActivity";
    private static final int RC_CHILD = 88;

    public static final String GROUP_ID = "com.mihalypapp.GROUP_ID";

    private TextView textViewTeacherName;
    private TextView textViewGroupType;
    private TextView textViewGroupYear;
    private TextView textViewGroupSize;
    private Button buttonGroup;
    private TextView textViewChildrenDisplay;

    private MenuItem itemSendMessageToTeacher;

    private String userRole;
    private Group group;
    private ArrayList<Child> childList = new ArrayList<>();
    private ChildCardArrayAdapter adapter;
    private ListView listView;

    private int groupId;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        intent = getIntent();
        groupId = intent.getIntExtra(GROUP_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewTeacherName = findViewById(R.id.text_view_teacher_name);
        textViewGroupType = findViewById(R.id.text_view_group_type);
        textViewGroupYear = findViewById(R.id.text_view_group_year);
        buttonGroup = findViewById(R.id.button_group);
        textViewGroupSize = findViewById(R.id.text_view_group_size);
        textViewChildrenDisplay = findViewById(R.id.text_view_children_display);

        adapter = new ChildCardArrayAdapter(GroupActivity.this, childList, 1);

        listView = findViewById(R.id.list_view_children);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (userRole.equals("PARENT")) {
                    Intent intent;
                    Child child = (Child) adapterView.getItemAtPosition(i);
                    intent = new Intent(GroupActivity.this, MessageActivity.class);
                    intent.putExtra(MessageActivity.PARTNER_NAME, child.getParentName());
                    intent.putExtra(MessageActivity.PARTNER_ID, child.getParentId());
                    Log.i(TAG, Integer.valueOf(child.getParentId()).toString());
                    startActivity(intent);
                } else {
                    Child child = (Child) adapterView.getItemAtPosition(i);
                    Intent intent = new Intent(GroupActivity.this, ChildActivity.class);
                    intent.putExtra(ChildActivity.CHILD_ID, child.getId());
                    intent.putExtra("from", "group");
                    startActivityForResult(intent, RC_CHILD);
                }
            }
        });

        fetchGroup();
    }

    private void fetchGroup() {
        childList.clear();

        JSONObject params = new JSONObject();
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
                                final JSONObject resGroup = response.getJSONArray("group").getJSONObject(0);
                                userRole = response.getString("userRole");

                                if (intent.hasExtra("request")) {
                                    if (Objects.requireNonNull(intent.getStringExtra("request")).equals("groupId")) {
                                        buttonGroup.setVisibility(View.VISIBLE);
                                        buttonGroup.setText("Select");
                                        buttonGroup.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent returnIntent = new Intent();
                                                returnIntent.putExtra("groupId", groupId);
                                                setResult(Activity.RESULT_OK, returnIntent);
                                                Log.i(TAG, Integer.valueOf(groupId).toString());
                                                finish();
                                            }
                                        });
                                    }
                                } else {
                                    if(!userRole.equals("TEACHER")) {
                                        itemSendMessageToTeacher.setVisible(true);
                                    }
                                    if (userRole.equals("PRINCIPAL") && !resGroup.getString("type").equals("FINISHED")) {
                                        buttonGroup.setVisibility(View.VISIBLE);
                                        if (resGroup.getString("type").equals("BIG")) {
                                            buttonGroup.setText("Finish");
                                            buttonGroup.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    openFinishDialog();
                                                }
                                            });
                                        } else {
                                            buttonGroup.setText("Upgrade");
                                            buttonGroup.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    openUpgradeDialog();
                                                }
                                            });
                                        }
                                    }
                                }

                                group = new Group(
                                        resGroup.getInt("groupid"),
                                        resGroup.getString("type"),
                                        resGroup.getString("teacherName"),
                                        response.getJSONArray("groupSize").getJSONObject(0).getInt("groupSize"),
                                        resGroup.getString("year")
                                );
                                group.setTeacherId(resGroup.getInt("teacherId"));
                                textViewTeacherName.setText(group.getTeacherName());
                                textViewGroupType.setText(group.getType());
                                textViewGroupYear.setText(group.getYear());

                                JSONArray children = response.getJSONArray("children");
                                if (children.length() > 0) {
                                    textViewChildrenDisplay.setText("Children");
                                } else {
                                    textViewChildrenDisplay.setText("No children");
                                }
                                for (int i = 0; i < children.length(); i++) {
                                    JSONObject child = children.getJSONObject(i);
                                    Child newChild = new Child(
                                            child.getInt("childId"),
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            "",
                                            child.getString("parentName"),
                                            child.getString("parentEmail")
                                    );
                                    newChild.setParentId(child.getInt("parentId"));
                                    childList.add(newChild);
                                }

                                adapter.notifyDataSetChanged();
                                listView.setAdapter(adapter);
                                textViewGroupSize.setText(Integer.valueOf(group.getSize()).toString());
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

    public void finishGroup() {
        JSONObject params = new JSONObject();
        if (groupId == -1) {
            Log.e(TAG, "-1 groupId");
            return;
        }
        try {
            params.put("groupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest finishGroupRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/finishGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(GroupActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                fetchGroup();
                                buttonGroup.setVisibility(View.INVISIBLE);
                            } else {
                                Toast.makeText(GroupActivity.this,"Failed!", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(finishGroupRequest);
    }

    public void upgradeGroup() {
        JSONObject params = new JSONObject();
        if (groupId == -1) {
            Log.e(TAG, "-1 groupId");
            return;
        }
        try {
            params.put("groupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest upgradeGroupRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/upgradeGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(GroupActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                fetchGroup();
                            } else {
                                Toast.makeText(GroupActivity.this,"Failed!", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(upgradeGroupRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_menu, menu);
        itemSendMessageToTeacher = menu.findItem(R.id.item_send_message_to_teacher);
        itemSendMessageToTeacher.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.item_send_message_to_teacher:
                intent = new Intent(this, MessageActivity.class);
                intent.putExtra(MessageActivity.PARTNER_NAME, group.getTeacherName());
                intent.putExtra(MessageActivity.PARTNER_ID, group.getTeacherId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void openFinishDialog() {
        FinishGroupDialog dialog = new FinishGroupDialog();
        dialog.show(getSupportFragmentManager(), "Finish group dialog");
    }

    public void openUpgradeDialog() {
        UpgradeGroupDialog dialog = new UpgradeGroupDialog();
        dialog.show(getSupportFragmentManager(), "Upgrade group dialog");
    }

    @Override
    public void onFinishYesClicked() {
        Log.i(TAG, Integer.valueOf(groupId).toString());
        finishGroup();
    }

    @Override
    public void onUpgradeYesClicked() {
        Log.i(TAG, Integer.valueOf(groupId).toString());
        upgradeGroup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CHILD) {
            if (resultCode == Activity.RESULT_OK) {
                fetchGroup();
                Log.i(TAG, Integer.valueOf(requestCode).toString());
            }
        }
    }
}
