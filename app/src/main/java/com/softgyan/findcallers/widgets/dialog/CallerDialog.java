package com.softgyan.findcallers.widgets.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.CallerInfoModel;
import com.softgyan.findcallers.receivers.SaveContactReceiver;
import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.utils.Utils;

public final class CallerDialog implements View.OnClickListener {
    private final WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private final LayoutInflater layoutInflater;
    private static final String TAG = "CallerDialog";
    private CallerInfoModel callerInfo;

    private static CallerDialog callerDialog;

    private View view = null;
    private View tempView = null;
    private final Context context;

    private CallerDialog(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        setupWindowDialog();
    }

    private void setupWindowDialog() {
        if (Utils.requestOverlayPermission(context)) return;
        int layoutParams;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams = WindowManager.LayoutParams.TYPE_PHONE;
        }

        view = layoutInflater.inflate(R.layout.layout_caller_info, null);

        int wmFlag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutParams,
                wmFlag,
                PixelFormat.TRANSLUCENT
        );

        params.x = 0;
        params.y = 0;
        view.setOnTouchListener(new View.OnTouchListener() {
            private final WindowManager.LayoutParams updatedParams = params;
            private int y;
            float touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        y = updatedParams.y;

                        touchedY = event.getRawY();

                        break;
                    }
                    case (MotionEvent.ACTION_MOVE): {
                        v.performClick();
                        updatedParams.y = (int) (y + (event.getRawY() - touchedY));
                        windowManager.updateViewLayout(view, updatedParams);
                    }
                }
                return false;
            }
        });


    }

    @SuppressLint("SetTextI18n")
    public void showDialog(final CallerInfoModel callerInfo, boolean isViewUpdate) {
        if (callerInfo == null) return;
        this.callerInfo = callerInfo;
        if (tempView != null && !isViewUpdate) {
            closeWindowManager();
            Log.d(TAG, "setupWindowDialog: close");
        } else {
            Log.d(TAG, "setupWindowDialog: view = null");
        }
        tempView = view;
        Log.d(TAG, "showDialog: isViewUpdate : "+isViewUpdate);
        LinearLayout linearLayout = view.findViewById(R.id.llOptionContainer);
        if (isViewUpdate) {
            Utils.showViews(linearLayout);
        }else{
            Utils.hideViews(linearLayout);
        }

        if (callerInfo.getMessage() != null) {
            TextView tvMessage = view.findViewById(R.id.tvNotification);
            tvMessage.setText(callerInfo.getMessage());
            Utils.showViews(tvMessage);
        }


        view.findViewById(R.id.ibClose).setOnClickListener(this);

        view.findViewById(R.id.tvCall).setOnClickListener(this);

        TextView tvName = view.findViewById(R.id.tvName);
        if (!Utils.isNull(callerInfo.getName())) {
            tvName.setText(callerInfo.getName());
        } else {
            tvName.setText("unknown Caller");
        }
        if (!Utils.isNull(callerInfo.getProfileUri())) {
            ShapeableImageView ivProfile = view.findViewById(R.id.sivProfile);
            Glide.with(context).load(callerInfo.getProfileUri()).into(ivProfile);
        }

        TextView tvNumber = view.findViewById(R.id.tvNumber);
        tvNumber.setText(callerInfo.getNumber());
        view.findViewById(R.id.tvBlock).setOnClickListener(this);
        view.findViewById(R.id.tvMessage).setOnClickListener(this);

        if (isViewUpdate) {
            try {
                windowManager.updateViewLayout(view, params);
            } catch (Exception e) {
                Log.d(TAG, "showDialogOverCall: error : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {

                windowManager.addView(view, params);
            } catch (Exception e) {
                Log.d(TAG, "showDialog: error : " + e.getMessage());
            }
        }
        if (isViewUpdate)
            new Handler().postDelayed(this::closeWindowManager, 1000 * 60);
    }

    private void closeWindowManager() {
        try {
            windowManager.removeView(view);
        } catch (Exception e) {
            Log.d(TAG, "closeWindowManager: exception : " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.ibClose) {
        } else if (id == R.id.tvCall) {
            CallHardware.makeCall(context, callerInfo.getNumber());
        } else if (id == R.id.tvMessage) {
            CallUtils.sendMessageIntent(context, callerInfo.getNumber());
        } else if (id == R.id.tvBlock) {
            Intent intent = new Intent(context, SaveContactReceiver.class);
            intent.putExtra(SaveContactReceiver.NUMBER_KEY, callerInfo.getNumber());
            intent.putExtra(SaveContactReceiver.MODE, SaveContactReceiver.BLOCK_NUMBER);
            intent.putExtra(SaveContactReceiver.NAME, callerInfo.getName());
            context.sendBroadcast(intent);


        }
        closeWindowManager();
    }


    public static CallerDialog getInstance(Context context) {
        if (callerDialog == null) {
            callerDialog = new CallerDialog(context.getApplicationContext());
        }
        return callerDialog;
    }

}

