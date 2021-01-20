package com.shrek.klib.logger;

public enum LogLevel {
	DEBUG("调试"),
	INFO("信息"),
	WARNING("警告"),
	ERROR("错误"),
	PRINT("打印");
	
	public String descript;//描述
	
	LogLevel(String descript){
		this.descript = descript;
	}
}
