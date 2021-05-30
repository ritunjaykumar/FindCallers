package com.softgyan.findcallers.widgets.dialog;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.softgyan.findcallers.R;

public class SingleValueDialog extends BottomSheetDialog implements View.OnClickListener {
    private final Context mContext;
    private final SingleValueDialogCallback callback;
    private EditText etValue;
    private ImageView ivLogo;
    private TextView tvTitle;
    private int requestCode;

    private DialogOption dialogOption;

    public SingleValueDialog(final Context context, DialogOption dialogOption, int requestCode,
                             SingleValueDialogCallback callback) {
        this(context, requestCode, callback);
        this.dialogOption = dialogOption;
    }


    public SingleValueDialog(final Context context, int requestCode, SingleValueDialogCallback callback) {
        super(context);
        this.mContext = context;
        this.callback = callback;
        this.dialogOption = DialogOption.SINGLE_LINE;
        this.requestCode = requestCode;
        initViewComponent();
    }


    private void initViewComponent() {
        this.setContentView(R.layout.layout_single_value_dialog);
        this.setCancelable(false);
        etValue = findViewById(R.id.etCustomValue);
        tvTitle = findViewById(R.id.tvTitle);
        ivLogo = findViewById(R.id.ivLogo);
        Button btnOk = findViewById(R.id.btnOk);
        Button btnCancel = findViewById(R.id.btnCancel);
        ImageButton clearText = findViewById(R.id.ibClear);

        if (btnCancel != null)
            btnCancel.setOnClickListener(this);
        if (btnOk != null)
            btnOk.setOnClickListener(this);
        if (clearText != null)
            clearText.setOnClickListener(this);

        switch (dialogOption) {
            case SINGLE_LINE: {
                etValue.setLines(1);
                etValue.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            }
            case MULTIPLE_LINE: {
                etValue.setMaxLines(3);
                etValue.setLines(2);
                etValue.setVerticalScrollBarEnabled(true);
                etValue.setMovementMethod(ScrollingMovementMethod.getInstance());
                etValue.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                etValue.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                etValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                etValue.setSingleLine(false);
            }
        }


    }

    public void setTitle(@NonNull String title) {
        tvTitle.setText(title);
    }

    public void setTextValue(String text) {
        etValue.setText(text);
    }

    public void setLogo(int logoResource) {
        ivLogo.setImageResource(logoResource);
    }

    public void setHint(@NonNull String hint) {
        etValue.setHint(hint);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnOk) {
            actionOk();
        } else if (id == R.id.btnCancel) {
            dismiss();
        } else if (id == R.id.ibClear) {
            etValue.setText(null);
        }
    }

    private void actionOk() {
        String number = etValue.getText().toString().trim();
        if (!TextUtils.isEmpty(number) && !number.equals("")) {
            callback.onGetValue(SingleValueDialog.this, requestCode, number);
        } else {
            Toast.makeText(mContext, "Invalid number", Toast.LENGTH_SHORT).show();
        }
    }


    public interface SingleValueDialogCallback {
        void onGetValue(@NonNull final SingleValueDialog dialog, int requestCode, @NonNull final String value);
    }

    public enum DialogOption {SINGLE_LINE, MULTIPLE_LINE}
}
