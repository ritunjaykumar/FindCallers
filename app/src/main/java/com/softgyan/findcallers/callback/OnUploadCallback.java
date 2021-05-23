package com.softgyan.findcallers.callback;

public interface OnUploadCallback {
    void onUploadSuccess(String message);

    void onUploadFailed(String failedMessage);
}
