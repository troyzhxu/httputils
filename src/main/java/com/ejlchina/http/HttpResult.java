package com.ejlchina.http;

import okhttp3.Headers;

/**
 * Http 请求结果
 *
 * @param <S> 请求成功时返回的数据类型
 * @param <F> 请求失败时返回的数据类型
 */
public class HttpResult<S, F> {

	private int state;
	private int status;
	private Headers headers;
	private S okData;
	private F failData;
	private Exception e;
	
	
	private HttpResult(int state, int status, Headers headers, S okData, F failData, Exception e) {
		this.state = state;
		this.status = status;
		this.headers = headers;
		this.okData = okData;
		this.failData = failData;
		this.e = e;
	}

	public static <T, M> HttpResult<T, M> exception(int compCode, Exception e) {
		return new HttpResult<>(compCode, 0, null, null, null, e);
	}
	
	public static <T, M> HttpResult<T, M> exception(int status, Headers headers, int compCode, Exception e) {
		return new HttpResult<>(compCode, status, headers, null, null, e);
	}
	
	public static <T, M> HttpResult<T, M> success(int status, Headers headers, T okData) {
		return new HttpResult<>(OnComplete.SUCCESS, status, headers, okData, null, null);
	}
	
	public static <T, M> HttpResult<T, M> fail(int status, Headers headers, M failData) {
		return new HttpResult<>(OnComplete.SUCCESS, status, headers, null, failData, null);
	}

	/**
	 * 
	 * @return 请求状态
	 */
	public int getState() {
		return state;
	}

	/**
	 * 
	 * @return HTTP 状态码
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
	 * HTTP状态码在 [200, 300) 之间 时
	 * @return 请求成功时报文体解析出的数据
	 */
	public S getOkData() {
		return okData;
	}

	/**
	 * HTTP状态码不在 [200, 300) 之间 时
	 * @return 请求失败时报文体解析出的数据
	 */
	public F getFailData() {
		return failData;
	}

	/**
	 * @return 请求中发生的异常
	 */
	public Exception getE() {
		return e;
	}

	@Override
	public String toString() {
		return "HttpResult [\n  state = " + state + 
				",\n  status = " + status + 
				",\n  headers = " + headers + 
				",\n  okData = " + okData + 
				",\n  failData = " + failData + 
				",\n  e = " + e + "\n]";
	}
	
}
