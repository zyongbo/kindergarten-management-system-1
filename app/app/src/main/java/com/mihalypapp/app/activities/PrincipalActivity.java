package com.mihalypapp.app.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.mihalypapp.app.fragments.AddUserFragment;
import com.mihalypapp.app.fragments.ListGroupsFragment;
import com.mihalypapp.app.fragments.ListUsersScreenSlideFragment;
import com.mihalypapp.app.R;

public class PrincipalActivity extends DrawerActivity {

    private static final String TAG = "PrincipalActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            case R.id.nav_add_user:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddUserFragment()).commit();
                break;
            case R.id.nav_users:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListUsersScreenSlideFragment()).commit();
                break;
            case R.id.nav_groups:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListGroupsFragment()).commit();
                break;
            case R.id.nav_logout:
                setResult(RESULT_LOGGED_OUT);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
