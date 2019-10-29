package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.mihalypapp.app.R;
import com.mihalypapp.app.fragments.ListDocumentsFragment;
import com.mihalypapp.app.fragments.ListMessagePartnersFragment;
import com.mihalypapp.app.fragments.ListNewsFragment;
import com.mihalypapp.app.fragments.ListPollsFragment;
import com.mihalypapp.app.fragments.MyChildrenFragment;
import com.mihalypapp.app.fragments.MyUserFragment;

public class ParentActivity extends DrawerActivity {

    private static final String TAG = "ParentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String GetTitle() {
        return getString(R.string.parent);
    }

    @Override
    public int GetLayout() {
        return R.layout.activity_parent;
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
            case R.id.nav_my_children:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyChildrenFragment()).commit();
                break;
            case R.id.nav_documents:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListDocumentsFragment()).commit();
                break;
            case R.id.nav_news:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListNewsFragment()).commit();
                break;
            case R.id.nav_polls:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListPollsFragment()).commit();
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
