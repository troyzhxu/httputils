package com.ejlchina.http;

import com.alibaba.fastjson.TypeReference;

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
 * @since 0.3.4
 */
public class HttpUtils {
	
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 */
    public static <S, F> AsyncHttpClient<S, F> async(String urlPath) {
        return new AsyncHttpClient<>(urlPath, null, null);
    }
    
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @param okType 请求成功时返回的数据类型
	 */
    public static <S, F> AsyncHttpClient<S, F> async(String urlPath, Class<S> okType) {
        return new AsyncHttpClient<>(urlPath, okType, null);
    }
    
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @param okTypeRef 请求成功时返回的数据类型
	 */
    public static <S, F> AsyncHttpClient<S, F> async(String urlPath, TypeReference<S> okTypeRef) {
        return new AsyncHttpClient<>(urlPath, okTypeRef.getType(), null);
    }
    
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @param okType 请求成功时返回的数据类型
	 * @param failType 请求失败时返回的数据类型
	 */
    public static <S, F> AsyncHttpClient<S, F> async(String urlPath, Class<S> okType, Class<F> failType) {
        return new AsyncHttpClient<>(urlPath, okType, failType);
    }
    
	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @param okTypeRef 请求成功时返回的数据类型
	 * @param failTypeRef 请求失败时返回的数据类型
	 */
    public static <S, F> AsyncHttpClient<S, F> async(String urlPath, TypeReference<S> okTypeRef, TypeReference<F> failTypeRef) {
        return new AsyncHttpClient<>(urlPath, okTypeRef.getType(), failTypeRef.getType());
    }

	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @param okTypeRef 请求成功时返回的数据类型
	 * @param failType 请求失败时返回的数据类型
	 */
    public static <S, F> AsyncHttpClient<S, F> async(String urlPath, TypeReference<S> okTypeRef, Class<F> failType) {
        return new AsyncHttpClient<>(urlPath, okTypeRef.getType(), failType);
    }

	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 */
    public static <S, F> SyncHttpClient<S, F> sync(String urlPath) {
        return new SyncHttpClient<>(urlPath, null, null);
    }
    
	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @param okType 请求成功时返回的数据类型
	 */
    public static <S, F> SyncHttpClient<S, F> sync(String urlPath, Class<S> okType) {
        return new SyncHttpClient<>(urlPath, okType, null);
    }
    
	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @param okTypeRef 请求成功时返回的数据类型
	 */
    public static <S, F> SyncHttpClient<S, F> sync(String urlPath, TypeReference<S> okTypeRef) {
        return new SyncHttpClient<>(urlPath, okTypeRef.getType(), null);
    }
    
	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @param okType 请求成功时返回的数据类型
	 * @param failType 请求失败时返回的数据类型
	 */
    public static <S, F> SyncHttpClient<S, F> sync(String urlPath, Class<S> okType, Class<F> failType) {
        return new SyncHttpClient<>(urlPath, okType, failType);
    }
    
	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @param okTypeRef 请求成功时返回的数据类型
	 * @param failTypeRef 请求失败时返回的数据类型
	 */
    public static <S, F> SyncHttpClient<S, F> sync(String urlPath, TypeReference<S> okTypeRef, TypeReference<F> failTypeRef) {
        return new SyncHttpClient<>(urlPath, okTypeRef.getType(), failTypeRef.getType());
    }

	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @param okTypeRef 请求成功时返回的数据类型
	 * @param failTypeRef 请求失败时返回的数据类型
	 */
    public static <S, F> SyncHttpClient<S, F> sync(String urlPath, TypeReference<S> okTypeRef, Class<F> failType) {
        return new SyncHttpClient<>(urlPath, okTypeRef.getType(), failType);
    }
    
}
