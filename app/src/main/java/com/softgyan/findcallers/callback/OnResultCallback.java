package com.softgyan.findcallers.callback;

import androidx.annotation.NonNull;

public interface OnResultCallback<V> {
    void onSuccess(@NonNull final V v);
    void onFailed(final String failedMessage);
}
