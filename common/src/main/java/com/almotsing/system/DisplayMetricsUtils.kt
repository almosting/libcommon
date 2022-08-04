package com.almotsing.system

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt


object DisplayMetricsUtils {
    fun dpToPixels(metrics: DisplayMetrics, dp: Float): Float {
        return dp * metrics.density
    }

    fun dpToPixels(metrics: DisplayMetrics, dp: Double): Double {
        return dp * metrics.density
    }

    fun dpToPixelsInt(metrics: DisplayMetrics, dp: Float): Int {
        return dpToPixels(metrics, dp).roundToInt()
    }

    fun dpToPixelsInt(metrics: DisplayMetrics, dp: Double): Int {
        return dpToPixels(metrics, dp).roundToInt()
    }

    fun dpToPixels(context: Context, dp: Float): Float {
        return dpToPixels(context.resources.displayMetrics, dp)
    }

    fun dpToPixels(context: Context, dp: Double): Double {
        return dpToPixels(context.resources.displayMetrics, dp)
    }

    fun dpToPixelsInt(context: Context, dp: Float): Int {
        return dpToPixelsInt(context.resources.displayMetrics, dp)
    }

    fun pixelsToDp(metrics: DisplayMetrics, pixels: Float): Float {
        return pixels / metrics.density
    }

    fun pixelsToDp(metrics: DisplayMetrics, pixels: Double): Double {
        return pixels / metrics.density
    }

    fun pixelsToDp(context: Context, pixels: Float): Float {
        return pixelsToDp(context.resources.displayMetrics, pixels)
    }

    fun pixelsToDp(context: Context, pixels: Double): Double {
        return pixelsToDp(context.resources.displayMetrics, pixels)
    }


    fun spToPixels(metrics: DisplayMetrics, sp: Float): Float {
        return sp * metrics.scaledDensity
    }

    fun spToPixels(metrics: DisplayMetrics, sp: Double): Double {
        return sp * metrics.scaledDensity
    }

    fun spToPixelsInt(metrics: DisplayMetrics, sp: Float): Int {
        return Math.round(spToPixels(metrics, sp))
    }

    fun spToPixelsInt(metrics: DisplayMetrics, sp: Double): Int {
        return Math.round(spToPixels(metrics, sp)).toInt()
    }

    fun spToPixels(context: Context, sp: Float): Float {
        return spToPixels(context.resources.displayMetrics, sp)
    }

    fun spToPixels(context: Context, sp: Double): Double {
        return spToPixels(context.resources.displayMetrics, sp)
    }

    fun spToPixelsInt(context: Context, sp: Float): Int {
        return spToPixelsInt(context.resources.displayMetrics, sp)
    }

    fun spToPixelsInt(context: Context, sp: Double): Int {
        return spToPixelsInt(context.resources.displayMetrics, sp)
    }

    fun pixelsToSp(metrics: DisplayMetrics, pixels: Float): Float {
        return pixels / metrics.scaledDensity
    }

    fun pixelsToSp(metrics: DisplayMetrics, pixels: Double): Double {
        return pixels / metrics.scaledDensity
    }

    fun pixelsToSp(context: Context, pixels: Float): Float {
        return pixelsToSp(context.resources.displayMetrics, pixels)
    }

    fun pixelsToSp(context: Context, pixels: Double): Double {
        return pixelsToSp(context.resources.displayMetrics, pixels)
    }
}