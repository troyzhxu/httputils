package com.ejlchina.http;


public interface HttpCall {

	/**
	 * 取消 Http 请求
	 */
	void cancel();

	/**
	 * @return 请求是否执行完成
	 */
	boolean isDone();

	/**
	 * @return 请求是否被取消
	 */
	boolean isCanceled();
	
}
