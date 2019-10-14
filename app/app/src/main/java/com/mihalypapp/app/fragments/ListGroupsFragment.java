package com.mihalypapp.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.AddGroupActivity;
import com.mihalypapp.app.activities.GroupActivity;
import com.mihalypapp.app.adapters.GroupCardAdapter;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class ListGroupsFragment extends Fragment {

    private static final String TAG = "ListGroupsFragment";

    private static final int RC_ADD_GROUP = 9;
    private static final int RC_OVERVIEW_GROUP = 10;
    public static final int RC_CHOOSE_GROUP_FOR_CHILD = 11;

    private ArrayList<Group> groupCardList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;
    private String filter = "";

    private RecyclerView recyclerView;
    private GroupCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    private Intent gIntent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_groups, container, false);

        setHasOptionsMenu(true);

        gIntent = getActivity().getIntent();

        if (gIntent.hasExtra("request")) {
            if (gIntent.getStringExtra("request").equals("groupId")) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                Toolbar toolbar = view.findViewById(R.id.toolbar_fragment_list_groups);
                toolbar.setVisibility(View.VISIBLE);
                ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Select group");

            }
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Groups");
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new GroupCardAdapter(groupCardList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchGroups();
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        swipeContainer = view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = "";
                    fetchGroups();
                }
            }
        });

        adapter.setOnItemClickListener(new GroupCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int groupId = groupCardList.get(position).getId();
                //Toast.makeText(getContext(), Integer.valueOf(groupId).toString() + " was clicked!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), GroupActivity.class);
                intent.putExtra(GroupActivity.GROUP_ID, groupId);
                Intent gIntent = getActivity().getIntent();
                if (gIntent.hasExtra("request")) {
                    if (gIntent.getStringExtra("request").equals("groupId")) {
                        intent.putExtra("request", "groupId");
                        startActivityForResult(intent, RC_CHOOSE_GROUP_FOR_CHILD);
                    }
                } else {
                    startActivityForResult(intent, RC_OVERVIEW_GROUP);
                }
            }
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddGroupActivity.class);
                startActivityForResult(intent, RC_ADD_GROUP);
            }
        });

        fetchGroups();
        return view;
    }

    private void fetchGroups() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
            params.put("filter", filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getGroupsRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/groups", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray groups = response.getJSONArray("groups");

                                removeProgressBar();

                                if (refreshing) {
                                    groupCardList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < groups.length(); i++) {
                                    JSONObject group = groups.getJSONObject(i);
                                    groupCardList.add(new Group(
                                            group.getInt("groupid"),
                                            group.getString("type"),
                                            group.getString("teacherName"),
                                            group.getString("YEAR"),
                                            R.drawable.ic_launcher_foreground
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(groupCardList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getGroupRequest ERROR");
                            }

                            swipeContainer.setRefreshing(false);
                            fetching = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(getGroupsRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        groupCardList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(groupCardList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            groupCardList.remove(groupCardList.size() - 1);
            adapter.notifyItemRemoved(groupCardList.size());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, Integer.valueOf(requestCode).toString());
        if (requestCode == ListGroupsFragment.RC_CHOOSE_GROUP_FOR_CHILD) {
            Intent returnIntent = new Intent();
            if (resultCode == Activity.RESULT_OK) {
                returnIntent.putExtra("groupId", data.getIntExtra("groupId", -1));
                Log.i(TAG, Integer.valueOf(data.getIntExtra("groupId", -1)).toString());
                getActivity().setResult(Activity.RESULT_OK, returnIntent);
                getActivity().finish();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = query;
                    fetchGroups();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = newText;
                    fetchGroups();
                }
                return false;
            }
        });
    }
}
