package com.softgyan.findcallers.widgets.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.call.system.SystemCalls;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.preferences.SettingPreference;

import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initComponent();
//        getCall();
    }



    private void initComponent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openActivity();
            }
        }, 1000);
    }

    private void openActivity() {
        Intent intent;
        if (!SettingPreference.isWelcomeActivitySet(this)) {
            intent = new Intent(this, WelcomeActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

//debugging remove it

    private void getCall() {
        Log.d(TAG, "getCall: called");
        LoaderCall lc = new LoaderCall(this);
        lc.execute();
    }

    private static final class LoaderCall extends AsyncTask<Void, Void, List<CallModel>> {
        private final Context context;


        public LoaderCall(Context context) {
            this.context = context;
        }

        @Override
        protected List<CallModel> doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: background start");
            final List<CallModel> list = SystemCalls.getCallList(context);
            for (CallModel cm : list) {
                Log.d(TAG, "onPostExecute: cm : " + cm);
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<CallModel> callNumberModels) {
            super.onPostExecute(callNumberModels);
            CommVar.callList.addAll(callNumberModels);
            Toast.makeText(context, "size : " + callNumberModels.size(), Toast.LENGTH_SHORT).show();

        }
    }


}