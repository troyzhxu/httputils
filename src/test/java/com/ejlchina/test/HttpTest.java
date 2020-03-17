package com.ejlchina.test;


import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.ejlchina.http.HttpCall;
import com.ejlchina.http.HttpClient;
import com.ejlchina.http.HttpResult;
import com.ejlchina.http.Preprocessor.Process;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;


public class HttpTest {


	private HttpClient buildHttpClient() {
		
		return HttpClient.builder()
				.config((Builder builder) -> {
					
					// 配置连接池 最小10个连接（不配置默认为 5）
					builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
					
					// 配置连接超时时间
					builder.connectTimeout(20, TimeUnit.SECONDS);
					
					builder.addInterceptor((Chain chain) -> {
						
						Request request = chain.request();

						return chain.proceed(request);
					});
					
		
				})
				.baseUrl("http://localhost:8080")
				.callbackExecutor((Runnable run) -> {
					runOnUiThread(run);
				})
				.addPreprocessor((Process process) -> {
					new Thread(() -> {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						process.getHttpTask().addHeader("Token", "yyyyy");
				
						process.proceed();
				
					}).start();
					
				})
				.addPreprocessor((Process process) -> {
					new Thread(() -> {

						process.getHttpTask().addUrlParam("actor", "Alice");
				
						process.proceed();
				
					}).start();
					
				})
				.build();
		
	}
	

	@Test
	public void syncHttpExample() {
		
		HttpClient http = buildHttpClient();
		// 同步请求
		HttpResult result = http.sync("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584365400942&di=d9e7890f13b7bc4b76080fdd490ed5d5&imgtype=0&src=http%3A%2F%2Ft8.baidu.com%2Fit%2Fu%3D1484500186%2C1503043093%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D1280%26h%3D853")
				.get();
		
		result.getBody().toFile("E:/3.jpg");
//		// 得到状态码
//		int status = result.getStatus();
//
//		// 得到返回头
//		Headers headers = result.getHeaders();
//
//		User user = result.getBody().toBean(User.class);
//		// 得到目标数据
//
//		
//		System.out.println("status = " + status);
//		System.out.println("headers = " + headers);
//		System.out.println("user = " + user);
		
		System.out.println("status = " + result.getStatus());
		
	}


	@Test
	public void syncJsonExample() {
		HttpClient http = buildHttpClient();
		// 同步请求
		HttpResult result = http.sync("/user/save")
				.addJsonParam("name", "Tom")
				.addJsonParam("age", 23)
				.post();
		
		System.out.println("result = " + result);
		System.out.println("isSuccessful = " + result.isSuccessful());
	}

	@Test
	public void asyncHttpExample() throws InterruptedException {
		HttpClient http = buildHttpClient();
		// 异步请求
		// 最终路径 http://api.demo.com/users/2
		HttpCall call = http.async("/user/show/{id}")
				// 设置路径参数
				.addPathParam("id", 2)
				// 设置回调函数
				.setOnResponse((HttpResult result) -> {
					System.out.println("000");
//					User user = result.getBody().toBean(User.class);
//					System.out.println("user = " + user);
				})
				.setOnException((Exception e) -> {
					e.printStackTrace();
				})
				// 发起  GET 请求
				.get();
		
		Thread.sleep(150);
		
		System.out.println("是否完成: " + call.isDone());
		System.out.println("是否取消: " + call.isCanceled());
		
		call.cancel();  // 取消请求
		
		System.out.println("是否取消: " + call.isCanceled());
		System.out.println("执行结果: " + call.getResult());
		System.out.println("是否完成: " + call.isDone());
//		
		Thread.sleep(100);
//		
//		System.out.println("是否完成: " + call.isDone());
//		System.out.println("是否取消: " + call.isCanceled());
//		System.out.println("执行状态: " + call.getResult());
		
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
