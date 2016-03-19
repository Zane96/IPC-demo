package com.example.zane.ipc_test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zane on 16/3/16.
 */
public class Book implements Parcelable {

    public int bookId;
    public String bookName;

    public Book(int bookId, String bookName) {
        this.bookId = bookId;
        this.bookName = bookName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(bookName);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>(){

        public Book createFromParcel(Parcel in){
            return new Book(in);
        }

        public Book[] newArray(int size){
            return new Book[size];
        }
    };

    private Book(Parcel in){
        bookId = in.readInt();
        bookName = in.readString();
    }

    @Override
    public String toString() {
        return "bookId " + bookId +" bookName " + bookName;
    }
}
