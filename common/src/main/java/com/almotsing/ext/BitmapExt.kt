package com.almotsing.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.annotation.NonNull
import androidx.exifinterface.media.ExifInterface
import androidx.core.graphics.scale
import java.io.*
import kotlin.math.min

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