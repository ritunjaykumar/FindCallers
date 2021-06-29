package com.softgyan.findcallers.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;
import com.softgyan.findcallers.models.UploadContactModel;
import com.softgyan.findcallers.models.UserInfoModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            final Task<Void> update = mFirestore.collection(FirebaseVar.User.USER_DB_NAME)
                    .document(docName)
                    .update(userMap);

            if (updateCallback != null) {
                update.addOnSuccessListener(unused -> updateCallback.onUploadSuccess("User info updated successfully"))
                        .addOnFailureListener(e -> updateCallback.onUploadFailed("some thing error try again"));
            }

        }

        public synchronized static void getUserInfo(final OnResultCallback<UserInfoModel> resultCallback) {
            try {
                mFirestore.collection(FirebaseVar.User.USER_DB_NAME)
                        .document(docName)
                        .get()
                        .addOnSuccessListener(ds -> {
                            if (!ds.exists()) {
                                resultCallback.onFailed("data not found");
                                return;
                            }
                            Boolean verifyEmail = ds.getBoolean(FirebaseVar.User.EMAIL_VERIFY);
                            boolean tempEmailVerify = false;
                            if (verifyEmail != null) {
                                tempEmailVerify = verifyEmail;
                            }
                            final Boolean aBoolean = ds.getBoolean(FirebaseVar.User.BUSINESS_ACCOUNT);
                            boolean tempBusinessAc;
                            if (aBoolean == null) {
                                tempBusinessAc = false;
                            } else {
                                tempBusinessAc = aBoolean;
                            }

                            UserInfoModel userInfoModel = UserInfoModel.getInstance(
                                    ds.getString(FirebaseVar.User.USER_NAME),
                                    ds.getString(FirebaseVar.User.USER_EMAIL),
                                    ds.getString(FirebaseVar.User.USER_PROFILE),
                                    ds.getString(FirebaseVar.User.USER_TAG),
                                    tempEmailVerify,
                                    ds.getString(FirebaseVar.User.USER_ADDRESS),
                                    tempBusinessAc

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
        private static final String TAG = "FirebaseDB";

        private static void updateMobileData(String mobile, String key, Object data) {
            HashMap<String, Object> mapData = new HashMap<>();
            mapData.put(key, data);

            FirebaseBasic.updateData(FirebaseVar.MobileNumber.MOBILE_DB_NAME, mobile, mapData, null);
        }

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
            if (Utils.trimNumber(mobileNumber).length() < 10) {
                callback.onFailed("invalid mobile number");
                return;
            }
            mFirestore.collection(FirebaseVar.MobileNumber.MOBILE_DB_NAME)
                    .document(mobileNumber)
                    .get()
                    .addOnSuccessListener(ds -> {
                        if (!ds.exists()) {
                            callback.onFailed("not found");
                            return;
                        }

                        String name = ds.getString(FirebaseVar.MobileNumber.USER_NAME);
                        String address = ds.getString(FirebaseVar.MobileNumber.ADDRESS);
                        String profileUrl = ds.getString(FirebaseVar.MobileNumber.PROFILE_URL);
                        String userEmail = ds.getString(FirebaseVar.MobileNumber.USER_EMAIL);
                        ContactModel contactModel = new ContactModel(name);
                        contactModel.setImage(profileUrl);
                        contactModel.setAddress(address);
                        contactModel.setEmailId(userEmail);
                        contactModel.setContactNumbers(new ContactNumberModel(ds.getId()));
                        callback.onSuccess(contactModel);

                    })
                    .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
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

                    HashMap<String, Object> tempSpam = new HashMap<>();
                    tempSpam.put(FirebaseVar.SpamDB.NAME + 1, spamMap.get(FirebaseVar.SpamDB.NAME));
                    tempSpam.put(FirebaseVar.SpamDB.SPAM_TYPE_KEY + 1, spamMap.get(FirebaseVar.SpamDB.SPAM_TYPE_KEY));
                    tempSpam.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, 1);
                    tempSpam.put(FirebaseVar.SpamDB.TOTAL_NAME, 1);

                    FirebaseBasic.uploadData(FirebaseVar.SpamDB.SPAM_DB_NAME, mobileNumber, tempSpam, new OnUploadCallback() {
                        @Override
                        public void onUploadSuccess(String message) {
                            if (callback != null)
                                callback.onUploadSuccess(message);
                        }

                        @Override
                        public void onUploadFailed(String failedMessage) {
                            if (callback != null)
                                callback.onUploadFailed(failedMessage);
                        }
                    });

                }
            });
        }

        /**
         * @param number   for document name
         * @param callback callback function
         *                 return type : name, type, vote
         */

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
                                tempMapSpam.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, ds.getLong(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE));
                                callback.onSuccess(tempMapSpam);
                            } else {
                                callback.onFailed("no data found..");
                            }
                        } catch (Exception e) {
                            callback.onFailed(e.getMessage());
                        }

                    })
                    .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
        }
    }

    public static final class Business {
        public static synchronized void getBusinessRecord(Context context, String dbName, int range,
                                                          GeoPoint currentPoint,
                                                          OnResultCallback<List<Map<String, Object>>> callback) {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle("getting " + dbName + " record..");
            progressDialog.show();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(FirebaseVar.Business.DB_NAME)
                    .document(dbName)
                    .collection(dbName)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        private final List<Map<String, Object>> businessRecords = new ArrayList<>();

                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                Log.d(TAG, "onComplete: size : "+documents.size());
                                boolean isFound = true;
                                for (DocumentSnapshot documentSnapshot : documents) {
                                    GeoPoint geoPoint = documentSnapshot.getGeoPoint(FirebaseVar.Business.GEO_POINT);
                                    if (geoPoint == null) {
                                        continue;
                                    }
                                    isFound = false;
                                    double sRange = Utils.getDistanceFromLatLonInKm(currentPoint.getLatitude(), currentPoint.getLongitude(),
                                            geoPoint.getLatitude(), geoPoint.getLongitude());
                                    Log.d(TAG, "onComplete: distance : "+sRange);
                                    if (sRange <= range) {
                                        Map<String, Object> records = documentSnapshot.getData();
                                        if (records != null) {
                                            records.put(FirebaseVar.Business.DB_TYPE_KEY, dbName);
                                            records.put(FirebaseVar.Business.DISTANCE_KEY, sRange);
                                            records.remove(FirebaseVar.Business.GEO_POINT);
                                        }
                                        businessRecords.add(records);
                                    }

                                }
                                if(isFound){
                                    callback.onFailed("record not found");
                                }else{
                                    callback.onSuccess(businessRecords);
                                }
                            } else {
                                callback.onFailed("something wrong, try again");
                            }
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        callback.onFailed(e.getMessage());
                    });
        }

        public static synchronized void saveBusinessRecord(Context context, String dbName,
                                                           Map<String, Object> businessRecord, OnUploadCallback callback) {

            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle("saving " + dbName + " record..");
            progressDialog.show();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                progressDialog.dismiss();
                callback.onUploadSuccess("your are not logged in");
                return;
            }

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(FirebaseVar.Business.DB_NAME)
                    .document(dbName)
                    .collection(dbName)
                    .document(firebaseUser.getUid())
                    .set(businessRecord)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        callback.onUploadSuccess("Upload Successful");
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        callback.onUploadFailed(e.getMessage());
                    });
        }

    }


    public static final class CallNotification {
        public static synchronized void setCallNotification(String mobileNumber,
                                                            HashMap<String, Object> notifyData, OnUploadCallback callback) {
            FirebaseBasic.uploadData(FirebaseVar.CallNotification.DB_NAME, Utils.trimNumber(mobileNumber), notifyData,
                    callback);
        }

        public static synchronized void getCallNotification(Context context, @NonNull String mobileNumber,
                                                            OnResultCallback<HashMap<String, Object>> callback) {
            /*ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle("getting Call Notification data");
            progressDialog.show();*/
            FirebaseBasic.getData(FirebaseVar.CallNotification.DB_NAME, Utils.trimNumber(mobileNumber),
                    new OnResultCallback<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot ds) {
                            try {
                                HashMap<String, Object> data = new HashMap<>();
                                String message = ds.getString(FirebaseVar.CallNotification.MESSAGE);
                                String startDate = ds.getString(FirebaseVar.CallNotification.START_DATE);
                                String endDate = ds.getString(FirebaseVar.CallNotification.END_DATE);
                                String startTime = ds.getString(FirebaseVar.CallNotification.START_TIME);
                                String endTime = ds.getString(FirebaseVar.CallNotification.END_TIME);

                                data.put(FirebaseVar.CallNotification.MESSAGE, message);
                                data.put(FirebaseVar.CallNotification.START_DATE, startDate);
                                data.put(FirebaseVar.CallNotification.END_TIME, endTime);
                                data.put(FirebaseVar.CallNotification.START_TIME, startTime);
                                data.put(FirebaseVar.CallNotification.END_DATE, endDate);

                                Log.d(TAG, "onSuccess: hash value : " + data.toString());
//                        progressDialog.dismiss();
                                callback.onSuccess(data);
                            } catch (Exception e) {
                                callback.onFailed(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailed(String failedMessage) {
//                    progressDialog.dismiss();
                            callback.onFailed(failedMessage);
                        }
                    });
        }

        public static synchronized void deleteCallNotification(@NonNull String mobileNumber,
                                                               OnUploadCallback callback) {
            FirebaseBasic.deleteDocument(FirebaseVar.CallNotification.DB_NAME, mobileNumber, callback);
        }
    }


}


/*GeoPoint geoPoint = ds.getGeoPoint(FirebaseVar.Doctor.GEO_POINT);
                            if (geoPoint == null) {
                                continue;
                            }
                            flag = false;
                            double sRange = Utils.getDistanceFromLatLonInKm(lat, lon,
                                    geoPoint.getLatitude(), geoPoint.getLongitude());
                            Log.d(TAG, "getDoctorRecord: sRange : " + sRange);
                            if (sRange <= range) {
                                final DoctorModel doctorDetails = getDoctorDetails(ds, sRange);
                                doctorList.add(doctorDetails);
                            }*/