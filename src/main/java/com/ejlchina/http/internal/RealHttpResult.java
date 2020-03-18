package com.ejlchina.http.internal;

import com.ejlchina.http.HttpResult;

import okhttp3.Headers;
import okhttp3.Response;


public class RealHttpResult implements HttpResult {

	private State state;
	private Response response;
	private Exception error;
	
	public RealHttpResult() {
	}
	
	public RealHttpResult(State state) {
		this.state = state;
	}
	
	public RealHttpResult(Response response) {
		response(response);
	}
	
	public RealHttpResult(State state, Exception error) {
		exception(state, error);
	}
	
	public void exception(State state, Exception error) {
		this.state = state;
		this.error = error;
	}
	
	public void response(Response response) {
		this.state = State.RESPONSED;
		this.response = response;
	}
	
	@Override
	public State getState() {
		return state;
	}

	@Override
	public int getStatus() {
		if (response != null) {
			return response.code();
		}
		return 0;
	}

	@Override
	public boolean isSuccessful() {
	    if (response != null) {
			return response.isSuccessful();
		}
		return false;
	}
	
	@Override
	public Headers getHeaders() {
		if (response != null) {
			return response.headers();
		}
		return null;
	}

	@Override
	public Body getBody() {
		if (response != null) {
			return new ResultBody(response.body());
		}
		return null;
	}
	
	@Override
	public Exception getError() {
		return error;
	}

	@Override
	public String toString() {
		Body body = getBody();
		String str = "HttpResult [\n  state: " + state + ",\n  status: " + getStatus() 
				+ ",\n  headers: " + getHeaders();
		if (body != null) {
			str += ",\n  contentType: " + body.getContentType()
			+ ",\n  body: " + body.toString();
		}
		return str + ",\n  error: " + error + "\n]";
	}
	
}
