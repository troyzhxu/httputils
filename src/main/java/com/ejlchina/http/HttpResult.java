package com.ejlchina.http;

import okhttp3.Headers;
import okhttp3.Response;

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
	
	
	HttpResult() {
	}
	
	HttpResult(State state, Exception error) {
		this.state = state;
		this.error = error;
	}
	
	HttpResult(State state, Response response) {
		this.state = state;
		this.response = response;
	}
	
	void exception(State state, Exception error) {
		this.state = state;
		this.error = error;
	}
	
	void response(Response response) {
		this.state = State.RESPONSED;
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

	/**
	 * 
	 * @return 是否响应成功，状态码在 [200..300) 之间
	 */
	public boolean isSuccessful() {
	    return response.isSuccessful();
	}
	
	/**
	 * @return 是否是重定向（300、301、302、303、307、308）
	 */
	public boolean isRedirect() {
		return response.isRedirect();
	}
	
	/**
	 * @return 响应头信息
	 */
	public Headers getHeaders() {
		return response.headers();
	}

	/**
	 * @return 响应报文体
	 */
	public ResultBody getBody() {
		return new ResultBody(response.body());
	}
	
	/**
	 * @return 执行中发生的异常
	 */
	public Exception getError() {
		return error;
	}

	@Override
	public String toString() {
		ResultBody body = getBody();
		return "HttpResult [\n  state: " + state + ",\n  status: " + getStatus() 
				+ ",\n  contentType: " + body.getContentType()
				+ ",\n  body: " + body.toString() + ",\n  error: " 
				+ error + "\n]";
	}

	
}
