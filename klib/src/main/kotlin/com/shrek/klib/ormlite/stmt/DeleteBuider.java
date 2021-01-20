package com.shrek.klib.ormlite.stmt;

import com.shrek.klib.ormlite.bo.ZDb;

public class DeleteBuider extends StmtBuilder<DeleteBuider> {
	
	public DeleteBuider(Class<? extends ZDb> clazz) {
		super(clazz);
	}

	@Override
	public String getSql() {
		return "DELETE FROM "+tableInfo.getTableName()+" WHERE "+getWhereSql();
	}

	protected void appendWhereStr(String str){
		if(whereBuffer.length() == 0){
//			whereBuffer.append(WHERE_KEYWORD);
		}
		whereBuffer.append(str);
	}
}
