package com.ejlchina.http.internal;

import com.ejlchina.http.Process;

public class RealProcess implements Process {

	// 总字节数
	private long total;
	// 已经完成字节数
	private long done;
	
	
	public RealProcess(long total, long done) {
		this.total = total;
		this.done = done;
	}
	
	/**
	 * @return 完成比例
	 */
	public double getRate() {
		return (double) done / total;
	}

	public long getTotal() {
		return total;
	}
	
	public void setTotal(long total) {
		this.total = total;
	}
	
	public long getDone() {
		return done;
	}
	
	public void setDone(long done) {
		this.done = done;
	}
	
}
