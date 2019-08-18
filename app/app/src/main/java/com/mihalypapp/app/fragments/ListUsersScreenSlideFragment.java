package com.mihalypapp.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mihalypapp.app.R;

public class ListUsersScreenSlideFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_users_screen_slide, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager pager = view.findViewById(R.id.pager);

        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(pager);

        return view;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private String[] tabTitles = new String[]{"Parents", "Teachers", "Principals"};

        ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ListParentsFragment();
                case 1:
                    return new ListTeachersFragment();
                case 2:
                    return new ListPrincipalsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

    }
}
