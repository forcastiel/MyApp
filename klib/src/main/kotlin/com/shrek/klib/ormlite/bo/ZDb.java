package com.shrek.klib.ormlite.bo;

//import com.google.gson.annotations.Expose;
import com.alibaba.fastjson.annotation.JSONField;
import com.shrek.klib.ormlite.ann.DatabaseField;

import java.util.Date;

public class ZDb {
	
	// 创建时间
	@DatabaseField
//	@Expose(serialize=false,deserialize=false)
	@JSONField(serialize = false,deserialize = false)
	public Date createTime;
	
	// 是否过期
	@DatabaseField
//	@Expose(serialize=false,deserialize=false)
	@JSONField(serialize = false,deserialize = false)
	public boolean isExpired;

	public ZDb() {
		super();
		createTime = new Date();
		isExpired = false;
	}

}
