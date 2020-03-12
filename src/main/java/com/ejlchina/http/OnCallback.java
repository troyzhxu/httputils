package com.ejlchina.http;

import okhttp3.Headers;

/**
 * Created by 15735 on 2017/1/3.
 */

public interface OnCallback<T> {

    void on(int status, Headers headers, T body);

}
