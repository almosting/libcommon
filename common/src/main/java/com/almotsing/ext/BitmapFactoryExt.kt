package com.almotsing.ext

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.view.View
import java.io.FileDescriptor
import java.io.InputStream

fun ByteArray.decodeBitmap(
    offset: Int,
    length: Int,
    options: BitmapFactory.Options? = null
): Bitmap? {
    return BitmapFactory.decodeByteArray(this, offset, length, options)
}

fun FileDescriptor.decodeBitmap(
    outPadding: Rect? = null,
    options: BitmapFactory.Options? = null
): Bitmap? {
    return BitmapFactory.decodeFileDescriptor(this, outPadding, options)
}

fun InputStream.decodeBitmap(
    outPadding: Rect? = null,
    options: BitmapFactory.Options? = null
): Bitmap? {
    return BitmapFactory.decodeStream(this, outPadding, options)
}

fun Resources.decodeBitmap(resId: Int, options: BitmapFactory.Options? = null): Bitmap? {
    return BitmapFactory.decodeResource(this, resId, options)
}

fun View.getInSampleSizeForBitmap(options: BitmapFactory.Options): Int {
    val width = width
    val height = height
    var inSampleSize = 1

    if (options.outHeight > height || options.outWidth > width) {
        try {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2

            while (halfHeight / inSampleSize >= height && halfWidth / inSampleSize >= width) {
                inSampleSize *= 2
            }
        } catch (e: ArithmeticException) {
        }
    }
    return inSampleSize
}
