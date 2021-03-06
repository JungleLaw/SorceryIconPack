package com.sorcerer.sorcery.iconpack.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.a.a.V;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Sorcerer on 2016/2/5 0005.
 */
public class IconRecyclerView extends RecyclerView implements LoadFinishCallBack{

    private View mEmptyView;

    private OnLoadMoreListener mOnLoadMoreListener;

    private AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            Adapter adapter = getAdapter();
            if(adapter!=null && mEmptyView!=null){
                if(adapter.getItemCount()==0){
                    setVisibility(GONE);
                    mEmptyView.setVisibility(VISIBLE);
                }else{
                    setVisibility(VISIBLE);
                    mEmptyView.setVisibility(GONE);
                }
            }
        }
    };

    private boolean isLoadingMore;

    public IconRecyclerView(Context context) {
        super(context);
    }

    public IconRecyclerView(Context context,
                            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IconRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnPauseListenerParams(ImageLoader imageLoader, boolean pauseOnScroll, boolean
            pauseOnFling){
        addOnScrollListener(new AutoLoadScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        mOnLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public void loadFinish(Object obj) {
        isLoadingMore = false;
    }

    public interface OnLoadMoreListener {
        void loadMore();
    }

    private class AutoLoadScrollListener extends RecyclerView.OnScrollListener {

        private ImageLoader mImageLoader;
        private boolean mPauseOnScroll;
        private boolean mPauseOnFling;

        public AutoLoadScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean
                pauseOnFling) {
            mImageLoader = imageLoader;
            mPauseOnScroll = pauseOnScroll;
            mPauseOnFling = pauseOnFling;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (getLayoutManager() instanceof LinearLayoutManager) {
                int lastVisibleItem = ((LinearLayoutManager) getLayoutManager())
                        .findLastVisibleItemPosition();
                int totalItemCount = IconRecyclerView.this.getAdapter().getItemCount();

                if (mOnLoadMoreListener != null && !isLoadingMore &&
                        lastVisibleItem >= totalItemCount - 2 && dy > 0) {
                    mOnLoadMoreListener.loadMore();
                    isLoadingMore = true;
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
            if (mImageLoader != null) {
                switch (newState) {
                    case 0:
                        mImageLoader.resume();
                        break;
                    case 1:
                        if (mPauseOnScroll) {
                            mImageLoader.pause();
                        } else {
                            mImageLoader.resume();
                        }
                        break;
                    case 2:
                        if (mPauseOnFling) {
                            mImageLoader.pause();
                        } else {
                            mImageLoader.resume();
                        }
                        break;
                }
            }
        }
    }


    public void setEmptyView(View view){
        mEmptyView = view;
    }

    public void removeEmptyView(){
        mEmptyView = null;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter!=null){
            adapter.registerAdapterDataObserver(mAdapterDataObserver);
        }

        mAdapterDataObserver.onChanged();
    }
}