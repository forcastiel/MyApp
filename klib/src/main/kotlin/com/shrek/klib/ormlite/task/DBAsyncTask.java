package com.shrek.klib.ormlite.task;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.DBUtil;
import com.shrek.klib.ormlite.ZDBHelper;

/**
 * 最好就是增删改操作  查询异步最后自己实现task类
 * @author shrek
 *
 */
public abstract class DBAsyncTask extends AsyncTask<Void, Void, Integer> {
	private ZDBHelper mHelper;
	//是否开启事务
	private boolean isTransaction;
	
	public DBAsyncTask(ZDBHelper mHelper,boolean isTransaction){
		if (mHelper == null) {
			throw new IllegalArgumentException("数据库异步任务 至少传入 ZWDBHelper对象");
		}
		this.mHelper = mHelper;
		this.isTransaction = isTransaction;
	}
	
	public DBAsyncTask(ZDBHelper mHelper){
		this(mHelper,false);
	}
	
	@Override
	protected final Integer doInBackground(Void... arg0) {
		mHelper.lockOperator();
		int optNum = 0;
		synchronized (ZDBHelper.LOCK_OBJ) {
			long before = System.currentTimeMillis();
			SQLiteDatabase db = mHelper.getDatabase(false);
			
			if(isTransaction){
				db.beginTransaction();  //手动设置开始事务
			}
			try {
				optNum = enforcerBackground(mHelper);
				if(isTransaction){
					db.setTransactionSuccessful(); //处理完成
				}
			} catch (Exception e) {
				e.printStackTrace();
				ZLog.i(this, "数据库操作失败 事务回滚!");
			}finally{
				if(isTransaction){
					db.endTransaction(); //处理完成
				}	
			}
			long after = System.currentTimeMillis();
			DBUtil.timeCompute(before, after);
		}
		
		mHelper.unLockOperator();
		return optNum;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
	}
	
	protected abstract Integer enforcerBackground(ZDBHelper mHelper);
}
