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

/**
 * 文件下载
 * @author Troy.Zhou
 * @since 2.2.0
 */
public class Download {
	
	private File file;
	private InputStream input;
	private OnCallback<Process> onProcess;
	private OnCallback<File> onDone;
	private Executor callbackExecutor;
	private long totalBytes;
	private long step = 0;
	private long stepBytes = 8192;
	private int buffSize = 2048;

	
	public Download(File file, InputStream input, long totalBytes, Executor executor) {
		this.file = file;
		this.input = input;
		this.totalBytes = totalBytes;
		this.callbackExecutor = executor;
	}

	/**
	 * 设置缓冲区大小，默认 2K（2048）
	 * @param buffSize 缓冲区大小（单位：字节）
	 * @return
	 */
	public Download setBuffSize(int buffSize) {
		if (buffSize > 0) {
			this.buffSize = buffSize;
		}
		return this;
	}
	
	/**
	 * 设置回调步进字节，默认 8K（8192）
	 * 表示每下载 stepBytes 个字节，执行一次进度回调
	 * @param stepBytes 步进字节
	 * @return Download 
	 */
	public Download setStepBytes(long stepBytes) {
		this.stepBytes = stepBytes;
		return this;
	}
	
	/**
	 * 设置回调步进比例
	 * 表示每下载 stepRate 比例，执行一次进度回调
	 * @param stepRate 步进比例
	 * @return Download
	 */
	public Download setStepRate(double stepRate) {
		if (stepRate > 0 && stepRate <= 1) {
			this.stepBytes = (long) (totalBytes * stepRate);
		}
		return this;
	}
	
	/**
	 * 设置下载进度回调
	 * @param onProcess
	 * @return
	 */
	public Download setOnProcess(OnCallback<Process> onProcess) {
		this.onProcess = onProcess;
		return this;
	}
	
	public Download setOnDone(OnCallback<File> onDone) {
		this.onDone = onDone;
		return this;
	}
	
	public void start() {
		OutputStream output;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new HttpException("无法获取文件[" + file.getAbsolutePath() + "]的输入流", e);
		}
		RealProcess process = new RealProcess(totalBytes);
		try {
			byte[] buff = new byte[buffSize];
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
		long done = process.getDone();
		if (done < step * stepBytes 
				&& done < totalBytes) {
			return;
		}
		step++;
		if (onProcess != null) {
			callbackExecutor.execute(() -> {
				onProcess.on(process);
			});
		}
		if (onDone != null && done >= totalBytes) {
			callbackExecutor.execute(() -> {
				onDone.on(file);
			});
		}
	}
	
}
