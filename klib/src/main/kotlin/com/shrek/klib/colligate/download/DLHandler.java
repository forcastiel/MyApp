package com.shrek.klib.colligate.download;

import com.shrek.klib.colligate.download.bo.DLTask;

public interface DLHandler {
	/**
	 * 下载时候出现错误 <不一定是主线程>
	 * @param task
	 * @param exception
	 * @return
	 */
	public int downLoadError(DLTask task,
							 Exception exception);

	/**
	 * 下载时候 文件存在
	 * @param task
	 * @return 是否不删除 true 不删除   false 删除
	 */
	public boolean isDLFileExist(DLTask task);

	/**
	 * 下载完成 做什么
	 * @param task
	 */
	public void postDownLoadingOnUIThread(DLTask task);

	/**
	 * 下载过程中 返回 <非UI线程>
	 */
	public void downLoadingProgressOnOtherThread(DLTask task, int hasDownSize);

	/**
	 * 下载前做什么<UI动作在主线程做>
	 * @param task
	 */
	public void preDownloadDoingOnUIThread(DLTask task);

	/**
	 * sdcard没有的时候 做什么动作 <UI线程>
	 * @param task
	 * @return 是否下载
	 */
	public boolean sdcardNoExistOnUIThread(DLTask task);

	/**
	 * 下载线程 冲突 怎么办 <非UI线程>
	 * @param task
	 * @param oldThreadNum  原来几个线程
	 * @return  CONFLICT_DEFAULT
	 */
	public int threadNumConflictOnOtherThread(DLTask task, int oldThreadNum);

	/**
	 * 下载完成后 打开文件失败 <非UI线程>
	 * @param task
	 * @param e
	 */
	public void openFileErrorOnOtherThread(DLTask task, Exception e);
}
