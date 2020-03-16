package com.ejlchina.test;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ejlchina.http.HttpCall;
import com.ejlchina.http.HttpClient;
import com.ejlchina.http.HttpResult;

import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.OkHttpClient.Builder;


public class HttpTest {

	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		
		HttpClient http = buildHttpClient();
		
		
		// 同步请求示例
		syncHttpExample(http);
		
		// 异步请求示例
		asyncHttpExample(http);
		
	}


	static HttpClient buildHttpClient() {
		
		return HttpClient.builder()
				.config((Builder builder) -> {
					
					// 配置连接池 最小10个连接（不配置默认为 5）
					builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
					
					// 配置连接超时时间
					builder.connectTimeout(20, TimeUnit.SECONDS);
		
				})
				.baseUrl("http://api.demo.com")
				.callbackExecutor((Runnable run) -> {
					runOnUiThread(run);
				})
				.build();
		
	}
	


	private static void syncHttpExample(HttpClient http) {
		
		// 同步请求
		// 最终路径 http://api.demo.com/users/1
		HttpResult result = http.sync("/users/{id}")
				// 设置路径参数
				.addPathParam("id", 1)
				// 发起  GET 请求
				.get();
		
		// 得到状态码
		int status = result.getStatus();

		// 得到返回头
		Headers headers = result.getHeaders();

		User user = result.getBody().toBean(User.class);
		// 得到目标数据

		
		System.out.println("status = " + status);
		System.out.println("headers = " + headers);
		System.out.println("user = " + user);
		
	}



	private static void asyncHttpExample(HttpClient http) throws InterruptedException {
		
		// 异步请求
		// 最终路径 http://api.demo.com/users/2
		HttpCall call = http.async("/users/{id}")
				// 设置路径参数
				.addPathParam("id", 2)
				// 设置回调函数
				.setOnResponse((HttpResult result) -> {
					User user = result.getBody().toBean(User.class);
				//	User user = result.bodyToBean(User.class);
					// 接收到解析好的 user 对象
					System.out.println("user = " + user);
				})
				// 发起  GET 请求
				.get();
 
		System.out.println("是否完成: " + call.isDone());
		System.out.println("是否取消: " + call.isCanceled());
		
//		call.cancel();  // 取消请求
		Thread.sleep(350);
		
		System.out.println("是否完成: " + call.isDone());
		System.out.println("是否取消: " + call.isCanceled());
		
	}


	static void runOnUiThread(Runnable run) {
		run.run();
	}
	
	
	static class User {
		
		int id;
		String name;
		
		public void setId(int id) {
			this.id = id;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return "User [id=" + id + ", name=" + name + "]";
		}
		
	}
	
}
