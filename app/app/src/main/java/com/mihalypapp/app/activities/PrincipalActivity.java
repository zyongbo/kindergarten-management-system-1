package com.mihalypapp.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.mihalypapp.app.fragments.AddUserFragment;
import com.mihalypapp.app.fragments.DeleteDocumentDialog;
import com.mihalypapp.app.fragments.ListChildrenFragment;
import com.mihalypapp.app.fragments.ListDocumentsFragment;
import com.mihalypapp.app.fragments.ListGroupsFragment;
import com.mihalypapp.app.fragments.ListMessagePartnersFragment;
import com.mihalypapp.app.fragments.ListUsersScreenSlideFragment;
import com.mihalypapp.app.R;
import com.mihalypapp.app.fragments.MyUserFragment;

import java.util.Objects;

public class PrincipalActivity extends DrawerActivity {

    private static final String TAG = "PrincipalActivity";

    private boolean hasRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra("request")) {
            hasRequest = true;
            if (Objects.requireNonNull(intent.getStringExtra("request")).equals("groupId")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListGroupsFragment()).commit();
            }
        }
    }

    @Override
    public String GetTitle() {
        return "Principal";
    }

    @Override
    public int GetLayout() {
        return R.layout.activity_principal;
    }

    @Override
    public Fragment GetFragment() {
        return new MyUserFragment();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_my_user:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyUserFragment()).commit();
                break;
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListMessagePartnersFragment()).commit();
                break;
            case R.id.nav_users:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListUsersScreenSlideFragment()).commit();
                break;
            case R.id.nav_children:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListChildrenFragment()).commit();
                break;
            case R.id.nav_groups:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListGroupsFragment()).commit();
                break;
            case R.id.nav_documents:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListDocumentsFragment()).commit();
                break;
            case R.id.nav_logout:
                setResult(RESULT_LOGGED_OUT);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(hasRequest) {
            finish();
        } else {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                moveTaskToBack(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
