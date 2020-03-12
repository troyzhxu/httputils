package com.ejlchina.http;

/**
 * Created by 15735 on 2017/1/3.
 */

public interface OnComplete {

    int EXCEPTION = -1;
    int CANCELED = 0;
    int SUCCESS = 1;
    int FAILURE = 2;
    int TIMEOUT = 3;
    int NETWORK_ERROR = 4;

    void onComplete(int state);

}
