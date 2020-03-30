package com.ejlchina.http.internal;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executor;

import com.ejlchina.http.Download;
import com.ejlchina.http.HttpTask;
import com.ejlchina.http.OnCallback;

public class TaskExecutor {

	private Executor ioExecutor;
	private Executor mainExecutor;
	private OnCallback<Download> downloadListener;
	
	public TaskExecutor(Executor ioExecutor, Executor mainExecutor, OnCallback<Download> downloadListener) {
		this.ioExecutor = ioExecutor;
		this.mainExecutor = mainExecutor;
		this.downloadListener = downloadListener;
	}

	public Executor getExecutor(boolean onIoThread) {
		if (onIoThread || mainExecutor == null) {
			return ioExecutor;
		}
		return mainExecutor;
	}

	public Download download(HttpTask<?> httpTask, File file, InputStream input, long skipBytes) {
		Download download = new Download(httpTask, file, input, this, skipBytes);
		if (downloadListener != null) {
			downloadListener.on(download);
		}
		return download;
	}
	
	public void execute(Runnable command, boolean onIoThread) {
		Executor executor = ioExecutor;
		if (mainExecutor != null && !onIoThread) {
			executor = mainExecutor;
		}
		executor.execute(command);
	}
	
}
