package com.nhance.android.fragments;

import java.util.Currency;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.enums.SectionRevenueModel;
import com.nhance.android.pojos.CostRate;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgSectionInfo;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class MyProgramPaymentInfoDialogFragment extends DialogFragment {

    private View  popupLayout;
    private OrgSectionInfo orgSecInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle);
        setCancelable(true);
        if (getArguments() != null) {
            orgSecInfo = (OrgSectionInfo) getArguments().getSerializable("orgSecInfo");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();
        getDialog().getWindow().setLayout(
                ViewUtils.getOrientationSpecificWidth(getActivity()),
                LayoutParams.WRAP_CONTENT);
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        popupLayout = inflater.inflate(R.layout.payment_info_popup, container, false);
        return popupLayout;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {

        if (orgSecInfo != null) {
            if (orgSecInfo.revenueModel.equals(SectionRevenueModel.FREE)) {
                popupLayout.findViewById(R.id.free_program_info_layout).setVisibility(View.VISIBLE);
            } else {
                popupLayout.findViewById(R.id.paid_program_info_layout).setVisibility(View.VISIBLE);
                CostRate costRate = orgSecInfo.costRate;

                String currencyCode = costRate.currencyCode;
                try {
                    currencyCode = Currency.getInstance(costRate.currencyCode).getSymbol();
                } catch (Throwable e) {
                    // swallow
                }
                String cost = currencyCode + " " + (costRate.value / 100) + " + Taxes Extra";

                ((TextView) popupLayout.findViewById(R.id.amount_paid_value)).setText(cost);
                ((TextView) popupLayout.findViewById(R.id.payment_order_id_value))
                        .setText(orgSecInfo.orderId);
                ((TextView) popupLayout.findViewById(R.id.paid_joined_on_head))
                        .setText(R.string.paid_on);
            }
            String timeJoined = DateUtils.formatDateTime(getActivity(),
                    orgSecInfo.timeJoined, DateUtils.FORMAT_SHOW_DATE);
            ((TextView) popupLayout.findViewById(R.id.paid_joined_on_value)).setText(timeJoined);
        } else {
            Toast.makeText(getActivity(),
                    "Some error occured. Close the dialog and try again", Toast.LENGTH_SHORT)
                    .show();
        }
        OnClickListener closeTestPopupListner = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dismiss();

            }
        };
        popupLayout.findViewById(R.id.close_payment_info_popup).setOnClickListener(
                closeTestPopupListner);

        super.onActivityCreated(arg0);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);
    }

}
