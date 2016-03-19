package com.example.zane.ipc_test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;


/**
 * 客户端代码。用来启动并绑定服务端的service
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity2";
    private TextView textView;
    private IBookManager remoteBinder;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String newBook = (String)msg.obj;
                    textView.setText(newBook);
                    break;
            }
        }
    };

    //给binder添加死亡代理通知,如果远程binder意外死亡了，那么就重新绑定
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (remoteBinder == null){
                return;
            }
            //解除绑定
            remoteBinder.asBinder().unlinkToDeath(mDeathRecipient, 0);
            remoteBinder = null;

            Intent intent = new Intent(MainActivity.this, BookManagerService.class);
            //重新绑定
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得服务端的binder
            IBookManager binder = IBookManager.Stub.asInterface(service);
            remoteBinder = binder;
            try {
                //获得书籍的信息
                List<Book> books = binder.getBookList();
                Log.i(TAG, "得到的数据类型是： "+books.getClass());
                Log.i(TAG, "数据是： "+books.toString());
                //客户端去添加书籍
                binder.addBook(new Book(3, "java核心技术"));
                List<Book> newBooks = binder.getBookList();
                Log.i(TAG, "得到的数据类型是： " + newBooks.getClass());
                Log.i(TAG, "新数据是： " + newBooks.toString());

                //注册监听
                binder.registerListener(listener);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //实现AIDL中注册监听的binder.因为这个方法是跑在客户端的binder池，所以不应该直接操作ui，需要通知到主线程去做ui操作
    private IOnNewBookArrivedListener listener = new IOnNewBookArrivedListener.Stub(){
        //异步
        @Override
        public void newBookArrived(Book book) throws RemoteException {
            Log.i(TAG, "新书更新通知： " + book.toString());
//            textView.setText(book.toString());
            Message message = new Message();
            message.what = 1;
            message.obj = book.toString();
            handler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.text);

        Intent intent = new Intent(MainActivity.this, BookManagerService.class);

        //只要binding关系存在，就自动启动service
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteBinder != null && remoteBinder.asBinder().isBinderAlive()){
            try {
                Log.i(TAG, "开始解除监听 "+listener);
                remoteBinder.unRegisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        unbindService(connection);
    }
}
