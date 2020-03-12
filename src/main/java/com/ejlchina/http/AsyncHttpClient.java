package com.ejlchina.http;

import java.io.IOException;
import java.lang.reflect.Type;

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
 * @since 0.3.5
 */
public class AsyncHttpClient<S, F> extends HttpClient<S, F, AsyncHttpClient<S, F>> {

	
    private OnCallback<S> onSuccess;
    private OnCallback<F> onFailure;
    private OnException onException;
    private OnComplete onComplete;
    private OnCallback<ResponseBody> onResponse;
	
    
	public AsyncHttpClient(String urlPath, Type okType, Type failType) {
		super(urlPath, okType, failType);
	}

	/**
	 * 设置请求成功（HTTP状态码在[200, 300)之间）后的回调函数
	 */
    public AsyncHttpClient<S, F> setOnSuccess(OnCallback<S> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

	/**
	 * 设置请求失败（HTTP状态码不在[200, 300)之间）后的回调函数
	 */
    public AsyncHttpClient<S, F> setOnFailure(OnCallback<F> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

	/**
	 * 设置请求发生异常后的回调函数
	 */
    public AsyncHttpClient<S, F> setOnException(OnException onException) {
        this.onException = onException;
        return this;
    }

	/**
	 * 设置请求完成后的回调函数，无论成功|失败|异常 都会被执行
	 */
    public AsyncHttpClient<S, F> setOnComplete(OnComplete onComplete) {
        this.onComplete = onComplete;
        return this;
    }
    
	/**
	 * 设置请求返回后的回调函数，参数未经解析，可用于返回非文本的请求，例如下载文件
	 */
    public AsyncHttpClient<S, F> setOnResponse(OnCallback<ResponseBody> onResponse) {
        this.onResponse = onResponse;
        return this;
    }
    
    /**
     * 发起 GET 请求
     */
    public HttpCall get() {
        return request("GET");
    }

    /**
     * 发起 POST 请求
     */
    public HttpCall post() {
        return request("POST");
    }

    /**
     * 发起 PUT 请求
     */
    public HttpCall put() {
        return request("PUT");
    }

    /**
     * 发起 DELETE 请求
     */
    public HttpCall delete() {
        return request("DELETE");
    }
    
    private HttpCall request(String method) {
    	return enqueueCall(prepareCall(method));
    }
    
    
    public static class HttpCallStatus implements HttpCall {

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
            	if (onResponse != null) {
            		onResponse.on(response.code(), response.headers(), response.body());
            	} else {
            		AsyncHttpClient.this.onResponse(response);
            	}
            }
        });
		return httpCall;
    }
    
    
    private void onResponse(Response response) {
        try {
            int code = response.code();
            Headers headers = response.headers();
        	String body = toString(response);
            processResponse(code, headers, body);
        } catch (IOException e) {
        	doOnException(e);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    protected void processResponse(int code, Headers headers, String body) {
        if (code >= 200 && code < 300) {
            try {
                doOnSuccess(code, headers, (S) parseObject(body, okType));
            } catch (Exception e) {
                doOnException(e);
            }
        } else {
            try {
                doOnFailure(code, headers, (F) parseObject(body, failType));
            } catch (Exception e) {
                doOnException(e);
            }
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

	private void doOnSuccess(int status, Headers headers, S body) {
		if (onComplete != null) {
            onComplete.onComplete(OnComplete.SUCCESS);
        }
		if (onSuccess != null) {
            onSuccess.on(status, headers, body);
        }
	}
	
	private void doOnFailure(int status, Headers headers, F body) {
		if (onComplete != null) {
            onComplete.onComplete(OnComplete.FAILURE);
        }
		if (onFailure != null) {
		    onFailure.on(status, headers, body);
		}
	}

	private void doOnException(Exception e) {
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
