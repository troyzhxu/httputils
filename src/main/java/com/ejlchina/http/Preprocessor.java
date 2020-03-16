package com.ejlchina.http;

/**
 * 预处理器，支持异步
 * @author Troy.Zhou
 */
public interface Preprocessor {

	
	void beforeReqest(Process process);
	
	
	interface Process {
		
		HttpTask<? extends HttpTask<?>> getHttpTask();
		
		HttpClient getHttpClient();
		
		void proceed();
		
	}
	
}
