package com.softgyan.findcallers.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;

import java.util.HashMap;

public final class FirebaseBasic {
    public synchronized static void uploadData(final String collectionName, final String documentName,
                                               Object object, OnUploadCallback callback) {
        if (collectionName == null || documentName == null) {
            callback.onUploadFailed("check collectionName or documentName may be invalid...");
            return;
        }

        final Task<Void> set = FirebaseFirestore.getInstance()
                .collection(collectionName)
                .document(documentName)
                .set(object);
        if (callback != null) {
            set.addOnSuccessListener(unused -> callback.onUploadSuccess("upload successful..."))
                    .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));
        }
    }

    public static synchronized void updateData(final String collectionName, final String documentName,
                                               HashMap<String, Object> data, OnSuccessfulCallback callback) {
        if (collectionName == null || documentName == null) {
            callback.notifyCall();
            return;
        }


        final Task<Void> update = FirebaseFirestore.getInstance()
                .collection(collectionName)
                .document(documentName)
                .update(data);
        if (callback != null) {
            update.addOnSuccessListener(unused -> callback.notifyCall());
        }
    }

    /**
     * @param collectionName provide suitable collection name
     * @param data           it is to be upload
     * @param callback       if you want to receive callback method otherwise pass null
     */
    public static synchronized void setData(final String collectionName, Object data,
                                            OnUploadCallback callback) {
        if (collectionName == null) {
            callback.onUploadFailed("collectionName is invalid...");
            return;
        }

        final Task<DocumentReference> add = FirebaseFirestore.getInstance()
                .collection(collectionName)
                .add(data);
        if (callback != null) {
            add.addOnSuccessListener(documentReference -> callback.onUploadSuccess("uploaded"))
                    .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));
        }
    }

}
