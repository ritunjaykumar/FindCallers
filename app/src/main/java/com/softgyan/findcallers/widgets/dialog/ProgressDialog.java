package com.softgyan.findcallers.widgets.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.utils.Utils;

public class ProgressDialog extends Dialog {
    private final TextView title;

    public ProgressDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.progress_layout);
        setCancelable(false);
        title = findViewById(R.id.tv_title);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setProgressTitle(String progressTitle) {
        if (progressTitle == null) {
            Utils.hideViews(title);
            return;
        }
        title.setText(progressTitle);
        Utils.showViews(title);
    }
}
