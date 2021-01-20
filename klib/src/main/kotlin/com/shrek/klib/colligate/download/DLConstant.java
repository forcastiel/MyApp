package com.shrek.klib.colligate.download;

public class DLConstant {

	/**
	 * 广播 以及广播的内容
	 */
	public static final String BROADCAST_TASK = "BROADCAST_TASK";
	public static final String DL_TASK_FLAG = "f";
	public static final String DL_TASK_MSG = "e";
	public static final String DL_TASK_OBJ = "d";

	/**
	 * 下载线程出现冲突 CONFLICT_DEFAULT 以这次为主 CONFLICT_LAST 以上次为主  CONFLICT_RETURN 返回
	 */
	public static final int CONFLICT_DEFAULT = 0x17;
	public static final int CONFLICT_LAST = 0x18;
	public static final int CONFLICT_RETURN = 0x19;

	/**
	 * 发生错误的时候 怎么做？ ERROR_DEFAULT返回 ERROR_AGAIN_ONCE 从新下载一次  ERROR_AGAIN_HEARTBEAT 心跳下载
	 */
	public static final int ERROR_AGAIN_HEARTBEAT = 0x35;
	public static final int ERROR_AGAIN_ONCE = 0x34;
	public static final int ERROR_DEFAULT = 0x33;

	//无效的下载数目
	public static final int NAN_THREAD_NUM = 1;

	/**
	 * 下载任务状态
	 */
	public static final int TASK_ERROR = 4;
	public static final int TASK_PAUSE = 3;
	public static final int TASK_RUN = 2;
	public static final int TASK_SUCESS = 5;
	public static final int TASK_WAIT = 1;

}