package com.mihalypapp.app.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.models.Child;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AbsenteeFragment extends Fragment {

    private static final String TAG = "AbsenteeFragment";

    private ListView listView;
    private Button buttonSelectAll;
    private Button buttonDeselectAll;
    private TextView textViewDateToday;

    private ArrayAdapter<Child> adapter;
    private ArrayList<Child> childList;

    private boolean isFetchedFirst = true;

    private int index;
    private int top;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_absentee, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Absentees");

        listView = view.findViewById(R.id.list_view_absentees);
        buttonSelectAll = view.findViewById(R.id.button_select_all);
        buttonDeselectAll = view.findViewById(R.id.button_deselect_all);
        textViewDateToday = view.findViewById(R.id.text_view_date_today);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
                index = listView.getFirstVisiblePosition();
                View v = listView.getChildAt(0);
                top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
                saveAbsentees();
            }
        });

        buttonSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, true);
                }
                saveAbsentees();
            }
        });

        buttonDeselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, false);
                }
                saveAbsentees();
            }
        });

        fetchAbsentees();

        return view;
    }

    private void saveAbsentees() {
        SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++) {
            Child c = (Child) listView.getItemAtPosition(i);

            if (sparseBooleanArray.get(i)) {
                for (int j = 0; j < childList.size(); j++) {
                    if (c.getId() == childList.get(j).getId()) {
                        childList.get(j).setIsCheckedToday("TRUE");
                    }
                }
            } else {
                for (int j = 0; j < childList.size(); j++) {
                    if (c.getId() == childList.get(j).getId()) {
                        childList.get(j).setIsCheckedToday("FALSE");
                    }
                }
            }
        }

        JSONArray array = new JSONArray();
        for (int i = 0; i < childList.size(); i++) {
            JSONObject object = new JSONObject();
            try {
                object.put("childId", childList.get(i).getId());
                object.put("isCheckedToday", childList.get(i).getIsCheckedToday());
                object.put("mealSubscription", childList.get(i).getMealSubscription());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(object);
        }

        JSONObject params = new JSONObject();
        try {
            params.put("absentees", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest saveAbsenteesRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "saveMyGroupAbsentees", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        fetchAbsentees();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getContext(),"Error savemy", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(saveAbsenteesRequest);
    }

    private void fetchAbsentees() {
        childList = new ArrayList<>();
        JsonObjectRequest childRequest = new JsonObjectRequest(Request.Method.GET, MainActivity.URL + "myGroupAbsentees", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray children = response.getJSONArray("children");
                                if (children.length() == 0) {
                                    buttonDeselectAll.setVisibility(View.INVISIBLE);
                                    buttonSelectAll.setVisibility(View.INVISIBLE);
                                    textViewDateToday.setText("You have no active group!");
                                } else {
                                    textViewDateToday.setText(getCurrentDate());
                                }
                                for (int i = 0; i < children.length(); i++) {
                                    JSONObject child = children.getJSONObject(i);
                                    Child c = new Child(
                                            child.getInt("childId"),
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            child.getInt("absences"),
                                            child.getString("isCheckedToday"));
                                    c.setMealSubscription(child.getInt("mealSubscription"));
                                    childList.add(c);
                                }
                                adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, childList);
                                listView.setAdapter(adapter);
                                for (int i = 0; i < childList.size(); i++) {
                                    if (childList.get(i).getIsCheckedToday().equals("TRUE")) {
                                        listView.setItemChecked(i, true);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                if (isFetchedFirst) {
                                    listView.setSelectionFromTop(index, top);
                                } else {
                                    isFetchedFirst = false;
                                }
                            } else {
                                Toast.makeText(getContext(),"Error fetchAbsentees", Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(childRequest);
    }

    private String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(date);
    }
}
