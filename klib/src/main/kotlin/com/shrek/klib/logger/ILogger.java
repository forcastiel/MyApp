package com.shrek.klib.logger;

import java.util.List;

public interface ILogger {

	void d(String tag, String message);

	void i(String tag, String message);

	void w(String tag, String message);

	void e(String tag, String message);
	
	void d(Object obj, String message);

	void i(Object obj, String message);

	void w(Object obj, String message);

	void e(Object obj, String message);

	void printlog(LogLevel mLogLevel, String tag, String message);

	/**
	 * 得到历史日志
	 * @return
	 */
	List<LoggerBo> getHistoryLogs();
	
}
