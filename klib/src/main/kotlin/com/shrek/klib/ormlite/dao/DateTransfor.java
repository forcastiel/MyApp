package com.shrek.klib.ormlite.dao;

import com.shrek.klib.ormlite.DBUtil;

import java.util.Date;

public class DateTransfor implements DBTransforDao<Date, String> {
	
	@Override
	public String parseFieldToColumn(Date fieldObj) {
		return String.format("%tF %tT",fieldObj,fieldObj);
	}

	@Override
	public Date parseColumnToField(String columnObj) {
		if(columnObj == null){
			return new Date();
		}
		return DBUtil.getFormatDate(columnObj);
	}

	@Override
	public void specialDoing() {

	}

	@Override
	public Object getFeildValueNull() {
		return null;
	}

	@Override
	public boolean isFeildNullFeild(Date f) {
		return f == null;
	}
	
}
