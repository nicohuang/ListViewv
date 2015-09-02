package com.hwz.listviewv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.hwz.listviewv.SwipeRefresh.SwipeRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshListView.IXListViewListener
{

    private SwipeRefreshListView slvOrder;
    List<Map<String,String>> list;
    SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<>();

        simpleAdapter = new SimpleAdapter(this,list,R.layout.item_list,new String []{"name"},new int[]{R.id.tv_name});
        slvOrder = (SwipeRefreshListView)findViewById(R.id.slv_order);
        slvOrder.setAdapter(simpleAdapter);

        slvOrder.setPullLoadEnable(false);
        slvOrder.setPullRefreshEnable(true);
        slvOrder.setXListViewListener(this);
        slvOrder.setOnItemClickListener(this);
        slvOrder.showStartHeader();
    }

    public void date()
    {
        slvOrder.stopLoadMore();
        slvOrder.stopRefresh();
        Map<String ,String > map = new HashMap<>();
        map.put("name","hehe");
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        simpleAdapter.notifyDataSetChanged();

    }

    @Override
    public void onRefresh()
    {
        date();
    }

    @Override
    public void onLoadMore()
    {

    }

    @Override
    public void onNetworkErrorRefresh()
    {

    }

    @Override
    public void onClick(View v)
    {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

    }
}
