package com.softgyan.findcallers.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.UploadContactModel;
import com.softgyan.findcallers.models.UserInfoModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        private static final String TAG = "MobileNumberInfo";

        private static void checkDocumentExits(String dbName, String documentId, OnResultCallback<DocumentSnapshot> callback) {
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


        public synchronized static void uploadContacts(UploadContactModel contactModel) {

            checkDocumentExits(FirebaseVar.MobileNumber.MOBILE_DB_NAME, contactModel.getMobileNumber(),
                    new OnResultCallback<DocumentSnapshot>() {
                        //on Exits
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot ds) {
                            try {
                                if (ds.getLong(FirebaseVar.MobileNumber.TOTAL_NAME) != null) {
                                    int totalName = ds.getLong(FirebaseVar.MobileNumber.TOTAL_NAME).intValue();
                                    ArrayList<String> names = new ArrayList<>();
                                    names.add(Objects.requireNonNull(ds.getString(FirebaseVar.MobileNumber.USER_NAME)).toLowerCase());
                                    for (int j = 1; j <= totalName; j++) {
                                        names.add(Objects.requireNonNull(ds.getString(FirebaseVar.MobileNumber.USER_NAME + j)).toLowerCase());
                                    }
                                    totalName += 1;

                                    if (!names.contains(contactModel.getUserName().toLowerCase())) {
                                        HashMap<String, Object> doc = new HashMap<>();
                                        doc.put(FirebaseVar.MobileNumber.TOTAL_NAME, totalName);
                                        doc.put(FirebaseVar.MobileNumber.USER_NAME + totalName, contactModel.getTotalName());

                                        FirebaseBasic.updateData(FirebaseVar.MobileNumber.MOBILE_DB_NAME, contactModel.getMobileNumber(),
                                                doc, null);

                                    } else {
                                        Log.d(TAG, "onSuccess: already exits");
                                    }
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "onSuccess: error : " + e.getMessage());
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

        public synchronized static void uploadContacts(List<UploadContactModel> uploadContactList, OnSuccessfulCallback sucCall) {
            for (UploadContactModel uploadContact : uploadContactList) {
                uploadContacts(uploadContact);
            }
            sucCall.notifyCall();
        }


        public synchronized static void getMobileNumber(@NonNull String mobileNumber, OnResultCallback<ContactModel> callback) {
            mFirestore.collection(FirebaseVar.MobileNumber.MOBILE_DB_NAME)
                    .document(mobileNumber)
                    .get()
                    .addOnSuccessListener(ds -> {
                        String name = ds.getString(FirebaseVar.MobileNumber.USER_NAME);
                        String address = ds.getString(FirebaseVar.MobileNumber.ADDRESS);
                        String profileUrl = ds.getString(FirebaseVar.MobileNumber.PROFILE_URL);
                        String userEmail = ds.getString(FirebaseVar.MobileNumber.USER_EMAIL);

                        ContactModel contactModel = new ContactModel(name);
                        contactModel.setImage(profileUrl);
                        contactModel.setAddress(address);
                        contactModel.setEmailId(userEmail);
                        callback.onSuccess(contactModel);

                    }).addOnFailureListener(e -> callback.onFailed(e.getMessage()));
        }
    }

}