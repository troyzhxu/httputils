package com.ejlchina.http;

/**
 * Created by 15735 on 2017/1/3.
 */

public interface OnComplete {

	/**
	 * 执行异常
	 */
    int EXCEPTION = -1;
    
    /**
     * 请求被取消
     */
    int CANCELED = 0;
    
    /**
     * 响应成功，Http状态码在 [200, 300) 之间
     */
    int SUCCESS = 1;
    
    /**
     * 响应失败，Http状态码在 [200, 300) 之外
     */
    int FAILURE = 2;
    
    /**
     * 网络超时
     */
    int TIMEOUT = 3;
    
    /**
     * 网络出错
     */
    int NETWORK_ERROR = 4;

    /**
     * @param state 执行状态
     * @see #EXCEPTION
     * @see #CANCELED
     * @see #SUCCESS
     * @see #FAILURE
     * @see #TIMEOUT
     * @see #NETWORK_ERROR
     */
    void onComplete(int state);

}
