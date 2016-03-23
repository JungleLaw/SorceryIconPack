package com.sorcerer.sorcery.iconpack.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.L;
import com.sorcerer.sorcery.iconpack.R;
import com.sorcerer.sorcery.iconpack.adapters.IconAdapter;
import com.sorcerer.sorcery.iconpack.adapters.RequestAdapter;
import com.sorcerer.sorcery.iconpack.models.AppInfo;
import com.sorcerer.sorcery.iconpack.ui.views.AutoLoadRecyclerView;
import com.sorcerer.sorcery.iconpack.util.Utility;

import java.lang.reflect.Field;
import java.util.List;

public class IconFragment extends Fragment {

    private int maxCol = 8;

    private Activity mParentActivity;
    private boolean mCustomPicker = false;
    private int mFlag;
    public static final int FLAG_NEW = 1;
    public static final int FLAG_ALL = 0;
    public static final int FLAG_ALI = 9;
    public static final int FLAG_CYANOGENMOD = 14;
    public static final int FLAG_BAIDU = 8;
    public static final int FLAG_GOOGLE = 10;
    public static final int FLAG_HTC = 2;
    public static final int FLAG_LENOVO = 3;
    public static final int FLAG_LG = 13;
    public static final int FLAG_MOTO = 12;
    public static final int FLAG_MICROSOFT = 11;
    public static final int FLAG_SAMSUNG = 5;
    public static final int FLAG_SONY = 6;
    public static final int FLAG_TENCENT = 7;
    public static final int FLAG_XIAOMI = 4;
    public static final int FLAG_FLYME = 15;

    private AutoLoadRecyclerView mGridView;
    private IconAdapter mIconAdapter;
    private SwipeRefreshLayout mSearchLayout;
    private SearchListener mSearchListener;
    private GridLayoutManager mGridLayoutManager;


    public interface SearchListener {
        void onSearch();
    }

    public IconFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icon, container, false);
        mGridView = (AutoLoadRecyclerView) view.findViewById(R.id.recyclerView_icon_gird);
        mGridLayoutManager = new GridLayoutManager(getContext(), calcNumOfRows());
        mGridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mGridLayoutManager.scrollToPosition(0);
        mGridView.setLayoutManager(mGridLayoutManager);
        mGridView.setHasFixedSize(true);
        mGridView.setItemAnimator(new DefaultItemAnimator());

        mSearchLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout_icon_search);
        try {
            Field f = mSearchLayout.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView) f.get(mSearchLayout);
            img.setImageResource(R.drawable.ic_action_search_icon_inner);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (mSearchListener != null) {
            mSearchLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mSearchLayout.setRefreshing(true);
                    mSearchListener.onSearch();
                    mSearchLayout.setRefreshing(false);
                }
            });
        } else {
            mSearchLayout.setEnabled(false);
        }

        mIconAdapter =
                new IconAdapter(getActivity(), getContext(), getArguments().getInt("flag", 0));

        if (mCustomPicker) {
            mIconAdapter.setCustomPicker(mParentActivity, mCustomPicker);
        }
        mGridView.setOnPauseListenerParams(ImageLoader.getInstance(), false, false);
        mGridView.setAdapter(mIconAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resize();
    }

    private int calcNumOfRows() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float s = getResources().getDimension(R.dimen.icon_grid_item_size) + getResources()
                .getDimension(R.dimen.icon_grid_item_margin);

        int res = (int) (size.x / s);
        return res > maxCol ? maxCol : res;
    }

    public RecyclerView getRecyclerView() {
        return mGridView;
    }

    public void showWithString(String s) {
        if (mIconAdapter != null) {
            mIconAdapter.showWithString(s);
        }
    }

    public void setSearchListener(@NonNull final SearchListener searchListener) {
        mSearchListener = searchListener;
    }

    public boolean isCustomPicker() {
        return mCustomPicker;
    }

    public void setCustomPicker(Activity activity, boolean customPicker) {
        mParentActivity = activity;
        mCustomPicker = customPicker;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resize();
    }

    private void resize() {
        mGridLayoutManager.setSpanCount(calcNumOfRows());
    }

    public IconAdapter getIconAdapter() {
        return mIconAdapter;
    }
}
