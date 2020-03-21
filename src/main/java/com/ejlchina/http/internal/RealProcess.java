package com.ejlchina.http.internal;

import com.ejlchina.http.Process;

public class RealProcess implements Process {

	// 总字节数
	private long total;
	// 已经完成字节数
	private long done;
	
	
	public RealProcess(long total) {
		this.total = total;
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
	
	public void addDone(long delt) {
		this.done += delt;
	}
	
}
