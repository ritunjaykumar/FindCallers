package com.softgyan.findcallers.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.models.UploadContactModel;
import com.softgyan.findcallers.models.UserInfoModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class FirebaseUserData {
    private static final FirebaseAuth mAuth;
    private static final FirebaseFirestore mFirestore;
    final static String docName;

    static {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        docName = mAuth.getUid();
    }

    public static final class UserInfo {

        public synchronized static void uploadUserInfo(final UserInfoModel userInfoModel,
                                                       final OnUploadCallback callback) {
            if (mAuth == null) return;

            if (docName == null) return;

            mFirestore.collection(FirebaseVar.User.USER_DB_NAME)
                    .document(docName)
                    .set(userInfoModel)
                    .addOnSuccessListener(v -> {
                        callback.onUploadSuccess("User details saved successfully");
                    })
                    .addOnSuccessListener(v -> {
                        callback.onUploadFailed("some thing error, try again");
                    });
        }

        public synchronized static void updateUser(final String[] key, final Object[] value,
                                                   OnUploadCallback updateCallback) {
            if (mAuth == null) return;
            if (key.length != value.length) return;
            final HashMap<String, Object> userMap = new HashMap<>();
            for (int i = 0; i < key.length; i++) {
                userMap.put(key[i], value[i]);
            }
            mFirestore.collection(FirebaseVar.User.USER_DB_NAME)
                    .document(docName)
                    .update(userMap)
                    .addOnSuccessListener(unused -> updateCallback.onUploadSuccess("User info updated successfully"))
                    .addOnFailureListener(e -> updateCallback.onUploadFailed("some thing error try again"));
        }

        public synchronized static void getUserInfo(final OnResultCallback<UserInfoModel> resultCallback) {
            mFirestore.collection(FirebaseVar.User.USER_DB_NAME)
                    .document(docName)
                    .get()
                    .addOnSuccessListener(ds -> {

                        Boolean verifyEmail = ds.getBoolean(FirebaseVar.User.EMAIL_VERIFY);
                        boolean tempEmailVerify = false;
                        if (verifyEmail != null) {
                            tempEmailVerify = verifyEmail;
                        }


                        UserInfoModel userInfoModel = UserInfoModel.getInstance(
                                ds.getString(FirebaseVar.User.USER_NAME),
                                ds.getString(FirebaseVar.User.USER_EMAIL),
                                ds.getString(FirebaseVar.User.USER_PROFILE),
                                ds.getString(FirebaseVar.User.USER_TAG),
                                tempEmailVerify,
                                ds.getString(FirebaseVar.User.USER_ADDRESS)

                        );
                        resultCallback.onSuccess(userInfoModel);
                    })
                    .addOnFailureListener(e -> resultCallback.onFailed("some thing error : " + e.getMessage()));
        }

    }

    public static final class MobileNumberInfo {
        private String address;
        private String mobileNumber;
        private String profileUrl;
        private String totalName;
        private String userEmail;
        private String userName;
        private String userSetName;


        private void checkDocumentExits(String dbName, String documentId, OnResultCallback<DocumentSnapshot> callback) {
            mFirestore.collection(dbName)
                    .document(documentId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                callback.onSuccess(document);
                            } else {
                                callback.onFailed(null);
                            }

                        }
                    }).addOnFailureListener(e -> callback.onFailed(null));

        }


        public void uploadContacts(UploadContactModel contactModel) {

            checkDocumentExits(FirebaseVar.MobileNumber.MOBILE_DB_NAME, contactModel.getMobileNumber(),
                    new OnResultCallback<DocumentSnapshot>() {
                        //on Exits
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot ds) {
                            if (ds.getLong(FirebaseVar.MobileNumber.TOTAL_NAME) != null) {
                                int totalName = ds.getLong(FirebaseVar.MobileNumber.TOTAL_NAME).intValue();
                                totalName += 1;
                                ArrayList<String> names = new ArrayList<>();
                                names.add(Objects.requireNonNull(ds.getString(FirebaseVar.MobileNumber.USER_NAME)).toLowerCase());
                                for (int j = 1; j <= totalName; j++) {
                                    names.add(Objects.requireNonNull(ds.getString(FirebaseVar.MobileNumber.USER_NAME + j)).toLowerCase());
                                }

                                if (!names.contains(contactModel.getUserName().toLowerCase())) {
                                    HashMap<String, Object> doc = new HashMap<>();
                                    doc.put(FirebaseVar.MobileNumber.TOTAL_NAME, totalName);
                                    doc.put(FirebaseVar.MobileNumber.USER_NAME + totalName, contactModel.getTotalName());
                                    mFirestore.collection(FirebaseVar.MobileNumber.MOBILE_DB_NAME)
                                            .document(contactModel.getMobileNumber()).update(doc);
                                } else {
                                }
                            }
                        }

                        //on not Exits
                        @Override
                        public void onFailed(String failedMessage) {
                            mFirestore.collection(FirebaseVar.MobileNumber.MOBILE_DB_NAME)
                                    .document(contactModel.getMobileNumber())
                                    .set(contactModel);
                        }
                    });



        }


    }

}
