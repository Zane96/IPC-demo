// IBookManager.aidl
package com.example.zane.ipc_test;

// Declare any non-default types here with import statements
import com.example.zane.ipc_test.Book;
import com.example.zane.ipc_test.IOnNewBookArrivedListener;
interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);

    void registerListener(IOnNewBookArrivedListener listener);
    void unRegisterListener(IOnNewBookArrivedListener listener);
}
