package com.ejlchina.http;

/**
 * 任务监听接口
 * @author 15735
 * @since 2.3.0
 */
public interface TaskListener<T> {

	
	void on(HttpTask<?> task, T data);

	
}
