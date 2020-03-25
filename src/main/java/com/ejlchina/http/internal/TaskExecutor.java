package com.ejlchina.http.internal;

import java.util.concurrent.Executor;

public class TaskExecutor {

	private Executor ioExecutor;
	private Executor mainExecutor;
	
	
	public TaskExecutor(Executor ioExecutor, Executor mainExecutor) {
		this.ioExecutor = ioExecutor;
		this.mainExecutor = mainExecutor;
	}

	public Executor getExecutor(boolean onIO) {
		if (onIO || mainExecutor == null) {
			return ioExecutor;
		}
		return mainExecutor;
	}

}
