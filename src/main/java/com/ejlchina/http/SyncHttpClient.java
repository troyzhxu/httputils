package com.ejlchina.http;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;


/**
 * 同步 Http 客户端
 *  
 * @author Troy.Zhou
 *
 * @param <S> 请求成功时返回的数据类型
 * @param <F> 请求失败时返回的数据类型
 * 
 * @since 0.3.5
 */
public class SyncHttpClient<S, F> extends HttpClient<S, F, SyncHttpClient<S, F>> {

	public SyncHttpClient(String urlPath, Type okType, Type failType) {
		super(urlPath, okType, failType);
	}
	
    /**
     * 
     * 发起 GET 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult<S, F> get() {
        return request("GET");
    }

    /**
     * 发起 POST 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult<S, F> post() {
        return request("POST");
    }

    /**
     * 发起 PUT 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult<S, F> put() {
        return request("PUT");
    }
    
    /**
     * 发起 DELETE 请求
     * @return 请求结果  
     * @see HttpResult
     */
    public HttpResult<S, F> delete() {
        return request("DELETE");
    }
    
    private HttpResult<S, F> request(String method) {
    	return executeCall(prepareCall(method));
    }

    protected HttpResult<S, F> executeCall(Call call) {
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
        	if (nothrow) {
        		return HttpResult.exception(toState(e), e);
        	}
        	throw new HttpException("请求执行异常", e);
        }
        return doWithResponse(response);
    }

	@SuppressWarnings("unchecked")
	private HttpResult<S, F> doWithResponse(Response response) {
		int code = response.code();
        Headers headers = response.headers();
        String body = null;
        try {
        	body = toString(response);
        } catch (IOException e) {
        	if (nothrow) {
        		return HttpResult.exception(code, headers, toState(e), e);
        	}
        	throw new HttpException("从返回体中读取文本异常", e);
        }
        try {
	        if (code >= 200 && code < 300) {
	            Object result = parseObject(body, okType);
	            return HttpResult.success(code, headers, (S) result);
	        } else {
	            Object result = parseObject(body, failType);
	            return HttpResult.fail(code, headers, (F) result);
	        }
        } catch (Exception e) {
        	if (nothrow) {
        		return HttpResult.exception(code, headers, OnComplete.EXCEPTION, e);
        	}
            throw new HttpException("数据解析异常: " + body, e);
        }
	}
	
}
