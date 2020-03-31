package com.ejlchina.http.internal;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executor;

import com.ejlchina.http.Download;
import com.ejlchina.http.GlobalCallback;
import com.ejlchina.http.HttpResult;
import com.ejlchina.http.HttpTask;
import com.ejlchina.http.OnCallback;
import com.ejlchina.http.TaskListener;
import com.ejlchina.http.HttpResult.State;

public class TaskExecutor {

	private Executor ioExecutor;
	private Executor mainExecutor;
	private TaskListener<Download> downloadListener;
	private GlobalCallback globalCallback;
	
	public TaskExecutor(Executor ioExecutor, Executor mainExecutor, TaskListener<Download> downloadListener, 
			GlobalCallback globalCallback) {
		this.ioExecutor = ioExecutor;
		this.mainExecutor = mainExecutor;
		this.downloadListener = downloadListener;
		this.globalCallback = globalCallback;
	}

	public Executor getExecutor(boolean onIoThread) {
		if (onIoThread || mainExecutor == null) {
			return ioExecutor;
		}
		return mainExecutor;
	}

	public Download download(HttpTask<?> httpTask, File file, InputStream input, long skipBytes) {
		Download download = new Download(file, input, this, skipBytes);
		if (downloadListener != null) {
			downloadListener.on(httpTask, download);
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
	
	public void executeOnComplete(HttpTask<?> task, OnCallback<State> onComplete, State state, boolean onIoThread) {
		if (globalCallback != null) {
			execute(() -> {
				if (globalCallback.onComplete(task, state) && onComplete != null) {
					onComplete.on(state);
				}
			}, onIoThread);
		} else if (onComplete != null) {
			execute(() -> { onComplete.on(state); }, onIoThread);
		}
	}
	
	public void executeOnResponse(HttpTask<?> task, OnCallback<HttpResult> onResponse, HttpResult result, boolean onIoThread) {
		if (globalCallback != null) {
			execute(() -> {
				if (globalCallback.onResponse(task, result) && onResponse != null) {
					onResponse.on(result);
				}
			}, onIoThread);
		} else if (onResponse != null) {
			execute(() -> { onResponse.on(result); }, onIoThread);
		}
	}

	public boolean executeOnException(HttpTask<?> task, OnCallback<Exception> onException, Exception error, boolean onIoThread) {
		if (globalCallback != null) {
			execute(() -> {
				if (globalCallback.onException(task, error) && onException != null) {
					onException.on(error);
				}
			}, onIoThread);
			return true;
		} else if (onException != null) {
			execute(() -> { onException.on(error); }, onIoThread);
			return true;
		}
		return false;
	}

}
