package com.ejlchina.http.internal;

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
	
	
	public HttpResult() {
	}
	
	public HttpResult(State state) {
		this.state = state;
	}
	
	public HttpResult(Response response) {
		response(response);
	}
	
	public HttpResult(State state, Exception error) {
		exception(state, error);
	}
	
	public void exception(State state, Exception error) {
		this.state = state;
		this.error = error;
	}
	
	public void response(Response response) {
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
		if (response != null) {
			return response.code();
		}
		return 0;
	}

	/**
	 * 
	 * @return 是否响应成功，状态码在 [200..300) 之间
	 */
	public boolean isSuccessful() {
	    if (response != null) {
			return response.isSuccessful();
		}
		return false;
	}
	
	/**
	 * @return 响应头信息
	 */
	public Headers getHeaders() {
		if (response != null) {
			return response.headers();
		}
		return null;
	}

	/**
	 * @return 响应报文体
	 */
	public ResultBody getBody() {
		if (response != null) {
			return new ResultBody(response.body());
		}
		return null;
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
		String str = "HttpResult [\n  state: " + state + ",\n  status: " + getStatus() 
				+ ",\n  headers: " + getHeaders();
		if (body != null) {
			str += ",\n  contentType: " + body.getContentType()
			+ ",\n  body: " + body.toString();
		}
		return str + ",\n  error: " + error + "\n]";
	}

	
}
