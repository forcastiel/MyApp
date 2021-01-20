package com.shrek.klib.exception;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Looper;

import com.shrek.klib.logger.ZLog;

import java.lang.Thread.UncaughtExceptionHandler;

public class ZAppException implements UncaughtExceptionHandler {
	public static final String TAG = "CrashHandler";
	private static ZAppException instance;
	private Context mContext;
	boolean isShowException;
	private UncaughtExceptionHandler mDefaultHandler;

	private ZAppException(Context context, boolean isShowException) {
		mContext = context;
		this.isShowException = isShowException;
		// 得到default handler
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	public static ZAppException getInstance(Context context,boolean isShowException) {
		if (instance == null) {
			instance = new ZAppException(context,isShowException);
		}
		instance.mContext = context;
		instance.isShowException = isShowException;
		return instance;
	}

	@Override
	public synchronized void uncaughtException(Thread thread, Throwable ex) {
		ex.printStackTrace();
		if (handleException(ex)) {
			return;
		}
		if (mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(final Throwable ex) {
		if (ex == null) {
			return false;
		}
		new Thread() {
			@Override
			public void run() {
				try {
					Looper.prepareMainLooper();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext)
						.setTitle("程序崩溃了").setCancelable(false)
						.setMessage("程序崩溃了...")
						.setPositiveButton("关闭APP", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								android.os.Process
										.killProcess(android.os.Process.myPid());
								System.exit(10);
							}
						});
				// .setNegativeButton("重新启动", new OnClickListener() {
				//
				// @Override
				// public void onClick(DialogInterface arg0, int arg1) {
				// // TODO Auto-generated method stub
				// final Intent intent =
				// mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// mContext.startActivity(intent);
				// }
				//
				// });
				if (isShowException) {
					// mBuilder.setNegativeButton("debug", new OnClickListener()
					// {
					//
					// @Override
					// public void onClick(DialogInterface arg0, int arg1) {
					// Intent intent = new Intent(mContext,
					// ErrorActivity.class);
					// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
					// Intent.FLAG_ACTIVITY_NEW_TASK);
					// mContext.startActivity(intent);
					// }
					//
					// });
					StackTraceElement[] stes = ex.getStackTrace();
					StringBuffer sb = new StringBuffer();
					sb.append("----异常类型---\n" + ex.toString() + "\n\n");
					sb.append("----异常详情---\n");
					for (StackTraceElement ste : stes) {
						sb.append(ste.toString() + "\n");
					}
					mBuilder.setMessage(sb.toString());

					ZLog.e2P(sb.toString());
				}
				try {
					mBuilder.create().show();
				} catch (Exception e) {
					Looper.myLooper().quit();
					e.printStackTrace();
					System.exit(0);
				}

				// 取消息队列第一个
//				try {
//					Looper mainLooper = Looper.getMainLooper();
//					Field queueField = Looper.class.getDeclaredField("mQueue");
//					queueField.setAccessible(true);
//					MessageQueue queue = (MessageQueue) queueField.get(mainLooper);
//					
//					Method nextMethod = MessageQueue.class.getDeclaredMethod("next");
//					nextMethod.setAccessible(true);
//					Message msg = (Message) nextMethod.invoke(queue);
//					if (msg == null) {
//						return;
//					}
//					Field field = Message.class.getDeclaredField("target");
//					field.setAccessible(true);
//					Handler target = (Handler) field.get(msg);
//					target.dispatchMessage(msg);
//					final long newIdent = Binder.clearCallingIdentity();
//					msg.recycle();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				/**
				 * 加载类出现的异常
				 */
				 Looper.loop();
			}
		}.start();

		return true;
	}
}