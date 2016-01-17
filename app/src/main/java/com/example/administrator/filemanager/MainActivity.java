package com.example.administrator.filemanager;

/**
 * 2015.09.11
 */

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity {

    FileHandler fileHandler = new FileHandler();
    MyAnimation myAnimation = new MyAnimation();
    MyAdapter myAdapter;
    ListView lv;
    List actvList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //listView用的是android的内置id
        lv = (ListView) findViewById(android.R.id.list);
        List<Map<String, Object>> list = fileHandler.openFile(FileHandler.parentFile);
        loadAdapter(list);
        //启动Service遍历所有文件，以便查找文件
        Intent i = new Intent(MainActivity.this, FindService.class);
//        startService(i);
        bindService(i, sc, BIND_AUTO_CREATE);
    }

    //实现ServiceConnection接口,接受List
    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FindService.MyBinder myBinder = (FindService.MyBinder) service;
            actvList = myBinder.getList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        if (position != 0) {
            File f = (File) myAdapter.list.get(position).get("1");
            List list = fileHandler.openFile(f);
            loadAdapter(list);
        }
    }

    //键盘事件,这里只设置了返回按钮事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            List list = fileHandler.back();
            loadAdapter(list);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //加载 MyAdapter
    void loadAdapter(List<Map<String, Object>> list) {
        //传入key和layout_id，Adapter可以获得key关联的value
        String[] str = new String[]{"0", "1", "2"};
        int[] i = new int[]{R.id.iv, R.id.tv, R.id.tv1};
        //注意这里的构造函数被我重写了
        myAdapter = new MyAdapter(actvList, lv, this, list, R.layout.activity_adapter, str, i);
        //ListActivity本身就相当于一个ListView，所以也可以用this.setListAdapter()加载
        lv.setAdapter(myAdapter);
        //listView设置动画(这里为什么只是打开显示？？？？)
        Animation alpha = myAnimation.getAlpha();
        Animation translate = myAnimation.getTranslate();
        AnimationSet set = myAnimation.getSet(alpha, translate);
        LayoutAnimationController lac = myAnimation.getController(set);
        lv.setLayoutAnimation(lac);
    }

}
