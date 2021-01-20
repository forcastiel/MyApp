package com.shrek.klib.colligate.download;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.shrek.klib.colligate.download.bo.DLTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadService extends Service {

	private static final String TAG = "UpdateService";

	public static AtomicBoolean isServiceShutDown;
	private Downloader mDownloader;

	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		isServiceShutDown = new AtomicBoolean(false);

		mDownloader = new Downloader(this);
	}

	public void onDestroy() {
		super.onDestroy();
		mDownloader.destroyDownloader();
		isServiceShutDown.set(true);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			int action = intent.getIntExtra(DLConstant.DL_TASK_FLAG,DLConstant.TASK_RUN);
			if(action == DLConstant.TASK_PAUSE){
				String url = intent.getStringExtra("d");
				mDownloader.stopTask(url);
			} else {
				DLTask task = (DLTask) intent.getSerializableExtra("d");
				mDownloader.addTask(task);
			}

		}
		return super.onStartCommand(intent, flags, startId);
	}

//	private void showNotify() {
//		notification.contentView = new RemoteViews(getApplication()
//				.getPackageName(), R.layout.update_notify);
//		notification.contentView.setTextViewText(R.id.notifyUI_progress,
//				"点击查看详情");
//		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
//		notification.flags |= Notification.FLAG_NO_CLEAR;
//		Intent i = new Intent(this, BaseDownLoadActivity.class);
//		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//				| Intent.FLAG_ACTIVITY_NEW_TASK);
//		PendingIntent contentIntent = PendingIntent.getActivity(this,
//				R.string.app_name, i, PendingIntent.FLAG_UPDATE_CURRENT);
//		notification.contentIntent = contentIntent;
//		if (mNotificationManager != null) {
//			mNotificationManager.notify(NOTIFICATION_ID, notification);
//		} else {
//			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//			mNotificationManager.notify(NOTIFICATION_ID, notification);
//		}
//	}

}