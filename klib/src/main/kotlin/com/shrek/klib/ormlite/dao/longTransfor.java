package com.shrek.klib.ormlite.dao;


public class longTransfor implements DBTransforDao<Long, String> {

	@Override
	public String parseFieldToColumn(Long fieldObj) {
		return fieldObj+"";
	}

	@Override
	public Long parseColumnToField(String columnObj) {
		return Long.parseLong(columnObj);
	}

	@Override
	public void specialDoing() {

	}

	@Override
	public Object getFeildValueNull() {
		return null;
	}

	@Override
	public boolean isFeildNullFeild(Long f) {
		return f == null;
	}
	
}
