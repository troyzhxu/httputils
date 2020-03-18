package com.ejlchina.http.internal;

import java.io.IOException;

import com.ejlchina.http.HttpCall;
import com.ejlchina.http.HttpTask;
import com.ejlchina.http.OnCallback;
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

	
    private OnCallback<RealHttpResult> onResponse;
    private OnCallback<Exception> onException;
    private OnCallback<State> onComplete;

	public AsyncHttpTask(HttpClient client, String urlPath) {
		super(client, urlPath);
	}


	/**
	 * 设置请求执行异常后的回调函数，设置后，相关异常将不再向上抛出
	 * @param onException 请求异常回调
	 * @return HttpTask 实例
	 */
    public AsyncHttpTask setOnException(OnCallback<Exception> onException) {
        this.onException = onException;
        return this;
    }

	/**
	 * 设置请求执行完成后的回调函数，无论成功|失败|异常 都会被执行
	 * @param onComplete 请求完成回调
	 * @return HttpTask 实例
	 */
    public AsyncHttpTask setOnComplete(OnCallback<State> onComplete) {
        this.onComplete = onComplete;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResponse 请求返回回调
	 * @return HttpTask 实例
	 */
    public AsyncHttpTask setOnResponse(OnCallback<RealHttpResult> onResponse) {
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
    	if (tag != null) {
    		httpClient.addTagCall(tag, call, this);
    	}
    	return call;
    }
    
    
    class PreHttpCall implements HttpCall {

    	private boolean canceled = false;
    	private HttpCall call;
    	
		@Override
		public boolean cancel() {
			boolean res = true;
			synchronized (this) {
				if (call != null) {
					res = call.cancel();
				} else {
					canceled = true;
				}
				notify();
			}
			if (tag != null && call == null) {
	    		httpClient.removeTagCall(AsyncHttpTask.this);
	    	}
			return res;
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
			notify();
		}

		@Override
		public synchronized RealHttpResult getResult() {
			if (canceled) {
				return new RealHttpResult(State.CANCELED);
			}
			if (call == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new HttpException(e.getMessage(), e);
				}
			}
			if (call != null) {
				return call.getResult();
			}
			return new RealHttpResult(State.CANCELED);
		}

    }
    
    class OkHttpCall implements HttpCall {

    	private Call call;
    	private RealHttpResult result;
    	
		public OkHttpCall(Call call) {
			this.call = call;
		}

		@Override
		public boolean cancel() {
			if (result == null) {
				call.cancel();
				return true;
			}
			return false;
		}

		@Override
		public boolean isDone() {
			return result != null;
		}

		@Override
		public boolean isCanceled() {
			return call.isCanceled();
		}

		@Override
		public synchronized RealHttpResult getResult() {
			if (result == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new HttpException(e.getMessage(), e);
				}
			}
			return result;
		}

		public void setResult(RealHttpResult result) {
			synchronized (this) {
				this.result = result;
				notify();
			}
			if (tag != null) {
	    		httpClient.removeTagCall(AsyncHttpTask.this);
	    	}
		}

    }
	
    private HttpCall executeCall(Call call) {
        OkHttpCall httpCall = new OkHttpCall(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            	State state = toState(e);
            	doOnException(state, e);
            	if (state == State.CANCELED) {
            		httpCall.setResult(new RealHttpResult(state));
            	} else {
            		httpCall.setResult(new RealHttpResult(state, e));
            	}
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            	RealHttpResult result = new RealHttpResult(response);
            	doOnResponse(result);
            	httpCall.setResult(result);
            }
			
        });
		return httpCall;
    }
    

	private void doOnResponse(RealHttpResult result) {
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
