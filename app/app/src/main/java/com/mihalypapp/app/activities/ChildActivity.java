package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Locale;
import java.util.Objects;

public class ChildActivity extends AppCompatActivity {

    private static final String TAG = "ChildActivity";

    public static final String CHILD_ID = "com.mihalypapp.CHILD_ID";

    private static final int RC_GROUP = 152;
    private static final int RC_ADD_LIABILITY = 99;

    private TextView textViewChildName;
    private TextView textViewParentName;
    private TextView textViewDateOfBirth;
    private TextView textViewTeacherName;
    private TextView textViewGroupType;
    private TextView textViewAbsences;
    private ListView listViewAbsences;
    private ListView listViewLiabilities;
    private TextView textViewAbsencesDisplay;
    private TextView textViewLiabilitiesDisplay;
    private TextView textViewLiabilityInThisMonth;
    private TextView textViewMealSubscription;

    private MenuItem itemRemoveChildFromGroup;
    private MenuItem itemAddChildToGroup;
    private MenuItem itemViewGroup;
    private MenuItem itemSendMessageToParent;
    private MenuItem itemSendMessageToTeacher;
    private MenuItem itemMealSubscription;
    private MenuItem itemAddLiabilityToChild;

    private ArrayList<String> absenceList = new ArrayList<>();
    private ArrayAdapter<String> absenceAdapter;

    private ArrayList<String> liabilityList = new ArrayList<>();
    private ArrayAdapter<String> liabilityAdapter;

