package com.softgyan.findcallers.firebase;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.utils.Utils;

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

    private static final String HOME_DIR = "images";
    private static final String PROFILE_DIR = "profileDir";

    public static synchronized void uploadImage(Uri imageUri, Context mContext, OnUploadCallback callback) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        String fileName = Utils.getUniqueId() + "." + Utils.getUriExtension(mContext, imageUri);

        StorageReference reference = storage.getReference().child(HOME_DIR +
                "/" + PROFILE_DIR +
                "/" + fileName
        );
        reference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUri = uriTask.getResult();
                    final String downloadUrl = String.valueOf(downloadUri);
                    callback.onUploadSuccess(downloadUrl);
                })
                .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));
    }

    public static synchronized void deleteImage(String imageUrl, OnUploadCallback deleteCallback) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
        imageRef.delete()
                .addOnSuccessListener(unused -> deleteCallback.onUploadSuccess(null))
                .addOnFailureListener(e -> deleteCallback.onUploadFailed(e.getMessage()));
    }

}
