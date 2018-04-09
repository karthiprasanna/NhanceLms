package com.nhance.android.interfaces;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

public interface INavgationDrawer {

    public View getDrawerView();

    public DrawerLayout getDrawerLayout();

    public View getSyncButton();

    public boolean isDrawerOpen();

    public void closeDrawer();

}
