package com.mysampleapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Chris on 5/15/2017.
 */

public class BackendPager extends FragmentStatePagerAdapter{
    int tabCount;

    public BackendPager(FragmentManager fm, int tabCount){
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                Route route = new Route();
                return route;
            case 1:
                Account account = new Account();
                return account;
            default:
                return null;
        }
    }

    @Override
    public int getCount(){
        return tabCount;
    }
}
