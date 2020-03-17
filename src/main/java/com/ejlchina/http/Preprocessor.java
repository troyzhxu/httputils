package com.ejlchina.http;

/**
 * 预处理器，支持异步
 * @author Troy.Zhou
 */
public interface Preprocessor {

	/**
	 * 在HTTP请求开始之前执行
	 * @param process HTTP请求进程
	 */
	void beforeReqest(Process process);
	
	
	interface Process {
		
		/**
		 * @return 当前的请求任务
		 */
		HttpTask<? extends HttpTask<?>> getTask();
		
		/**
		 * @return HttpClient
		 */
		HttpClient getClient();
		
		/**
		 * 继续HTTP请求任务
		 */
		void proceed();
		
	}
	
}
