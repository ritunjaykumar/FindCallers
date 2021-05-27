package com.softgyan.findcallers.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.CallerInfoModel;

public class CallerDialog {
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private LayoutInflater layoutInflater;
    private int layoutParams;
    private static final String TAG = "CallerDialog";

    private View view;

    private final Context context;

    public CallerDialog(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        setupWindowDialog();
    }

    private void setupWindowDialog() {
        if (Utils.requestOverlayPermission(context)) return;
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
    }

    public void showDialog(final CallerInfoModel callerInfo, boolean isViewUpdate) {
        if (callerInfo == null) return;
        if (isViewUpdate) {
            LinearLayout linearLayout = view.findViewById(R.id.llOptionContainer);
            Utils.showViews(linearLayout);
        }

        if (!Utils.isNull(callerInfo.getMessage())) {
            TextView tvMessage = view.findViewById(R.id.tvNotification);
            tvMessage.setText(callerInfo.getMessage());
            Utils.showViews(tvMessage);
        }


        view.findViewById(R.id.ibClose).setOnClickListener(v -> {
            windowManager.removeView(view);

        });
        view.findViewById(R.id.tvCall).setOnClickListener(v -> {
            CallHardware.makeCall(context, callerInfo.getNumber());

        });
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


        if (isViewUpdate) {
            try {
                windowManager.updateViewLayout(view, params);
            } catch (Exception e) {
                Log.d(TAG, "showDialogOverCall: error : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            windowManager.addView(view, params);
        }
    }

}

