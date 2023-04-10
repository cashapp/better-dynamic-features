package com.example.attr

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

class CustomTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
  init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView)
    val textAttribute = a.getString(R.styleable.CustomTextView_specialText)
    this.text = textAttribute

    val containingActivity = findActivity()
    Log.wtf("CustomTextView", "Initialized in $containingActivity, with specialText=$textAttribute")

    a.recycle()
  }

  private fun findActivity(): Activity? {
    var itContext = context
    while (itContext is ContextWrapper) {
      if (itContext is Activity) {
        return itContext
      }
      itContext = itContext.baseContext
    }

    return null
  }
}
