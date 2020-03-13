package com.ejlchina.http;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * 异步 Http 客户端
 *  
 * @author Troy.Zhou
 *
 * @param <S> 请求成功时返回的数据类型
 * @param <F> 请求失败时返回的数据类型
 * 
 */
public class AsyncHttpClient<S, F> extends HttpClient<S, F, AsyncHttpClient<S, F>> {

	protected static Executor executor;
	
    private OnCallback<S> onSuccess;
    private OnCallback<F> onFailure;
    private OnException onException;
    private OnComplete onComplete;
    private OnCallback<ResponseBody> onResponse;

	public AsyncHttpClient(String urlPath, Type okType, Type failType) {
		super(urlPath, okType, failType);
	}

	/**
	 * 设置响应成功（HTTP状态码在[200, 300)之间）的回调函数
	 * 设置了OnSuccess，就不可以再设置 OnResponse 回调
	 * @param onSuccess 请求成功回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpClient<S, F> setOnSuccess(OnCallback<S> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

	/**
	 * 设置响应失败（HTTP状态码在[200, 300)之外）的回调函数
	 * 设置了OnFailure，就不可以再设置 OnResponse 回调
	 * @param onFailure 请求失败回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpClient<S, F> setOnFailure(OnCallback<F> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

	/**
	 * 设置请求异常后的回调函数
	 * @param onException 请求异常回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpClient<S, F> setOnException(OnException onException) {
        this.onException = onException;
        return this;
    }

	/**
	 * 设置请求完成后的回调函数，无论成功|失败|异常 都会被执行
	 * @param onComplete 请求完成回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpClient<S, F> setOnComplete(OnComplete onComplete) {
        this.onComplete = onComplete;
        return this;
    }
    
	/**
	 * 设置请求响应后的回调函数，参数未经解析，可用于返回非文本的请求，例如下载文件
	 * 设置了OnResponse，就不可以再设置 OnSuccess 和 OnFailure 回调
	 * @param onResponse 请求返回回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpClient<S, F> setOnResponse(OnCallback<ResponseBody> onResponse) {
        this.onResponse = onResponse;
        return this;
    }
    
    /**
     * 发起 GET 请求
     * @return HttpCall
     */
    public HttpCall get() {
        return request("GET");
    }

    /**
     * 发起 POST 请求
     * @return HttpCall
     */
    public HttpCall post() {
        return request("POST");
    }

    /**
     * 发起 PUT 请求
     * @return HttpCall
     */
    public HttpCall put() {
        return request("PUT");
    }

    /**
     * 发起 DELETE 请求
     * @return HttpCall
     */
    public HttpCall delete() {
        return request("DELETE");
    }
    
    private HttpCall request(String method) {
    	return enqueueCall(prepareCall(method));
    }
    
    class HttpCallStatus implements HttpCall {

    	private Call call;
    	private boolean done;
    	
		public HttpCallStatus(Call call) {
			this.call = call;
		}

		@Override
		public void cancel() {
			call.cancel();
		}

		@Override
		public boolean isDone() {
			return done;
		}

		@Override
		public boolean isCanceled() {
			return call.isCanceled();
		}

		public void setDone(boolean done) {
			this.done = done;
		}
		
    }
	
    private HttpCall enqueueCall(Call call) {
        HttpCallStatus httpCall = new HttpCallStatus(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            	httpCall.setDone(true);
            	doOnException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            	httpCall.setDone(true);
            	doWithResponse(response);
            }
			
        });
		return httpCall;
    }
    
    @SuppressWarnings("unchecked")
	private void doWithResponse(Response response) {
    	if (onResponse != null) {
    		doOnResponse(response);
    		return;
    	}
    	int code = response.code();
        Headers headers = response.headers();
        Object result = null;
        try {
        	String body = toString(response);
        	if (code >= 200 && code < 300) {
        		result = parseObject(body, okType);
	        } else {
	        	result = parseObject(body, failType);
	        }
        } catch (Exception e) {
        	doOnException(e);
        	return;
        }
        if (code >= 200 && code < 300) {
        	doOnSuccess(code, headers, (S) result);
        } else {
        	doOnFailure(code, headers, (F) result);
        }
    }
    
    @Override
    protected void assertNotConflict(boolean isGetRequest) {
        super.assertNotConflict(isGetRequest);
        if (onResponse != null) {
        	if (onSuccess != null) {
        		throw new HttpException("方法 setOnResponse 与 setOnSuccess 不能同时调用！");
        	}
        	if (onFailure != null) {
        		throw new HttpException("方法 setOnResponse 与 setOnFailure 不能同时调用！");
        	}
        }
    }

	private void doOnResponse(Response response) {
		if (executor != null) {
			executor.execute(() -> {
				exeOnResponse(response);
			});
		} else {
			exeOnResponse(response);
		}
	}
	
	private void doOnSuccess(int status, Headers headers, S body) {
		if (executor != null) {
			executor.execute(() -> {
				exeOnSuccess(status, headers, body);
			});
		} else {
			exeOnSuccess(status, headers, body);
		}
	}

	private void doOnFailure(int status, Headers headers, F body) {
		if (executor != null) {
			executor.execute(() -> {
				exeOnFailure(status, headers, body);
			});
		} else {
			exeOnFailure(status, headers, body);
		}
	}

	private void doOnException(Exception e) {
		if (executor != null) {
			executor.execute(() -> {
				exeOnException(e);
			});
		} else {
			exeOnException(e);
		}
	}
    
	private void exeOnResponse(Response response) {
		if (onComplete != null) {
		    onComplete.onComplete(OnComplete.SUCCESS);
		}
		if (onResponse != null) {
			onResponse.on(response.code(), response.headers(), response.body());
		}
	}
	
	private void exeOnSuccess(int status, Headers headers, S body) {
		if (onComplete != null) {
            onComplete.onComplete(OnComplete.SUCCESS);
        }
		if (onSuccess != null) {
            onSuccess.on(status, headers, body);
        }
	}

	private void exeOnFailure(int status, Headers headers, F body) {
		if (onComplete != null) {
            onComplete.onComplete(OnComplete.FAILURE);
        }
		if (onFailure != null) {
		    onFailure.on(status, headers, body);
		}
	}

	private void exeOnException(Exception e) {
		int state = toState(e);
		if (onComplete != null) {
            onComplete.onComplete(state);
        }
		if (onException != null) {
		    onException.onException(e);
		} else if (state != OnComplete.CANCELED && !nothrow) {
			throw new HttpException(e.getMessage(), e);
		}
	}
	
}
