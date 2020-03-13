package com.quebix.bunachat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quebix.bunachat.Fragment.UsersFragment;
import com.quebix.bunachat.Model.Chat;

import java.util.ArrayList;
import java.util.List;

class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final static String TAG = "SectionsPagerAdapter";
    private final List<Fragment> fragList=new ArrayList<>();
    private final List<String> fragTitle = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }

    @Override
    public int getCount() {
        return fragTitle.size();
    }

    public CharSequence getPageTitle(int position){
        return fragTitle.get(position);
    }

    public void addFrag(Fragment f, String s){
        fragList.add(f);
        fragTitle.add(s);
    }

}
