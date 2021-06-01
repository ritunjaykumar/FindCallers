package com.softgyan.findcallers.widgets.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.softgyan.findcallers.R;

public class DialingPadBehavior implements View.OnClickListener {
    private final Context mContext;
    private final BottomSheetBehavior<View> mBottomSheetBehavior;
    private final View view;
    private TextView tvNumberDial;
    private final DailingPadBehaviorListener dialingPadListener;
    private String filterNumber = null;

    public DialingPadBehavior(@NonNull Context context, View view, DailingPadBehaviorListener dialingPadListener) {
        this.mContext = context;
        this.view = view;
        mBottomSheetBehavior = BottomSheetBehavior.from(view);
        this.dialingPadListener = dialingPadListener;
        initComponent();
    }

    private void initComponent() {
        mBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        final TextView[][] numberPad = new TextView[4][3];
        final LinearLayout mainLayout = view.findViewById(R.id.main_layout);
        for (int i = 0; i < 4; i++) {
            if (mainLayout != null) {
                final LinearLayout innerLayout = (LinearLayout) mainLayout.getChildAt(i);
                numberPad[i][0] = (TextView) innerLayout.getChildAt(0);
                numberPad[i][0].setOnClickListener(this);

                numberPad[i][1] = (TextView) innerLayout.getChildAt(1);
                numberPad[i][1].setOnClickListener(this);

                numberPad[i][2] = (TextView) innerLayout.getChildAt(2);
                numberPad[i][2].setOnClickListener(this);
            }
        }
        tvNumberDial = view.findViewById(R.id.tv_show_number);
        ImageButton clearText = view.findViewById(R.id.ib_clear);
        assert clearText != null;
        clearText.setOnClickListener(this);
        ImageButton hideKeyPad = view.findViewById(R.id.ib_hide_key_pad);
        assert hideKeyPad != null;
        hideKeyPad.setOnClickListener(this);

        CardView callNow = view.findViewById(R.id.card_view);
        callNow.setOnClickListener(this);

        ImageButton ibScan = view.findViewById(R.id.ib_scan_number);
        ibScan.setOnClickListener(this);

    }

    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            dialingPadListener.onStateChange(mBottomSheetBehavior, newState);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            //it is blank
        }
    };


    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            final String text = ((TextView) v).getText().toString();
            appendText(text);
            dialingPadListener.onClickOnKey(filterNumber);
        } else if (v instanceof ImageButton) {
            final int id = v.getId();
            if (id == R.id.ib_clear) {
                removeLastChar();
                dialingPadListener.onClickOnKey(filterNumber);
            } else if (id == R.id.ib_hide_key_pad) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else if(id == R.id.ib_scan_number){
                dialingPadListener.onClickCamera();
            }

        } else if (v.getId() == R.id.card_view) {
            final String number = getText();
            if (number == null) {
                Toast.makeText(mContext, "invalid number", Toast.LENGTH_SHORT).show();
                return;
            }
            dialingPadListener.onClickOnCallButton(number);
        }
    }

    private void appendText(String text) {
        tvNumberDial.append(text);
        filterNumber = getText();
    }


    private String getText() {
        if (tvNumberDial != null) {
            String text = tvNumberDial.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                return null;
            }
            return text;
        }
        return null;
    }

    public void setText(String text) {
        tvNumberDial.setText(text);
    }

    private void removeLastChar() {
        final String str = getText();
        if (str != null && str.equals("")) return;
        if (str != null) {
            String subString = str.substring(0, str.length() - 1);
            setText(subString);
        }

        filterNumber = getText();
    }

    public BottomSheetBehavior<View> getBottomSheetBehavior() {
        return mBottomSheetBehavior;
    }

    public interface DailingPadBehaviorListener {
        void onStateChange(final BottomSheetBehavior<View> bottomSheetBehavior, int state);

        void onClickOnKey(final String key);

        void onClickOnCallButton(final String number);

        void onClickCamera();
    }


}
