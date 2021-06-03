package com.softgyan.findcallers.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.UploadContactModel;
import com.softgyan.findcallers.models.UserInfoModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class FirebaseDB {
    private static final FirebaseAuth mAuth;
    private static final FirebaseFirestore mFirestore;
    final static String docName;
    private static final String TAG = "FirebaseDB";

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
            try {
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
            } catch (Exception e) {
                resultCallback.onFailed("you don't have account");
            }
        }


        public synchronized static void deleteAccount(Context context, OnUploadCallback callback) {
            final boolean connectionAvailable = Utils.isInternetConnectionAvailable(context);
            if (!connectionAvailable) {
                callback.onUploadFailed("Check your internet connection.");
                return;
            }
            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentUser.delete()
                        .addOnSuccessListener(unused -> callback.onUploadSuccess(null))
                        .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));
            }
        }


        public synchronized static void deleteUserRecord(Context context, String documentID, OnUploadCallback callback) {
            if (!Utils.isInternetConnectionAvailable(context)) {
                callback.onUploadFailed("Check your internet connection.");
                return;
            }
            mFirestore.collection(FirebaseVar.User.USER_DB_NAME)
                    .document(documentID)
                    .delete()
                    .addOnSuccessListener(unused -> callback.onUploadSuccess(null))
                    .addOnFailureListener(e -> callback.onUploadFailed(e.getMessage()));
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


    public static final class SpamDB {
        public static void uploadSpamNumber(HashMap<String, Object> spamMap, OnUploadCallback callback) {
            if (spamMap.get(FirebaseVar.SpamDB.MOBILE_NUMBER) == null) {
                return;
            }

            String mobileNumber = Utils.trimNumber(spamMap.get(FirebaseVar.SpamDB.MOBILE_NUMBER).toString());
            MobileNumberInfo.checkDocumentExits(FirebaseVar.SpamDB.SPAM_DB_NAME, mobileNumber, new OnResultCallback<DocumentSnapshot>() {
                @Override
                public void onSuccess(@NonNull DocumentSnapshot ds) {
                    //if exits
                    try {
                        int totalName = Objects.requireNonNull(ds.getLong(FirebaseVar.SpamDB.TOTAL_NAME)).intValue();
                        int totalVote = Objects.requireNonNull(ds.getLong(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE)).intValue();
                        totalVote++;
                        totalName++;
                        HashMap<String, Object> tempSpam = new HashMap<>();
                        tempSpam.put(FirebaseVar.SpamDB.NAME + totalName, spamMap.get(FirebaseVar.SpamDB.NAME));
                        tempSpam.put(FirebaseVar.SpamDB.SPAM_TYPE_KEY + totalName, spamMap.get(FirebaseVar.SpamDB.SPAM_TYPE_KEY));
                        tempSpam.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, totalVote);
                        tempSpam.put(FirebaseVar.SpamDB.TOTAL_NAME, totalName);

                        FirebaseBasic.updateData(FirebaseVar.SpamDB.SPAM_DB_NAME, mobileNumber,
                                tempSpam, () -> {
                                    if (callback != null) {
                                        callback.onUploadSuccess("upload successful");
                                    }
                                });


                    } catch (Exception e) {
                        Log.d(TAG, "onSuccess: error message : " + e.getMessage());
                        if (callback != null) {
                            callback.onUploadFailed(e.getMessage());
                        }
                    }

                }

                @Override
                public void onFailed(String failedMessage) {
                    //not exits
                    FirebaseBasic.uploadData(FirebaseVar.SpamDB.SPAM_DB_NAME, mobileNumber, spamMap, new OnUploadCallback() {
                        @Override
                        public void onUploadSuccess(String message) {
                            callback.onUploadSuccess(message);
                        }

                        @Override
                        public void onUploadFailed(String failedMessage) {
                            callback.onUploadFailed(failedMessage);
                        }
                    });

                }
            });
        }

        public static void getSpamNumber(String number, OnResultCallback<HashMap<String, Object>> callback) {
            mFirestore.collection(FirebaseVar.SpamDB.SPAM_DB_NAME)
                    .document(number)
                    .get()
                    .addOnSuccessListener(ds -> {
                        try {
                            HashMap<String, Object> tempMapSpam = new HashMap<>();
                            int totalName = ds.getLong(FirebaseVar.SpamDB.TOTAL_NAME).intValue();
                            tempMapSpam.put(FirebaseVar.SpamDB.MOBILE_NUMBER, number);
                            tempMapSpam.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, ds.getLong(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE).intValue());
                            if (totalName > 1) {
                                tempMapSpam.put(FirebaseVar.SpamDB.NAME, ds.getString(FirebaseVar.SpamDB.NAME + 1));
                                tempMapSpam.put(FirebaseVar.SpamDB.SPAM_TYPE_KEY, ds.getString(FirebaseVar.SpamDB.SPAM_TYPE_KEY + 1));
                            }
                            callback.onSuccess(tempMapSpam);
                        } catch (Exception e) {
                            callback.onFailed(e.getMessage());
                        }

                    })
                    .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
        }
    }

}