    private Child selectedChild = new Child();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_child);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.child);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewChildName = findViewById(R.id.text_view_child_name);
        textViewParentName = findViewById(R.id.text_view_parent_name);
        textViewDateOfBirth = findViewById(R.id.text_view_date_of_birth);
        textViewTeacherName = findViewById(R.id.text_view_teacher_name);
        textViewGroupType = findViewById(R.id.text_view_group_type);
        textViewAbsences = findViewById(R.id.text_view_absences);
        listViewAbsences = findViewById(R.id.list_view_absences);
        listViewAbsences.setNestedScrollingEnabled(true);
        listViewLiabilities = findViewById(R.id.list_view_liabilities);
        listViewLiabilities.setNestedScrollingEnabled(true);
        textViewAbsencesDisplay = findViewById(R.id.text_view_absences_display);
        textViewLiabilitiesDisplay = findViewById(R.id.text_view_liabilities_display);
        textViewLiabilityInThisMonth = findViewById(R.id.text_view_liability_in_this_month);
        textViewMealSubscription = findViewById(R.id.text_view_meal_subscription);


        liabilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, liabilityList);
        absenceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, absenceList);

        fetchChild();
    }

    private void fetchChild() {
        absenceList.clear();
        liabilityList.clear();

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

        JsonObjectRequest childRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "child", params,
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
                                selectedChild.setParentName(child.getString("parentName"));
                                selectedChild.setParentId(child.getInt("parentId"));
                                selectedChild.setMealSubscription(child.getInt("mealSubscription"));
                                if(selectedChild.getMealSubscription() == 1) {
                                    textViewMealSubscription.setText("ACTIVE");
                                } else {
                                    textViewMealSubscription.setText("DISABLED");
                                }
                                textViewChildName.setText(child.getString("childName"));
                                textViewParentName.setText(child.getString("parentName"));
                                textViewDateOfBirth.setText(child.getString("childBirth"));
                                textViewAbsences.setText(child.getString("absences"));
                                if(response.getString("liabilityInThisMonth").equals("null")) {
                                    textViewLiabilityInThisMonth.setText("0");
                                } else {
                                    textViewLiabilityInThisMonth.setText(response.getString("liabilityInThisMonth"));
                                }

                                if (response.getString("userRole").equals("TEACHER")) {
                                    textViewTeacherName.setText(child.getString("teacherName"));
                                    textViewGroupType.setText(child.getString("groupType"));
                                    itemSendMessageToParent.setVisible(true);
                                    itemViewGroup.setVisible(false);
                                    itemAddLiabilityToChild.setVisible(true);
                                } else if (response.getString("userRole").equals("PRINCIPAL")) {
                                    itemAddLiabilityToChild.setVisible(true);
                                    itemSendMessageToParent.setVisible(true);
                                    itemSendMessageToTeacher.setVisible(true);
                                    if (selectedChild.getGroupId() == -1) {
                                        itemViewGroup.setVisible(false);
                                        itemAddChildToGroup.setVisible(true);
                                        itemRemoveChildFromGroup.setVisible(false);
                                        textViewTeacherName.setText("-");
                                        textViewGroupType.setText("-");
                                    } else {
                                        if (getIntent().hasExtra("from")) {
                                            if (getIntent().getStringExtra("from").equals("group")) {
                                                itemViewGroup.setVisible(false);
                                            } else {
                                                itemViewGroup.setVisible(true);
                                            }
                                        } else {
                                            itemViewGroup.setVisible(true);
                                        }
                                        itemAddChildToGroup.setVisible(false);
                                        itemRemoveChildFromGroup.setVisible(true);
                                        textViewTeacherName.setText(child.getString("teacherName"));
                                        selectedChild.setTeacherName(child.getString("teacherName"));
                                        selectedChild.setTeacherId(child.getInt("teacherId"));
                                        textViewGroupType.setText(child.getString("groupType"));
                                    }
                                } else if (response.getString("userRole").equals("PARENT")) {
                                    itemSendMessageToTeacher.setVisible(true);
                                    if (selectedChild.getMealSubscription() == 1) {
                                        itemMealSubscription.setChecked(true);
                                    } else {
                                        itemMealSubscription.setChecked(false);
                                    }
                                    itemMealSubscription.setVisible(true);
                                    if (selectedChild.getGroupId() != -1) {
                                        textViewTeacherName.setText(child.getString("teacherName"));
                                        selectedChild.setTeacherName(child.getString("teacherName"));
                                        selectedChild.setTeacherId(child.getInt("teacherId"));
                                        textViewGroupType.setText(child.getString("groupType"));
                                    }
                                }

                                JSONArray absences = response.getJSONArray("absences");
                                if (absences.length() == 0) {
                                    textViewAbsencesDisplay.setText(getString(R.string.no_absences));
                                } else {
                                    textViewAbsencesDisplay.setText(R.string.absences);
                                    for (int i = 0; i < absences.length(); i++) {
                                        JSONObject absence = absences.getJSONObject(i);
                                        absenceList.add(absence.getString("date"));
                                    }
                                }
                                absenceAdapter.notifyDataSetChanged();
                                listViewAbsences.setAdapter(absenceAdapter);

                                JSONArray liabilities = response.getJSONArray("liabilities");
                                if (liabilities.length() == 0) {
                                    textViewLiabilitiesDisplay.setText(R.string.no_liability);
                                } else {
                                    textViewLiabilitiesDisplay.setText(R.string.liabilities);
                                    for (int i = 0; i < liabilities.length(); i++) {
                                        JSONObject liability = liabilities.getJSONObject(i);
                                        liabilityList.add(liability.getString("liabilityDate") + " - " + liability.getString("liabilityType") + " - " + Integer.valueOf(liability.getInt("liabilityCharge")).toString());
                                    }
                                }
                                liabilityAdapter.notifyDataSetChanged();
                                listViewLiabilities.setAdapter(liabilityAdapter);
                            } else {

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

        JsonObjectRequest removeChildFromGroupRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "removeChildFromGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response:" + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(ChildActivity.this, getString(R.string.child_suc_removed), Toast.LENGTH_SHORT).show();
                                fetchChild();
                                setResult(Activity.RESULT_OK);
                            } else {
                                Toast.makeText(ChildActivity.this, getString(R.string.child_wasnt_deleted_from_group), Toast.LENGTH_SHORT).show();
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

        JsonObjectRequest addChildToGroupRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "addChildToGroup", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response:" + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(ChildActivity.this, getString(R.string.child_suc_added), Toast.LENGTH_SHORT).show();
                                fetchChild();
                            } else {
                                Toast.makeText(ChildActivity.this, getString(R.string.child_wasnt_added_to_group), Toast.LENGTH_SHORT).show();
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
        itemSendMessageToParent = menu.findItem(R.id.item_send_message_to_parent);
        itemSendMessageToTeacher = menu.findItem(R.id.item_send_message_to_teacher);
        itemMealSubscription = menu.findItem(R.id.item_meal_subscription);
        itemAddLiabilityToChild = menu.findItem(R.id.item_add_liability_to_child);

        itemMealSubscription.setVisible(false);
        itemSendMessageToTeacher.setVisible(false);
        itemRemoveChildFromGroup.setVisible(false);
        itemAddChildToGroup.setVisible(false);
        itemAddChildToGroup.setVisible(false);
        itemSendMessageToParent.setVisible(false);
        itemAddLiabilityToChild.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
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
            case R.id.item_send_message_to_parent:
                intent = new Intent(this, MessageActivity.class);
                intent.putExtra(MessageActivity.PARTNER_NAME, selectedChild.getParentName());
                intent.putExtra(MessageActivity.PARTNER_ID, selectedChild.getParentId());
                startActivity(intent);
                return true;
            case R.id.item_send_message_to_teacher:
                intent = new Intent(this, MessageActivity.class);
                intent.putExtra(MessageActivity.PARTNER_NAME, selectedChild.getTeacherName());
                intent.putExtra(MessageActivity.PARTNER_ID, selectedChild.getTeacherId());
                startActivity(intent);
                return true;
            case R.id.item_meal_subscription:
                if (itemMealSubscription.isChecked()) {
                    item.setChecked(false);
                    selectedChild.setMealSubscription(0);
                } else {
                    item.setChecked(true);
                    selectedChild.setMealSubscription(1);
                }
                setMealSubscription();
                return true;
            case R.id.item_add_liability_to_child:
                intent = new Intent(this, AddLiabilityActivity.class);
                intent.putExtra(AddLiabilityActivity.CHILD_ID, selectedChild.getId());
                startActivityForResult(intent, RC_ADD_LIABILITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setMealSubscription() {
        JSONObject params = new JSONObject();
        try {
            params.put("childId", selectedChild.getId());
            params.put("mealSubscription", selectedChild.getMealSubscription());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, Integer.valueOf(selectedChild.getId()).toString());

        JsonObjectRequest setMealSubscriptionRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "setMealSubscription", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response:" + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(ChildActivity.this, getString(R.string.sub_updated_suc), Toast.LENGTH_SHORT).show();
                                fetchChild();
                            } else {
                                Toast.makeText(ChildActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
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
        requestQueue.add(setMealSubscriptionRequest);
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
                    //Toast.makeText(this, "Group ID can't be -1", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Group ID can't be -1");
                }
            }
        }
        if (requestCode == RC_ADD_LIABILITY) {
            if (resultCode == Activity.RESULT_OK) {
                fetchChild();
            }
        }
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
