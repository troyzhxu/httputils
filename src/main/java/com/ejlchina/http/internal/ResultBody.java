package com.ejlchina.http.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.Executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ejlchina.http.Download;
import com.ejlchina.http.HttpResult.Body;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ResultBody implements Body {

	private Response response;
	private Executor callbackExecutor;
	private long skipBytes;

	ResultBody(Response response, Executor callbackExecutor, long skipBytes) {
		this.response = response;
		this.callbackExecutor = callbackExecutor;
		this.skipBytes = skipBytes;
	}

	@Override
	public MediaType getContentType() {
		return response.body().contentType();
	}

	@Override
	public long getContentLength() {
		return response.body().contentLength();
	}

	@Override
	public InputStream toByteStream() {
		return response.body().byteStream();
	}

	@Override
	public byte[] toBytes() {
		try {
			return response.body().bytes();
		} catch (IOException e) {
			throw new HttpException("报文体转化字节数组出错", e);
		}
	}

	@Override
	public Reader toCharStream() {
		return response.body().charStream();
	}

	@Override
	public String toString() {
		try {
			return response.body().string();
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
		if (!file.exists()) {
			try {
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				file.createNewFile();
			} catch (IOException e) {
				response.body().close();
				throw new HttpException(
						"Cannot create file [" + file.getAbsolutePath() + "]", e);
			}
		}
		ResponseBody body = response.body();
		return new Download(file, body.byteStream(), body.contentLength(), callbackExecutor, skipBytes);
	}
	
	@Override
	public Download toFolder(String dirPath) {
		String fileName = resolveFileName();
		String filePath = resolveFilePath(dirPath, fileName);
		int index = 0;
		File file = new File(filePath);
		while (file.exists()) {
			String indexFileName = indexFileName(fileName, index++);
			filePath = resolveFilePath(dirPath, indexFileName);
			file = new File(filePath);
		}
		return toFile(file);
	}

	@Override
	public Download toFolder(File dir) {
		if (dir.exists() && !dir.isDirectory()) {
			response.body().close();
			throw new HttpException("文件下载失败：文件[" + dir.getAbsolutePath() + "]已存在，并且不是一个目录！");
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return toFolder(dir.getAbsolutePath());
	}
	
	private String resolveFilePath(String dirPath, String fileName) {
		if (dirPath.endsWith("\\") || dirPath.endsWith("/")) {
			return dirPath + fileName;
		}
		return dirPath + "\\" + fileName;
	}

	private String indexFileName(String fileName, int index) {
		int i = fileName.lastIndexOf('.');
		if (i < 0) {
			return fileName + "(" + index + ")";
		}
		String ext = fileName.substring(i);
		if (i > 0) {
			String name = fileName.substring(0, i);
			return name + "(" + index + ")" + ext;
		}
		return "(" + index + ")" + ext;
	}
	
	private String resolveFileName() {
		String fileName = response.header("Content-Disposition");
        // 通过Content-Disposition获取文件名，这点跟服务器有关，需要灵活变通
        if (fileName == null || fileName.length() < 1) {
        	fileName = response.request().url().encodedPath();
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        } else {
            try {
				fileName = URLDecoder.decode(fileName.substring(
				    fileName.indexOf("filename=") + 9), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new HttpException("解码文件名失败", e);
			}
            // 有些文件名会被包含在""里面，所以要去掉，不然无法读取文件后缀
            fileName = fileName.replaceAll("\"", "");
        }
        return fileName;
	}
	
}
