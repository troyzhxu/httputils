package com.ejlchina.http;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import okhttp3.Headers;
import okhttp3.MediaType;


/**
 * Http 执行结果
 */
public interface HttpResult {


	public enum State {
		
		/**
		 * 执行异常
		 */
	    EXCEPTION,
	    
	    /**
	     * 请求被取消
	     */
	    CANCELED,
	    
	    /**
	     * 请求已响应
	     */
	    RESPONSED,
	    
	    /**
	     * 网络超时
	     */
	    TIMEOUT,
	    
	    /**
	     * 网络出错
	     */
	    NETWORK_ERROR
		
	}
	
	/**
	 * HTTP响应报文体
	 */
	public interface Body {
		
		/**
		 * @return 媒体类型
		 */
		MediaType getContentType();
		
		/**
		 * @return 报文体字节长度
		 */
		long getContentLength();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字节流
		 */
		InputStream toByteStream();
		
		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字节数组
		 */
		byte[] toBytes();
		
		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字符流
		 */
		Reader toCharStream();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字符串
		 */
		String toString();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转Json对象
		 */
		JSONObject toJsonObject();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转Json数组
		 */
		JSONArray toJsonArray();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param <T> 目标泛型
		 * @param type 目标类型
		 * @return 报文体Json文本转JavaBean
		 */
		<T> T toBean(Class<T> type);

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param <T> 目标泛型
		 * @param typeRef 目标类型
		 * @return 报文体Json文本转JavaBean
		 */
		<T> T toBean(TypeReference<T> typeRef);

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param filePath 目标路径
		 * @return 报文体保存到指定路径的文件
		 */
		File toFile(String filePath);

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param file 目标文件
		 * @return 报文体保存到指定文件并返回
		 */
		File toFile(File file);
		
	}
	

	/**
	 * @return 执行状态
	 */
	State getState();

	/**
	 * @return HTTP状态码
	 */
	int getStatus();

	/**
	 * @return 是否响应成功，状态码在 [200..300) 之间
	 */
	boolean isSuccessful();
	
	/**
	 * @return 响应头信息
	 */
	Headers getHeaders();

	/**
	 * @return 响应报文体
	 */
	Body getBody();
	
	/**
	 * @return 执行中发生的异常
	 */
	Exception getError();
	
}
