package com.ejlchina.http;

/**
 * 预处理器，支持异步
 * 在HTTP请求任务正式开始之前执行
 * @author Troy.Zhou
 */
public interface Preprocessor {

	/**
	 * 在HTTP请求开始之前执行
	 * @param process HTTP请求进程
	 * @return true: 该处理器可以并行处理请求任务，false: 该处理器只能串行处理请求任务
	 */
	boolean doProcess(Process process);
	
	
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
