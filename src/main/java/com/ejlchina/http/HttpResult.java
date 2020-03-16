package com.ejlchina.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Http 请求结果
 *
 */
public class HttpResult {

	private State state;
	private Response response;
	private Exception error;
	
	
	public static enum State {
		
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
	
	
	HttpResult(State state, Exception error) {
		this.state = state;
		this.error = error;
	}
	
	HttpResult(State state, Response response) {
		this.state = state;
		this.response = response;
	}
	
	/**
	 * @return 执行状态
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return HTTP状态码
	 */
	public int getStatus() {
		return response.code();
	}

	/**
	 * 
	 * @return 是否返回成功，状态码在 [200..300) 之间
	 */
	public boolean isSuccessful() {
	    return response.isSuccessful();
	}
	
	/**
	 * @return 是否是重定向（300、301、302、303、307、308）
	 */
	public boolean isRedirect() {
		return response.isRedirect();
	}
	
	/**
	 * @return 返回头信息
	 */
	public Headers getHeaders() {
		return response.headers();
	}

	/**
	 * @return 请求结果报文体
	 */
	public ResultBody getBody() {
		return new ResultBody(response.body());
	}
	
	
	public class ResultBody {
		
		private ResponseBody body;

		ResultBody(ResponseBody body) {
			this.body = body;
		}

		public MediaType getContentType() {
			return body.contentType();
		}
		
		public long getContentLength() {
			return body.contentLength();
		}
		
		public InputStream toByteStream() {
			return body.byteStream();
		}
		
		public byte[] toBytes() {
			try {
				return body.bytes();
			} catch (IOException e) {
				throw new HttpException("报文体转化字节数组出错", e);
			}
		}
		
		public Reader toCharReader() {
			return body.charStream();
		}
		
		public String toString() {
			try {
				return body.string();
			} catch (IOException e) {
				throw new HttpException("报文体转化字符串出错", e);
			}
		}
		
		public <T> T toBean(Class<T> type) {
			return JSON.parseObject(toString(), type);
		}
		
		public <T> T toBean(TypeReference<T> typeRef) {
			return JSON.parseObject(toString(), typeRef.getType());
		}
		
		public File toFile(String filePath) {
			return toFile(new File(filePath));
		}
		
		public File toFile(File file) {
			if (file.exists() && !file.delete()) {
				throw new HttpException(
						"Destination file [" + file.getAbsolutePath() + "] already exists and could not be deleted");
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new HttpException(
						"Cannot create file [" + file.getAbsolutePath() + "]");
			}
			OutputStream output;
			try {
				output = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				throw new HttpException("无法获取文件[" + file.getAbsolutePath() + "]的输入流", e);
			}
			InputStream input = body.byteStream();
			
			
			
			
			return file;
		}
		
		
	}
	
	
	/**
	 * @return 请求中发生的异常
	 */
	public Exception getError() {
		return error;
	}
	
}
