package com.appspiriment.utils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.launchPlayStorePage(){
    Intent(
        Intent.ACTION_VIEW,
        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        setPackage("com.android.vending")
    }.let {
        try {
            startActivity(it)
        } catch (_: Exception) { }
    }
}