package com.shrek.klib.colligate.download;


import android.content.Context;
import android.content.Intent;

import com.shrek.klib.colligate.download.bo.DLTask;
import com.shrek.klib.colligate.download.bo.DLThreadTask;
import com.shrek.klib.colligate.download.db.DLDatabaseHelper;
import com.shrek.klib.logger.ZLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class DownloadCallable implements Runnable {
    private Context context;

    private long beginTime;
    private int controlUpdateCache = 0;
    private DLThreadTask dtTask;
    private DLDatabaseHelper mDBOperator;

    public Object obj;
    private DLTask task;

    public DownloadCallable(Context context, DLTask mDLTask,
                            DLThreadTask mDLThreadTask, DLDatabaseHelper mDLDatabaseHelper) {
        this.obj = new Object();
        ;
        this.task = mDLTask;
        this.context = context;
        this.dtTask = mDLThreadTask;
        this.controlUpdateCache = 0;
        this.mDBOperator = mDLDatabaseHelper;
    }

    private void sendBroadCast(String info) {
        if (task.isSendBrocadcast) {
            Intent intent = new Intent();
            intent.setAction(DLConstant.BROADCAST_TASK);
            intent.putExtra(DLConstant.DL_TASK_OBJ, task);
            intent.putExtra(DLConstant.DL_TASK_MSG, info);
            context.sendBroadcast(intent);
        }
        ZLog.i(DownloadCallable.class, "任务名: " + task.fileName + " 线程ID:"
                + dtTask.threadId + " 下载的进度: "
                + dtTask.hasDownloadLength + "/" + dtTask.downloadBlock);
        DLHandler handler = Downloader.allCallbacks.get(task);
        if (handler != null) {
            Set<DLThreadTask> set = Downloader.allTasks.get(task);
            int sumSize = 0;
            for (DLThreadTask dtTask : set) {
                sumSize += dtTask.hasDownloadLength;
            }
            handler.downLoadingProgressOnOtherThread(task, sumSize);
        }
    }

    public DLThreadTask getDtTask() {
        return this.dtTask;
    }

    public DLTask getTask() {
        return this.task;
    }

    @Override
    public void run() {
        if (!dtTask.isFinish()) {
            task.states.set(DLConstant.TASK_RUN);
            task.setErrorMessage("");

            beginTime = System.currentTimeMillis() / 1000;
            InputStream inStream = null;
            RandomAccessFile threadfile = null;
            try {
                URL dlUrl = new URL(task.downLoadUrl);
                HttpURLConnection http = (HttpURLConnection) dlUrl
                        .openConnection();
                http.setConnectTimeout(10000);
                http.setReadTimeout(80000);
                http.setRequestMethod("GET");
                http.setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                http.setRequestProperty("Accept-Language", "zh-CN");
                http.setRequestProperty("Referer", dlUrl.toString());
                http.setRequestProperty("Charset", "UTF-8");
                long startPos = dtTask.breakPointPosition;
                long endPos = dtTask.breakPointPosition
                        - dtTask.hasDownloadLength + dtTask.downloadBlock - 1;
                // startPos = ((downloader.getFileSize()/4)*3) + downLength;
                // endPos = downloader.getFileSize();
                // }else{
                // startPos = block * (threadId - 1) + downLength;
                // endPos = block * threadId -1;
                // }

                ZLog.i(DownloadCallable.this, "下载ID:" + dtTask.threadId
                        + "起始下载点: " + startPos + "   结束的下载点: " + endPos);
                http.setRequestProperty("Range", "bytes=" + startPos + "-"
                        + endPos);
                http.setRequestProperty(
                        "User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                http.setRequestProperty("Connection", "Keep-Alive");

                inStream = http.getInputStream();
                byte[] buffer = new byte[1024];
                int offset = 0;
                // System.out.println("task.getSavePath()==>"+task.getSavePath());
                threadfile = new RandomAccessFile(task.getSavePath(), "rwd");
                threadfile.seek(startPos);
                while ((offset = inStream.read(buffer, 0, 1024)) != -1) {
                    // if(task.getStates() == DownloadThreadTask.TASK_PAUSE){
                    // Log.i(TAG,task.getFileName()+"锟矫伙拷锟街讹拷锟截憋拷锟竭筹拷 " +
                    // task.getThreadId());
                    // this.wait();
                    // Log.i(TAG,task.getFileName()+"锟矫伙拷锟街讹拷锟截憋拷锟竭筹拷锟斤拷锟斤拷 " +
                    // task.getThreadId());
                    // }
                    // else if(task.getStates() == DownloadThreadTask.TASK_RUN){
                    // this.notify();
                    // }
                    if (DownloadService.isServiceShutDown.get()) {
                        task.setErrorMessage("下载服务关闭!");
                        sendBroadCast("下载服务关闭!");
                        threadfile.close();
                        inStream.close();
                        return;
                    }

                    if (task.states.get() == DLConstant.TASK_PAUSE) {
                        task.setErrorMessage("下载暂停!");
                        sendBroadCast("下载暂停!");
                        threadfile.close();
                        inStream.close();
                        return;
                    }

                    if (task.states.get() == DLConstant.TASK_ERROR) {
                        task.setErrorMessage("下载出现错误!");
                        sendBroadCast("下载出现错误!");
                        threadfile.close();
                        inStream.close();
                        return;
                    }

                    threadfile.write(buffer, 0, offset);
                    dtTask.hasDownloadLength += offset;
                    // downLength += offset;
                    controlUpdateCache++;
                    if (controlUpdateCache >= 200) {
                        // downloader.update(this.threadId,
                        // dataBuffer.get(dataBuffer.size()-1));
                        // dataBuffer.clear();
                        dtTask.costTime += (System.currentTimeMillis() / 1000 - beginTime);
                        task.setErrorMessage("");
//                        mDBOperator.updateThreadTask(dtTask);
                        controlUpdateCache = 0;
                        beginTime = System.currentTimeMillis() / 1000;
                        sendBroadCast("");
                    }
                }

                dtTask.costTime += (System.currentTimeMillis() / 1000 - beginTime);
                task.setErrorMessage("");
//                mDBOperator.updateThreadTask(dtTask);
                controlUpdateCache = 0;
                beginTime = System.currentTimeMillis() / 1000;

                threadfile.close();
                inStream.close();

                if (dtTask.isFinish()) {
                    // this.isShutDownThread = true;
                    sendBroadCast("任务名: " + task.fileName + " 线程ID:"
                            + dtTask.threadId + " 下载完成!");
                    task.states.set(DLConstant.TASK_SUCESS);
                }

            } catch (Exception e) {
                e.printStackTrace();
                dtTask.hasDownloadLength = -1;
                sendBroadCast("任务名: " + task.fileName + " 线程ID:"
                        + dtTask.threadId + " 下载出现错误!");
                task.setErrorMessage("下载出现错误!");
//                mDBOperator.updateTask(task);
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                    }
                }
                if (threadfile != null) {
                    try {
                        threadfile.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
}
