package com.softgyan.findcallers.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class CopyPasteUtils {
    private static final String SIMPLE_TEXT = "simple_text";
    private static final String TAG = CopyPasteUtils.class.getName();

    private static ClipboardManager getSystemClipBoard(Context context) {
        return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public static void copyToClipboard(Context context, String toCopy) {
        ClipboardManager clipboardManager = getSystemClipBoard(context);
        ClipData clipData = ClipData.newPlainText(SIMPLE_TEXT, toCopy);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(context, toCopy + " is copied", Toast.LENGTH_SHORT).show();
    }

    public static boolean showPasteMenu(Context context) {
        ClipboardManager clipboardManager = getSystemClipBoard(context);
        if (!clipboardManager.hasPrimaryClip()) {
            return false;
        } else
            return clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
    }

    public static String pasteFromClipboard(Context context) {
        ClipData clipData = getSystemClipBoard(context).getPrimaryClip();
        ClipData.Item item = clipData.getItemAt(0);
        Log.d(TAG, "pasteFromClipboard: paste text : " + item.getText());

        return item.getText().toString();
    }
}
