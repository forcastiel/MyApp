package com.shrek.klib.ormlite.dao;

import com.shrek.klib.ormlite.DBUtil;

import java.util.Calendar;
import java.util.Date;

public class CalendarTransfor implements DBTransforDao<Calendar, String> {

	@Override
	public String parseFieldToColumn(Calendar fieldObj) {
		Date date = fieldObj.getTime();
		return String.format("%tF %tT",date,date);
	}

	@Override
	public Calendar parseColumnToField(String columnObj) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(DBUtil.getFormatDate(columnObj));
		return cal;
	}

	
	@Override
	public void specialDoing() {

	}

	@Override
	public Object getFeildValueNull() {
		return null;
	}

	@Override
	public boolean isFeildNullFeild(Calendar f) {
		return f==null;
	}
	
}
