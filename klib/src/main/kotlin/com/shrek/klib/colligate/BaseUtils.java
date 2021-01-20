package com.shrek.klib.colligate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.shrek.klib.colligate.download.DLConstant;
import com.shrek.klib.colligate.download.DLHandler;
import com.shrek.klib.colligate.download.DownloadService;
import com.shrek.klib.colligate.download.Downloader;
import com.shrek.klib.colligate.download.bo.DLTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 工具类
 * 
 * @author shrek
 * 
 */
public class BaseUtils {

	public static final DecimalFormat df = new DecimalFormat("#.00");

	/**
	 * 通过名字 得到id的值
	 * 
	 * @param idName
	 * @return
	 */
	public static int getIdValueIntoR(Context ctx, String idName) {
		return ctx.getResources().getIdentifier(idName, "id",
				ctx.getPackageName());
	}

	/**
	 * 检测网络链接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 打开文件
	 * 
	 * @param file
	 */
	public static void openFile(File file, Context context) {
		Intent intent = new Intent();

		String fName = file.getName();
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0) {
			// 文件没有后缀名
			Toast.makeText(context, "无法识别的文件类型!", Toast.LENGTH_LONG).show();
			return;
		}
		/* 获取文件的后缀名 */
		// String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		// if (end.indexOf("txt") != -1 || end.indexOf("htm") != -1) {
		// intent.setClass(context, ShowTxtAndWeb.class);
		// intent.putExtra("url", file.getPath());
		// context.startActivity(intent);
		// } else {

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		// 获取文件file的MIME类型
		String type = getMIMEType(file);
		// 设置intent的data和Type属性。
		intent.setDataAndType(Uri.fromFile(file), type);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(context, "该文件类型无法打开!", Toast.LENGTH_LONG).show();
		}

	}

	// 建立一个MIME类型与文件后缀名的匹配表
	static final String[][] MIME_MapTable = {
			// {后缀名， MIME类型}
			{ ".3gp", "video/3gpp" },
			{ ".apk", "application/vnd.android.package-archive" },
			{ ".asf", "video/x-ms-asf" }, { ".avi", "video/x-msvideo" },
			{ ".bin", "application/octet-stream" }, { ".bmp", "image/bmp" },
			{ ".c", "text/plain" }, { ".class", "application/octet-stream" },
			{ ".conf", "text/plain" }, { ".cpp", "text/plain" },
			{ ".doc", "application/msword" }, { ".dot", "application/msword" },

			{ ".xla", "application/vnd.ms-excel" },
			{ ".xlc", " application/vnd.ms-excel" },
			{ ".xll", "application/vnd.ms-excel" },
			{ ".xlm", " application/vnd.ms-excel" },
			{ ".xls", "application/vnd.ms-excel" },
			{ ".xlt", " application/vnd.ms-excel" },
			{ ".xlw", "application/vnd.ms-excel" },

			{ ".exe", "application/octet-stream" }, { ".gif", "image/gif" },
			{ ".gtar", "application/x-gtar" }, { ".gz", "application/x-gzip" },
			{ ".h", "text/plain" }, { ".htm", "text/html" },
			{ ".html", "text/html" }, { ".jar", "application/java-archive" },
			{ ".java", "text/plain" }, { ".jpeg", "image/jpeg" },
			{ ".jpg", "image/jpeg" }, { ".js", "application/x-javascript" },
			{ ".log", "text/plain" }, { ".m3u", "audio/x-mpegurl" },
			{ ".m4a", "audio/mp4a-latm" }, { ".m4b", "audio/mp4a-latm" },
			{ ".m4p", "audio/mp4a-latm" }, { ".m4u", "video/vnd.mpegurl" },
			{ ".m4v", "video/x-m4v" }, { ".mov", "video/quicktime" },
			{ ".mp2", "audio/x-mpeg" }, { ".mp3", "audio/x-mpeg" },
			{ ".mp4", "video/mp4" },
			{ ".mpc", "application/vnd.mpohun.certificate" },
			{ ".mpe", "video/mpeg" }, { ".mpeg", "video/mpeg" },
			{ ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" },
			{ ".mpga", "audio/mpeg" },
			{ ".msg", "application/vnd.ms-outlook" }, { ".ogg", "audio/ogg" },
			{ ".pdf", "application/pdf" }, { ".png", "image/png" },
			{ ".pps", "application/vnd.ms-powerpoint" },
			{ ".ppt", "application/vnd.ms-powerpoint" },
			{ ".prop", "text/plain" },
			{ ".rar", "application/x-rar-compressed" },
			{ ".rc", "text/plain" }, { ".rmvb", "audio/x-pn-realaudio" },
			{ ".rtf", "application/rtf" }, { ".sh", "text/plain" },
			{ ".tar", "application/x-tar" },
			{ ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },

			{ ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
			{ ".wmv", "audio/x-ms-wmv" },
			{ ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
			{ ".z", "application/x-compress" }, { ".zip", "application/zip" },
			{ "", "*/*" } };

	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param file
	 */
	public static String getMIMEType(File file) {
		String type = "*/*";
		String fName = file.getName();
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0) {
			return type;
		}
		/* 获取文件的后缀名 */
		String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		if (end == "")
			return type;
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

	/**
	 * 将str转化为int 如果出现异常 返回0
	 * 
	 * @param str
	 * @return
	 */
	public static int parseInt(String str, int defaultValue) {
		int i = defaultValue;
		if (str != null && !str.equals("")) {
			try {
				i = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				i = defaultValue;
			}
		}
		return i;
	}

	public static int parseInt(String str) {
		return parseInt(str, 0);
	}

	public static long parseLong(String str, long defaultValue) {
		long i = defaultValue;
		if (str != null && !str.equals("")) {
			try {
				i = Long.parseLong(str);
			} catch (NumberFormatException e) {
				i = defaultValue;
			}
		}
		return i;
	}

	public static boolean parseBoolean(String str) {
		return parseBoolean(str, false);
	}

	public static boolean parseBoolean(String str, boolean defaultValue) {
		boolean i = defaultValue;
		if (str != null && !str.equals("")) {
			try {
				i = Boolean.parseBoolean(str);
			} catch (NumberFormatException e) {
				i = defaultValue;
			}
		}
		return i;
	}

	public static long parseLong(String str) {
		return parseLong(str, 0);
	}

	public static double parseDouble(String str, double defaultValue) {
		double i = defaultValue;
		if (str != null && !str.equals("")) {
			try {
				i = Double.parseDouble(str);
			} catch (NumberFormatException e) {
				i = defaultValue;
			}
		}
		return i;
	}

	public static double parseDouble(String str) {
		return parseDouble(str, 0);
	}

	public static Float parseFloat(String str, float defaultValue) {
		float i = defaultValue;
		if (str != null && !str.equals("")) {
			try {
				i = Float.parseFloat(str);
			} catch (NumberFormatException e) {
				i = defaultValue;
			}
		}
		return i;
	}

	public static Float parseFloat(String str) {
		return parseFloat(str, 0);
	}

	/**
	 * 判断 字符串 是否有效
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isStringValid(String string) {
		return string != null && !"".equals(string);
	}

	/**
	 * 判断 是不是这个Activity
	 */
	@SuppressWarnings("unchecked")
	public static <T> T judgeContextToActivity(Object obj,
			Class<? extends Context> activityClass) {
		if (obj != null) {
			Class<?> ctxClass = obj.getClass();
			if (ctxClass.hashCode() == activityClass.hashCode()) {

				T futureObj = (T) obj;
				return futureObj;
			}
		}
		return null;
	}

	/**
	 * #########################################################################
	 * ############################ 图片处理类 ###################################
	 * #########################################################################
	 */

	/**
	 * 通过用户指定的 大小 加载图片
	 * 
	 * @param filePath
	 * @param imgWidth
	 *            为-1采用原图大小 0 不以宽做等比缩放 其他就是缩放后的图片大小
	 * @param imgHeight
	 * @return
	 * @throws Exception
	 */
	public static Bitmap decodeBitmapWithOps(String filePath, int imgWidth,
			int imgHeight) throws Exception {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		int orgHeight = options.outHeight;
		int orgWidth = options.outWidth;
		if (imgWidth == -1) {
			imgWidth = orgWidth;
		}

		if (imgHeight == -1) {
			imgHeight = orgWidth;
		}

		int inSampleSize = 1;

		if (imgHeight == 0) {
			inSampleSize = Math.round((float) orgWidth / (float) imgWidth);
		} else if (imgWidth == 0) {
			inSampleSize = Math.round((float) orgHeight / (float) imgHeight);
		} else {
			if (orgWidth > orgHeight) {
				inSampleSize = Math
						.round((float) orgHeight / (float) imgHeight);
			} else {
				inSampleSize = Math.round((float) orgWidth / (float) imgWidth);
			}
		}
		//
		// for (int i = 0; i < 10; i++) {
		// if(inSampleSize<=(2^i)){
		// inSampleSize = 2^i;
		// break;
		// }
		// }
		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		return bitmap;
	}

	/**
	 * Bitmap → Drawable
	 */
	@SuppressWarnings("deprecation")
	public static Drawable bitmap2Drawable(Bitmap bm) {
		if (bm == null) {
			return null;
		}
		BitmapDrawable bd = new BitmapDrawable(bm);
		bd.setTargetDensity(bm.getDensity());
		return new BitmapDrawable(bm);
	}

	/**
	 * Drawable → Bitmap
	 */
	@SuppressWarnings("deprecation")
	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * byte[] → Bitmap
	 */
	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(b, 0, b.length);
	}

	/**
	 * Bitmap → byte[]
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		if (bm == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// public static void downloadFile(Context context, DLTask mDLTask,
	// DLHandler handler){
	// if (handler != null ){
	// if (Downloader.allCallbacks == null){
	// Downloader.allCallbacks = new WeakHashMap();
	// }
	// Downloader.allCallbacks.put(paramDLTask, paramDLHandler);
	// }
	// Intent localIntent1 = new Intent(paramContext, DownloadService.class);
	// Intent localIntent2 = localIntent1.putExtra("d", paramDLTask);
	// ComponentName localComponentName =
	// paramContext.startService(localIntent1);
	// }

	/**
	 * 通过size 获取文件大小显示字符串
	 * 
	 * @param size
	 * @return
	 */
	public static String getFileSize(long size) {
		String sizeStr = "0B";
		if (size < 1024L) {
			sizeStr = size + "B";
		} else if (size >= 1024L && size < 1024 * 1024) {
			sizeStr = df.format(size / 1024.0) + "K";
		} else if (size >= 1024 * 1024 && size < 1024 * 1024 * 1024) {
			sizeStr = df.format(size / (1024.0 * 1024)) + "M";
		} else {
			sizeStr = df.format(size / (1024.0 * 1024 * 1024)) + "G";
		}
		return sizeStr;
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
			os.flush();
		} catch (Exception ex) {
		}
	}

	/**
	 * 判断sd卡是否存在
	 */
	public static boolean isSdCardExist() {
		boolean isExist = false;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			isExist = true;
		}
		return isExist;
	}

	/**
	 * 隐藏键盘
	 * 
	 * @param act
	 * @param token
	 */
	public static void hideSoftInput(Activity act, IBinder token) {
		if (token != null) {
			InputMethodManager im = (InputMethodManager) act
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			im.hideSoftInputFromWindow(token,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
	 * 
	 * @param v
	 * @param event
	 * @return
	 */
	public static boolean isShouldHideInput(View v, MotionEvent event,
			Collection<View> keyboardFocusViews) {
		if (v != null && v instanceof EditText) {
			View[] views;
			if (keyboardFocusViews != null) {
				views = new View[keyboardFocusViews.size() + 1];
				views[0] = v;

				int temp = 1;
				for (View focusView : keyboardFocusViews) {
					views[temp] = focusView;
					temp++;
				}
			} else {
				views = new View[1];
				views[0] = v;
			}

			return !isViewInArea(event.getX(), event.getY(), views);
		}
		// 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
		return false;
	}

	/**
	 * 判断坐标 是不是在这个区域内
	 * 
	 * @param pointX
	 * @param pointY
	 * @param keyboardFocusViews
	 * @return
	 */
	public static boolean isViewInArea(float pointX, float pointY,
			View... keyboardFocusViews) {
		// boolean isInArea = false;
		for (View view : keyboardFocusViews) {
			int[] location = { 0, 0 };
			view.getLocationInWindow(location);

			int left = location[0], top = location[1], bottom = top
					+ view.getHeight(), right = left + view.getWidth();
			if (pointX > left && pointX < right && pointY > top
					&& pointY < bottom) {
				return true;
			}

		}
		return false;
	}

	/**
	 * 判断 能否ping通服务器地址
	 * 
	 * @param ipAddress
	 * @return
	 */
	public static boolean pingHost(final String ipAddress) {
		FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				Process p = Runtime.getRuntime().exec(
						"ping -c 1 -t 5 -W 100 " + ipAddress);
				ThreadUtil stdoutUtil = new ThreadUtil(p.getInputStream());
				ThreadUtil erroroutUtil = new ThreadUtil(p.getErrorStream());
				// 启动线程读取缓冲区数据
				stdoutUtil.start();
				erroroutUtil.start();
				
				int status = p.waitFor();
				p.destroy();
				return status == 0;
			}
		}) ;
		
		new Thread(task).start();
		
		try {
			boolean result = task.get(2, TimeUnit.SECONDS);
			return result;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}
		return false;
		
//		try {
//			Process p = Runtime.getRuntime().exec(
//					"ping -c 1 -t 5 -W 100 " + ipAddress);
//			// 创建2个线程，分别读取输入流缓冲区和错误流缓冲区
//			ThreadUtil stdoutUtil = new ThreadUtil(p.getInputStream());
//			ThreadUtil erroroutUtil = new ThreadUtil(p.getErrorStream());
//			// 启动线程读取缓冲区数据
//			stdoutUtil.start();
//			erroroutUtil.start();
//			int status = p.waitFor();
//			p.destroy();
//			return status == 0;
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		return false;
	}

	static class ThreadUtil implements Runnable {
		// 设置读取的字符编码
		private String character = "GB2312";
		private List<String> list;
		private InputStream inputStream;

		public ThreadUtil(InputStream inputStream) {
			this.inputStream = inputStream;
			this.list = new ArrayList<String>();
		}

		public void start() {
			Thread thread = new Thread(this);
			thread.setDaemon(true);// 将其设置为守护线程
			thread.start();
		}

		public void run() {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(inputStream,
						character));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line != null) {
						list.add(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					// 释放资源
					inputStream.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 返回打开文件的意图
	 * 
	 * @param file
	 */
	public static Intent getOpenFileIntent(File file, Context context) {
		Intent intent = new Intent();

		String fName = file.getName();
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0) {
			// 文件没有后缀名
			Toast.makeText(context, "无法识别的文件类型!", Toast.LENGTH_LONG).show();
			return null;
		}
		/* 获取文件的后缀名 */
		// String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		// 获取文件file的MIME类型
		String type = getMIMEType(file);
		// 设置intent的data和Type属性。
		intent.setDataAndType(Uri.fromFile(file), type);
		return intent;

	}

	/**
	 * 根据文件名 得到对应的 图标
	 */
//	public static int getIcon(File f) {
//		if (!f.isDirectory()) {
//			// 文件
//			String name = f.getName().toLowerCase();
//			if (name.indexOf(".txt") > 0) {
//				return R.drawable.mimetype_text_txt;
//			} else if (name.indexOf(".doc") > 0) {
//				return R.drawable.mimetype_office_doc;
//			} else if (name.indexOf(".apk") > 0) {
//				return R.drawable.mimetype_app_apk;
//			} else if (name.indexOf(".htm") > 0) {
//				return R.drawable.mimetype_htm_html;
//			} else if (name.indexOf(".png") > 0 || name.indexOf(".jpg") > 0
//					|| name.indexOf(".bmp") > 0 || name.indexOf(".gif") > 0) {
//				return R.drawable.mimetype_img;
//			} else if (name.indexOf(".pdf") > 0) {
//				return R.drawable.mimetype_office_pdf;
//			} else if (name.indexOf(".mp3") > 0 || name.indexOf(".wma") > 0) {
//				return R.drawable.mimetype_sound;
//			} else if (name.indexOf(".mp4") > 0 || name.indexOf(".rm") > 0
//					|| name.indexOf(".rmvb") > 0) {
//				return R.drawable.mimetype_video;
//			}
//		} else {
//			// 文件夹
//			return R.drawable.mimetype_folder;
//		}
//
//		return R.drawable.mimetype_null;
//	}


	public static void downloadFile(Context context, DLTask task,
			DLHandler handler) {
		if (handler != null) {
			if (Downloader.allCallbacks == null) {
				Downloader.allCallbacks = new ConcurrentHashMap<DLTask, DLHandler>();
			}
			Downloader.allCallbacks.put(task, handler);
			handler.preDownloadDoingOnUIThread(task);
		}
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(DLConstant.DL_TASK_OBJ, task);
		context.startService(intent);
	}

	public static void stopDownLoad(Context context, String dlPath){
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(DLConstant.DL_TASK_OBJ, dlPath);
		intent.putExtra(DLConstant.DL_TASK_FLAG, DLConstant.TASK_PAUSE);
		context.startService(intent);
	}
}
