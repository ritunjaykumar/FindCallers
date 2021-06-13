package com.softgyan.findcallers.widgets.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
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
import com.softgyan.findcallers.receivers.SaveContactReceiver;
import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.utils.Utils;

public final class CallerDialog implements View.OnClickListener {
    private final WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private final LayoutInflater layoutInflater;
    private static final String TAG = "CallerDialog";
    private CallerInfoModel callerInfo;

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


    }

    @SuppressLint("SetTextI18n")
    public void showDialog(final CallerInfoModel callerInfo, boolean isViewUpdate) {
        if (callerInfo == null) return;
        this.callerInfo = callerInfo;
        if (isViewUpdate) {
            LinearLayout linearLayout = view.findViewById(R.id.llOptionContainer);
            Utils.showViews(linearLayout);
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
            windowManager.addView(view, params);
        }
        if (isViewUpdate)
            new Handler().postDelayed(this::closeWindowManager, 1000 * 60);
    }

    private void closeWindowManager() {
        windowManager.removeView(view);
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

}

