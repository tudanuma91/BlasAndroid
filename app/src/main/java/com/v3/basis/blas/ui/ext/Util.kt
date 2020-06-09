package com.v3.basis.blas.ui.ext

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.v3.basis.blas.blasclass.analytics.BlasLogger
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 *  ローカルにログファイを保存する関数
 *  呼び出した関数、クラスの詳細をファイルに記録する
 *  @param msg: String オプションのメッセージをログに記録する
 */
fun traceLog(msg: String) {
    BlasLogger.logEvent(Throwable(), msg)
}

/**
 * 指定フォルダをZIPファイルに圧縮する
 * @param directory ディレクトリのパス
 * @param zipFile ZIPファイルのパス
 */
fun zipAll(directory: String, zipFile: String) {
    val sourceFile = File(directory)

    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use {
        it.use {
            zipFiles(it, sourceFile, "")
        }
    }
}

/**
 * ファイルをZIPに圧縮する
 * @param zipOut ZipOutputStreamのインスタンス
 * @param sourceFile Fileインスタンス
 * @param parentDirPath 親ディレクトリ
 */
private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {

    val data = ByteArray(2048)

    val list = sourceFile.listFiles()
    if (list == null) {
        FirebaseCrashlytics.getInstance().log("")
        return
    }
    for (f in list) {

        if (f.isDirectory) {
            val entry = ZipEntry(f.name + File.separator)
            entry.time = f.lastModified()
            entry.isDirectory
            entry.size = f.length()

            Log.i("zip", "Adding Directory: " + f.name)
            zipOut.putNextEntry(entry)

            //Call recursively to add files within this directory
            zipFiles(zipOut, f, f.name)
        } else {

            if (!f.name.contains(".zip")) { //If folder contains a file with extension ".zip", skip it
                FileInputStream(f).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val path = parentDirPath + File.separator + f.name
                        Log.i("zip", "Adding file: $path")
                        val entry = ZipEntry(path)
                        entry.time = f.lastModified()
                        entry.isDirectory
                        entry.size = f.length()
                        zipOut.putNextEntry(entry)
                        while (true) {
                            val readBytes = origin.read(data)
                            if (readBytes == -1) {
                                break
                            }
                            zipOut.write(data, 0, readBytes)
                        }
                    }
                }
            } else {
                zipOut.closeEntry()
                zipOut.close()
            }
        }
    }
}

@Throws(Exception::class)
fun unzip(zipFilePath: String, unzipAtLocation: String): Boolean {
    val archive = File(zipFilePath)
    return try {
        val zipfile = ZipFile(archive)
        val e: Enumeration<*> = zipfile.entries()
        while (e.hasMoreElements()) {
            val entry = e.nextElement() as ZipEntry
            unzipEntry(zipfile, entry, unzipAtLocation)
        }
        true
    } catch (e: Exception) {
        Log.e("Unzip zip", "Unzip exception", e)
        false
    }
}

@Throws(IOException::class)
private fun unzipEntry(
    zipfile: ZipFile,
    entry: ZipEntry,
    outputDir: String
) {
    if (entry.isDirectory) {
        createDir(File(outputDir, entry.name))
        return
    }
    val outputFile = File(outputDir, entry.name)
    val exists = outputFile.parentFile?.exists() ?: false
    if (exists.not() && outputFile.parentFile != null) {
        createDir(outputFile.parentFile!!)
    }
    Log.v("ZIP E", "Extracting: $entry")
    val zin: InputStream = zipfile.getInputStream(entry)
    val inputStream = BufferedInputStream(zin)
    val outputStream = BufferedOutputStream(FileOutputStream(outputFile))
    try {
        //IOUtils.copy(inputStream, outputStream);
        try {
            var c = inputStream.read()
            while (c != -1) {
                outputStream.write(c)
                c = inputStream.read()
            }
        } finally {
            outputStream.close()
        }
    } finally {
        outputStream.close()
        inputStream.close()
    }
}

private fun createDir(dir: File) {
    if (dir.exists()) {
        return
    }
    Log.v("ZIP E", "Creating dir " + dir.name)
    if (!dir.mkdirs()) {
        throw RuntimeException("Can not create dir $dir")
    }
}
