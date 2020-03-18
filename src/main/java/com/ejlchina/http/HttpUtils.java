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
	
	
	private static HTTP http;
	
	
	public static void of(HTTP http) {
		HttpUtils.http = http;
	}
	
	
	static HTTP getHttp() {
		if (http != null) {
			return http;
		}
		synchronized (HttpUtils.class) {
			if (http != null) {
				return http;
			}
			http = HTTP.builder().build();
			return http;
		}
	}
	
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @return 异步 HttpClient
	 */
    public static AsyncHttpTask async(String urlPath) {
    	return getHttp().async(urlPath);
    }

	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @return 同步 HttpClient
	 */
    public static SyncHttpTask sync(String urlPath) {
    	return getHttp().sync(urlPath);
    }
    
    /**
     * 根据标签取消HTTP任务
     * @param tag 标签
     * @return 取消的任务数量
     */
    public static int cancel(String tag) {
    	return getHttp().cancel(tag);
    }
    
}
