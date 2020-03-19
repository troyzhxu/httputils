package com.ejlchina.http;

import com.ejlchina.http.internal.AsyncHttpTask;
import com.ejlchina.http.internal.SyncHttpTask;

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
	
	
	static synchronized HTTP getHttp() {
		if (http != null) {
			return http;
		}
		http = HTTP.builder().build();
		return http;
	}
	
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @return 异步 HttpTask
	 */
    public static AsyncHttpTask async(String urlPath) {
    	return getHttp().async(urlPath);
    }

	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @return 同步 HttpTask
	 */
    public static SyncHttpTask sync(String urlPath) {
    	return getHttp().sync(urlPath);
    }
    
    /**
     * 根据标签取消HTTP任务，只要任务的标签包含指定的Tag就会被取消
     * @param tag 标签
     * @return 被取消的任务数量
     */
    public static int cancel(String tag) {
    	return getHttp().cancel(tag);
    }
    
}
