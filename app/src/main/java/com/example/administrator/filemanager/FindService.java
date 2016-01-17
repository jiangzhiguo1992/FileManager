package com.example.administrator.filemanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindService extends Service {
    List list = new ArrayList();
    File fileRoot = new File("/");

    public FindService() {
    }

    //返回到Activity的对象，返回之后可以上转型对象获得MyBinder，再获得List
    @Override
    public IBinder onBind(Intent intent) {
        IBinder ib = new MyBinder();
        return ib;
    }

    //自定义Binder，传送到Activity取值
    class MyBinder extends Binder {
        public List getList() {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    getFileList(fileRoot);
                }
            });
            t.start();
            return list;
        }
    }

    //获取List
    void getFileList(File dir) {
        File[] dirs = dir.listFiles();
        for (File f : dirs) {
            if (f.canRead()) {
                if (f.isFile()) {
                    list.add(f.getName());
                } else if (f.isDirectory()) {
                    if (f.listFiles().length != 0) {
                        getFileList(f);
                    } else {
                        list.add(f.getName());
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
