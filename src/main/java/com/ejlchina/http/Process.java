package com.ejlchina.http;

/**
 * 进度（上传或下载）
 * @since 2.2.0
 */
public interface Process {

	/**
	 * @return 完成比例
	 */
	double getRate();

	/**
	 * @return 总任务量
	 */
	long getTotal();
	
	/**
	 * @return 已完成任务量
	 */
	long getDone();
	
}
