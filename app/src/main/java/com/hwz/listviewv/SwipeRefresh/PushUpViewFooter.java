package com.hwz.listviewv.SwipeRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hwz.listviewv.R;


/**
 * 上推刷新时加载更多的界面
 *
 * @author deVlin
 */
public class PushUpViewFooter extends LinearLayout
{
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;
    public final static int STATE_ALL = 3;

    private Context mContext;

    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;

    private boolean isVisibile;

    private int state;

    public PushUpViewFooter(Context context)
    {
        super(context);
        initView(context);
    }

    public PushUpViewFooter(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    public int getState()
    {
        return state;
    }

    /**
     * 设置状态
     *
     * @param state
     */
    public void setState(int state)
    {
        mHintView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mHintView.setVisibility(View.INVISIBLE);
        this.state = state;
        if (state == STATE_READY)
        {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.pullup_footer_hint_ready);
        }
        else if (state == STATE_LOADING)
        {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else if (state == STATE_ALL)
        {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.pullup_show_all);
        }
        else
        {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.pullup_footer_hint_normal);
        }
    }

    public int getBottomMargin()
    {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        return lp.bottomMargin;
    }

    public void setBottomMargin(int height)
    {
        if (height < 0) return;
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.bottomMargin = height;
        mContentView.setLayoutParams(lp);
    }

    /**
     * 默认状态
     */
    public void normal()
    {
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }


    /**
     * 加载圈
     */
    public void loading()
    {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏
     */
    public void hide()
    {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
        isVisibile = false;
    }

    /**
     * 显示
     */
    public void show()
    {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(lp);
        isVisibile = true;
    }

    public boolean isVisibile()
    {
        return isVisibile;
    }

    public void setVisibile(boolean isVisibile)
    {
        this.isVisibile = isVisibile;
    }

    private void initView(Context context)
    {
        mContext = context;
        LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.item_pullup_footer, null);
        addView(moreView);
        moreView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        mContentView = moreView.findViewById(R.id.rl_footer_content);
        mProgressBar = moreView.findViewById(R.id.pro_footer);
        mHintView = (TextView) moreView.findViewById(R.id.tv_footer_hint);
    }


}
