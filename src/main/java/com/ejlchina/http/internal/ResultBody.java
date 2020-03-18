package com.ejlchina.http.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ejlchina.http.HttpResult.Body;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;

public class ResultBody implements Body {

	private ResponseBody body;

	ResultBody(ResponseBody body) {
		this.body = body;
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
	public <T> T toBean(TypeReference<T> typeRef) {
		return JSON.parseObject(toString(), typeRef.getType());
	}

	@Override
	public File toFile(String filePath) {
		return toFile(new File(filePath));
	}

	@Override
	public File toFile(File file) {
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
		OutputStream output;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new HttpException("无法获取文件[" + file.getAbsolutePath() + "]的输入流", e);
		}
		InputStream input = body.byteStream();
		try {
			byte[] buff = new byte[1024];
			int len = -1;
			while ((len = input.read(buff)) != -1) {
				output.write(buff, 0, len);
			}
		} catch (IOException e) {
			throw new HttpException("流传输失败", e);
		} finally {
			Util.closeQuietly(output);
			body.close();
		}
		return file;
	}
	
}
