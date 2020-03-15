package com.ejlchina.http;

import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Http 请求结果
 *
 */
public class HttpResult {

	private State state;
	private Response response;
	private Exception error;
	
	
	public static enum State {
		
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
	
	
	HttpResult(State state, Exception error) {
		this.state = state;
		this.error = error;
	}
	
	HttpResult(State state, Response response) {
		this.state = state;
		this.response = response;
	}
	
	/**
	 * @return 执行状态
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return HTTP状态码
	 */
	public int getStatus() {
		return response.code();
	}

	public boolean isSuccessful() {
	    return response.isSuccessful();
	}
	
	/**
	 * @return 返回头信息
	 */
	public Headers getHeaders() {
		return response.headers();
	}

	/**
	 * @return 请求中发生的异常
	 */
	public Exception getError() {
		return error;
	}

	public ResponseBody getBody() {
		return response.body();
	}

	
}
