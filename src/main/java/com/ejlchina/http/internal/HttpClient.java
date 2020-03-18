package com.ejlchina.http.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;

import com.ejlchina.http.Configurator;
import com.ejlchina.http.HTTP;
import com.ejlchina.http.HttpCall;
import com.ejlchina.http.HttpTask;
import com.ejlchina.http.Preprocessor;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpClient implements HTTP {

	
	private OkHttpClient client;
	
	private String baseUrl;
	// 媒体类型
	private Map<String, String> mediaTypes;
	// 回调执行器
	private Executor callbackExecutor;
	// 预处理器
	private Preprocessor[] preprocessors;

	private List<TagCall> tagCalls;
	
	
	private HttpClient(Builder builder) {
		this.client = builder.client;
		this.baseUrl = builder.baseUrl;
		this.mediaTypes = builder.mediaTypes;
		this.callbackExecutor = builder.callbackExecutor;
		this.preprocessors = builder.preprocessors.toArray(new Preprocessor[builder.preprocessors.size()]);
		this.tagCalls = Collections.synchronizedList(new LinkedList<>());
	}
	
	@Override
    public AsyncHttpTask async(String urlPath) {
        return new AsyncHttpTask(this, urlPath(urlPath));
    }

	@Override
    public SyncHttpTask sync(String urlPath) {
        return new SyncHttpTask(this, urlPath(urlPath));
    }
   
	@Override
	public int cancel(String tag) {
		if (tag == null) {
			return 0;
		}
		int count = 0;
		Iterator<TagCall> it = tagCalls.iterator();
		while (it.hasNext()) {
			TagCall tagCall = it.next();
			if (tag.equals(tagCall.tag)) {
				if (tagCall.call.cancel()) {
					count++;
				}
				it.remove();
			}
		}
		return count;
	}
    
	public int getTagCallCount() {
		return tagCalls.size();
	}
	
	public void addTagCall(String tag, HttpCall call, HttpTask<?> task) {
		tagCalls.add(new TagCall(tag, call, task));
	}
	
	public void removeTagCall(HttpTask<?> task) {
		Iterator<TagCall> it = tagCalls.iterator();
		while (it.hasNext()) {
			TagCall tagCall = it.next();
			if (tagCall.task == task) {
				it.remove();
				break;
			}
		}
	}
	
	class TagCall {
		
		String tag;
		HttpCall call;
		HttpTask<?> task;
		
		public TagCall(String tag, HttpCall call, HttpTask<?> task) {
			this.tag = tag;
			this.call = call;
			this.task = task;
		}
		
	}
	
	public Call callRequest(Request request) {
    	return client.newCall(request);
    }
    
	public MediaType getMediaType(String type) {
        String mediaType = mediaTypes.get(type);
        if (mediaType != null) {
            return MediaType.parse(mediaType);
        }
        return MediaType.parse("application/octet-stream");
    }

	public void executeCallback(Runnable callback) {
    	if (callbackExecutor != null) {
    		callbackExecutor.execute(callback);
    	} else {
    		callback.run();
    	}
    }

	public void preprocess(HttpTask<? extends HttpTask<?>> httpTask, Runnable request) {
    	if (preprocessors.length > 0) {
    		HttpProcess process = new HttpProcess(preprocessors, 
    				httpTask, request);
    		preprocessors[0].doProcess(process);
    	} else {
    		request.run();
    	}
    }
    
    /**
     * 串行预处理器
     * @author Troy.Zhou
     */
    static class SerialPreprocessor implements Preprocessor {
    	
    	// 预处理器
    	private Preprocessor preprocessor;
    	// 待处理的任务队列
    	private Queue<Process> pendings;
    	// 是否有任务正在执行
    	private boolean running = false;
    	
		SerialPreprocessor(Preprocessor preprocessor) {
			this.preprocessor = preprocessor;
			this.pendings = new LinkedList<>();
		}

		@Override
		public void doProcess(Process process) {
			boolean should = true;
			synchronized (this) {
				if (running) {
					pendings.add(process);
					should = false;
				} else {
					running = true;
				}
			}
			if (should) {
				preprocessor.doProcess(process);
			}
		}
		
		public void afterProcess() {
			Process process = null;
			synchronized (this) {
				if (pendings.size() > 0) {
					process = pendings.poll();
				} else {
					running = false;
				}
			}
			if (process != null) {
				preprocessor.doProcess(process);
			}
		}
    	
    }
    
    
    class HttpProcess implements Preprocessor.Process {

    	private int index;
    	
    	private Preprocessor[] preprocessors;
    	
    	private HttpTask<? extends HttpTask<?>> httpTask;
    	
    	private Runnable request;
    	
		public HttpProcess(Preprocessor[] preprocessors, 
				HttpTask<? extends HttpTask<?>> httpTask, 
						Runnable request) {
			this.index = 1;
			this.preprocessors = preprocessors;
			this.httpTask = httpTask;
			this.request = request;
		}

		@Override
		public HttpTask<? extends HttpTask<?>> getTask() {
			return httpTask;
		}

		@Override
		public HTTP getHttp() {
			return HttpClient.this;
		}

		@Override
		public void proceed() {
			if (index > 0) {
				Preprocessor last = preprocessors[index - 1];
				if (last instanceof SerialPreprocessor) {
					((SerialPreprocessor) last).afterProcess();
				}
			}
			if (index < preprocessors.length) {
				preprocessors[index++].doProcess(this);
			} else {
				request.run();
			}
		}

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
		throw new HttpException("在设置 BaseUrl 之前，您必须使用全路径URL发起请求，当前URL为：" + urlPath);
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
	     * 配置 OkHttpClient
	     * @param configurator 配置器
	     * @return Builder
	     */
		public Builder config(Configurator configurator) {
			this.configurator = configurator;
			return this;
		}
		
	    /**
	     * 设置 baseUrl
	     * @param baseUrl 全局URL前缀
	     * @return Builder
	     */
		public Builder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}
		
		/**
		 * 配置媒体类型
		 * @param mediaTypes 媒体类型
		 * @return Builder
		 */
		public Builder mediaTypes(Map<String, String> mediaTypes) {
			this.mediaTypes.putAll(mediaTypes);
			return this;
		}
		
		/**
		 * 配置媒体类型
		 * @param key 媒体类型KEY
		 * @param value 媒体类型VALUE
		 * @return Builder
		 */
		public Builder mediaTypes(String key, String value) {
			this.mediaTypes.put(key, value);
			return this;
		}
		
	    /**
	     * 设置回调执行器，例如实现切换线程功能，只对异步请求有效
	     * @param executor 回调执行器
	     * @return Builder
	     */
		public Builder callbackExecutor(Executor executor) {
			this.callbackExecutor = executor;
			return this;
		}
		
		/**
		 * 添加可并行处理请求任务的预处理器
		 * @param preprocessor 预处理器
		 * @return Builder
		 */
		public Builder addPreprocessor(Preprocessor preprocessor) {
			preprocessors.add(preprocessor);
			return this;
		}
		
		/**
		 * 添加预处理器
		 * @param preprocessor 预处理器
		 * @return Builder
		 */
		public Builder addSerialPreprocessor(Preprocessor preprocessor) {
			preprocessors.add(new SerialPreprocessor(preprocessor));
			return this;
		}
		
		public HTTP build() {
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
