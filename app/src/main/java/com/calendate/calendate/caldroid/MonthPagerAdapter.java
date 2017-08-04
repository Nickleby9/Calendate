package com.calendate.calendate.caldroid;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.roomorama.caldroid.CaldroidFragment;

import java.util.ArrayList;

/**
 * MonthPagerAdapter holds 4 fragments, which provides fragment for current
 * month, previous month and next month. The extra fragment helps for recycle
 * fragments.
 *
 * @author thomasdao
 */
public class MonthPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<com.roomorama.caldroid.DateGridFragment> fragments;

    // Lazily create the fragments
    public ArrayList<com.roomorama.caldroid.DateGridFragment> getFragments() {
        if (fragments == null) {
            fragments = new ArrayList<com.roomorama.caldroid.DateGridFragment>();
            for (int i = 0; i < getCount(); i++) {
                fragments.add(new com.roomorama.caldroid.DateGridFragment());
            }
        }
        return fragments;
    }

    public void setFragments(ArrayList<com.roomorama.caldroid.DateGridFragment> fragments) {
        this.fragments = fragments;
    }

    public MonthPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        com.roomorama.caldroid.DateGridFragment fragment = getFragments().get(position);
        return fragment;
    }

    @Override
    public int getCount() {
        // We need 4 gridviews for previous month, current month and next month,
        // and 1 extra fragment for fragment recycle
        return CaldroidFragment.NUMBER_OF_PAGES;
    }

}
