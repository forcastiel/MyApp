package com.shrek.klib.ormlite.dao;

import com.shrek.klib.ormlite.stmt.StmtBuilder;

public class StringTransfor implements DBTransforDao<String, String> {

	@Override
	public String parseFieldToColumn(String fieldObj) {
		return "'"+fieldObj+"'";
	}

	@Override
	public String parseColumnToField(String columnObj) {
		return columnObj;
	}

	@Override
	public void specialDoing() {

	}

	@Override
	public Object getFeildValueNull() {
		return StmtBuilder.NULL_STR;
	}

	@Override
	public boolean isFeildNullFeild(String f) {
		return StmtBuilder.NULL_STR.equals(f);
	}

}
