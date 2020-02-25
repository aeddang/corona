package com.lib.util
import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Size
import android.view.View
import android.view.WindowManager
import java.security.MessageDigest


object CommonUtil{
    const val appTag = "CommonUtil"
    //사진방향
    fun exifOrientationToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    // 로컬 파일경로 Uri 로 content://
    fun getImageContentUri(context : Context, absPath:String) : Uri? {

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            , arrayOf(MediaStore.Images.Media._ID)
            , MediaStore.Images.Media.DATA + "=? "
            , arrayOf(absPath), null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            return Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )

        } else if (!absPath.isEmpty()) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, absPath)
            return context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
        } else {
            return null
        }
    }

    fun enterDefaultMode(ac: Activity) {

        ac.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        ac.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
    }

    fun enterFullScreenMode(ac: Activity) {
        ac.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        ac.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }


    @SuppressLint("PackageManagerGetSignatures")
    fun getApplicationSignature(context:Context) {
        val packageName: String = context.packageName
        val signatureList: List<String>
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                if (sig.hasMultipleSigners()) {
                    // Send all with apkContentsSigners
                    sig.apkContentsSigners.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        Log.d(appTag, "ApplicationSignature ${Base64.encodeToString(digest.digest(), Base64.NO_WRAP)}")
                        Log.d(appTag, "ApplicationSignature ${byte2HexFormatted(digest.digest())}")

                    }
                } else {
                    sig.signingCertificateHistory.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        Log.d(appTag, "ApplicationSignature ${Base64.encodeToString(digest.digest(), Base64.NO_WRAP)}")
                        Log.d(appTag, "ApplicationSignature ${byte2HexFormatted(digest.digest())}")

                    }
                }
            } else {
                val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                sig.map {
                    val digest = MessageDigest.getInstance("SHA")
                    digest.update(it.toByteArray())
                    Log.d(appTag, "ApplicationSignature ${Base64.encodeToString(digest.digest(), Base64.NO_WRAP)}")
                    Log.d(appTag, "ApplicationSignature ${byte2HexFormatted(digest.digest())}")

                }
            }

        } catch (e: Exception) {
            // Handle error
        }

    }

    fun byte2HexFormatted(arr: ByteArray): String {
        val str = StringBuilder(arr.size * 2)
        for (i in arr.indices) {
            var h = Integer.toHexString(arr[i].toInt())
            val l = h.length
            if (l == 1) h = "0$h"
            if (l > 2) h = h.substring(l - 2, l)
            str.append(h.toUpperCase())
            if (i < arr.size - 1) str.append(':')
        }
        return str.toString()
    }


}



fun Size.getCropRatioSize(crop: Size):RectF{
    val cropRatio = crop.width.toFloat()/crop.height.toFloat()
    val originWidth = width.toFloat()
    val originHeight = height.toFloat()

    var ratioWidth = originWidth
    var ratioHeight = originWidth / cropRatio
    if( ratioHeight > originHeight ){
        ratioHeight = originHeight
        ratioWidth = originHeight * cropRatio
    }
    val marginX = (originWidth - ratioWidth)/2
    val marginY = (originHeight - ratioHeight)/2
    return RectF(marginX,marginY, marginX + ratioWidth ,marginY + ratioHeight)

}