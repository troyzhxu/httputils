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
	private OnCallback<Error> onError;
	private Executor callbackExecutor;
	private long totalBytes;
	private long step = 0;
	private long stepBytes = 8192;
	private int buffSize = 2048;
	private volatile int status;
	private RealProcess process;
	private Object lock = new Object();
	
	public Download(File file, InputStream input, long totalBytes, Executor executor) {
		this.file = file;
		this.input = input;
		this.totalBytes = totalBytes;
		this.callbackExecutor = executor;
		this.process = new RealProcess(totalBytes);
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
	 * 设置回调步进字节，默认 8K（8192），该值若小于 buffSize，就相当与 buffSize
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
	 * @param onProcess 进度回调函数
	 * @return Download
	 */
	public Download setOnProcess(OnCallback<Process> onProcess) {
		this.onProcess = onProcess;
		return this;
	}
	
	/**
	 * 设置下载完成回调
	 * @param onDone 完成回调函数
	 * @return Download
	 */
	public Download setOnDone(OnCallback<File> onDone) {
		this.onDone = onDone;
		return this;
	}
	
	/**
	 * 设置下载失败回调
	 * @param onError 失败回调函数
	 * @return Download
	 */
	public Download setOnError(OnCallback<Error> onError) {
		this.onError = onError;
		return this;
	}
	
	/**
	 * 开始下载
	 * @return 下载控制器
	 */
	public Ctrl start() {
		return start(0);
	}

	/**
	 * 开始下载，跳过 skipBytes 个字节（用于断点续传）
	 * @param skipBytes 跳过的字节数
	 * @return 下载控制器
	 */
	public Ctrl start(long skipBytes) {
		status = Ctrl.STATUS__DOWNLOADING;
		new Thread(() -> {
			doDownload(skipBytes);
		}).start();
		return new Ctrl();
		
	}
	
	public class Ctrl {
		
		/**
		 * 已取消
		 */
		public static final int STATUS__CANCELED = -1;
		
		/**
		 * 下载中
		 */
		public static final int STATUS__DOWNLOADING = 1;
		
		/**
		 * 已暂停
		 */
		public static final int STATUS__PAUSED = 2;
		
		/**
		 * 已完成
		 */
		public static final int STATUS__DONE = 3;
		
		/**
		 * 错误
		 */
		public static final int STATUS__ERROR = 4;
		
		/**
		 * @set {@link #STATUS__CANCELED}
		 * @set {@link #STATUS__DOWNLOADING}
		 * @set {@link #STATUS__PAUSED}
		 * @set {@link #STATUS__DONE}
		 * @return 下载状态
		 */
		public int status() {
			return status;
		}
		
		/**
		 * 暂停下载任务
		 */
		public void pause() {
			synchronized (lock) {
				if (status == STATUS__DOWNLOADING) {
					status = STATUS__PAUSED;
				}
			}
		}
		
		/**
		 * 继续下载任务
		 */
		public void resume() {
			synchronized (lock) {
				if (status == STATUS__PAUSED) {
					status = STATUS__DOWNLOADING;
				}
			}
		}
		
		/**
		 * 取消下载任务
		 */
		public void cancel() {
			synchronized (lock) {
				if (status == STATUS__PAUSED || status == STATUS__DOWNLOADING) {
					status = STATUS__CANCELED;
				}
			}
		}
		
	}
	
	public class Error {
		
		private long doneBytes;
		
		private IOException exception;

		Error(long doneBytes, IOException exception) {
			this.doneBytes = doneBytes;
			this.exception = exception;
		}
		
		/**
		 * @return 下载文件
		 */
		public File getFile() {
			return file;
		}
		
		/**
		 * @return 已下载字节数
		 */
		public long getDoneBytes() {
			return doneBytes;
		}

		/**
		 * @return 异常信息
		 */
		public IOException getException() {
			return exception;
		}
		
	}
	
	private void doDownload(long skipBytes) {
		OutputStream output;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			status = Ctrl.STATUS__ERROR;
			Util.closeQuietly(input);
			throw new HttpException("无法获取文件[" + file.getAbsolutePath() + "]的输入流", e);
		}
		try {
			while (status != Ctrl.STATUS__CANCELED && status != Ctrl.STATUS__DONE) {
				if (status == Ctrl.STATUS__DOWNLOADING) {
					byte[] buff = new byte[buffSize];
					int len = -1;
					input.skip(skipBytes);
					while ((len = input.read(buff)) != -1) {
						output.write(buff, 0, len);
						process.addDoneBytes(len);
						doOnProcess(process);
						if (status == Ctrl.STATUS__CANCELED 
								|| status == Ctrl.STATUS__PAUSED) {
							break;
						}
					}
					if (len == -1) {
						synchronized (lock) {
							status = Ctrl.STATUS__DONE;
						}
					}
				}
			}
		} catch (IOException e) {
			synchronized (lock) {
				status = Ctrl.STATUS__ERROR;
			}
			if (onError != null) {
				callbackExecutor.execute(() -> {
					onError.on(new Error(process.getDoneBytes(), e));
				});
			} else {
				throw new HttpException("流传输失败", e);
			}
		} finally {
			Util.closeQuietly(output);
			Util.closeQuietly(input);
			if (status == Ctrl.STATUS__CANCELED) {
				file.delete();
			}
		}
	}
	
	
	private void doOnProcess(Process process) {
		long done = process.getDoneBytes();
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
