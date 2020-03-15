package com.ejlchina.http;

import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * Http 请求结果
 *
 */
public class HttpResult {

	private State state;
	private int status;
	private Headers headers;
	private ResponseBody body;
	private Exception e;
	
	
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
	
	
	HttpResult(State state, Exception e) {
		this.state = state;
		this.e = e;
	}
	
	HttpResult(State state, int status, Headers headers, ResponseBody body) {
		this.state = state;
		this.status = status;
		this.headers = headers;
		this.body = body;
	}
	
	
//    protected Object parseObject(String body, Type type) throws Exception {
//        Object result = body;
//        if (type != null && !type.equals(STR_TYPE) && body != null) {
//            result =JSON.parseObject(body, type);
//        }
//        return result;
//    }
//	
	/**
	 * @return 执行状态
	 * @see OnComplete#EXCEPTION
     * @see OnComplete#CANCELED
     * @see OnComplete#RESPONSED
     * @see OnComplete#FAILURE
     * @see OnComplete#TIMEOUT
     * @see OnComplete#NETWORK_ERROR
	 */
	public State getState() {
		return state;
	}

	/**
	 * 
	 * @return HTTP状态码
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return 返回头信息
	 */
	public Headers getHeaders() {
		return headers;
	}

	/**
	 * @return 请求中发生的异常
	 */
	public Exception getE() {
		return e;
	}

	public ResponseBody getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "HttpResult [\n  state = " + state + 
				",\n  status = " + status + 
				",\n  headers = " + headers + 
				",\n  e = " + e + "\n]";
	}
	
}
