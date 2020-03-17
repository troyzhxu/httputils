package com.ejlchina.http;

import java.io.IOException;

import com.ejlchina.http.HttpResult.State;

import okhttp3.Call;


/**
 * 同步 Http 请求任务
 *  
 * @author Troy.Zhou
 *
 * @param <S> 请求成功时返回的数据类型
 * @param <F> 请求失败时返回的数据类型
 * 
 */
public class SyncHttpTask extends HttpTask<SyncHttpTask> {

	public SyncHttpTask(HttpClient client, String urlPath) {
		super(client, urlPath);
	}
	
    /**
     * 发起 GET 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult get() {
        return request("GET");
    }

    /**
     * 发起 POST 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult post() {
        return request("POST");
    }

    /**
     * 发起 PUT 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult put() {
        return request("PUT");
    }
    
    /**
     * 发起 DELETE 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult delete() {
        return request("DELETE");
    }
    
    private synchronized HttpResult request(String method) {
    	HttpResult result = new HttpResult();
    	httpClient.preprocess(this, () -> {
        	Call call = prepareCall(method);
            try {
                result.response(call.execute());
            } catch (IOException e) {
            	result.exception(toState(e), e);
            }
            synchronized (SyncHttpTask.this) {
            	SyncHttpTask.this.notify();
            }
    	});
    	if (result.getState() == null) {
    		try {
    			SyncHttpTask.this.wait();
			} catch (InterruptedException e) {
				throw new HttpException("等待异常", e);
			}
    	}
    	Exception e = result.getError();
    	if (e != null && result.getState() != State.CANCELED 
    			&& !nothrow) {
    		throw new HttpException("请求执行异常", e);
    	}
        return result;
    }

}
