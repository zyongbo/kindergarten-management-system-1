package com.mihalypapp.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.mihalypapp.app.activities.AddPollActivity;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.activities.PollActivity;
import com.mihalypapp.app.adapters.PollCardAdapter;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.Poll;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListPollsFragment extends Fragment {

    private static final String TAG = "ListPollsFragment";

    private static final int RC_POLL_ACTIVITY = 99;
    private static final int RC_ADD_POLL = 919;

    private ArrayList<Poll> pollCardList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;

    private RecyclerView recyclerView;
    private PollCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_polls, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Polls");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new PollCardAdapter(pollCardList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchPolls();
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
                    fetchPolls();
                }
            }
        });

        adapter.setOnItemClickListener(new PollCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Intent intent = new Intent(getContext(), PollActivity.class);
                intent.putExtra("question", pollCardList.get(position).getQuestion());
                intent.putExtra("pollId", pollCardList.get(position).getPollID());
                intent.putExtra("status", pollCardList.get(position).getStatus());
                startActivityForResult(intent, RC_POLL_ACTIVITY);
            }
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddPollActivity.class);
                startActivityForResult(intent, RC_ADD_POLL);
            }
        });

        fetchPolls();
        return view;
    }


    private void fetchPolls() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getPollsRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "polls", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray polls = response.getJSONArray("polls");

                                removeProgressBar();

                                if (refreshing) {
                                    pollCardList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < polls.length(); i++) {
                                    JSONObject poll = polls.getJSONObject(i);
                                    Poll p = (new Poll(
                                            poll.getInt("pollId"),
                                            poll.getInt("groupId"),
                                            poll.getString("question"),
                                            poll.getString("date"),
                                            poll.getString("status")
                                    ));
                                    if (response.getString("userRole").equals("PARENT")) {
                                        p.setChildren(poll.getString("childName"));
                                    }
                                    pollCardList.add(p);
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(pollCardList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getPollsRequest ERROR");
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
        requestQueue.add(getPollsRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        pollCardList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(pollCardList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            pollCardList.remove(pollCardList.size() - 1);
            adapter.notifyItemRemoved(pollCardList.size());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, Integer.valueOf(requestCode).toString());
        if (requestCode == RC_ADD_POLL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshing = true;
                offset = 0;
                fetchPolls();
            }
        }
        if (requestCode == RC_POLL_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                refreshing = true;
                offset = 0;
                fetchPolls();
            }
        }
    }
}
