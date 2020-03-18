package com.ejlchina.test;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.ejlchina.http.HTTP;
import com.ejlchina.http.HttpCall;
import com.ejlchina.http.Preprocessor.Process;
import com.ejlchina.http.internal.HttpClient;
import com.ejlchina.http.HttpResult;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;


public class HttpTest {

	@Test
	public void testIterator() {
		List<Integer> list = new ArrayList<>();
		list.add(0);
		list.add(1);
		list.add(2);
		list.add(3);
		
		Iterator<Integer> it = list.iterator();
		
		it.next();
		it.next();
		it.remove();
		
		System.out.println(list);
		
	}
	
	@Test
	public void testPreprocessor() {
		
		HTTP http = HTTP.builder()
			.baseUrl("http://localhost:8080")
			.addPreprocessor((Process process) -> {
				System.out.println("并行预处理-开始");
//				new Thread(() -> {
					sleep(2000);
					System.out.println("并行预处理-结束");
					process.proceed();
//				}).start();
			})
			.addSerialPreprocessor((Process process) -> {
				System.out.println("串行预处理-开始");
				new Thread(() -> {
					sleep(3000);
					System.out.println("串行预处理-结束");
					process.proceed();
				}).start();
			})
			.build();
		
		
		new Thread(() -> {
			http.async("/user/show/1")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				}).get();
		}).start();
		
		new Thread(() -> {
			http.async("/user/show/2")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				}).get();
		}).start();
		
		sleep(10000);
	}
	


	@Test
	public void testCancel() {
		
		HTTP http = HTTP.builder()
			.baseUrl("http://localhost:8080")
			.build();
		
		HttpCall call = http.async("/user/show/1")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.tag("A")
				.get();

		System.out.println(((HttpClient) http).getTagCallCount());
		
		http.async("/user/show/2")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.tag("A")
				.get();
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		http.async("/user/show/3")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.tag("A")
				.get();
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		http.async("/user/show/4")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.tag("B")
				.get();
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		System.out.println("单独取消：" + call.cancel());

		((HttpClient) http).getTagCallCount();
		
		System.out.println("标签取消：" + http.cancel("B"));
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		sleep(2000);

		System.out.println(((HttpClient) http).getTagCallCount());
//		System.out.println("isDone = " + call.isDone());
//		System.out.println("isCanceled = " + call.isCanceled());
//		
//		System.out.println("取消结果 = " + call.cancel());
//		
//		System.out.println("isDone = " + call.isDone());
//		System.out.println("isCanceled = " + call.isCanceled());
//		
//		sleep(100);
//		System.out.println("++++++++");
//		
//		System.out.println("isDone = " + call.isDone());
//		System.out.println("isCanceled = " + call.isCanceled());
	}
	

	
	
	private HTTP buildHttp() {
		
		return HTTP.builder()
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
						process.getTask().addHeader("Token", "yyyyy");
				
						process.proceed();
						
					}).start();
				})
				.addPreprocessor((Process process) -> {
					new Thread(() -> {

						process.getTask().addUrlParam("actor", "Alice");
				
						process.proceed();
				
					}).start();
				})
				.build();
		
	}
	

	@Test
	public void syncHttpExample() {
		
		HTTP http = buildHttp();
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
		HTTP http = buildHttp();
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
		HTTP http = buildHttp();
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

	
	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
