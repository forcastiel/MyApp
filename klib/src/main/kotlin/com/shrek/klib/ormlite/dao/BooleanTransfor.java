package com.shrek.klib.ormlite.dao;

public class BooleanTransfor implements DBTransforDao<Boolean, Integer> {

	@Override
	public Integer parseFieldToColumn(Boolean fieldObj) {
		return fieldObj?1:0;
	}

	@Override
	public Boolean parseColumnToField(Integer columnObj) {
		if(columnObj == null){
			return false;
		}
		return columnObj == 1;
	}

	@Override
	public void specialDoing() {

	}

	@Override
	public Object getFeildValueNull() {
		return false;
	}

	@Override
	public boolean isFeildNullFeild(Boolean f) {
		return false;
	}

	
}
