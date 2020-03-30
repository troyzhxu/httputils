package com.ejlchina.http;

import com.ejlchina.http.HttpResult.State;

/**
 * 全局回调处理器
 * @author 15735
 * @since 2.3.0
 */
public interface GlobalCallback {

	/**
	 * 全局响应回调
	 * @param task 所属的 HttpTask
	 * @param result 响应结果
	 * @return 是否继续执行 task 的响应回调
	 */
	boolean onResponse(HttpTask<?> task, HttpResult result);
	
	/**
	 * 全局异常回调
	 * @param task 所属的 HttpTask
	 * @param error 异常信息
	 * @return 是否继续执行 task 的异常回调
	 */
	boolean onException(HttpTask<?> task, Exception error);
	
	/**
	 * 全局完成回调
	 * @param task 所属的 HttpTask
	 * @param state 完成状态
	 * @return 是否继续执行 task 的完成回调
	 */
	boolean onOnComplete(HttpTask<?> task, State state);
	
}
