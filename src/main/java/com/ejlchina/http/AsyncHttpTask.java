package com.ejlchina.http;

import java.io.IOException;

import com.ejlchina.http.HttpResult.State;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * 异步 Http 请求任务
 *  
 * @author Troy.Zhou
 * 
 */
public class AsyncHttpTask extends HttpTask<AsyncHttpTask> {

	
    private OnCallback<HttpResult> onResponse;
    private OnCallback<Exception> onException;
    private OnCallback<State> onComplete;

	public AsyncHttpTask(HttpClient client, String urlPath) {
		super(client, urlPath);
	}


	/**
	 * 设置请求异常后的回调函数，设置后，与HttpClient有关的异常将不再向上抛出
	 * @param onException 请求异常回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpTask setOnException(OnCallback<Exception> onException) {
        this.onException = onException;
        return this;
    }

	/**
	 * 设置请求完成后的回调函数，无论成功|失败|异常 都会被执行
	 * @param onComplete 请求完成回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpTask setOnComplete(OnCallback<State> onComplete) {
        this.onComplete = onComplete;
        return this;
    }
    
	/**
	 * 设置请求响应后的回调函数，参数未经解析，可用于返回非文本的请求，例如下载文件
	 * 设置了OnResponse，就不可以再设置 OnSuccess 和 OnFailure 回调
	 * @param onResponse 请求返回回调
	 * @return HttpClient 实例
	 */
    public AsyncHttpTask setOnResponse(OnCallback<HttpResult> onResponse) {
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
    	PreHttpCall call = new PreHttpCall();
    	httpClient.preprocess(this, () -> {
    		synchronized (call) {
    			if (!call.isCanceled()) {
    				call.setCall(executeCall(prepareCall(method)));
        		}
			}
    	});
    	return call;
    }
    
    
    class PreHttpCall implements HttpCall {

    	private boolean canceled = false;
    	private HttpCall call;
    	
		@Override
		public synchronized void cancel() {
			if (call != null) {
				call.cancel();
			} else {
			}
			canceled = true;
		}

		@Override
		public boolean isDone() {
			if (call != null) {
				return call.isDone();
			}
			return canceled;
		}

		@Override
		public boolean isCanceled() {
			if (call != null) {
				return call.isCanceled();
			}
			return canceled;
		}

		public void setCall(HttpCall call) {
			this.call = call;
		}

		@Override
		public State getState() {
			if (call != null) {
				return call.getState();
			}
			if (canceled) {
				return State.CANCELED;
			}
			return null;
		}

    }
    
    class OkHttpCall implements HttpCall {

    	private Call call;
    	private boolean done = false;
    	private State state;
    	
		public OkHttpCall(Call call) {
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

		@Override
		public State getState() {
			return state;
		}

		public void setState(State state) {
			this.state = state;
			this.done = true;
		}

    }
	
    private HttpCall executeCall(Call call) {
        OkHttpCall httpCall = new OkHttpCall(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            	State state = toState(e);
            	httpCall.setState(state);
            	doOnException(state, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            	httpCall.setState(State.RESPONSED);
            	doOnResponse(new HttpResult(response));
            }
			
        });
		return httpCall;
    }
    

	private void doOnResponse(HttpResult result) {
		httpClient.executeCallback(() -> {
			if (onComplete != null) {
			    onComplete.on(State.RESPONSED);
			}
			if (onResponse != null) {
				onResponse.on(result);
			}
		});
	}
	
	private void doOnException(State state, Exception e) {
		httpClient.executeCallback(() -> {
			if (onComplete != null) {
	            onComplete.on(state);
	        }
			// 请求取消不作为一种异常来处理
			if (state != State.CANCELED) {
				if (onException != null) {
				    onException.on(e);
				} else if (!nothrow) {
					throw new HttpException(e.getMessage(), e);
				}
			}
		});
	}
	
}
