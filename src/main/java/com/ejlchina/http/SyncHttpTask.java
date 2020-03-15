package com.ejlchina.http;

import java.io.IOException;

import com.ejlchina.http.HttpResult.State;

import okhttp3.Call;
import okhttp3.Response;


/**
 * 同步 Http 客户端
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
    
    private HttpResult request(String method) {
    	Call call = prepareCall(method);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
        	if (nothrow) {
        		return new HttpResult(toState(e), e);
        	}
        	throw new HttpException("请求执行异常", e);
        }
        return new HttpResult(State.RESPONSED, response);
    }

}
