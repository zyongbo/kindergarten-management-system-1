package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mihalypapp.app.fragments.DatePickerFragment;
import com.mihalypapp.app.models.Group;
import com.mihalypapp.app.models.Teacher;
import com.mihalypapp.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class AddChildActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "AddChildActivity";

    private TextInputLayout textInputLayoutChildName;
    private String childNameInput;

    private TextView textViewDate;
    private String date;

    private Button buttonDate;

    private ArrayList<User> parentList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteParents;
    private TextInputLayout textInputLayoutParents;
    private AutoCompleteParentAdapter autoCompleteParentAdapter;
    private User selectedParent;
    private boolean isParentSelected = false;

    private ArrayList<Group> groupList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteGroups;
    private TextInputLayout textInputLayoutGroups;
    private AutoCompleteGroupAdapter autoCompleteGroupAdapter;
    private Group selectedGroup;
    private boolean isGroupSelected = false;

    private Button buttonAddChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add a new child");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textInputLayoutChildName = findViewById(R.id.text_input_child_name);

        textViewDate = findViewById(R.id.text_view_date);

        buttonDate = findViewById(R.id.button_date);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        textInputLayoutParents = findViewById(R.id.text_input_parents);
        textInputLayoutParents.setEnabled(false);
        autoCompleteParents = findViewById(R.id.auto_complete_parents);
        autoCompleteParents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedParent = (User) adapterView.getAdapter().getItem(i);
                //Toast.makeText(AddChildActivity.this, Integer.valueOf(selectedParent.getId()).toString(), Toast.LENGTH_SHORT).show();
                isParentSelected = true;
                textInputLayoutParents.setError(null);
            }
        });
        autoCompleteParents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isParentSelected = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        textInputLayoutGroups = findViewById(R.id.text_input_groups);
        textInputLayoutGroups.setEnabled(false);
        autoCompleteGroups = findViewById(R.id.auto_complete_groups);
        autoCompleteGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGroup = (Group) adapterView.getAdapter().getItem(i);
                //Toast.makeText(AddChildActivity.this, Integer.valueOf(selectedGroup.getId()).toString(), Toast.LENGTH_SHORT).show();
                isGroupSelected = true;
                textInputLayoutGroups.setError(null);
            }
        });
        autoCompleteGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isGroupSelected = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        buttonAddChild = findViewById(R.id.button_add_child);
        buttonAddChild.setEnabled(false);
        buttonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateChildName() | !validateDate() | !validateParent() | !validateGroup()) {
                    return;
                }
                addChild();
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
                Toast.makeText(AddChildActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
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
                                            group.getString("year")
                                    ));
                                    autoCompleteGroupAdapter = new AutoCompleteGroupAdapter(AddChildActivity.this, groupList);
                                    autoCompleteGroups.setAdapter(autoCompleteGroupAdapter);
                                }
                                textInputLayoutGroups.setEnabled(true);
                                buttonAddChild.setEnabled(true);
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
                Toast.makeText(AddChildActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(fetchGroupsRequest);
    }

    private void addChild() {
        JSONObject params = new JSONObject();
        try {
            params.put("childName", childNameInput);
            params.put("parentId", selectedParent.getId());
            params.put("groupId", selectedGroup.getId());
            params.put("birth", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addUserRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/addChild", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "addGroup response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                clearFields();
                                Toast.makeText(AddChildActivity.this, "Group successfully added!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddChildActivity.this, "ERROR!?", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddChildActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addUserRequest);
    }


    private boolean validateChildName() {
        childNameInput = textInputLayoutChildName.getEditText().getText().toString().trim();

        if (childNameInput.isEmpty()) {
            textInputLayoutChildName.setError("Field can't be empty.");
            return false;
        } else {
            textInputLayoutChildName.setError(null);
            return true;
        }
    }

    private boolean validateParent() {
        if (isParentSelected) {
            textInputLayoutParents.setError(null);
            return true;
        } else {
            textInputLayoutParents.setError("Please select a parent!");
            return false;
        }
    }

    private boolean validateGroup() {
        if (isGroupSelected) {
            textInputLayoutGroups.setError(null);
            return true;
        } else {
            textInputLayoutGroups.setError("Please select a group!");
            return false;
        }
    }

    private boolean validateDate() {
        if (date != null) {
            textViewDate.setError(null);
            return true;
        } else {
            textViewDate.setError("Please select a date!");
            return false;
        }
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

    private void clearFields() {
        textInputLayoutChildName.getEditText().setText("");
        textViewDate.setText("");
        date = null;
        textInputLayoutParents.getEditText().setText("");
        textInputLayoutGroups.getEditText().setText("");
        getCurrentFocus().clearFocus();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, i);
        c.set(Calendar.MONTH, i1);
        c.set(Calendar.DAY_OF_MONTH, i2);
        date = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
        textViewDate.setText(date);
        textViewDate.setError(null);
    }
}
