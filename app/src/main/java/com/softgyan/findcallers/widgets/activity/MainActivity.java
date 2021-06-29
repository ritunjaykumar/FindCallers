package com.softgyan.findcallers.widgets.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.models.UserInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.FragmentAdapter;
import com.softgyan.findcallers.widgets.dialog.AlertDialog;
import com.softgyan.findcallers.widgets.fragment.CallFragment;
import com.softgyan.findcallers.widgets.fragment.ContactFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1407;
    private DrawerLayout mDrawerLayout;
    private ViewPager viewPager;
    private int mPosition = 0;
    private TabLayout tabLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        if (Utils.requestOverlayPermission(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }


        Log.d(TAG, "onCreate: call list size : " + CommVar.callList.size());
        Log.d(TAG, "onCreate: contact list size : " + CommVar.contactsList.size());
        initNavigation(toolbar);
        initViewComponent();
        setUpFragmentAdapter();
    }

    @Override
    public void onBackPressed() {
        if (mPosition != 0) {
            viewPager.setCurrentItem(0, true);
            return;
        }

        super.onBackPressed();
    }

    private void initNavigation(Toolbar toolbar) {
        mDrawerLayout = findViewById(R.id.mainDrawer);
        mNavigationView = findViewById(R.id.main_side_navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.start, R.string.close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(item -> {
            closeDrawer();
            navigationItemSelected(item.getItemId());
            return true;
        });

        setUpHeader();

    }

    private void navigationItemSelected(int itemId) {
        Intent intent;
        if (itemId == R.id.navSearchNumber) {
            intent = new Intent(this, SearchNumberActivity.class);
        } else if (itemId == R.id.navBlockNumberList) {
            intent = new Intent(this, BlockNumberActivity.class);
        } else if (itemId == R.id.navBackupRestore) {
            intent = new Intent(this, BackupAndRestoreActivity.class);
        } else if (itemId == R.id.navFindMobile) {
            intent = new Intent(this, FindMobileActivity.class);
        } else if (itemId == R.id.navSearchImportantNumber) {
            intent = new Intent(this, SearchImportantNumberActivity.class);
        } else if (itemId == R.id.navSetting) {
            intent = new Intent(this, SettingActivity.class);
        } else if (itemId == R.id.navLogout) {
            AlertDialog alertDialog = new AlertDialog(this,( alertDialog1, requestCode) -> {
                logout();
                alertDialog1.dismiss();
            });
            alertDialog.setAlertTitle("Alert!");
            alertDialog.setMessage("Sure, Do you want do Logout?");
            alertDialog.show();

            return;
        } else {
            return;
        }
        startActivity(intent);
    }

    private void logout() {

       /* deleteDatabase(CallHelper.DATABASE_NAME);
        deleteDatabase(ContactHelper.DATABASE_NAME);
        deleteDatabase(SpamDbHelper.SPAM_DB);*/
        FirebaseAuth.getInstance().signOut();
        CommVar.callList.clear();
        CommVar.contactsList.clear();
        AppPreference.clearPreference(this);
        Intent intent = new Intent(this, AccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void initViewComponent() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setUpFragmentAdapter() {
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(new CallFragment(), getString(R.string.phone));
        fragmentAdapter.addFragment(new ContactFragment(), getString(R.string.contact));
        viewPager.setAdapter(fragmentAdapter);
        viewPager.addOnPageChangeListener(callback);
        tabLayout.setupWithViewPager(viewPager);
    }

    private final ViewPager.OnPageChangeListener callback = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //blanked
        }

        @Override
        public void onPageSelected(int position) {
            mPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //blank method
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        setUpHeader();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Utils.requestOverlayPermission(this)) {
                finish();
            }
        }
    }

    private void setUpHeader() {
        View v = mNavigationView.getHeaderView(0);
        ShapeableImageView imageView = v.findViewById(R.id.sivProfile);
        TextView tvName = v.findViewById(R.id.tvName);
        TextView tvEmail = v.findViewById(R.id.tvEmail);
        TextView viewProfile = v.findViewById(R.id.viewProfile);
        viewProfile.setOnClickListener(v1 -> {
            Intent intent = new Intent(MainActivity.this, UserAccountSettingActivity.class);
            intent.putExtra(UserAccountSettingActivity.IS_SAVE_DATA, false);
            startActivity(intent);
            closeDrawer();
        });

        FirebaseDB.UserInfo.getUserInfo(new OnResultCallback<UserInfoModel>() {
            @Override
            public void onSuccess(@NonNull UserInfoModel userInfoModel) {
                if (userInfoModel.getUserName() != null) {
                    tvName.setText(userInfoModel.getUserName());
                }
                if (userInfoModel.getUserEmail() != null) {
                    tvEmail.setText(userInfoModel.getUserEmail());
                }
                Glide.with(MainActivity.this).load(userInfoModel.getUserProfile())
                        .placeholder(R.drawable.ic_image)
                        .into(imageView);
            }

            @Override
            public void onFailed(String failedMessage) {
                Toast.makeText(MainActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }
}

