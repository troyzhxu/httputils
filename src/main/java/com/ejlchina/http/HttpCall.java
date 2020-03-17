package com.ejlchina.http;

import com.ejlchina.http.HttpResult.State;

public interface HttpCall {

	/**
	 * 取消 Http 请求
	 */
	void cancel();

	/**
	 * @return 请求是否被取消
	 */
	boolean isCanceled();
	
	/**
	 * @return 请求是否执行完成，包含取消和失败
	 */
	boolean isDone();
	
	/**
	 * @return 请求执行完成的状态，若请求未执行完，则返回 null
	 */
	State getState();
	
}
