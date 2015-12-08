package com.aptitekk.binghamapp.Views.SlidingTab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    LinkedHashMap<CharSequence, Fragment> titleFragmentMap;

    public ViewPagerAdapter(FragmentManager fragmentManager, LinkedHashMap<CharSequence, Fragment> titleFragmentMap) {
        super(fragmentManager);

        this.titleFragmentMap = titleFragmentMap;
    }

    /**
     * Gives the fragment that belongs to the specified tab.
     *
     * @param tabIndex The index of the tab to get the fragment of.
     * @return The fragment belonging to the specified tab.
     */
    @Override
    public Fragment getItem(int tabIndex) {
        Iterator<Fragment> fragmentIterator = titleFragmentMap.values().iterator();
        for (int i = 0; i < tabIndex; i++)
            fragmentIterator.next();
        return fragmentIterator.next();
    }

    /**
     * Gives the title of the specified tab.
     *
     * @param tabIndex The index of the tab to get the title of.
     * @return The name of the specified tab.
     */
    @Override
    public CharSequence getPageTitle(int tabIndex) {
        Iterator<CharSequence> fragmentIterator = titleFragmentMap.keySet().iterator();
        for (int i = 0; i < tabIndex; i++)
            fragmentIterator.next();
        return fragmentIterator.next();
    }

    /**
     * Gives the number (count) of tabs in the view.
     *
     * @return Number of tabs.
     */
    @Override
    public int getCount() {
        return titleFragmentMap.size();
    }
}