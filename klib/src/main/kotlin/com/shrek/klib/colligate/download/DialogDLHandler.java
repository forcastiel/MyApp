package com.shrek.klib.colligate.download;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.shrek.klib.colligate.BaseUtils;
import com.shrek.klib.colligate.download.bo.DLTask;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.thread.HandlerEnforcer;
import com.shrek.klib.thread.ZThreadEnforcer;

/**
 * 默认的 弹出框 处理器
 *
 * @author shrek
 *
 */
public abstract class DialogDLHandler implements DLHandler {
	private ProgressDialog progressDialog;
	private Context ctx;
	private Handler handler;

	ZThreadEnforcer enforcer;

	public DialogDLHandler(Context ctx) {
		super();
		this.ctx = ctx;
		enforcer = HandlerEnforcer.newInstance();
	}

	@Override
	public int downLoadError(final DLTask task, Exception exception) {
		enforcer.enforceMainThread(new Runnable() {

			@Override
			public void run() {
				showNormalError(false, "出错啦", "下载出现异常", new Runnable() {

					@Override
					public void run() {
						BaseUtils.downloadFile(ctx, task, DialogDLHandler.this);
						ZLog.i(DialogDLHandler.this, "任务重试中,任务路径："
								+ task.downLoadUrl);
					}
				});
			}
		});

		return DLConstant.ERROR_DEFAULT;
	}

	@Override
	public abstract boolean isDLFileExist(DLTask task);

	@Override
	public void postDownLoadingOnUIThread(final DLTask task) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

		if (!task.isAutoOpen) {
//			showNormalError(true, "下载完成", "文件" + task.fileName + "下载完成!",
//					new Runnable() {
//
//						@Override
//						public void run() {
//							ZLog.i(DialogDLHandler.this,
//									"打开文件,任务路径：" + task.downLoadUrl);
//							BaseUtils.openFile(new File(new File(task.savePath),
//									task.fileName), ctx);
//						}
//					});
		}
	}

	@Override
	public void downLoadingProgressOnOtherThread(final DLTask task, final int hasDownSize) {
		if (progressDialog != null) {
			enforcer.enforceMainThread(new Runnable() {

				@Override
				public void run() {
					progressDialog.setMessage("正在下载" + task.fileName);
					progressDialog.setMax((int) task.totalSize);
					progressDialog.setProgress(hasDownSize);
				}
			});
		}
	}

	@Override
	public void preDownloadDoingOnUIThread(final DLTask task) {
		if (ctx != null) {
			progressDialog = new ProgressDialog(ctx);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle("下载中");
			progressDialog.setMessage("正在获取下载信息,请稍等...");
			progressDialog.setCancelable(false);
			progressDialog.setButton("取消下载", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					BaseUtils.stopDownLoad(ctx,task.downLoadUrl);
					progressDialog.dismiss();
				}
			});
//			progressDialog.setButton(1, "取消下载", new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					BaseUtils.stopDownLoad(ctx,task.downLoadUrl);
//					progressDialog.dismiss();
//				}
//			});

			if (ctx != null && progressDialog != null
					&& !progressDialog.isShowing())
				progressDialog.show();
		}

		handler = new Handler();
	}

	@Override
	public boolean sdcardNoExistOnUIThread(DLTask task) {
		AlertDialog.Builder build = new AlertDialog.Builder(ctx);
		build.setTitle("下载失败").setMessage("SD卡不存在,没法存储文件!")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		if (ctx != null) {
			build.show();
		}
		return false;
	}

	@Override
	public int threadNumConflictOnOtherThread(DLTask task, int oldThreadNum) {
		return DLConstant.CONFLICT_DEFAULT;
	}

	@Override
	public void openFileErrorOnOtherThread(final DLTask task, Exception e) {
		showNormalError(false, "出错啦", "打开文件失败!", new Runnable() {

			@Override
			public void run() {
				BaseUtils.downloadFile(ctx, task, DialogDLHandler.this);
				ZLog.i(DialogDLHandler.this, "任务重试中,任务路径："
						+ task.downLoadUrl);
			}
		});
	}

	private void showNormalError(boolean isSuccess, String errorTitle,
								 String errorContent, final Runnable run) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

		if (ctx != null && errorTitle != null && errorContent != null) {
			AlertDialog.Builder build = new AlertDialog.Builder(ctx);
			build.setTitle(errorTitle)
					.setMessage(errorContent)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int which) {
//									dialog.cancel();
									dialog.dismiss();
									// run.run();
								}
							});

			build.setNegativeButton(isSuccess ? "打开" : "重试",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if(run != null){
								run.run();
							}
							arg0.dismiss();
						}

					});

			if (ctx != null) {
				build.show();
			}
		}

	}
}
