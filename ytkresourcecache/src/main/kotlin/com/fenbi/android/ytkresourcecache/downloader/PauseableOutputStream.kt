package com.fenbi.android.ytkresourcecache.downloader

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Created by yangjw on 2019/1/21.
 */

private fun String.tmp() = "$this-tmp"

class PauseableOutputStream(val path: String) : OutputStream() {

    private val tmpFile by lazy {
        val file = File(path.tmp())
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.createNewFile()
        }
        return@lazy file
    }

    private val innerOutputStream by lazy {
        FileOutputStream(tmpFile)
    }

    fun length(): Long = tmpFile.length()

    fun onCacheSuccess() {
        tmpFile.renameTo(File(path))
    }

    fun onCacheFailed() {
        tmpFile.delete()
    }

    override fun write(b: Int) {
        innerOutputStream.write(b)
    }

    override fun write(b: ByteArray?) {
        innerOutputStream.write(b)
    }

    override fun write(b: ByteArray?, off: Int, len: Int) {
        innerOutputStream.write(b, off, len)
    }

    override fun flush() {
        innerOutputStream.flush()
    }

    override fun close() {
        innerOutputStream.close()
    }
}