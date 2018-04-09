package com.nhance.android.activities.baseclasses;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by android on 11/4/2016.
 */
public abstract class NhanceBaseFragment extends Fragment{
    public NhanceBaseActivity m_cObjNhanceBaseActivity;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() instanceof NhanceBaseActivity){
            m_cObjNhanceBaseActivity = (NhanceBaseActivity) getActivity();
            m_cObjNhanceBaseActivity.prentInit();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() instanceof NhanceBaseActivity){
            m_cObjNhanceBaseActivity = (NhanceBaseActivity) getActivity();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() instanceof NhanceBaseActivity){
            m_cObjNhanceBaseActivity = (NhanceBaseActivity) getActivity();

        }
    }



}
