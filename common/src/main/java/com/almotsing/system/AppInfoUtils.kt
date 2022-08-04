package com.almotsing.system

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

object AppInfoUtils {

    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission(Manifest.permission.QUERY_ALL_PACKAGES)
    fun getInstalledApplications(context: Context): List<ApplicationInfo> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    fun getApplicationIcon(context: Context, info: ApplicationInfo): Drawable {
        return info.loadIcon(context.packageManager)
    }

    fun getDisplayName(context: Context, info: ApplicationInfo): CharSequence {
        return info.loadLabel(context.packageManager)
    }

    fun isSystemApp(info: ApplicationInfo): Boolean {
        return (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
}