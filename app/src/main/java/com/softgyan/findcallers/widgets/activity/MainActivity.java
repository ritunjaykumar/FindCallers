package com.softgyan.findcallers.widgets.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.FragmentAdapter;
import com.softgyan.findcallers.widgets.fragment.CallFragment;
import com.softgyan.findcallers.widgets.fragment.ContactFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1407;
    private DrawerLayout mDrawerLayout;
    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;
    private int mPosition = 0;
    private TabLayout tabLayout;

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
        init();
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
        NavigationView mNavigationView = findViewById(R.id.main_side_navigation_view);
        LinearLayout mContentView = findViewById(R.id.contentViewLinearLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.start, R.string.close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        View v = mNavigationView.getHeaderView(0);
        ShapeableImageView imageView = v.findViewById(R.id.sivProfile);
        TextView viewProfile = v.findViewById(R.id.viewProfile);
        viewProfile.setOnClickListener(v1 -> {
            Intent intent = new Intent(MainActivity.this, UserAccountSettingActivity.class);
            intent.putExtra(UserAccountSettingActivity.DELETE_ACCOUNT_SHOW, true);
            startActivity(intent);
            closeDrawer();
        });
        mNavigationView.setNavigationItemSelectedListener(item -> {
            closeDrawer();
            navigationItemSelected(item.getItemId());
            return true;
        });


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
        } else {
            return;
        }
        startActivity(intent);
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void init() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setUpFragmentAdapter() {
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(CallFragment.getInstance(), getString(R.string.phone));
        fragmentAdapter.addFragment(ContactFragment.getInstance(), getString(R.string.contact));
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Utils.requestOverlayPermission(this)) {
                finish();
            }
        }
    }
}