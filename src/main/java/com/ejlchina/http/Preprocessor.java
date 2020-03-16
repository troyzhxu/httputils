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
		HttpTask<? extends HttpTask<?>> getHttpTask();
		
		/**
		 * @return HttpClient
		 */
		HttpClient getHttpClient();
		
		/**
		 * 继续HTTP请求任务
		 */
		void proceed();
		
	}
	
}
