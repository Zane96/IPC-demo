// IOnNewBookArrivedListener.aidl
package com.example.zane.ipc_test;

// Declare any non-default types here with import statements

//监听服务端是否有新书籍，如果有新书籍就立即推送到客户端,观察者模式
import com.example.zane.ipc_test.Book;
interface IOnNewBookArrivedListener {
    void newBookArrived(in Book book);
}
