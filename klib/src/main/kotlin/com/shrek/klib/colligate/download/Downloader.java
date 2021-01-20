package com.shrek.klib.colligate.download;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.shrek.klib.colligate.BaseUtils;
import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.colligate.download.bo.DLTask;
import com.shrek.klib.colligate.download.bo.DLThreadTask;
import com.shrek.klib.colligate.download.db.DLDatabaseHelper;
import com.shrek.klib.logger.ZLog;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Downloader {
    public static final int NOTIFICATION_ID = 0x22;
    // 所有任务的回掉
    public static Map<DLTask, DLHandler> allCallbacks;
    // 所有任务和 下载的线程
    public static volatile Map<DLTask, Set<DLThreadTask>> allTasks;

    // 默认的 路径
    private static File defaultSavePath;

    private static final int keepAliveTime = 3; // 线程池
    private static final int maxThreadPoolSize = 6; // 线程池最大线程数

    private Context context;
    // 错误的任务 用于任务失败后 重试
    private Map<DLTask, Integer> errorTasks;
    // private Notification infoNotification;

    private DLDatabaseHelper mDBOperator;
    // 执行任务线程池
    private ExecutorService mExecutorService;

    private Random mRandom;
    private SimpleDateFormat mSimpleDateFormat;
    // 拆分任务线程池
    private ExecutorService splitExecutor;

    public Downloader(Context mContext) {
        this.mRandom = new Random();
        this.context = mContext;
        if (allTasks == null) {
            allTasks = Collections
                    .synchronizedMap(new HashMap<DLTask, Set<DLThreadTask>>());
        }
        this.errorTasks = Collections
                .synchronizedMap(new HashMap<DLTask, Integer>());

        defaultSavePath = new File(Environment.getExternalStorageDirectory(),
                "baseDLLoader");
        if (!defaultSavePath.exists()) {
            boolean bool = defaultSavePath.mkdir();
            if (!bool)
                ZLog.i(Downloader.this, "baseDLLoader 外部文件夹创建失败!");
        }

        if (!defaultSavePath.exists()) {
            defaultSavePath = new File(Environment.getDataDirectory(),
                    "baseDLLoader");
            boolean bool = defaultSavePath.mkdir();
            if (!bool) {
                ZLog.i(Downloader.this, "baseDLLoader 下载文件夹创建失败!");
            }
        }

        mExecutorService = new ThreadPoolExecutor(maxThreadPoolSize,
                maxThreadPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()) {

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (r instanceof DownloadCallable) {
                    DownloadCallable callable = (DownloadCallable) r;
                    DLTask dlTask = callable.getTask();

                    final Set<DLThreadTask> sets = allTasks.get(dlTask);
                    boolean isFinish = true;
                    for (DLThreadTask dtTask1 : sets) {
                        if (!dtTask1.isFinish()) {
                            isFinish = false;
                            break;
                        }
                    }

                    if (isFinish) {
                        postTaskDoing(dlTask);
                    }
                }

            }
        };

        this.splitExecutor = Executors.newFixedThreadPool(1);

        mDBOperator = new DLDatabaseHelper(mContext);
        this.mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }


    public void stopTask(String downUrl) {
        if (StringUtils.isEmpty(downUrl)) {
            throw new NullPointerException("Download URL null !");
        }
        for (Map.Entry<DLTask, Set<DLThreadTask>> entry : allTasks.entrySet()) {
            DLTask task = entry.getKey();
            if (downUrl.equals(task.downLoadUrl)) {
                task.states.set(DLConstant.TASK_PAUSE);
            }
        }
    }

    /**
     * 添加任务
     */
    public void addTask(final DLTask mDLTask) {
        DLHandler mHandler = getTaskHandler(mDLTask);
        if (!BaseUtils.isSdCardExist()) {
            Toast.makeText(this.context, "SD卡不存在,无法下载.", Toast.LENGTH_SHORT)
                    .show();
            if (mHandler != null && !mHandler.sdcardNoExistOnUIThread(mDLTask)) {
                return;
            }
        }

        final Set<DLTask> tasks = allTasks.keySet();
        for (DLTask task : tasks) {
            if (task.equals(mDLTask)
                    && task.states.get() == DLConstant.TASK_RUN) {
                Toast.makeText(context,
                        "下载列表中已经存在该下载任务,如需重新下载,请删除原任务后,再次提交下载任务.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        splitExecutor.execute(new Runnable() {

            @Override
            public void run() {
                spliteTask(mDLTask);
            }
        });
    }

    private void download(DLTask mDLTask) throws Exception {
        if (mDLTask.savePath == null) {
            mDLTask.savePath = defaultSavePath.getPath();
        }
        File localFile = new File(mDLTask.savePath, mDLTask.fileName);

        // 算出 记录的总共下载的
        long sumSize = 0;
        final Set<DLThreadTask> dtTasks = allTasks.get(mDLTask);
        for (DLThreadTask dtTask : dtTasks) {
            sumSize += dtTask.hasDownloadLength;
        }
        DLHandler handler = getTaskHandler(mDLTask);
        if (localFile.exists()) {
//			if (localFile.length() == mDLTask.totalSize
//					&& sumSize == localFile.length()) { // 如果源文件和新文件大小不一样
            if (handler != null && !handler.isDLFileExist(mDLTask)) {
                localFile.delete();
                print("检测到文件夹中,有相同的文件名存在,做了删除处理!");
                // 清空线程下载记录
                for (DLThreadTask dtTask : dtTasks) {
                    dtTask.hasDownloadLength = 0;
                }
                mDBOperator.updateTasks(mDLTask, dtTasks);
            } else if (handler != null && handler.isDLFileExist(mDLTask)) {
                postTaskDoing(mDLTask);
                print("任务" + mDLTask.downLoadUrl + " 不用下载,上次已经存在!");
                return;
            }
        }

        try {
            for (DLThreadTask dtTask : dtTasks) {// 开启线程进行下载
                if (mDLTask.isNewTask || !dtTask.isFinish()) {
                    /*
                     * 判断线程是否已经完成下载,否则继续下载 添加线程任务 进线程池
					 */
                    DownloadCallable dc = new DownloadCallable(context,
                            mDLTask, dtTask, mDBOperator);
                    mExecutorService.execute(dc);
                } else {
                    //任务已经完成 执行安装
                    postTaskDoing(mDLTask);
                }
            }

        } catch (Exception e) {
            print(e.toString());
            sendTaskProgressBroadCast("下载失败!", mDLTask);
            throw new Exception("file download fail");
        }
    }

    public static DLTask getDLTask(String url) {
        final Set<DLTask> tasks = allTasks.keySet();
        for (DLTask task : tasks) {
            if (task.downLoadUrl != null && task.downLoadUrl.equals(url)) {
                return task;
            }
        }
        return null;
    }

    /**
     * 获取文件名
     */
    private String getFileName(HttpURLConnection conn, String downloadPath) {
        String filename = downloadPath
                .substring(downloadPath.lastIndexOf('/') + 1);
        print("通过HttpURLConnection获得文件名 : " + filename);
        if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
            for (int i = 0; ; i++) {
                String mine = conn.getHeaderField(i);
                if (mine == null)
                    break;
                if ("content-disposition".equals(conn.getHeaderFieldKey(i)
                        .toLowerCase())) {
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(
                            mine.toLowerCase());
                    if (m.find())
                        return m.group(1);
                }
            }
            filename = UUID.randomUUID() + ".tmp";// 默认取一个文件名
        }
        print("文件名 为:" + filename);
        return filename;
    }

    private DLHandler getTaskHandler(DLTask mDLTask) {
        if (allCallbacks != null) {
            return allCallbacks.get(mDLTask);
        }
        return null;
    }

    /**
     * 任务完成后 做什么
     */
    private void postTaskDoing(final DLTask mDLTask) {
        long deffTime = mDLTask.createTime - new Date().getTime();
        ZLog.i(Downloader.class, "下载任务" + mDLTask.downLoadUrl
                + "都完成了 \n 耗时:" + (deffTime / 1000L) + "s  实际记录耗时间："
                + mDLTask.costTime / 1000L);

        mDLTask.states.set(DLConstant.TASK_SUCESS);
        sendTaskProgressBroadCast(null, mDLTask);
        // if(){
        // mNotificationManager.cancel(NOTIFICATION_ID);
        // }

        Intent intent = BaseUtils.getOpenFileIntent(mDLTask.getSavePath(),
                context);
        // showNotify(intent, mDLTask.fileName);

        final DLHandler task = getTaskHandler(mDLTask);
        if (task != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    task.postDownLoadingOnUIThread(mDLTask);
                }
            });
        }

        if (mDLTask.isAutoOpen) {
            try {
                this.context.startActivity(intent);
                return;
            } catch (Exception exception) {
                Toast.makeText(this.context, "该文件类型无法打开!", Toast.LENGTH_LONG).show();
                if (task != null) {
                    task.openFileErrorOnOtherThread(mDLTask, exception);
                }
            }
        }

        allTasks.remove(mDLTask);
        allCallbacks.remove(mDLTask);
        errorTasks.remove(mDLTask);
    }

    private void print(String message) {
        ZLog.i(this, message);
    }

    /**
     * 错误任务重试
     *
     * @param mDLTask
     * @param delayTime
     */
    private final void retryErrorTask(final DLTask mDLTask, long delayTime) {
        Timer localTimer = new Timer();
        TimerTask timeTask = new TimerTask() {

            @Override
            public void run() {
                print("任务:" + mDLTask.downLoadUrl + " 现在开始重试下载!");
                Intent intent = new Intent(context, DownloadService.class);
                intent.putExtra("d", mDLTask);
                context.startService(intent);
            }
        };
        localTimer.schedule(timeTask, delayTime);
        print("任务:" + mDLTask.downLoadUrl + "  " + delayTime + "ms后重试下载!");
    }

    /**
     * 发送广播
     */
    private void sendTaskProgressBroadCast(String info, DLTask mDLTask) {
        if (mDLTask.isSendBrocadcast) {
            Intent intent = new Intent();
            intent.setAction(DLConstant.BROADCAST_TASK);
            intent.putExtra(DLConstant.DL_TASK_OBJ, mDLTask);
            intent.putExtra(DLConstant.DL_TASK_MSG, info);
            this.context.sendBroadcast(intent);
        }
    }

    public void destroyDownloader() {
        this.mExecutorService.shutdown();
        this.splitExecutor.shutdown();
        this.mExecutorService = null;
        this.splitExecutor = null;
        allTasks.clear();
        this.errorTasks.clear();
        this.mDBOperator = null;
    }

    /**
     * 出现错误做什么
     *
     * @param errorInfo
     * @param mDLTask
     * @param mException
     */
    public void errorDoing(String errorInfo, DLTask mDLTask,
                           Exception mException) {
        sendTaskProgressBroadCast(errorInfo, mDLTask);
        allTasks.remove(mDLTask);
        mDLTask.setErrorMessage(errorInfo);
        mDBOperator.updateTask(mDLTask);
        DLHandler mDLHandler = getTaskHandler(mDLTask);
        if (mDLHandler != null) {
            long delayTime = 0L;
            switch (mDLHandler.downLoadError(mDLTask, mException)) {
                case DLConstant.ERROR_AGAIN_ONCE:
                    if (errorTasks.containsKey(mDLTask)) {
                        errorTasks.remove(mDLTask);
                    } else {
                        delayTime = 30000L;
                        errorTasks.put(mDLTask, 1);
                    }
                    break;
                case DLConstant.ERROR_AGAIN_HEARTBEAT:
                    int lastCount = errorTasks.get(mDLTask).intValue();
                    lastCount++;
                    switch (lastCount) {
                        case 1:
                            delayTime = 30000L;
                            break;
                        case 2:
                            delayTime = 60000L;
                            break;
                        case 3:
                            delayTime = 300000L;
                            break;
                        case 4:
                            delayTime = 600000L;
                            break;
                        default:

                            break;
                    }
                    errorTasks.put(mDLTask, lastCount);
                    break;
                default:
                    break;
            }

            if (delayTime != 0) {
                retryErrorTask(mDLTask, delayTime);
            }
        }

    }

    /**
     * 获取Http响应头字段
     *
     * @param http
     * @return
     */
    public Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0; ; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null)
                break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }

    /**
     * 打印Http头字段
     *
     * @param http
     */
    public void printResponseHeader(HttpURLConnection http) {
        Map<String, String> header = getHttpResponseHeader(http);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey() + ":" : "";
            print(key + entry.getValue());
        }
    }

    public void spliteTask(DLTask mDLTask) {
        mDLTask.states.set(DLConstant.TASK_RUN);

        DLHandler mDLHandler = getTaskHandler(mDLTask);
        if (mDLTask.dlThreadNum > 0) {
            if (mDLTask.dlThreadNum > maxThreadPoolSize) {
                mDLTask.dlThreadNum = 1;
                sendTaskProgressBroadCast("任务设置线程数量异常,使用默认线程数量下载!", mDLTask);
            }
        }

        //比较原来的任务和现在的任务是否一致
        Set<DLThreadTask> dtTasks = new HashSet<>();
        Set<DLThreadTask> oldDTTasks = mDBOperator.getDownloadTaskByPath(mDLTask .hashCode());
        int oldThreadSize = oldDTTasks.size();
        sendTaskProgressBroadCast("从数据库中获取下载信息条数:" + oldThreadSize, mDLTask);

        //是否开启新任务
        boolean isNewTask = mDLTask.isNewTask;
        
        if (oldThreadSize > 0 && oldThreadSize != mDLTask.dlThreadNum) {
            if (mDLHandler != null) {
                switch (mDLHandler.threadNumConflictOnOtherThread(mDLTask, oldThreadSize)) {
                    case DLConstant.CONFLICT_LAST:
                        DLTask dbDLTask = mDBOperator.getTaskByPath(mDLTask.downLoadUrl);
                        mDLTask = dbDLTask;
                        dtTasks = oldDTTasks;
                        break;
                    case DLConstant.CONFLICT_RETURN:
                        print("下载线程数目与上一次不符合,用户设置退出下载!");
                        return;
                    default:
                        // 以这次为主
                        mDBOperator.delete(mDLTask.hashCode());
                        isNewTask = true;
                        break;
                }
            } else {
                // 以这次为主
                mDBOperator.delete(mDLTask.hashCode());
                isNewTask = true;
            }
        }

        // 初始化下载线程
        if(isNewTask){
            for (int i = 0; i < mDLTask.dlThreadNum; i++) {
                DLThreadTask dtt = new DLThreadTask();
                dtt.threadId = i;
                dtt.taskHashCode = dtTasks.hashCode();
                dtTasks.add(dtt);
            }
            mDBOperator.saveThTasks(mDLTask.hashCode(), dtTasks);
        } 
        allTasks.put(mDLTask, dtTasks);

		/*
		 * 连接 获取文件信息
		 */
        HttpURLConnection conn = null;
        try {
            File saveDir = null;
            if (mDLTask.savePath != null) {
                saveDir = new File(mDLTask.savePath);
                if (!saveDir.exists()) {
                    saveDir.mkdir();
                }
            } else {
                mDLTask.savePath = defaultSavePath.getPath();
            }

            URL url = new URL(mDLTask.downLoadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(
                    "Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/pdf, application/msword, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", mDLTask.downLoadUrl);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                long fileSize = conn.getContentLength();// 根据响应获取文件大小
                if (fileSize <= 0) {
                    fileSize = BaseUtils.parseLong(conn.getHeaderField("File-Length"));
                }
                sendTaskProgressBroadCast("请求获得文件大小:" + fileSize, mDLTask);
                mDLTask.totalSize = fileSize;
                if (fileSize <= 0) {
                    Exception e = new Exception("无法通过网络获得文件大小!");
                    errorDoing("请求获得文件大小:" + fileSize, mDLTask, e);
                    throw e;
                }

                // 计算每条线程应该下载的数据长度
                long avgSize = fileSize / mDLTask.dlThreadNum;
                HashMap<Integer, Long> block = new HashMap<Integer, Long>();
                for (int i = 0; i < mDLTask.dlThreadNum; i++) {
                    if (i == mDLTask.dlThreadNum - 1) {
                        block.put(i, avgSize
                                + (fileSize - (avgSize * mDLTask.dlThreadNum)));

                    } else {
                        block.put(i, avgSize);
                    }
                    print("第" + (i + 1) + "线程需要下载的大小为" + block.get(i));
                    sendTaskProgressBroadCast("第" + (i + 1) + "线程需要下载的大小为"
                            + block.get(i), mDLTask);
                }


                if (mDLTask.fileName == null) {
                    mDLTask.fileName = getFileName(conn, mDLTask.downLoadUrl);
                } else {
                    print("下载文件名 : " + mDLTask.fileName);
                }

                // File saveFile = new File(mDLTask.savePath, fileName);//
                // 构建保存文件
                mDLTask.totalSize = fileSize;
                if (isNewTask) {
					/*
					 * 源文件不存在 无下载记录 所有数据重置
					 */
                    for (DLThreadTask dtTask : dtTasks) {
                        dtTask.breakPointPosition = avgSize * dtTask.threadId;
                        dtTask.downloadBlock = block.get(dtTask.threadId);
                        dtTask.taskHashCode = mDLTask.hashCode();
                        // tasks.get(i).setGuid(UUID.randomUUID().toString());
                        dtTask.costTime = 0;
                        dtTask.hasDownloadLength = 0;
                        // tasks.add(tasks.get(i));
                    }

                } else {
                    // 数据库有下载记录
                    for (DLThreadTask dtTask : dtTasks) {
                        dtTask.breakPointPosition = avgSize * dtTask.threadId;
                        dtTask.downloadBlock = block.get(dtTask.threadId);
                        dtTask.taskHashCode = mDLTask.hashCode();

                        sendTaskProgressBroadCast("下载名称：" + mDLTask.fileName
                                        + "  下载线程===>" + dtTask.threadId
                                        + "  已经下载长度====>" + dtTask.hasDownloadLength,
                                mDLTask);
                    }
                }
                mDBOperator.updateTask(mDLTask);
                mDBOperator.updateTasks(mDLTask, dtTasks);

                download(mDLTask);
            } else {
                String errorInfo = "请求服务地址下载无响应!";
                Exception e = new Exception(errorInfo);
                errorDoing(errorInfo, mDLTask, e);
                throw e;
            }

        } catch (MalformedURLException e) {
            String errorInfo = "创建URL地址出现错误!";
            errorDoing(errorInfo, mDLTask, e);
            e.printStackTrace();
        } catch (IOException e) {
            String errorInfo = "网络数据传输异常!";
            errorDoing(errorInfo, mDLTask, e);
            e.printStackTrace();
        } catch (Exception e) {
            String errorInfo = "下载地址连接失败,请确认下载地址是否有效";
            errorDoing(errorInfo, mDLTask, e);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
