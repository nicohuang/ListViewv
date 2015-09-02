package com.hwz.listviewv.SwipeRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.hwz.listviewv.R;


/**
 * 滑动刷新 列表
 *
 * @author deVlin
 */
public class SwipeRefreshListView extends ListView implements OnScrollListener
{

    private final static int SCROLLBACK_HEADER = 0;
    private final static int SCROLLBACK_FOOTER = 1;
    // 滚动回时间
    private final static int SCROLL_DURATION = 400;
    // when pull up >= 50px
    private final static int PULL_LOAD_MORE_DELTA = 50;
    // support iOS like pull
    private final static float OFFSET_RADIO = 1.8f;
    // 保存Y值
    private float mLastY = -1;
    // 用于滚动回
    private Scroller mScroller;
    // 滚动监听
    private OnScrollListener mScrollListener;
    // 触发刷新和加载更多的接口。
    private IXListViewListener mListViewListener;
    // －－ 头界面
    private UnderViewHeader underViewHeader;
    private RelativeLayout mHeaderViewContent;
    private int mHeaderViewHeight;
    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = false;
    // -- 尾界面
    private PushUpViewFooter pushUpViewFooter;
    private boolean mEnablePullLoad;
    private boolean mPullLoading;
    private boolean mIsFooterReady = false;
    // 网络错误界面
    private NetworkErrorView networkErrorView;
    private boolean isNetworkError = false;
    // 总列表项,用于检测列表视图的底部。
    private int mTotalItemCount;
    // mScroller,滚动页眉或页脚。
    private int mScrollBack;

    /**
     * @param context
     */
    public SwipeRefreshListView(Context context)
    {
        super(context);
        initWithContext(context);
    }

