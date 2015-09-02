package com.hwz.listviewv.SwipeRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.hwz.listviewv.R;


/**
 * 下啦刷新界面
 *
 * @author deVin
 */
public class NetworkErrorView extends LinearLayout
{
    private LinearLayout mContainer;

    public NetworkErrorView(Context context)
    {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public NetworkErrorView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    /**
     * 隐藏
     */
    public void hide()
    {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = 0;
        mContainer.setLayoutParams(lp);
    }

    /**
     * 显示
     */
    public void show()
    {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mContainer.setLayoutParams(lp);
    }

    private void initView(Context context)
    {
        // 初始情况，设置下拉刷新view高度为0
        LayoutParams lp = new LayoutParams(
                LayoutParams.FILL_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.item_network_error, null);
        addView(mContainer, lp);
        mContainer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    }


}
