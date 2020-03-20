package com.ejlchina.http.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ejlchina.http.Download;
import com.ejlchina.http.HttpResult.Body;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

public class ResultBody implements Body {

	private ResponseBody body;
	private Executor callbackExecutor;

	ResultBody(ResponseBody body, Executor callbackExecutor) {
		this.body = body;
		this.callbackExecutor = callbackExecutor;
	}

	@Override
	public MediaType getContentType() {
		return body.contentType();
	}

	@Override
	public long getContentLength() {
		return body.contentLength();
	}

	@Override
	public InputStream toByteStream() {
		return body.byteStream();
	}

	@Override
	public byte[] toBytes() {
		try {
			return body.bytes();
		} catch (IOException e) {
			throw new HttpException("报文体转化字节数组出错", e);
		}
	}

	@Override
	public Reader toCharStream() {
		return body.charStream();
	}

	@Override
	public String toString() {
		try {
			return body.string();
		} catch (IOException e) {
			throw new HttpException("报文体转化字符串出错", e);
		}
	}

	@Override
	public JSONObject toJsonObject() {
		return JSON.parseObject(toString());
	}

	@Override
	public JSONArray toJsonArray() {
		return JSON.parseArray(toString());
	}

	@Override
	public <T> T toBean(Class<T> type) {
		return JSON.parseObject(toString(), type);
	}
	
	@Override
	public <T> List<T> toList(Class<T> type) {
		return JSON.parseArray(toString(), type);
	}

	@Override
	public Download toFile(String filePath) {
		return toFile(new File(filePath));
	}

	@Override
	public Download toFile(File file) {
		if (file.exists() && !file.delete()) {
			throw new HttpException(
					"Destination file [" + file.getAbsolutePath() + "] already exists and could not be deleted");
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new HttpException(
					"Cannot create file [" + file.getAbsolutePath() + "]");
		}
		return new Download(file, body.byteStream(), callbackExecutor).start();
	}
	
}
