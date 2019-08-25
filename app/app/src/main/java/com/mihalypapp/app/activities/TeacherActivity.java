package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.mihalypapp.app.R;
import com.mihalypapp.app.fragments.GroupFragment;

public class TeacherActivity extends DrawerActivity {

    private static final String TAG = "TeacherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String GetTitle() {
        return "Teacher";
    }

    @Override
    public int GetLayout() {
        return R.layout.activity_teacher;
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
            case R.id.nav_my_groups:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GroupFragment()).commit();
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
