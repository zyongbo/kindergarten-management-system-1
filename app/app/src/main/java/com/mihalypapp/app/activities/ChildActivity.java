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
import android.widget.ArrayAdapter;
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
import com.mihalypapp.app.models.Child;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ChildActivity extends AppCompatActivity {

    private static final String TAG = "ChildActivity";

    public static final String CHILD_ID = "com.mihalypapp.CHILD_ID";

    private static final int RC_GROUP = 152;

    private TextView textViewChildName;
    private TextView textViewParentName;
    private TextView textViewDateOfBirth;
    private TextView textViewTeacherName;
    private TextView textViewGroupType;
    private TextView textViewAbsences;
    private ListView listViewAbsences;
    private TextView textViewAbsencesDisplay;

    private MenuItem itemRemoveChildFromGroup;
    private MenuItem itemAddChildToGroup;
    private MenuItem itemViewGroup;

    private ArrayList<String> absenceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private Child selectedChild;

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
        textViewAbsences = findViewById(R.id.text_view_absences);
        listViewAbsences = findViewById(R.id.list_view_absences);
        textViewAbsencesDisplay = findViewById(R.id.text_view_absences_display);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, absenceList);



        fetchChild();
    }

    private void fetchChild() {
        JSONObject params = new JSONObject();
        final Intent intent = getIntent();
        final int childId = intent.getIntExtra(CHILD_ID, -1);
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
                                if (child.getString("groupType").equals("null")) {
                                    selectedChild = new Child(child.getInt("childId"), -1);
                                } else {
                                    selectedChild = new Child(child.getInt("childId"), child.getInt("groupId"));
                                }
                                textViewChildName.setText(child.getString("childName"));
                                textViewParentName.setText(child.getString("parentName"));
                                textViewDateOfBirth.setText(child.getString("childBirth"));
                                textViewAbsences.setText(child.getString("absences"));

                                if (selectedChild.getGroupId() == -1) {
                                    itemViewGroup.setVisible(false);
                                    itemAddChildToGroup.setVisible(true);
                                    itemRemoveChildFromGroup.setVisible(false);
                                    textViewTeacherName.setText("-");
                                    textViewGroupType.setText("-");
                                } else {
                                    itemAddChildToGroup.setVisible(false);
                                    itemRemoveChildFromGroup.setVisible(true);
                                    itemViewGroup.setVisible(true);
                                    textViewTeacherName.setText(child.getString("teacherName"));
                                    textViewGroupType.setText(child.getString("groupType"));
                                }

                                JSONArray absences = response.getJSONArray("absences");
                                if (absences.length() == 0) {
                                    textViewAbsencesDisplay.setText("No absences");
                                } else {
                                    textViewAbsencesDisplay.setText("Absences");
                                }
                                for (int i = 0; i < absences.length(); i++) {
                                    JSONObject absence = absences.getJSONObject(i);
                                    absenceList.add(absence.getString("date"));
                                }
                                adapter.notifyDataSetChanged();
                                listViewAbsences.setAdapter(adapter);
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

    private void removeChildFromGroup() {
        JSONObject params = new JSONObject();
        try {
            params.put("childId", selectedChild.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, Integer.valueOf(selectedChild.getId()).toString());

        JsonObjectRequest removeChildFromGroupRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/removeChildFromGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response:" + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(ChildActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                                fetchChild();
                                setResult(Activity.RESULT_OK);
                            } else {
                                Toast.makeText(ChildActivity.this, "The child wasn't deleted from the group!", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(removeChildFromGroupRequest);
    }

    private void addChildToGroup(int groupId) {
        JSONObject params = new JSONObject();
        try {
            params.put("childId", selectedChild.getId());
            params.put("groupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, Integer.valueOf(selectedChild.getId()).toString());

        JsonObjectRequest addChildToGroupRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/addChildToGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response:" + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(ChildActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                                fetchChild();
                            } else {
                                Toast.makeText(ChildActivity.this, "The child wasn't added to the group!", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(addChildToGroupRequest);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.child_menu, menu);
        itemRemoveChildFromGroup = menu.findItem(R.id.item_remove_from_group);
        itemAddChildToGroup = menu.findItem(R.id.item_add_to_group);
        itemViewGroup = menu.findItem(R.id.item_view_group);
        itemRemoveChildFromGroup.setVisible(false);
        itemAddChildToGroup.setVisible(false);
        itemAddChildToGroup.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.item_remove_from_group:
                removeChildFromGroup();
                return true;
            case R.id.item_add_to_group:
                intent = new Intent(this, PrincipalActivity.class);
                intent.putExtra("request", "groupId");
                startActivityForResult(intent, RC_GROUP);
                return true;
            case R.id.item_view_group:
                if (getIntent().hasExtra("from")) {
                    if (getIntent().getStringExtra("from").equals("group")) {
                        finish();
                    }
                } else {
                    intent = new Intent(this, GroupActivity.class);
                    intent.putExtra(GroupActivity.GROUP_ID, selectedChild.getGroupId());
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GROUP) {
            if (resultCode == Activity.RESULT_OK) {
                int groupId = data.getIntExtra("groupId", -1);
                if (groupId != -1) {
                    addChildToGroup(groupId);
                } else {
                    Toast.makeText(this, "Group ID can't be -1", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
