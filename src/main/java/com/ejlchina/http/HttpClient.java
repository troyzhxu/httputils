package com.ejlchina.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpClient {

	
	private OkHttpClient client;
	
	private String baseUrl;
	
	private Map<String, String> mediaTypes;

	private Executor callbackExecutor;

	private Preprocessor[] preprocessors;
	
	
	private HttpClient(Builder builder) {
		this.client = builder.client;
		this.baseUrl = builder.baseUrl;
		this.mediaTypes = builder.mediaTypes;
		this.callbackExecutor = builder.callbackExecutor;
		this.preprocessors = builder.preprocessors.toArray(new Preprocessor[builder.preprocessors.size()]);
	}
	

	/**
	 * 异步请求
	 * @param urlPath 请求地址
	 * @return 异步 HttpClient
	 */
    public AsyncHttpTask async(String urlPath) {
        return new AsyncHttpTask(this, urlPath(urlPath));
    }

	/**
	 * 同步请求
	 * @param urlPath 请求地址
	 * @return 同步 HttpClient
	 */
    public SyncHttpTask sync(String urlPath) {
        return new SyncHttpTask(this, urlPath(urlPath));
    }
   
    
    Call callRequest(Request request) {
    	return client.newCall(request);
    }
    
    MediaType getMediaType(String type) {
        String mediaType = mediaTypes.get(type);
        if (mediaType != null) {
            return MediaType.parse(mediaType);
        }
        return MediaType.parse("application/octet-stream");
    }

    void executeCallback(Runnable callback) {
    	if (callbackExecutor != null) {
    		callbackExecutor.execute(callback);
    	} else {
    		callback.run();
    	}
    }
    
    
    void preprocess(HttpTask<? extends HttpTask<?>> httpTask, Runnable request) {
    	if (preprocessors.length > 0) {
			preprocessors[0].beforeReqest(new HttpProcess(preprocessors, 
    				httpTask, request));
    	} else {
    		request.run();
    	}
    }
    
    
    class HttpProcess implements Preprocessor.Process {

    	private int index;
    	
    	private Preprocessor[] preprocessors;
    	
    	private HttpTask<? extends HttpTask<?>> httpTask;
    	
    	private Runnable request;
    	
		public HttpProcess(Preprocessor[] preprocessors, 
				HttpTask<? extends HttpTask<?>> httpTask, Runnable request) {
			this.index = 1;
			this.preprocessors = preprocessors;
			this.httpTask = httpTask;
			this.request = request;
		}

		@Override
		public HttpTask<? extends HttpTask<?>> getHttpTask() {
			return httpTask;
		}

		@Override
		public HttpClient getHttpClient() {
			return HttpClient.this;
		}

		@Override
		public void proceed() {
			if (index < preprocessors.length) {
				Preprocessor preprocessor = preprocessors[index++];
				preprocessor.beforeReqest(this);
			} else {
				request.run();
			}
		}

    }
    
    
	public static Builder builder() {
		return new Builder();
	}
	
	public Builder newBuilder() {
		return new Builder(this);
	}
	
	
	private String urlPath(String urlPath) {
		boolean isFullPath = urlPath.startsWith("https://") 
    			|| urlPath.startsWith("http://");
		if (isFullPath) {
			return urlPath;
		}
		if (baseUrl != null) {
			return baseUrl + urlPath;
		}
		throw new HttpException("在设置 BaseUrl 之前，您必须使用全路径URL发起请求，当前URL为：" + urlPath 
				+ "\n若要设置 BaseUrl，请使用 HttpClient.setBaseUrl() 方法");
	}

    
    
	public static class Builder {
		
		private OkHttpClient client;
		
		private String baseUrl;
		
		private Map<String, String> mediaTypes;
		
		private Configurator configurator;
		
		private Executor callbackExecutor;

		private List<Preprocessor> preprocessors;
		

		public Builder() {
			mediaTypes = new HashMap<>();
        	mediaTypes.put("*", "application/octet-stream");
        	mediaTypes.put("png", "image/png");
        	mediaTypes.put("jpg", "image/jpeg");
        	mediaTypes.put("jpeg", "image/jpeg");
        	mediaTypes.put("wav", "audio/wav");
        	mediaTypes.put("mp3", "audio/mp3");
        	mediaTypes.put("mp4", "video/mpeg4");
        	mediaTypes.put("txt", "text/plain");
        	mediaTypes.put("xls", "application/x-xls");
        	mediaTypes.put("xml", "text/xml");
        	mediaTypes.put("apk", "application/vnd.android.package-archive");
        	mediaTypes.put("doc", "application/msword");
        	mediaTypes.put("pdf", "application/pdf");
        	mediaTypes.put("html", "text/html");
        	preprocessors = new ArrayList<>();
		}
		
		private Builder(HttpClient hc) {
			this.client = hc.client; 
			this.baseUrl = hc.baseUrl;
			this.mediaTypes = hc.mediaTypes;
			this.preprocessors = new ArrayList<>();
			Collections.addAll(this.preprocessors, hc.preprocessors);
		}
		
	    /**
	     * 配置 httpClient
	     * @param configurator 配置器
	     */
		public Builder config(Configurator configurator) {
			this.configurator = configurator;
			return this;
		}
		
	    /**
	     * 设置 baseUrl
	     * @param baseUrl 全局URL前缀
	     */
		public Builder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}
		
		public Builder mediaTypes(Map<String, String> mediaTypes) {
			this.mediaTypes.putAll(mediaTypes);
			return this;
		}
		
		public Builder mediaTypes(String key, String value) {
			this.mediaTypes.put(key, value);
			return this;
		}
		
	    /**
	     * 设置回调执行器，例如实现切换线程功能，只对异步请求有效
	     * @param executor 回调执行器
	     */
		public Builder callbackExecutor(Executor callbackExecutor) {
			this.callbackExecutor = callbackExecutor;
			return this;
		}
		
		/**
		 * 添加预处理器
		 * @param preprocessor 预处理器
		 * @return Builder
		 */
		public Builder addPreprocessor(Preprocessor preprocessor) {
			if (preprocessors == null) {
				preprocessors = new ArrayList<>();
			}
			preprocessors.add(preprocessor);
			return this;
		}
		
		public HttpClient build() {
			if (configurator != null || client == null) {
				OkHttpClient.Builder builder = new OkHttpClient.Builder();
				if (configurator != null) {
					configurator.config(builder);
				}
		    	client = builder.build();
			}
			return new HttpClient(this);
		}
		
	}
	
}
