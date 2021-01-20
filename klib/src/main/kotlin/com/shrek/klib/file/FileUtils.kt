package com.shrek.klib.file

import android.app.DownloadManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import com.shrek.klib.colligate.AndroidVersionCheckUtils
import com.shrek.klib.extension.e
import java.io.*
import java.nio.charset.Charset

object FileUtils {

    /**
     * 检查如果外部存储器是内置的或是可移动的。
     *
     * @return 如果外部存储是可移动的(就像一个SD卡)返回为 true,否则false。
     */
    val isExternalStorageRemovable: Boolean
        get() = if (AndroidVersionCheckUtils.hasGingerbread()) {
            Environment.isExternalStorageRemovable()
        } else true

    /**
     * 得到一个可用的缓存目录(如果外部可用使用外部,否则内部)。
     *
     * @param context 上下文信息
     * @return 返回目录名字
     */
    fun getDiskCacheDir(context: Context): File {
        // 检查是否安装或存储媒体是内置的,如果是这样,试着使用
        // 外部缓存 目录
        // 否则使用内部缓存 目录
        val cachePath = if (Environment.MEDIA_MOUNTED == Environment
                        .getExternalStorageState() || !isExternalStorageRemovable)
            getExternalCacheDir(
                    context).path
        else
            context.cacheDir.path

        return File(cachePath + File.separator)
    }


    /**
     * 获得外部应用程序缓存目录
     *
     * @param context 上下文信息
     * @return 外部缓存目录
     */
    fun getExternalCacheDir(context: Context): File {
        if (AndroidVersionCheckUtils.hasFroyo()) {
            // return context.getExternalCacheDir();
            return Environment.getExternalStorageDirectory()
        }
        val cacheDir = ("/data/data/" + context.packageName
                + "/cache")
        return File(cacheDir)
    }

    /**
     * 智能的创建文件夹
     * 外置sdcard
     *
     * @return
     */
    fun wisdomMakeDir(context: Context): File {
        var makeDir: File? = File("")
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            makeDir = context.externalCacheDir
        } else {
            makeDir = context.cacheDir
        }
        if (!makeDir!!.exists()) {

        }
        return makeDir
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    fun deleteFile(file: File): Boolean {
        if (!file.exists()) {
            return true
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                deleteFile(f)
            }
        }
        return file.delete()
    }


    /**
     * 拷贝文件
     *
     * @param src
     * @param dest
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyFile(src: File, dest: File) {
//        var inChannel: FileChannel? = null
//        var outChannel: FileChannel? = null
//        try {
//            if (!dest.exists()) {
//                dest.createNewFile()
//            }
//            inChannel = FileInputStream(src).channel
//            inChannel?.map(FileChannel.MapMode.READ_ONLY,0,2048)
//            outChannel = FileOutputStream(dest).channel
//            inChannel!!.transferTo(0, inChannel.size(), outChannel)
//        }catch (e:Exception){ e.printStackTrace() } finally {
//            inChannel?.close()
//            outChannel?.close()
//        }
        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            if (!dest.exists()) {
                dest.createNewFile()
            }
            inputStream = FileInputStream(src)
            outputStream = FileOutputStream(dest)
            val byteArray = ByteArray(2048)
            var length = -1
            do {
                length = inputStream.read(byteArray)
                outputStream.write(byteArray,0, length)
            } while ( length != -1 )

            outputStream.flush()
        }catch (e:Exception){ e.printStackTrace() } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } finally {
                inputStream = null
                outputStream = null
            }
        }

    }

    fun downLoad(context: Context, downloadUrl: String, fileName: String) {
        //创建下载任务,downloadUrl就是下载链接
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("/download/", fileName)
        //获取下载管理器
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request)
    }
}

/**
 * 循环迭代某个文件夹
 */
fun File.foreach(process:(File)->Unit) {
    if ( exists() ){
        if (isDirectory){
            ( listFiles() ?: emptyArray<File>() ).forEach { it.foreach(process) }
        } else {
            process(this)
        }
    }
}

