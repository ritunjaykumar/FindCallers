package com.softgyan.findcallers.widgets.dialog;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.utils.Utils;

public class ShowCallInfoDialog implements View.OnClickListener {

    private final WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private final LayoutInflater layoutInflater;
    private static final String TAG = "CallerDialog";
    private String number;
    private View view;

    private final Context context;

    public ShowCallInfoDialog(Context context) {
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
        view = layoutInflater.inflate(R.layout.layout_call_notification, null);

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

    public void showDialog(String message, @NonNull String number) {
        TextView tvMessage = view.findViewById(R.id.tvCallMessage);
        TextView tvNumber = view.findViewById(R.id.tvNumber);
        tvMessage.setText(message);
        tvNumber.setText(number);
        this.number = number;
        view.findViewById(R.id.tvCall).setOnClickListener(this);
        view.findViewById(R.id.tvMessage).setOnClickListener(this);
        view.findViewById(R.id.tvClose).setOnClickListener(this);
        windowManager.addView(view, params);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvCall) {
            CallHardware.doCall(context, number);
        } else if (id == R.id.tvMessage) {
            CallUtils.sendMessageIntent(context, number);
        }
        closeWindow();
    }

    private void closeWindow() {
        windowManager.removeView(view);
    }
}
