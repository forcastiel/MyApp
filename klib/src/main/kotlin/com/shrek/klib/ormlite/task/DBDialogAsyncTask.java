package com.shrek.klib.ormlite.task;

import android.app.ProgressDialog;
import android.content.Context;

import com.shrek.klib.ormlite.ZDBHelper;

/**
 * 到弹出框的数据库异步请求
 * @author shrek
 *
 */
public abstract class DBDialogAsyncTask extends DBAsyncTask {
	private String title;
	private String message;
	private ProgressDialog progressDialog;
	Context ctx;

	public DBDialogAsyncTask(Context ctx , ZDBHelper mHelper, boolean isTransaction, String title, String message) {
		super(mHelper, isTransaction);
		this.title = title;
		this.message = message;
		
		this.ctx = ctx;
	}

	@Override
	protected final void onPreExecute() {
		super.onPreExecute();
		progressDialog = new ProgressDialog(ctx);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle(this.title);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(false);
		progressDialog.show();
		onPreDoing();
	}
	
	protected void onPreDoing(){
		
	}
	
	@Override
	protected abstract Integer enforcerBackground(ZDBHelper mHelper);

	
	protected void onPostDoing(Integer result){
		
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		onPostDoing(result);
	}

}
