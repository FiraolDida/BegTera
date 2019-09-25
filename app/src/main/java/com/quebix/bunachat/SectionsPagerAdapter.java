package com.quebix.bunachat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                MatchupFragment matchupFragment = new MatchupFragment();
                return matchupFragment;
            case 1:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 2:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 3:
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;

                default:
                    return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "MATCH-UP";
            case 1:
                return "REQUESTS";
            case 2:
                return "CHATS";
            case 3:
                return "FRIENDS";
                default:
                    return null;
        }
    }
}
