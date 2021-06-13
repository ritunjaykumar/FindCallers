package com.softgyan.findcallers.firebase;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.utils.Utils;

import java.util.HashMap;
import java.util.List;

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

    public static synchronized void getData(final String collectionName, OnResultCallback<List<DocumentSnapshot>> callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    callback.onSuccess(documents);
                })
                .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
    }

    public static synchronized void getData(final String collectionName, @NonNull String documentName
            , OnResultCallback<DocumentSnapshot> callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionName)
                .document(documentName)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
    }


    public static synchronized void deleteDocument(final String collectionName, final String documentName,
                                                   OnUploadCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(collectionName)
                .document(Utils.trimNumber(documentName))
                .delete()
                .addOnSuccessListener(unused -> callback.onUploadSuccess("Delete successful"))
                .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));

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

    public static synchronized void uploadBusinessRecord(String collectionName, String documentName,
                                                         HashMap<String, Object> businessData, OnUploadCallback callback) {

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(FirebaseVar.Business.DB_NAME)
                .document(collectionName)
                .collection(collectionName)
                .document(documentName)
                .set(businessData)
                .addOnSuccessListener(unused -> callback.onUploadFailed(null))
                .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));
    }


}