/**
 * 文件的大小
 */
fun File.size() : Long{
    var value = 0L
    foreach { value += it.length() }
    return value
}

/**
 * 文件数量
 */
fun File.count(onlyFile: Boolean = false): Int{
    var value = 0
    foreach {
        if (onlyFile) {
            if (it.isFile) { value += 1 }
        } else {
            value += 1
        }
    }
    return value
}

/**
 * 找到目录中唯一的文件
 */
fun File.uniqueFile(): File?{
    val count = count(true)
    if (count == 0 || count > 1){ return null }
    var result: File ?= null
    foreach {
        if (it.isFile){
            result = it
            return@foreach
        }
    }
    return result
}

/**
 * 文件的格式大小  212.34KB
 */
fun File.formatSize(decimalNum:Int = 1) : String {
    return size().fileFormatSize(decimalNum)
}

fun Long.fileFormatSize(decimalNum:Int = 1) : String  {
    var formatValue = this * 1.0
    val units = arrayOf("B","KB","MB","GB","TB")
    var index = 0
    while(formatValue > 1024*0.9 && index < units.size){
        formatValue /= 1024
        index += 1
    }
    return String.format("%.2f",formatValue) + units[index]
}

fun String.fileMimeType(): String{
    val mmr = MediaMetadataRetriever()
    var mime = "*/*"
    try {
        mmr.setDataSource(this)
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    } catch (e: IllegalStateException) {
        return mime
    } catch (e: IllegalArgumentException) {
        return mime
    } catch (e: RuntimeException) {
        return mime
    }

    return mime
}

inline fun String.appentStr4Length(length: Int): String {
    var resultStr = this
    try {
        var strLen = 0F//计算原字符串所占长度
        for (i in 0 until this.length) {
            strLen += if  (this[i].isDigit() || this[i].isLatin() ){
                1F
            } else if( this[i].isChinese() ){
                2F
            } else if (this[i].category == CharCategory.SURROGATE){
                0.5F
            } else {
                1F
            }
            e("result ${this[i]} category ${this[i].category}")
        }
        e("result ${this} length ${strLen} txtlength ${this.length}")
        if (strLen >= length) {
            return this
        }
        val remain = length - strLen//计算所需补充空格长度

        for (i in 0 until remain.toInt()) {
            resultStr += " "
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return resultStr
}

fun Char.isLatin(): Boolean {
    val ub = Character.UnicodeBlock.of(this)
    return ub == Character.UnicodeBlock.BASIC_LATIN ||
            ub == Character.UnicodeBlock.LATIN_1_SUPPLEMENT ||
            ub == Character.UnicodeBlock.LATIN_EXTENDED_A ||
            ub == Character.UnicodeBlock.LATIN_EXTENDED_B ||
            ub == Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL
}
fun Char.isChinese(): Boolean {
    val ub = Character.UnicodeBlock.of(this)
    return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
            ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
            ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
            ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
            ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
            ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
            ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
}

inline fun String.convert(str: String): String{
    return this.replace(str, "\\${str}")
}

inline fun File.appendBufferedWriter(charset: Charset = Charsets.UTF_8, bufferSize: Int = DEFAULT_BUFFER_SIZE): BufferedWriter =
        appendWriter(charset).buffered(bufferSize)

inline fun File.appendWriter(charset: Charset = Charsets.UTF_8): OutputStreamWriter =
        appendOutputStream().writer(charset)

inline fun File.appendOutputStream(): FileOutputStream {
    return FileOutputStream(this, true)
}
inline fun File.suffixStr(): String{
    return if(this.absolutePath.isEmpty()){
        ""
    } else {
        val start = absolutePath.lastIndexOf(".")
        if (start != -1 ) {
            return absolutePath.substring(start)
        } else {
            ""
        }
    }
}

inline fun File.noSuffixStr(): String {
    return if(this.name.isEmpty()){
        ""
    } else {
        val start = name.lastIndexOf(".")
        if (start != -1 ) {
            return name.substringBeforeLast(".")
        } else {
            this.name
        }
    }
}