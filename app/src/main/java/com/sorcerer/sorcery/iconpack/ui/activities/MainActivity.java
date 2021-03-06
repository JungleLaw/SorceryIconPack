package com.sorcerer.sorcery.iconpack.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quinny898.library.persistentsearch.SearchResult;
import com.sorcerer.sorcery.iconpack.BuildConfig;
import com.sorcerer.sorcery.iconpack.R;
import com.sorcerer.sorcery.iconpack.adapters.DrawerMenuAdapter;
import com.sorcerer.sorcery.iconpack.adapters.ViewPageAdapter;
import com.sorcerer.sorcery.iconpack.databinding.ActivityMainBinding;
import com.sorcerer.sorcery.iconpack.databinding.LayoutDrawerMenuBinding;
import com.sorcerer.sorcery.iconpack.models.SorceryMenuItem;
import com.sorcerer.sorcery.iconpack.ui.fragments.IconFragment;
import com.sorcerer.sorcery.iconpack.ui.views.SearchBox;
import com.sorcerer.sorcery.iconpack.util.PermissionsHelper;
import com.sorcerer.sorcery.iconpack.util.ResourceHelper;
import com.sorcerer.sorcery.iconpack.util.ToolbarOnGestureListener;
import com.sorcerer.sorcery.iconpack.util.UpdateHelper;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import im.fir.sdk.FIR;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG_MAIN_ACTIVITY";

    public static final int REQUEST_ICON_DIALOG = 100;

    private ActivityMainBinding mBinding;
    public static Intent mLaunchIntent;
    private ViewPageAdapter mPageAdapter;
    private SearchBox mSearchBox;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private boolean mCustomPicker = false;
    private Context mContext = this;
    private Activity mActivity = this;
    private RecyclerView mMenuView;
    private AppBarLayout mAppBarLayout;
    private ViewPager.OnPageChangeListener mPageChangeListener =
            new ViewPager.OnPageChangeListener() {

                private int times = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {


                    if (position != mViewPager.getCurrentItem()) {
                        closeSearch();
                    }

                    if (position == 0 && positionOffset == 0 && positionOffsetPixels == 0) {
                        times++;
                        if (times >= 3) {
                            mDrawerLayout.openDrawer(mNavigationView);
                        }
                    }
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        times = 0;
                    }
                }
            };

    private SearchBox.SearchListener mSearchListener = new SearchBox.SearchListener() {
        @Override
        public void onSearchOpened() {
            mAppBarLayout.setExpanded(true);
        }

        @Override
        public void onSearchCleared() {

        }

        @Override
        public void onSearchClosed() {
            mSearchBox.clearResults();
        }

        @Override
        public void onSearchTermChanged(String term) {
            ((IconFragment) mPageAdapter.getItem(mViewPager.getCurrentItem()))
                    .showWithString(term.toLowerCase());
        }

        @Override
        public void onSearch(String result) {

        }

        @Override
        public void onResultClick(SearchResult result) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "create main activity");

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mLaunchIntent = getIntent();
        String action = getIntent().getAction();

        mCustomPicker = action.equals("com.novalauncher.THEME");

        setSupportActionBar(mBinding.toolbar);
        setToolbarDoubleTap(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initTabAndPager();

        initSearchBox();

        mAppBarLayout = mBinding.appBarLayoutMain;

        mDrawerLayout = mBinding.drawerLayoutMain;

        mNavigationView = mBinding.navigationMain;
        assert mNavigationView != null;
        initDrawerView();

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, mBinding.toolbar,
                        R.string.nav_open, R.string.nav_close) {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        invalidateOptionsMenu();
                        syncState();

                        closeSearch();

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        invalidateOptionsMenu();
                        syncState();
                    }
                };
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (!mCustomPicker) {
            FIR.init(getApplicationContext());

            Bmob.initialize(this, getString(R.string.bmob_app_id));
            UpdateHelper updateHelper =
                    new UpdateHelper(this);
            updateHelper.update();
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(getString(R.string.select_an_icon));
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("sorcery icon pack",
                MODE_PRIVATE);
        int launchTimes = sharedPreferences.getInt("launch times", 0);
        if (launchTimes == 0) {
            mDrawerLayout.openDrawer(mNavigationView);
        } else {
            if (sharedPreferences.getInt("ver", 0) < BuildConfig.VERSION_CODE) {
                sharedPreferences.edit().putInt("ver", BuildConfig.VERSION_CODE).apply();
                ImageLoader.getInstance().clearDiskCache();
            }
        }
        if (!sharedPreferences.getBoolean("know help", false)) {
            sharedPreferences.edit().putBoolean("know help", true).apply();
        }


        sharedPreferences.edit().putInt("launch times", launchTimes + 1).apply();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (Build.VERSION.SDK_INT >= 23) {
            PermissionsHelper.requestWriteExternalStorage(this);
            PermissionsHelper.requestReadPhoneState(this);
        }
    }

    private void initTabAndPager() {
        mTabLayout = mBinding.tabLayoutIcon;

        mViewPager = mBinding.viewPagerIcon;

        assert mViewPager != null;
        mViewPager.setOffscreenPageLimit(1);

        mViewPager.addOnPageChangeListener(mPageChangeListener);

        mPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        generateFragments(mPageAdapter);
        mViewPager.setAdapter(mPageAdapter);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initSearchBox() {
        mSearchBox = mBinding.searchBoxMainIcon;
        mSearchBox.setLogoText("Sorcery Icons");
        mSearchBox.setHint(ResourceHelper.getString(mContext,R.string.search_hint));
        mSearchBox.setSearchListener(mSearchListener);
        mSearchBox.setSearchWithoutSuggestions(true);
        mSearchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                mDrawerLayout.openDrawer(mNavigationView);
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "RockwellStd.otf");
        mSearchBox.setLogoTypeface(typeface);
    }

    private void initDrawerView() {
        View head = mNavigationView.getHeaderView(0);
        LayoutDrawerMenuBinding drawerMenuBinding = DataBindingUtil.bind(head);

        mMenuView = drawerMenuBinding.recyclerViewDrawerMenu;

        List<SorceryMenuItem.OnSelectListener> listeners = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            final int finalI = i;
            listeners.add(new SorceryMenuItem.OnSelectListener() {
                @Override
                public void onSelect() {
                    if (finalI == 0) {
                        activityShift(ApplyActivity.class);
                    } else if (finalI == 1) {
                        activityShift(FeedbackActivity.class);
                    } else if (finalI == 2) {
                        activityShift(LabActivity.class);
                    } else if (finalI == 3) {
                        activityShift(HelpActivity.class);
                    } else if (finalI == 4) {
                        activityShift(DonateActivity.class);
                    } else if (finalI == 5) {
                        UpdateHelper updateHelper =
                                new UpdateHelper(mContext,
                                        mBinding.coordinatorLayoutMain);
                        updateHelper.update();
                    } else if (finalI == 6) {
                        Intent intent = new Intent(mContext, AboutDialogActivity.class);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.fade_in, 0);
                    }
                    mDrawerLayout.closeDrawers();
                }
            });
        }

        List<SorceryMenuItem> list = new ArrayList<>();
        list.add(new SorceryMenuItem(listeners.get(0), R.drawable.ic_input_black_24dp,
                getString(R.string.nav_item_apply)));
        list.add(new SorceryMenuItem(listeners.get(1), R.drawable.ic_mail_black_24dp,
                getString(R.string.nav_item_feedback)));
        list.add(new SorceryMenuItem(listeners.get(2), R.drawable.ic_settings_black_24dp,
                getString(R.string.nav_item_lab)));

        list.add(new SorceryMenuItem(listeners.get(3), R.drawable.ic_help_black_24dp,
                getString(R.string.nav_item_help)));
        list.add(new SorceryMenuItem(listeners.get(4), R.drawable.ic_attach_money_black_24dp,
                getString(R.string.nav_item_donate)));

        list.add(new SorceryMenuItem(null, 0, null));

        list.add(new SorceryMenuItem(listeners.get(5), 0, getString(R.string.nav_item_update)));
        list.add(new SorceryMenuItem(listeners.get(6), 0, getString(R.string.nav_item_about)));

        DrawerMenuAdapter adapter = new DrawerMenuAdapter(this, list);
        mMenuView.setAdapter(adapter);
        mMenuView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));
        mMenuView.setHasFixedSize(true);
    }

    private void setToolbarDoubleTap(Toolbar toolbar) {
        final GestureDetector detector = new GestureDetector(this,
                new ToolbarOnGestureListener(new ToolbarOnGestureListener.DoubleTapListener() {
                    @Override
                    public void onDoubleTap() {
                        int index = mViewPager.getCurrentItem();
                        ViewPageAdapter adapter = (ViewPageAdapter) mViewPager.getAdapter();
                        IconFragment fragment = (IconFragment) adapter.getItem(index);
                        fragment.getRecyclerView().smoothScrollToPosition(0);
                    }
                }));
        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });
    }

    private IconFragment getIconFragment(int flag) {
        Bundle args = new Bundle();
        args.putInt("flag", flag);
        IconFragment fragment = new IconFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void generateFragments(ViewPageAdapter adapter) {

        String[] name = getResources().getStringArray(R.array.tab_name);

        adapter.addFragment(generateFragment(IconFragment.FLAG_NEW), name[0]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_ALL), name[1]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_ALI), name[2]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_BAIDU), name[3]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_CYANOGENMOD), name[4]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_GOOGLE), name[5]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_HTC), name[6]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_LENOVO), name[7]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_LG), name[8]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_MOTO), name[9]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_MICROSOFT), name[10]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_SAMSUNG), name[11]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_SONY), name[12]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_TENCENT), name[13]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_MIUI), name[14]);
        adapter.addFragment(generateFragment(IconFragment.FLAG_FLYME), name[15]);
    }

    private IconFragment generateFragment(int flag) {
        IconFragment iconFragment = getIconFragment(flag);
        iconFragment.setSearchListener(new IconFragment.SearchListener() {
            @Override
            public void onSearch() {
                mSearchBox.toggleSearch();
            }
        });
        iconFragment.setCustomPicker(this, mCustomPicker);
        return iconFragment;
    }

    private void activityShift(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == PermissionsHelper.WRITE_EXTERNAL_STORAGE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new UpdateHelper(this).update();
            } else {
                Toast.makeText(this, getString(R.string.please_give_permission), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void closeSearch() {
        mSearchBox.closeSearch();
    }


}
