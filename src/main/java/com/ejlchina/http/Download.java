package com.ejlchina.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import com.ejlchina.http.internal.HttpException;
import com.ejlchina.http.internal.RealProcess;

import okhttp3.internal.Util;

public class Download {

	private File file;
	private InputStream input;
	private OnCallback<Process> onProcess;
	private OnCallback<File> onFile;
	private Executor callbackExecutor;
	private long contentLength;
	
	public Download(File file, InputStream input, long contentLength, Executor executor) {
		this.file = file;
		this.input = input;
		this.contentLength = contentLength;
		this.callbackExecutor = executor;
	}

	public Download setOnProcess(OnCallback<Process> onProcess) {
		this.onProcess = onProcess;
		return this;
	}
	
	public Download setOnFile(OnCallback<File> onFile) {
		this.onFile = onFile;
		return this;
	}
	
	public void start() {
		OutputStream output;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new HttpException("无法获取文件[" + file.getAbsolutePath() + "]的输入流", e);
		}
		RealProcess process = new RealProcess(contentLength);
		doOnProcess(process);
		try {
			byte[] buff = new byte[1024];
			int len = -1;
			while ((len = input.read(buff)) != -1) {
				output.write(buff, 0, len);
				process.addDone(len);
				doOnProcess(process);
			}
		} catch (IOException e) {
			throw new HttpException("流传输失败", e);
		} finally {
			Util.closeQuietly(output);
			Util.closeQuietly(input);
		}
	}
	
	
	private void doOnProcess(Process process) {
		callbackExecutor.execute(() -> {
			onProcess.on(process);
		});
		if (process.getDone() < process.getTotal()) {
			return;
		}
		callbackExecutor.execute(() -> {
			onFile.on(file);
		});
	}
	
}
