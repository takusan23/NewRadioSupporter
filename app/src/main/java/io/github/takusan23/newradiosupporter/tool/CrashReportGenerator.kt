package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat

object CrashReportGenerator {

    /**
     * これを[MainActivity]なんかに置いておけばいいと思います。
     * クラッシュした際にレポートを保存する処理が書いてあります。
     *
     * @param context Context
     */
    fun initCrashReportGenerator(context: Context) {
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            // 保存先
            val crashReportFolder = File(context.getExternalFilesDir(null), "crash_report").apply {
                if (!exists()) {
                    mkdir()
                }
            }
            val crashReportDate = SimpleDateFormat("yyyyMMdd-HHmmss").format(System.currentTimeMillis())
            // ファイルに書き込む
            File(crashReportFolder, "crash_$crashReportDate.txt").apply {
                createNewFile()
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                e.printStackTrace(printWriter)
                printWriter.flush()
                val crashReport = stringWriter.toString()
                writeText(crashReport)
            }
            // Androidのクラッシュダイアログを表示
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
        }
    }
}