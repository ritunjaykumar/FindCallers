package com.softgyan.findcallers.widgets.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.R;

public class AlertDialog extends Dialog implements View.OnClickListener {
    private final OnConfirmClick confirmClick;
    private final TextView tvTitle, tvMessage;
    private int requestCode = -1;

    public AlertDialog(@NonNull Context context, OnConfirmClick confirmClick) {
        super(context);
        setContentView(R.layout.layout_alert_dialog);
        this.confirmClick = confirmClick;
        setCancelable(false);
        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button buttonOkay = findViewById(R.id.btnConfirm);
        Button buttonCancel = findViewById(R.id.btnCancel);

        buttonCancel.setOnClickListener(this);
        buttonOkay.setOnClickListener(this);
    }

    public AlertDialog(@NonNull Context context, int requestCode, OnConfirmClick confirmClick) {
        this(context, confirmClick);
        this.requestCode = requestCode;
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.btnCancel) {
            this.dismiss();
        } else if (vId == R.id.btnConfirm) {
            confirmClick.onConfirm(this, requestCode);
        }
    }

    public void setAlertTitle(String title) {
        tvTitle.setText(title);
    }

    public void setMessage(String message) {
        tvMessage.setText(message);
    }

    public interface OnConfirmClick {
        /**
         * @param alertDialog can't be null
         *                    use always dismiss function with dialog to close dialog
         */
        void onConfirm(@NonNull AlertDialog alertDialog, int requestCode);
    }
}
