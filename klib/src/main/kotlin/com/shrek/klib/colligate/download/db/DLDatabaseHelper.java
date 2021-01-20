package com.shrek.klib.colligate.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.shrek.klib.colligate.download.bo.DLTask;
import com.shrek.klib.colligate.download.bo.DLThreadTask;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.ZDBHelper;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.dao.DBDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DLDatabaseHelper extends ZDBHelper {

    public DLDatabaseHelper(Context context) {
        super(context, "download", 1, 10);
    }

    @Override
    public Class<? extends ZDb>[] loadDatabaseClazz() {
        @SuppressWarnings("unchecked")
        Class<? extends ZDb>[] clazz = new Class[]{DLTask.class,
                DLThreadTask.class};
        return clazz;
    }

    /**
     * 删除下载线程任务
     * @param taskHashCode
     */
    public void delete(int taskHashCode) {
        DBDao<DLThreadTask> dao = getDao(DLThreadTask.class);
        long num = dao.deleteObj("taskHashCode = " + taskHashCode);
        dlPrint("删除下载的线程任务[" + taskHashCode + "]", num);
    }

    /**
     * 得到所有的下载任务
     *
     * @return
     */
    public List<DLTask> getAllTasks() {
        DBDao<DLTask> dao = getDao(DLTask.class);
        return dao.queryAllObjs();
    }

    /**
     * 得到已经下载完成的任务
     *
     * @return
     */
    public List<DLTask> getDLOverTasks() {
        SQLiteDatabase mDatabase = getDatabase(true);
        ArrayList<DLTask> allDLList = new ArrayList<DLTask>();
        Cursor taskCursor = mDatabase.query("DLTask", null, null, null, null,
                null, null);
        if (taskCursor.moveToNext()) {
            DLTask mDLTask = new DLTask();
            mDLTask.downLoadUrl = taskCursor.getString(taskCursor
                    .getColumnIndex("downLoadUrl"));
            mDLTask.totalSize = taskCursor.getLong(taskCursor
                    .getColumnIndex("totalSize"));
            Cursor threadCursor = mDatabase.query("DLThreadTask",
                    new String[]{"SUM(hasDownloadLength)"},
                    "taskHashCode = " + mDLTask.hashCode(), null, null, null,
                    "threadId");
            if (threadCursor.moveToNext()) {
                int downSize = threadCursor.getInt(0);
                if (downSize == mDLTask.totalSize) {
                    mDLTask.savePath = taskCursor.getString(taskCursor
                            .getColumnIndex("savePath"));
                    mDLTask.dlThreadNum = taskCursor.getInt(taskCursor
                            .getColumnIndex("dlThreadNum"));
                    mDLTask.fileName = taskCursor.getString(taskCursor
                            .getColumnIndex("fileName"));
                    mDLTask.createTime = taskCursor.getLong(taskCursor
                            .getColumnIndex("createTime"));
                    mDLTask.setErrorMessage(taskCursor.getString(taskCursor
                            .getColumnIndex("errorMessage")));
                    allDLList.add(mDLTask);
                }
                threadCursor.close();
            }
        }
        taskCursor.close();
        mDatabase.close();
        return allDLList;
    }

    /**
     * 得到已经下载数
     *
     * @param taskHaseCode
     * @return
     */
    public int getDownloadSizeByPath(int taskHaseCode) {
        SQLiteDatabase mDatabase = getDatabase(true);
        int size = 0;
        Cursor cursor = mDatabase.query("DLThreadTask",
                new String[]{"sum(hasDownloadLength)"}, "taskHashCode = "
                        + taskHaseCode, null, null, null, "threadId");
        if (cursor.moveToNext()) {
            size = cursor.getInt(0);
        }
        cursor.close();
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
        return size;
    }

    public Set<DLThreadTask> getDownloadTaskByPath(int taskHaseCode) {

        Set<DLThreadTask> ttSet = new HashSet<DLThreadTask>();
        SQLiteDatabase mDatabase = getDatabase(true);
        Cursor mCursor = mDatabase.query("DLThreadTask", null,
                "taskHashCode = " + taskHaseCode, null, null, null, "threadId");
        while (mCursor.moveToNext()) {
            DLThreadTask mDLThreadTask = new DLThreadTask();
            mDLThreadTask.idCode = mCursor.getInt(mCursor
                    .getColumnIndex("idCode"));
            mDLThreadTask.breakPointPosition = mCursor.getLong(mCursor
                    .getColumnIndex("breakPointPosition"));
            mDLThreadTask.costTime = mCursor.getInt(mCursor
                    .getColumnIndex("costTime"));
            mDLThreadTask.downloadBlock = mCursor.getLong(mCursor
                    .getColumnIndex("downloadBlock"));
            mDLThreadTask.hasDownloadLength = mCursor.getLong(mCursor
                    .getColumnIndex("hasDownloadLength"));
            mDLThreadTask.threadId = mCursor.getInt(mCursor
                    .getColumnIndex("threadId"));
            ttSet.add(mDLThreadTask);
        }
        mCursor.close();
        mDatabase.close();
        return ttSet;
    }

    public DLTask getTaskByPath(String path) {
        SQLiteDatabase mDatabase = getDatabase(true);
        DLTask mDLTask = new DLTask();
        Cursor localCursor = mDatabase.query("DLTask", null, "downLoadUrl = '"
                + path + "'", null, null, null, null);
        if (localCursor.moveToNext()) {
            mDLTask.downLoadUrl = localCursor.getString(localCursor
                    .getColumnIndex("downLoadUrl"));
            mDLTask.savePath = localCursor.getString(localCursor
                    .getColumnIndex("savePath"));
            mDLTask.dlThreadNum = localCursor.getInt(localCursor
                    .getColumnIndex("dlThreadNum"));
            mDLTask.totalSize = localCursor.getLong(localCursor
                    .getColumnIndex("totalSize"));
            mDLTask.fileName = localCursor.getString(localCursor
                    .getColumnIndex("fileName"));
            mDLTask.createTime = localCursor.getLong(localCursor
                    .getColumnIndex("createTime"));
            mDLTask.setErrorMessage(localCursor.getString(localCursor
                    .getColumnIndex("errorMessage")));
        }
        localCursor.close();
        mDatabase.close();
        return mDLTask;
    }

    public void saveThTasks(int haseCode, Set<DLThreadTask> ttSet) {
        if (ttSet.size() == 0) {
            return;
        }

        SQLiteDatabase mDatabase = getDatabase(false);
        mDatabase.beginTransaction();
        for (DLThreadTask dtTask : ttSet) {
            dtTask.setIdCode();
            mDatabase
                    .execSQL(
                            "Insert Or Replace into DLThreadTask (idCode,taskHashCode,threadId,downloadBlock,hasDownloadLength,breakPointPosition,costTime) values (?,?,?,?,?,?,?);",
                            new String[]{dtTask.idCode + "",
                                    dtTask.taskHashCode + "",
                                    dtTask.threadId + "",
                                    dtTask.downloadBlock + "",
                                    dtTask.hasDownloadLength + "",
                                    dtTask.breakPointPosition + "",
                                    dtTask.costTime + ""});
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }

    }

    public void updateTask(DLTask mDLTask) {
        SQLiteDatabase mDatabase = getDatabase(true);
        ContentValues cValues = new ContentValues();
        cValues.put("errorMessage", mDLTask.errorMessage);

        int i = mDatabase.update("DLTask", cValues, "downLoadUrl = ?",
                new String[]{mDLTask.downLoadUrl});
        ZLog.i(DLDatabaseHelper.class, "task信息更新,更新数: " + i);
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
        return;

    }

    public void updateTasks(DLTask mTask, Set<DLThreadTask> ttSet) {
        SQLiteDatabase mDatabase = getDatabase(false);
        mDatabase.beginTransaction();
        mDatabase
                .execSQL(
                        "Insert Or Replace into DLTask (downLoadUrl,savePath,dlThreadNum,totalSize,fileName,createTime) values (?,?,?,?,?,?);",
                        new String[]{mTask.downLoadUrl, mTask.savePath,
                                mTask.dlThreadNum + "", mTask.totalSize + "",
                                mTask.fileName, mTask.createTime + ""});
        for (DLThreadTask dtt : ttSet) {
            dtt.setIdCode();
            mDatabase
                    .execSQL(
                            "Insert Or Replace into DLThreadTask (idCode,taskHashCode,threadId,downloadBlock,hasDownloadLength,breakPointPosition,costTime) values (?,?,?,?,?,?,?);",
                            new String[]{dtt.idCode + "",
                                    String.valueOf(dtt.taskHashCode),
                                    String.valueOf(dtt.threadId),
                                    String.valueOf(dtt.downloadBlock),
                                    String.valueOf(dtt.hasDownloadLength),
                                    String.valueOf(dtt.breakPointPosition),
                                    dtt.costTime + ""});
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
    }

    public void updateThreadTask(DLTask task, DLThreadTask dtTask) {
        dtTask.setIdCode();
        SQLiteDatabase mDatabase = getDatabase(false);
        ContentValues cValues = new ContentValues();
        cValues.put("errorMessage", task.errorMessage);
        int num = mDatabase.update("DLTask", cValues, "downLoadUrl = ?",
                new String[]{task.downLoadUrl});

        ContentValues cValues1 = new ContentValues();
        cValues1.put("downloadBlock", dtTask.downloadBlock);
        cValues1.put("hasDownloadLength", dtTask.hasDownloadLength);
        cValues1.put("breakPointPosition", dtTask.breakPointPosition);
        cValues1.put("costTime", dtTask.costTime);
        int tNum = mDatabase.update("DLThreadTask", cValues1, "idCode = "
                + dtTask.idCode, null);
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
    }

    public void updateThreadTask(DLThreadTask dtTask) {
        SQLiteDatabase mDatabase = getDatabase(false);
        dtTask.setIdCode();
        ContentValues cValues = new ContentValues();
        cValues.put("downloadBlock", dtTask.downloadBlock);
        cValues.put("hasDownloadLength", dtTask.hasDownloadLength);
        cValues.put("breakPointPosition", dtTask.breakPointPosition);
        cValues.put("costTime", dtTask.costTime);
        int num = mDatabase.update("DLThreadTask", cValues, "idCode = "
                + dtTask.idCode, null);
        ZLog.i(DLDatabaseHelper.class, "更新DLThreadTask成功,影响的记录数:"
                + num);
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
    }

    private void dlPrint(String info, long num) {
        ZLog.i(this, String.format("%s,影响的记录条数:%d", info, num));
    }

    class DBOptTask extends AsyncTask<Void, Void, Void> {
        Runnable run;

        public DBOptTask(Runnable run) {
            this.run = run;
        }

        protected Void doInBackground(Void... paramArrayOfVoid) {
            if (this.run != null) {
                run.run();
            }
            return null;
        }
    }

}
