package com.softgyan.findcallers.widgets.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.R;

public class InputDialog extends Dialog {
    private TextView tvTitle;
    private EditText etValue;
    private final InputDialogCallback callback;

    public InputDialog(@NonNull Context context, InputDialogCallback callback) {
        super(context);
        setContentView(R.layout.layout_single_value_dialog);
        setCancelable(false);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvTitle = findViewById(R.id.tvTitle);
        etValue = findViewById(R.id.etCustomValue);
        Button btnOk = findViewById(R.id.btnOk);
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            dismiss();
        });
        btnOk.setOnClickListener(v -> {
            final String text = getTextFromValue();
            if (text != null) {
                callback.onGetText(InputDialog.this,text);
            } else {
                Toast.makeText(getContext(), "invalid text", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }


    private String getTextFromValue() {
        String tempStr = etValue.getText().toString();
        if (tempStr.length() == 1) {
            return null;
        }
        return tempStr;
    }


    public interface InputDialogCallback {
        void onGetText(@NonNull InputDialog dialog,@NonNull final String text);
    }

}
