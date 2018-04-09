package com.nhance.android.listners;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class UnLimitedScrollListner implements OnScrollListener {

    private static final String TAG     = "UnLimitedScrollListner";

    public boolean              loadingMore;
    public boolean              hasMore = true;
    IListScrollListner        listScrollListner;

    public UnLimitedScrollListner(IListScrollListner listScrollListner) {

        this.listScrollListner = listScrollListner;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {

        // last item that is visible
        int lastInScreen = firstVisibleItem + visibleItemCount;
        if (hasMore && (lastInScreen == totalItemCount) && !loadingMore) {
            loadingMore = true;
            Log.d(TAG, "loadingMore items :" + loadingMore);

            Log.d(TAG, "visible item: " + visibleItemCount);
            if (listScrollListner != null) {
                listScrollListner.onScrollToBottom(view.getChildAt(visibleItemCount - 1));
            }
        }
    }
}
