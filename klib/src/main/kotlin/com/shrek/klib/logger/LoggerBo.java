package com.shrek.klib.logger;

import java.util.Date;

public class LoggerBo {

	private Date time;

	private String tag;

	private String packageName;

	private String message;

	public LoggerBo(Date time, String tag, String packageName, String message) {
		super();
		this.time = time;
		this.tag = tag;
		this.packageName = packageName;
		this.message = message;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "LoggerBo [time=" + time + ", tag=" + tag + ", packageName="
				+ packageName + ", message=" + message + "]";
	}

}
