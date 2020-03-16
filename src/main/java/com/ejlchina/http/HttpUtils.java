package com.ejlchina.http;

/**
 * Http 工具类，封装 OkHttp

 * 特性： 
 *   同步请求
 *   异步请求
 *   Restfull路径
 *   文件上传
 *   JSON自动解析
 *   TCP连接池
 *   Http2
 *   
 * @author Troy.Zhou
 */
public class HttpUtils {
	
	
	private static HttpClient httpClient;
	
	
	public static void of(HttpClient httpClient) {
		HttpUtils.httpClient = httpClient;
	}
	
	
	static HttpClient getHttpClient() {
		if (httpClient != null) {
			return httpClient;
		}
		synchronized (HttpUtils.class) {
			if (httpClient != null) {
				return httpClient;
			}
			httpClient = HttpClient.builder().build();
			return httpClient;
		}
	}
	
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @return 异步 HttpClient
	 */
    public static AsyncHttpTask async(String urlPath) {
    	return getHttpClient().async(urlPath);
    }

	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @return 同步 HttpClient
	 */
    public static SyncHttpTask sync(String urlPath) {
    	return getHttpClient().sync(urlPath);
    }
    
}
