package com.mihalypapp.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpCookie;

public class MyUserFragment extends Fragment {

    private static final String TAG = "MyUserFragment";

    private TextView textViewFullName;
    private TextView textViewEmail;
    private TextView textViewRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_user, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My user");

        textViewFullName = view.findViewById(R.id.text_view_full_name);
        textViewEmail = view.findViewById(R.id.text_view_email);
        textViewRole = view.findViewById(R.id.text_view_role);

        fetchUserData();
        return view;
    }

    private void fetchUserData() {
                JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.157:3000/myUserData", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                String role = response.getString("role");
                                String name = response.getString("name");
                                String email = response.getString("email");

                                textViewFullName.setText(name);
                                textViewEmail.setText(email);
                                textViewRole.setText(role);

                            } else {
                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(loginRequest);
    }
}
