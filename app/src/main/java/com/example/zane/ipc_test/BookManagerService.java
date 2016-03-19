package com.example.zane.ipc_test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Zane on 16/3/16.
 * 远程服务，运行在新的进程
 */
public class BookManagerService extends Service{

    private static final String TAG = "BookManagerService";

    //读写并发(线程同步安全)的集合
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    //通过底层相同的binder作为key来保证value（传输对象）不会因为ipc的传输而导致传输对象变化
    private RemoteCallbackList<IOnNewBookArrivedListener> listeners = new RemoteCallbackList<>();
    //线程同步安全的boolean类型,判断service是否销毁
    private AtomicBoolean isServiceDestory = new AtomicBoolean(false);

    public BookManagerService() {
        mBookList.add(new Book(1, "Android开发艺术探索"));
        mBookList.add(new Book(2, "第一行代码"));
    }

    //实现AIDL文件中的binder
    private Binder mBinder = new IBookManager.Stub(){

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            listeners.register(listener);
        }

        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            listeners.unregister(listener);
        }


    };

    @Override
    public void onCreate() {
        super.onCreate();

        //每5秒中生成一本新书，然后去通知所有的客户端去更新书籍信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isServiceDestory.get()) {

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //生成新的一本书
                    int bookId = mBookList.size() + 1;
                    Book book = new Book(bookId, "安卓安卓安卓安卓安卓好坑!!");

                    //把这本书的信息通知给所有的注册了监听的客户端
//                    for (int i = 0; i < listeners.size(); i++){
//                        IOnNewBookArrivedListener listener = listeners.get(i);
//                        try {
//                            int j = i + 1;
//                            Log.i(TAG, "通知第 "+" "+String.valueOf(j)+" 个客户端");
//                            listener.newBookArrived(book);
//                        } catch (RemoteException e) {
//                            e.printStackTrace();
//                        }
//                    }

                    final int N = listeners.beginBroadcast();
                    for (int i = 0; i < N; i++){
                        int j = i + 1;
                        Log.i(TAG, "通知第 "+" "+String.valueOf(j)+" 个客户端");
                        IOnNewBookArrivedListener listener = listeners.getBroadcastItem(i);
                        try {
                            listener.newBookArrived(book);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    listeners.finishBroadcast();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceDestory.set(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
