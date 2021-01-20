package com.shrek.klib.file;

import android.content.Context;

import com.shrek.klib.colligate.AndroidVersionCheckUtils;
import com.shrek.klib.colligate.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 文件操作类
 *
 * @author shrek
 * @date: 2016-04-01
 */
public class FileOperator {

//    private String path;

    Context context;

    private File optFile;

    OptCallBack callback;

    private Writer mWriter;

    public FileOperator(Context context, String path, OptCallBack callback) {
        super();
        this.callback = callback;
//        this.path = path;
        this.context = context;

        File parentDir = null;
        if (AndroidVersionCheckUtils.hasFroyo()) {
            parentDir = FileUtils.INSTANCE.getDiskCacheDir(context);
        } else {
            parentDir = FileUtils.INSTANCE.getDiskCacheDir(context);
        }

        try {
            createFile(parentDir, path);
        } catch (Exception exception) {
            if (callback != null) {
                callback.createFileError(exception);
            }
        }

        optFile = new File(parentDir, path);
    }

    public FileOperator(Context context, String path) {
        this(context, path, null);
    }

    public File getOptFile() {
        return optFile;
    }


    /**
     * 得到输出流
     *
     * @return
     */
    private Writer getWriter() throws IOException{
        if (mWriter == null) {
            mWriter = new BufferedWriter(new FileWriter(optFile, true), 2048);

        }
        return mWriter;
    }

    /**
     * 停止输出
     */
    public void stopWrite(){
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (Exception e) {

            } finally {
                mWriter = null;
            }
        }
    }

    public void writeMsg(String msg) {
        try {
            Writer curWriter = getWriter();
            curWriter.write(msg);
            curWriter.write('\n');
            curWriter.flush();
        } catch (Exception e) {
            if (callback != null) {
                callback.writeError(e);
            }
        }
    }

    public void readMsg(ReadCallback readCallback) {
        if (readCallback == null) {
            return;
        }

        FileReader fr = null;
        BufferedReader buffReader = null;
        try {
            fr = new FileReader(optFile);
            buffReader = new BufferedReader(fr, 2048);
            String line = null;
            while ((line = buffReader.readLine()) != null) {
                readCallback.readLine(line);
            }

        } catch (Exception e) {
            if (callback != null) {
                callback.readError(e);
            }
        } finally {
            if (buffReader != null) {
                try {
                    buffReader.close();
                } catch (Exception e) {
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                }
            }
        }
    }




    /**
     * 删除文件
     *
     * @param rule
     */
    public void deleteFile(DeleteRule rule) {
        deleteFile(optFile, rule);
    }


    private void deleteFile(File delFile, DeleteRule rule) {
        if (rule == null) {
            optFile.deleteOnExit();
            return;
        }

        if (optFile.isFile()) {
            if (rule.canDelete(optFile)) {
                optFile.deleteOnExit();
            }
        } else {
            File[] subFiles = optFile.listFiles();
            for (File fileTemp : subFiles) {
                deleteFile(fileTemp, rule);
            }
        }
    }


    /**
     * 创建文件
     *
     * @param rootFile
     * @param path     123/234/567/999.txt
     */
    private void createFile(File rootFile, String path) throws IOException {
        String tempString = new String(path);
        String subTemp = StringUtils.getFirstSubString(path, File.separator);
        while (StringUtils.isEmpty(subTemp) && tempString.length() > 1) {
            tempString = tempString.substring(1);
            subTemp = StringUtils.getFirstSubString(tempString, File.separator);
        }
        File subFile = new File(rootFile, subTemp);

        if (subTemp.indexOf(".") != -1) {
            if (subFile.exists()) {
                return;
            }
            subFile.createNewFile();
        } else {
            if (!subFile.exists()) {
                subFile.mkdir();
            }

            tempString = tempString.replace(subTemp, "");
            if (tempString.indexOf(File.separator) == 0) {
                tempString = tempString.substring(1, tempString.length());
            }

            if (!StringUtils.isEmpty(tempString)) {
                createFile(subFile, tempString);
            }
        }
    }


    public static interface OptCallBack {

        void createFileError(Exception ex);

        void writeError(Exception ex);

        void readError(Exception ex);
    }

    public static interface DeleteRule {

        boolean canDelete(File delFile);

    }

    public static interface ReadCallback {

        void readLine(String line);

    }
}