    public SwipeRefreshListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initWithContext(context);
    }

    public SwipeRefreshListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initWithContext(context);
    }

    private void initWithContext(Context context)
    {
        mScroller = new Scroller(context, new DecelerateInterpolator());

        super.setOnScrollListener(this);

        // 初始化头部信息
        underViewHeader = new UnderViewHeader(context);
        mHeaderViewContent = (RelativeLayout) underViewHeader
                .findViewById(R.id.xlistview_header_content);
        addHeaderView(underViewHeader);

        // 初始化尾部信息
        pushUpViewFooter = new PushUpViewFooter(context);


        //初始化网络错误界面
        networkErrorView = new NetworkErrorView(context);
        final float scale = context.getResources().getDisplayMetrics().density;
        mHeaderViewHeight = (int) (60 * scale + 0.5f);
        // 初始化头部高度
        underViewHeader.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener()
                {
                    @Override
                    public void onGlobalLayout()
                    {
                        mHeaderViewHeight = mHeaderViewContent.getHeight();
                        getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });

        //网络错误界面点击事件
        networkErrorView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setPullRefreshEnable(true);
                hideNetworkError(true, false);
                isNetworkError = false;
                networkErrorView.setVisibility(View.INVISIBLE);
                removeFooterView(networkErrorView);
                mListViewListener.onNetworkErrorRefresh();
            }
        });
    }

    @Override
    public void setAdapter(ListAdapter adapter)
    {
        if (!mIsFooterReady)
        {
            mIsFooterReady = true;
            addFooterView(pushUpViewFooter);
        }
        super.setAdapter(adapter);
    }

    /**
     * 启动或禁用下啦刷新
     *
     * @param enable
     */
    public void setPullRefreshEnable(boolean enable)
    {
        mEnablePullRefresh = enable;
        if (!mEnablePullRefresh)
        { // disable, hide the content
            mHeaderViewContent.setVisibility(View.INVISIBLE);
        }
        else
        {
            mHeaderViewContent.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 启用或禁用加载更多
     *
     * @param enable
     */
    public void setPullLoadEnable(boolean enable)
    {
        mEnablePullLoad = enable;
        if (!mEnablePullLoad)
        {
            pushUpViewFooter.hide();
            pushUpViewFooter.setOnClickListener(null);
        }
        else
        {
            mPullLoading = false;
            pushUpViewFooter.show();
            pushUpViewFooter.setState(PushUpViewFooter.STATE_NORMAL);

            // both "pull up" and "click" will invoke load more.
            pushUpViewFooter.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startLoadMore();
                }
            });
        }
    }

    /**
     * 停止刷新
     */
    public void stopRefresh()
    {
        if (mPullRefreshing == true)
        {
            mPullRefreshing = false;
            underViewHeader.setState(UnderViewHeader.STATE_NORMAL);
            resetHeaderHeight();
        }
    }

    /**
     * 停止加载,重置页脚视图。
     */
    public void stopLoadMore()
    {
        if (mPullLoading == true)
        {
            mPullLoading = false;
            pushUpViewFooter.setState(PushUpViewFooter.STATE_NORMAL);
        }
    }

    /**
     * 已显示所有数据
     */
    public void alreadyShowAll()
    {
        if (mPullLoading == true)
        {
            mPullLoading = false;
            pushUpViewFooter.setState(PushUpViewFooter.STATE_ALL);
        }
    }

    /**
     * 显示网络错误
     */
    public void showNetworkError()
    {
        if (isNetworkError)
        {
            return;
        }
        networkErrorView.setVisibility(View.VISIBLE);
        networkErrorView.show();
        addFooterView(networkErrorView);

        if (!isNetworkError)
        {
            setPullLoadEnable(false);
            setPullRefreshEnable(false);
            removeFooterView(pushUpViewFooter);
        }
        isNetworkError = true;
    }

    /**
     * 隐藏网络错误
     */
    public void hideNetworkError(boolean isRefresh, boolean isLoad)
    {
        if (!isNetworkError)
        {
            return;
        }
        networkErrorView.setVisibility(View.GONE);
        networkErrorView.hide();
        removeFooterView(networkErrorView);
        if (isNetworkError)
        {
            setPullLoadEnable(isLoad);
            setPullRefreshEnable(isRefresh);
            addFooterView(pushUpViewFooter);
        }
        isNetworkError = false;
    }


    private void invokeOnScrolling()
    {
        if (mScrollListener instanceof OnXScrollListener)
        {
            OnXScrollListener l = (OnXScrollListener) mScrollListener;
            l.onXScrolling(this);
        }
    }

    /**
     * 更新 头部高度
     *
     * @param delta
     */
    private void updateHeaderHeight(float delta)
    {

        underViewHeader.setVisiableHeight((int) delta
                + underViewHeader.getVisiableHeight());
        if (mEnablePullRefresh && !mPullRefreshing)
        { // 未处于刷新状态，更新箭头
            if (underViewHeader.getVisiableHeight() > mHeaderViewHeight)
            {
                underViewHeader.setState(UnderViewHeader.STATE_READY);
            }
            else
            {
                underViewHeader.setState(UnderViewHeader.STATE_NORMAL);
            }
        }
        setSelection(0); // scroll to top each time
    }

    /**
     * 刷新 头部高度
     */
    private void resetHeaderHeight()
    {
        int height = underViewHeader.getVisiableHeight();
        if (height == 0) // not visible.
            return;
        // refreshing and header isn't shown fully. do nothing.
        if (mPullRefreshing && height <= mHeaderViewHeight)
        {
            return;
        }
        int finalHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mPullRefreshing && height > mHeaderViewHeight)
        {
            finalHeight = mHeaderViewHeight;
        }
        mScrollBack = SCROLLBACK_HEADER;
        mScroller.startScroll(0, height, 0, finalHeight - height,
                SCROLL_DURATION);
        // trigger computeScroll
        invalidate();
    }

    /**
     * 修改加载更多的高度
     *
     * @param delta
     */
    private void updateFooterHeight(float delta)
    {
        int height = pushUpViewFooter.getBottomMargin() + (int) delta;
        if (mEnablePullLoad && !mPullLoading)
        {
            if (height > PULL_LOAD_MORE_DELTA)
            { // height enough to invoke load
                // more.
                pushUpViewFooter.setState(PushUpViewFooter.STATE_READY);
            }
            else
            {
                pushUpViewFooter.setState(PushUpViewFooter.STATE_NORMAL);
            }
        }
        pushUpViewFooter.setBottomMargin(height);

        // setSelection(mTotalItemCount - 1); // scroll to bottom
    }

    /**
     * 刷新 尾部高度
     */
    private void resetFooterHeight()
    {
        int bottomMargin = pushUpViewFooter.getBottomMargin();
        if (bottomMargin > 0)
        {
            mScrollBack = SCROLLBACK_FOOTER;
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
                    SCROLL_DURATION);
            invalidate();
        }
    }

    /**
     * 开始显示加载更多
     */
    private void startLoadMore()
    {
        mPullLoading = true;
        if (mListViewListener != null && pushUpViewFooter.getState() != PushUpViewFooter.STATE_LOADING)
        {
            pushUpViewFooter.setState(PushUpViewFooter.STATE_LOADING);
            mListViewListener.onLoadMore();
        }
    }

    /**
     * 一开始出现加载头
     *
     * @return
     */
    public void showStartHeader()
    {
        // invoke refresh
        updateHeaderHeight(mHeaderViewHeight);
        invokeOnScrolling();
        underViewHeader.setVisiableHeight(mHeaderViewHeight);
        mPullRefreshing = true;
        underViewHeader.setState(UnderViewHeader.STATE_REFRESHING);
        if (mListViewListener != null)
        {
            mListViewListener.onRefresh();
        }
        resetHeaderHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (mLastY == -1)
        {
            mLastY = ev.getRawY();
        }

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getFirstVisiblePosition() == 0
                        && (underViewHeader.getVisiableHeight() > 0 || deltaY > 0)
                        && mHeaderViewContent.getVisibility() == View.VISIBLE)
                {
                    // the first item is showing, header has shown or pull down.
                    if (underViewHeader.getmState() != UnderViewHeader.STATE_REFRESHING)
                    {
                        updateHeaderHeight(deltaY / OFFSET_RADIO);
                        invokeOnScrolling();
                    }

                }
                else if (getLastVisiblePosition() == mTotalItemCount - 1
                        && (pushUpViewFooter.getBottomMargin() > 0 || deltaY < 0)
                        && pushUpViewFooter.isVisibile())
                {
                    // last item, already pulled up or want to pull up.
                    updateFooterHeight(-deltaY / OFFSET_RADIO);
                }
                break;
            default:
                mLastY = -1; // reset
                if (getFirstVisiblePosition() == 0 && mHeaderViewContent.getVisibility() == View.VISIBLE)
                {
                    // invoke refresh
                    if (mEnablePullRefresh
                            && underViewHeader.getVisiableHeight() > mHeaderViewHeight
                            && underViewHeader.getmState() != UnderViewHeader.STATE_REFRESHING)
                    {
                        mPullRefreshing = true;
                        underViewHeader.setState(UnderViewHeader.STATE_REFRESHING);
                        if (mListViewListener != null)
                        {
                            mListViewListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();

                }
                if (getLastVisiblePosition() == mTotalItemCount - 1 && pushUpViewFooter.isVisibile())
                {
                    // invoke load more.
                    if (mEnablePullLoad
                            && pushUpViewFooter.getBottomMargin() > PULL_LOAD_MORE_DELTA)
                    {
                        startLoadMore();
                    }
                    resetFooterHeight();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll()
    {
        if (mScroller.computeScrollOffset())
        {
            if (mScrollBack == SCROLLBACK_HEADER)
            {
                underViewHeader.setVisiableHeight(mScroller.getCurrY());
            }
            else
            {
                pushUpViewFooter.setBottomMargin(mScroller.getCurrY());
            }
            postInvalidate();
            invokeOnScrolling();
        }
        super.computeScroll();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l)
    {
        mScrollListener = l;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (mScrollListener != null)
        {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount)
    {
        // send to user's listener
        mTotalItemCount = totalItemCount;
        if (mScrollListener != null)
        {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
    }

    public void setXListViewListener(IXListViewListener l)
    {
        mListViewListener = l;
    }


    /**
     * you can listen ListView.OnScrollListener or this one. it will invoke
     * onXScrolling when header/footer scroll back.
     */
    public interface OnXScrollListener extends OnScrollListener
    {
        public void onXScrolling(View view);
    }


    /**
     * implements this interface to get refresh/load more event.
     */
    public interface IXListViewListener
    {
        public void onRefresh();

        public void onLoadMore();

        public void onNetworkErrorRefresh();
    }
}
