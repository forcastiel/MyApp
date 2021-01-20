package com.shrek.klib.colligate.download.bo;

import com.shrek.klib.ormlite.ann.DatabaseField;
import com.shrek.klib.ormlite.ann.DatabaseTable;
import com.shrek.klib.ormlite.bo.ZDb;

@DatabaseTable(tableName="DLThreadTask")
public class DLThreadTask extends ZDb{
	//断点位置
	@DatabaseField
	public volatile long breakPointPosition;
	
	//花费的时间
	@DatabaseField
	public int costTime;
	
	//要下载的长度
	@DatabaseField
	public long downloadBlock;
	
	//已经下载的长度
	@DatabaseField
	public volatile long hasDownloadLength;
	
	@DatabaseField(id = true)
	public int idCode;

	@DatabaseField
	public int taskHashCode;
	
	//线程id
	@DatabaseField
	public int threadId;

	public boolean isFinish() {
		if (hasDownloadLength >= downloadBlock){
			return true;
		}
		return false;
	}

	public void setDownloadBlock(long paramLong) {
		this.downloadBlock = paramLong;
	}

	public void setIdCode() {
		this.idCode = taskHashCode * 10+threadId;
	}

}
