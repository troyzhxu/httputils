package com.ejlchina.test;


import java.util.concurrent.TimeUnit;

import com.ejlchina.http.HttpCall;
import com.ejlchina.http.HttpClient;
import com.ejlchina.http.HttpResult;
import com.ejlchina.http.HttpUtils;

import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.OkHttpClient.Builder;


public class HttpTest {

	
	public static void main(String[] args) throws InterruptedException {
		
		// 全局配置
		configHttpClient();
		
		// 同步请求示例
		syncHttpExample();
		
		// 异步请求示例
		asyncHttpExample();
		
		
	}


	private static void configHttpClient() {
		
		// HttpClient 全局配置
		HttpClient.config((Builder builder) -> {
			
			// 配置连接池 最小10个连接（不配置默认为 5）
			builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
			
			// 配置连接超时时间
			builder.connectTimeout(20, TimeUnit.SECONDS);

			// 其它配置: 拦截器、SSL、缓存、代理...
		});
		
		// 配置 BaseUrl
		HttpClient.setBaseUrl("http://api.demo.com");
		
		// 配置回调函数执行器
		HttpClient.setExecutor((Runnable callback) -> {
		
			// 配置所有回调再UI线程执行
			runOnUiThread(() -> {
				callback.run();
			});
		});

	}
	


	private static void syncHttpExample() {
		
		// 同步请求
		// 最终路径 http://api.demo.com/users/1
		HttpResult<User, ?> result = HttpUtils.sync("/users/{id}", User.class)
				// 设置路径参数
				.addPathParam("id", 1)
				// 发起  GET 请求
				.get();
		
		// 得到状态码
		int status = result.getStatus();

		// 得到返回头
		Headers headers = result.getHeaders();

		// 得到目标数据
		User user = result.getOkData();

		
		System.out.println("status = " + status);
		System.out.println("headers = " + headers);
		System.out.println("user = " + user);
		
	}



	private static void asyncHttpExample() throws InterruptedException {
		
		// 异步请求
		// 最终路径 http://api.demo.com/users/2
		HttpCall call = HttpUtils.async("/users/{id}", User.class)
				// 设置路径参数
				.addPathParam("id", 2)
				// 设置回调函数
				.setOnSuccess((int status, Headers headers, User user) -> {
					// 接收到解析好的 user 对象

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
