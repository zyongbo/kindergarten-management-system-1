package com.mihalypapp.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.activities.MessageActivity;
import com.mihalypapp.app.adapters.MessagePartnerCardAdapter;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.MessagePartner;
import com.mihalypapp.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListMessagePartnersFragment extends Fragment {

    private static final String TAG = "ListMessagePartnersF";

    private ArrayList<MessagePartner> messagePartnerList = new ArrayList<>();
    private ArrayList<User> userPrincipals = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;

    private RecyclerView recyclerView;
    private MessagePartnerCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    private MenuItem itemPrincipals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_message_partners, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Messages");

        setHasOptionsMenu(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //linearLayoutManager.setReverseLayout(true);
        adapter = new MessagePartnerCardAdapter(messagePartnerList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchMyMessagePartners();
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
                    fetchMyMessagePartners();
                }
            }
        });

        adapter.setOnItemClickListener(new MessagePartnerCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int partnerId = messagePartnerList.get(position).getPartnerId();
                String partnerName = messagePartnerList.get(position).getPartnerName();
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra(MessageActivity.PARTNER_ID, partnerId);
                intent.putExtra(MessageActivity.PARTNER_NAME, partnerName);
                startActivity(intent);
            }
        });

        fetchMyMessagePartners();
        fetchPrincipals();
        return view;
    }

    private void fetchMyMessagePartners() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getMessagePartnerRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "myMessagePartners", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray messagePartners = response.getJSONArray("myMessagePartners");

                                removeProgressBar();

                                if (refreshing) {
                                    messagePartnerList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < messagePartners.length(); i++) {
                                    JSONObject messagePartner = messagePartners.getJSONObject(i);
                                    messagePartnerList.add(new MessagePartner(
                                            messagePartner.getInt("partnerId"),
                                            messagePartner.getString("partnerName"),
                                            messagePartner.getString("datetime"),
                                            R.drawable.ic_launcher_foreground
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(messagePartnerList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getMessagePartnerRequest ERROR");
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
        requestQueue.add(getMessagePartnerRequest);
    }

    private void fetchPrincipals() {
        userPrincipals.clear();

        final JsonObjectRequest fetchPrincipalsRequest = new JsonObjectRequest(Request.Method.GET, MainActivity.URL + "principals", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray principals = response.getJSONArray("principals");

                                for (int i = 0; i < principals.length(); i++) {
                                    JSONObject principal = principals.getJSONObject(i);
                                    User userPrincipal = new User();
                                    userPrincipal.setId(principal.getInt("userId"));
                                    userPrincipal.setName(principal.getString("name"));
                                    userPrincipals.add(userPrincipal);
                                }
                                addPrincipalSubItems();

                            } else {
                                Log.e(TAG, "fetchPrincipals ERROR");
                            }
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
        requestQueue.add(fetchPrincipalsRequest);
    }

    private void addPrincipalSubItems() {
        SubMenu sub = itemPrincipals.getSubMenu();
        sub.clear();
        for(int i = 0; i < userPrincipals.size(); i++) {
            sub.add(0, userPrincipals.get(i).getId(), 0, userPrincipals.get(i).getName());
        }
    }

    private void addProgressBar() {
        showingProgressBar = true;
        messagePartnerList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(messagePartnerList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            messagePartnerList.remove(messagePartnerList.size() - 1);
            adapter.notifyItemRemoved(messagePartnerList.size());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.message_partners_menu, menu);

        itemPrincipals = menu.findItem(R.id.item_principals);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(!item.hasSubMenu()) {
            Intent intent = new Intent(getContext(), MessageActivity.class);
            intent.putExtra(MessageActivity.PARTNER_ID, item.getItemId());
            intent.putExtra(MessageActivity.PARTNER_NAME, item.getTitle());
            startActivity(intent);
        }
        return false;
    }
}
