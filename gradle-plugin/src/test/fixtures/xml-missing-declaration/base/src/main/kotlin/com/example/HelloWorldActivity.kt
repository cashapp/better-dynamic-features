package com.example

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout

class HelloWorldActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(FrameLayout(this))

    val appInfo = baseContext.packageManager.getApplicationInfo(baseContext.packageName, PackageManager.GET_META_DATA)
    val metaData = appInfo.metaData

    Log.d("Alecs log", "sup")
  }
}