package com.almotsing.ext

import android.R.attr
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import androidx.annotation.NonNull
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.*
import kotlin.math.min
import kotlin.math.roundToInt


fun Bitmap.cropCenterSquare(sideLength: Int? = null, filter: Boolean = true): Bitmap {
    val minLength = min(width, height)
    val left = ((width / 2f) - (minLength / 2f)).toInt()
    val top = ((height / 2f) - (minLength / 2f)).toInt()
    var bitmap = Bitmap.createBitmap(this, left, top, minLength, minLength)
    sideLength?.also {
        bitmap = Bitmap.createScaledBitmap(bitmap, sideLength, sideLength, filter)
        bitmap = bitmap.scale(sideLength, sideLength, filter)
    }
    return bitmap
}

fun Bitmap.fixOrientation(file: File): Bitmap {
    val exif = ExifInterface(file)
    val swapDimension =
        when (exif.rotationDegrees) {
            0 -> return this
            90 -> true
            180 -> false
            270 -> true
            else -> return this
        }
    val matrix = Matrix()
    matrix.postRotate(exif.rotationDegrees.toFloat())
    return Bitmap.createBitmap(
        this,
        0, 0,
        if (!swapDimension) width else height,
        if (!swapDimension) height else width,
        matrix, true
    )
}

fun Bitmap.fixOrientation(fileDescriptor: FileDescriptor): Bitmap {
    val exif = ExifInterface(fileDescriptor)
    val swapDimension =
        when (exif.rotationDegrees) {
            0 -> return this
            90 -> true
            180 -> false
            270 -> true
            else -> return this
        }
    val matrix = Matrix()
    matrix.postRotate(exif.rotationDegrees.toFloat())
    return Bitmap.createBitmap(
        this,
        0, 0,
        if (!swapDimension) width else height,
        if (!swapDimension) height else width,
        matrix, true
    )
}

fun Bitmap.fixOrientation(inputStream: InputStream): Bitmap {
    val exif = ExifInterface(inputStream)
    val swapDimension =
        when (exif.rotationDegrees) {
            0 -> return this
            90 -> true
            180 -> false
            270 -> true
            else -> return this
        }
    val matrix = Matrix()
    matrix.postRotate(exif.rotationDegrees.toFloat())
    return Bitmap.createBitmap(
        this,
        0, 0,
        if (!swapDimension) width else height,
        if (!swapDimension) height else width,
        matrix, true
    )
}

fun Bitmap.save(
    file: File,
    compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 80
): Boolean = compress(compressFormat, quality, FileOutputStream(file))


fun Bitmap.convert(
    compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): Bitmap {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(compressFormat, quality, byteArrayOutputStream)
    return BitmapFactory.decodeByteArray(
        byteArrayOutputStream.toByteArray(),
        0,
        byteArrayOutputStream.size()
    )
}

fun Bitmap.toBytes(
    @NonNull format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(format, quality, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun Bitmap.asBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun Bitmap.asBitmap(byteArray: ByteArray, requestWidth: Int, requestHeight: Int): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
    options.inJustDecodeBounds = false
    options.inSampleSize = calcSampleSize(options, requestWidth, requestHeight)
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun Bitmap.fromDrawable(drawable: Drawable, width: Int, height: Int): Bitmap {

    drawable.setBounds(0, 0, attr.width, attr.height)
    val result: Bitmap = Bitmap.createBitmap(attr.width, attr.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    drawable.draw(canvas)
    return result
}

fun Bitmap.scaleBitmap(bitmap: Bitmap, requestWidth: Int, requestHeight: Int): Bitmap {
    val width: Int = bitmap.width
    val height: Int = bitmap.height
    val matrix = Matrix()
    matrix.postScale(width / requestWidth.toFloat(), height / requestHeight.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
}

fun Bitmap.rotateBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
    val width: Int = bitmap.width
    val height: Int = bitmap.height
    val matrix = Matrix()
    matrix.postRotate(attr.rotation.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
}

fun calcSampleSize(options: BitmapFactory.Options, requestWidth: Int, requestHeight: Int): Int {
    val imageWidth = options.outWidth
    val imageHeight = options.outHeight
    var reqWidth = requestWidth
    var reqHeight = requestHeight
    if (requestWidth <= 0) {
        reqWidth = if (requestHeight > 0)
            (imageWidth * requestHeight / imageHeight.toFloat()).toInt()
        else
            imageWidth
    }
    if (requestHeight <= 0) {
        reqHeight = if (requestWidth > 0)
            (imageHeight * requestWidth / imageHeight.toFloat()).toInt()
        else
            imageHeight
    }
    var inSampleSize = 1
    if ((imageHeight > reqHeight) || (imageWidth > reqWidth)) {
        inSampleSize = if (imageWidth > imageHeight)
            (imageHeight / reqHeight.toFloat()).roundToInt()
        else
            (imageWidth / reqWidth.toFloat()).roundToInt()
    }
    return inSampleSize
}

