/*
 * Copyright (C) 2021 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.euicc

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.util.Log

object EuiccDisabler {
    private const val TAG = "SonyEuiccDisabler"

    private val EUICC_DEPENDENCIES = listOf(
        "com.google.android.gms",
        "com.google.android.gsf",
    )

    private val EUICC_PACKAGES = listOf(
        "com.google.android.euicc",
        "com.google.euiccpixel",
        "com.google.android.ims",
    )

    private fun isInstalled(pm: PackageManager, pkgName: String) = runCatching {
        val info = pm.getPackageInfo(pkgName, PackageInfoFlags.of(0))
        val appInfo = info.applicationInfo
        appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_INSTALLED) != 0
    }.getOrDefault(false)

    private fun isInstalledAndEnabled(pm: PackageManager, pkgName: String) = runCatching {
        val info = pm.getPackageInfo(pkgName, PackageInfoFlags.of(0))
        val appInfo = info.applicationInfo
        if (appInfo != null) {
            Log.d(TAG, "package $pkgName installed, enabled = ${appInfo.enabled}")
            appInfo.enabled
        } else {
            false
        }
    }.getOrDefault(false)

    fun enableOrDisableEuicc(context: Context) {
        val pm = context.packageManager
        val disable = EUICC_DEPENDENCIES.any { !isInstalledAndEnabled(pm, it) }
        val flag = if (disable) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        for (pkg in EUICC_PACKAGES) {
            if (isInstalled(pm, pkg)) {
                pm.setApplicationEnabledSetting(pkg, flag, 0)
            }
        }
    }
}
