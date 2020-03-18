package com.ejlchina.http;

import com.ejlchina.http.internal.ResultBody;

import okhttp3.Headers;


/**
 * Http 请求结果
 */
public interface HttpResult {


	enum State {
		
		/**
		 * 执行异常
		 */
	    EXCEPTION,
	    
	    /**
	     * 请求被取消
	     */
	    CANCELED,
	    
	    /**
	     * 请求已响应
	     */
	    RESPONSED,
	    
	    /**
	     * 网络超时
	     */
	    TIMEOUT,
	    
	    /**
	     * 网络出错
	     */
	    NETWORK_ERROR
		
	}

	/**
	 * @return 执行状态
	 */
	State getState();

	/**
	 * @return HTTP状态码
	 */
	int getStatus();

	/**
	 * 
	 * @return 是否响应成功，状态码在 [200..300) 之间
	 */
	boolean isSuccessful();
	
	/**
	 * @return 响应头信息
	 */
	Headers getHeaders();

	/**
	 * @return 响应报文体
	 */
	ResultBody getBody();
	
	/**
	 * @return 执行中发生的异常
	 */
	Exception getError();
	
}
